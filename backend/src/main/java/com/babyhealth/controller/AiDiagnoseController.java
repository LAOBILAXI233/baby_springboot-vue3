package com.babyhealth.controller;

import com.babyhealth.ai.AiAnalysisService;
import com.babyhealth.dto.ApiResponse;
import com.babyhealth.service.TipsStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
public class AiDiagnoseController {

    private static final Logger log = LoggerFactory.getLogger(AiDiagnoseController.class);

    private final RestTemplate restTemplate;
    private final AiAnalysisService aiAnalysisService;
    private final TipsStore tipsStore;

    @Value("${app.ai.api-key:}")
    private String apiKey;

    @Value("${app.ai.api-url:https://api.siliconflow.cn/v1/chat/completions}")
    private String apiUrl;

    public AiDiagnoseController(RestTemplate restTemplate, AiAnalysisService aiAnalysisService, TipsStore tipsStore) {
        this.restTemplate = restTemplate;
        this.aiAnalysisService = aiAnalysisService;
        this.tipsStore = tipsStore;
    }

    @GetMapping("/check")
    public ApiResponse<Map<String, Object>> check() {
        Map<String, Object> result = new HashMap<>();
        boolean keyMissing = apiKey == null || apiKey.isBlank() || "PLACEHOLDER".equals(apiKey);
        result.put("key_configured", !keyMissing);
        result.put("key_prefix", keyMissing ? "N/A" : apiKey.substring(0, Math.min(8, apiKey.length())) + "...");
        result.put("api_url", apiUrl);

        if (keyMissing) {
            result.put("status", "KEY_MISSING");
            return ApiResponse.success("请在 application.yml 中配置 app.ai.api-key", result);
        }

        try {
            String testPayload = """
                    {"model":"THUDM/GLM-4.1V-9B-Thinking","messages":[{"role":"user","content":[{"type":"text","text":"回复OK"}]}],"max_tokens":10}""";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            HttpEntity<String> entity = new HttpEntity<>(testPayload, headers);

            long t0 = System.currentTimeMillis();
            ResponseEntity<String> response = restTemplate.postForEntity(apiUrl, entity, String.class);
            long t1 = System.currentTimeMillis();

            result.put("status", response.getStatusCode().value());
            result.put("response_time_ms", t1 - t0);
            result.put("response_preview", response.getBody().length() > 100 ? response.getBody().substring(0, 100) : response.getBody());
            return ApiResponse.success("API 连接成功", result);
        } catch (Exception e) {
            result.put("status", "ERROR");
            result.put("error", e.getClass().getSimpleName() + ": " + e.getMessage());
            return ApiResponse.error(500, "API 连接失败: " + e.getMessage());
        }
    }

    @GetMapping("/tips")
    public ApiResponse<List<Map<String, String>>> getTips() {
        List<Map<String, String>> current = tipsStore.getAll();

        new Thread(() -> {
            try {
                log.info("Starting async tips generation...");
                List<Map<String, String>> generated = aiAnalysisService.generateTips();
                log.info("Async tips generation returned {} tips", generated.size());
                if (!generated.isEmpty()) {
                    tipsStore.merge(generated);
                }
            } catch (Exception e) {
                log.error("Async tips generation failed: {}", e.getMessage(), e);
            }
        }, "tips-generator").start();

        return ApiResponse.success(current);
    }
}
