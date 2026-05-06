package com.babyhealth.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.*;

@Service
public class AiAnalysisService {

    private static final Logger log = LoggerFactory.getLogger(AiAnalysisService.class);

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${app.ai.api-key:}")
    private String apiKey;

    @Value("${app.ai.api-url:https://api.siliconflow.cn/v1/chat/completions}")
    private String apiUrl;

    private static final String VISION_MODEL = "THUDM/GLM-4.1V-9B-Thinking";
    private static final String TEXT_MODEL = "THUDM/GLM-Z1-9B-0414";
    private static final int MAX_IMAGE_DIMENSION = 1024;
    private static final int READ_TIMEOUT_MS = 300_000;

    private static final String ANALYSIS_PROMPT = """
            你是一位专业的婴幼儿营养师和食物分析专家。请分析图片中的食物，并以严格 JSON 格式回复，不要包含任何其他文字。

            字段说明：
            - foodName: 食物名称（中文）
            - mealType: 餐食类型，只能是 "早餐"、"午餐"、"晚餐"、"加餐" 之一
            - healthScore: 0-100 的整数健康评分，考量营养均衡度、适合婴幼儿程度、食材新鲜度
            - aiResult: 简要分析结果（中文，20-50字），描述食物主要营养成分和特点
            - suggestion: 针对婴幼儿的膳食建议（中文，30-60字），指出改进方向或搭配建议
            - nutrients: 营养元素数组，每个元素包含 name（营养素名称）和 level（"高"/"中"/"低" 之一）

            JSON 格式示例：
            {"foodName":"鸡蛋西红柿面","mealType":"午餐","healthScore":85,"aiResult":"富含优质蛋白质和番茄红素，搭配合理。","suggestion":"可搭配少量绿叶蔬菜增加膳食纤维。","nutrients":[{"name":"蛋白质","level":"高"},{"name":"维生素C","level":"中"},{"name":"膳食纤维","level":"低"}]}""";

    private static final String TIPS_PROMPT = """
            你是一个幽默的程序员，擅长用夸张的高科技术语来描述日常小事。请生成10条搞笑的"假装在加载"提示语，每条包含 fake（唬人的技术术语）和 reveal（揭穿真相的吐槽）。

            要求：
            - fake: 用看起来很厉害但完全用不上的技术术语，如"正在加载光线追踪""正在启动量子纠缠引擎"
            - reveal: 用不同风格的吐槽揭穿，开头要多样化，如"醒醒，""想多了，""别做梦了，""说实话，""不好意思，""认真的吗，""得了吧，""别闹了，""省省吧，""你信吗，"等，不要重复使用同一种开头
            - 主题可以围绕：食物分析、营养识别、AI计算、图像处理、微软式中文、中译中、机翻笑话、中文式英语、你知道吗等
            - 语气轻松幽默


            严格以 JSON 数组格式返回，不要包含任何其他文字 JSON 格式示例：
            [{"fake":"正在加载光线追踪","reveal":"醒醒，分析个饭哪用光线追踪"},{"fake":"正在启动量子纠缠引擎","reveal":"想多了，量子纠缠管不了你吃啥"}]""";

    public AiAnalysisService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public AiResult analyzeFood(byte[] imageBytes, String imageUrl) {
        boolean keyMissing = apiKey == null || apiKey.isBlank() || "PLACEHOLDER".equals(apiKey);
        log.info("AiAnalysis start | key configured: {} | key prefix: {} | model: {}",
                !keyMissing,
                keyMissing ? "N/A" : apiKey.substring(0, Math.min(8, apiKey.length())) + "...",
                VISION_MODEL);

        if (keyMissing) {
            return new AiResult(
                    "未知食物", 70, "AI分析接口尚未配置，请在 application.yml 中设置 app.ai.api-key",
                    "加餐", "请配置硅基流动 API Key 后使用完整功能", "[]", 100, ""
            );
        }

        try {
            byte[] compressed = compressImage(imageBytes);
            String base64Image = Base64.getEncoder().encodeToString(compressed);
            String dataUrl = "data:image/jpeg;base64," + base64Image;

            Map<String, Object> requestBody = buildRequestBody(dataUrl);
            String jsonRequest = objectMapper.writeValueAsString(requestBody);

            log.info("Calling SiliconFlow | url: {} | original: {}B → compressed: {}B | body: {}B",
                    apiUrl, imageBytes.length, compressed.length, jsonRequest.length());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            HttpEntity<String> entity = new HttpEntity<>(jsonRequest, headers);

            long t0 = System.currentTimeMillis();
            ResponseEntity<String> response = restTemplate.postForEntity(apiUrl, entity, String.class);
            long t1 = System.currentTimeMillis();

            log.info("SiliconFlow responded in {}ms | status: {}", t1 - t0, response.getStatusCode());

            return parseResponse(response.getBody());
        } catch (Exception e) {
            log.error("AiAnalysis failed: {} | type: {}", e.getMessage(), e.getClass().getSimpleName());
            return new AiResult(
                    "分析失败", 0, "AI分析异常: " + e.getMessage(),
                    "加餐", "请稍后重试", "[]", 0, "分析异常，请重试"
            );
        }
    }

    private byte[] compressImage(byte[] original) {
        try {
            BufferedImage img = ImageIO.read(new ByteArrayInputStream(original));
            if (img == null) {
                log.warn("ImageIO cannot read the image, sending original");
                return original;
            }

            int w = img.getWidth();
            int h = img.getHeight();
            int maxDim = Math.max(w, h);

            if (maxDim <= MAX_IMAGE_DIMENSION && original.length < 150_000) {
                log.info("Image already small enough ({}x{}, {}B), skipping resize", w, h, original.length);
                return original;
            }

            double scale = Math.min(1.0, (double) MAX_IMAGE_DIMENSION / maxDim);
            int newW = (int) (w * scale);
            int newH = (int) (h * scale);

            BufferedImage resized = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = resized.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.drawImage(img, 0, 0, newW, newH, null);
            g.dispose();

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(resized, "jpg", baos);

            byte[] result = baos.toByteArray();
            log.info("Image compressed: {}x{} ({}B) → {}x{} ({}B), scale={}",
                    w, h, original.length, newW, newH, result.length, String.format("%.2f", scale));
            return result;
        } catch (Exception e) {
            log.warn("Image compression failed: {}, sending original", e.getMessage());
            return original;
        }
    }

    private Map<String, Object> buildRequestBody(String dataUrl) {
        Map<String, Object> imagePart = new HashMap<>();
        imagePart.put("type", "image_url");
        Map<String, String> imageUrlMap = new HashMap<>();
        imageUrlMap.put("url", dataUrl);
        imagePart.put("image_url", imageUrlMap);

        Map<String, Object> textPart = new HashMap<>();
        textPart.put("type", "text");
        textPart.put("text", ANALYSIS_PROMPT);

        Map<String, Object> message = new HashMap<>();
        message.put("role", "user");
        message.put("content", List.of(textPart, imagePart));

        Map<String, Object> requestBody = new LinkedHashMap<>();
        requestBody.put("model", VISION_MODEL);
        requestBody.put("messages", List.of(message));

        return requestBody;
    }

    private AiResult parseResponse(String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            String content = root.path("choices").get(0).path("message").path("content").asText("");

            if (content.isBlank()) {
                log.warn("AI response content is empty");
                return new AiResult("未知食物", 60, "AI 返回结果为空", "加餐", "请重试", "[]", 50, "AI没有返回有效结果，请重试");
            }

            String cleaned = content.trim();
            if (cleaned.startsWith("```")) {
                int firstNewline = cleaned.indexOf('\n');
                if (firstNewline >= 0) {
                    cleaned = cleaned.substring(firstNewline + 1);
                }
                if (cleaned.endsWith("```")) {
                    cleaned = cleaned.substring(0, cleaned.length() - 3);
                }
                cleaned = cleaned.trim();
            }

            try {
                JsonNode result = objectMapper.readTree(cleaned);
                String foodName = result.path("foodName").asText("未知食物");
                String mealType = result.path("mealType").asText("加餐");
                int healthScore = result.path("healthScore").asInt(60);
                String aiResult = result.path("aiResult").asText("");
                String suggestion = result.path("suggestion").asText("");
                String nutrients = result.has("nutrients") ? result.path("nutrients").toString() : "[]";

                int confidence = calculateConfidence(result, foodName);
                String feedback = generateFeedback(confidence, foodName);

                log.info("Parse success | food: {} | score: {} | confidence: {} | nutrients: {}", foodName, healthScore, confidence, nutrients);
                return new AiResult(foodName, healthScore, aiResult, mealType, suggestion, nutrients, confidence, feedback);
            } catch (Exception e) {
                log.warn("JSON parse failed, using raw content | error: {}", e.getMessage());
                return new AiResult("识别结果", 60, content.length() > 100 ? content.substring(0, 100) : content, "加餐", "建议咨询专业营养师", "[]", 55, "结果格式异常，仅供参考");
            }
        } catch (Exception e) {
            log.error("Response parse failed: {}", e.getMessage());
            return new AiResult("分析失败", 0, "解析响应失败: " + e.getMessage(), "加餐", "请稍后重试", "[]", 0, "解析失败，请重试");
        }
    }

    public record AiResult(
            String foodName,
            int healthScore,
            String nutritionInfo,
            String mealType,
            String suggestion,
            String nutrients,
            int confidence,
            String feedback
    ) {}

    private int calculateConfidence(JsonNode result, String foodName) {
        if (result.has("confidence")) {
            return Math.max(0, Math.min(100, result.path("confidence").asInt(70)));
        }
        String food = foodName.toLowerCase();
        if (food.contains("未知") || food.contains("失败")) return 20;
        if (food.contains("可能") || food.contains("疑似")) return 45;
        return 75 + (int)(Math.random() * 20);
    }

    private String generateFeedback(int confidence, String foodName) {
        if (confidence < 40) {
            String[] roasts = {
                "这图糊得我都怀疑人生了，这真的是" + foodName + "吗？",
                "我瞪大了我的电子眼，也没法确定这是不是" + foodName + "...",
                "说实话，这照片拍得跟抽象艺术一样，我看不出来是啥",
                "AI表示很委屈：这光线、这角度，臣妾做不到啊！",
                "我怀疑你在考验我的智商，但我没有证据"
            };
            return roasts[(int)(Math.random() * roasts.length)];
        } else if (confidence < 70) {
            String[] warns = {
                "看着有点像" + foodName + "，但别全信我，我眼神不太好",
                "大概有六成把握是" + foodName + "，建议你确认一下",
                "这看起来像是" + foodName + "，但我不敢打包票",
                "AI警告：识别结果仅供参考，请以实物为准",
                "我尽力了... 应该是" + foodName + "吧？"
            };
            return warns[(int)(Math.random() * warns.length)];
        }
        return "";
    }

    public List<Map<String, String>> generateTips() {
        boolean keyMissing = apiKey == null || apiKey.isBlank() || "PLACEHOLDER".equals(apiKey);
        if (keyMissing) {
            return Collections.emptyList();
        }

        try {
            Map<String, Object> textPart = new HashMap<>();
            textPart.put("type", "text");
            textPart.put("text", TIPS_PROMPT);

            Map<String, Object> message = new HashMap<>();
            message.put("role", "user");
            message.put("content", List.of(textPart));

            Map<String, Object> requestBody = new LinkedHashMap<>();
            requestBody.put("model", TEXT_MODEL);
            requestBody.put("messages", List.of(message));
            requestBody.put("max_tokens", 800);

            String jsonRequest = objectMapper.writeValueAsString(requestBody);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            HttpEntity<String> entity = new HttpEntity<>(jsonRequest, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(apiUrl, entity, String.class);

            JsonNode root = objectMapper.readTree(response.getBody());
            String content = root.path("choices").get(0).path("message").path("content").asText("");

            String cleaned = content.trim();
            if (cleaned.startsWith("```")) {
                int firstNewline = cleaned.indexOf('\n');
                if (firstNewline >= 0) cleaned = cleaned.substring(firstNewline + 1);
                if (cleaned.endsWith("```")) cleaned = cleaned.substring(0, cleaned.length() - 3);
                cleaned = cleaned.trim();
            }

            JsonNode arr = objectMapper.readTree(cleaned);
            List<Map<String, String>> tips = new ArrayList<>();
            for (JsonNode item : arr) {
                String fake = item.path("fake").asText("");
                String reveal = item.path("reveal").asText("");
                if (!fake.isBlank() && !reveal.isBlank()) {
                    tips.add(Map.of("fake", fake, "reveal", reveal));
                }
            }

            log.info("Generated {} tips", tips.size());
            return tips;
        } catch (Exception e) {
            log.warn("Tips generation failed: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    public void logTipsError(String msg) {
        log.warn("Async tips generation failed: {}", msg);
    }
}
