package com.babyhealth.controller;

import com.babyhealth.dto.ApiResponse;
import com.babyhealth.entity.FoodRecord;
import com.babyhealth.service.FoodRecordService;
import com.babyhealth.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/food")
public class FoodController {

    private final FoodRecordService foodRecordService;
    private final UserService userService;

    public FoodController(FoodRecordService foodRecordService, UserService userService) {
        this.foodRecordService = foodRecordService;
        this.userService = userService;
    }

    @PostMapping("/analyze")
    public ApiResponse<Map<String, Object>> analyzeFood(
            @RequestParam Long userId,
            @RequestParam("file") MultipartFile file) {
        FoodRecord record = foodRecordService.analyzeAndSave(userId, file);
        Map<String, Object> data = new HashMap<>();
        data.put("id", record.getId());
        data.put("foodName", record.getFoodName());
        data.put("healthScore", record.getHealthScore());
        data.put("suggestion", record.getSuggestion());
        data.put("mealType", record.getMealType());
        data.put("aiResult", record.getAiResult());
        data.put("imageUrl", record.getImageUrl());
        data.put("nutrients", record.getNutrients());
        data.put("confidence", record.getConfidence());
        data.put("feedback", record.getFeedback());
        return ApiResponse.success("分析完成", data);
    }

    @GetMapping("/records")
    public ApiResponse<Page<FoodRecord>> getRecords(
            @RequestParam Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        if (userService.isAdmin(userId)) {
            return ApiResponse.success(foodRecordService.getAllRecords(pageable));
        }
        return ApiResponse.success(foodRecordService.getUserRecords(userId, pageable));
    }

    @GetMapping("/record/{id}")
    public ApiResponse<FoodRecord> getRecord(@PathVariable Long id) {
        return ApiResponse.success(foodRecordService.getRecordById(id));
    }

    @DeleteMapping("/record/{id}")
    public ApiResponse<Void> deleteRecord(
            @PathVariable Long id,
            @RequestParam Long userId) {
        foodRecordService.deleteRecord(id, userId);
        return ApiResponse.success("删除成功", null);
    }

    @PostMapping("/records/batch-delete")
    public ApiResponse<Void> batchDeleteRecords(
            @RequestBody List<Long> ids,
            @RequestParam Long userId) {
        foodRecordService.deleteRecords(ids, userId);
        return ApiResponse.success("批量删除成功", null);
    }
}
