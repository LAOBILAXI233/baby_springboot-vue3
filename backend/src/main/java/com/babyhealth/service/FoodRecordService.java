package com.babyhealth.service;

import com.babyhealth.ai.AiAnalysisService;
import com.babyhealth.entity.FoodRecord;
import com.babyhealth.repository.FoodRecordRepository;
import com.babyhealth.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public class FoodRecordService {

    private final FoodRecordRepository foodRecordRepository;
    private final FileService fileService;
    private final AiAnalysisService aiAnalysisService;
    private final UserService userService;

    public FoodRecordService(FoodRecordRepository foodRecordRepository,
                             FileService fileService,
                             AiAnalysisService aiAnalysisService,
                             UserService userService) {
        this.foodRecordRepository = foodRecordRepository;
        this.fileService = fileService;
        this.aiAnalysisService = aiAnalysisService;
        this.userService = userService;
    }

    public FoodRecord analyzeAndSave(Long userId, MultipartFile file) {
        String imageUrl = fileService.store(file);

        FoodRecord record = new FoodRecord();
        record.setUserId(userId);
        record.setImageUrl(imageUrl);

        try {
            byte[] imageBytes = file.getBytes();
            AiAnalysisService.AiResult result = aiAnalysisService.analyzeFood(imageBytes, imageUrl);
            record.setFoodName(result.foodName());
            record.setHealthScore(result.healthScore());
            record.setSuggestion(result.suggestion());
            record.setMealType(result.mealType());
            record.setAiResult(result.nutritionInfo());
            record.setNutrients(result.nutrients());
            record.setConfidence(result.confidence());
            record.setFeedback(result.feedback());
        } catch (Exception e) {
            record.setFoodName("分析失败");
            record.setHealthScore(0);
            record.setSuggestion("请重试");
            record.setMealType("未知");
            record.setAiResult("AI分析异常: " + e.getMessage());
            record.setNutrients("[]");
            record.setConfidence(0);
            record.setFeedback("分析异常，请重试");
        }

        return foodRecordRepository.save(record);
    }

    public Page<FoodRecord> getUserRecords(Long userId, Pageable pageable) {
        return foodRecordRepository.findByUserIdOrderByCreateTimeDesc(userId, pageable);
    }

    public FoodRecord getRecordById(Long id) {
        return foodRecordRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("记录不存在"));
    }

    public void deleteRecord(Long recordId, Long currentUserId) {
        FoodRecord record = foodRecordRepository.findById(recordId)
                .orElseThrow(() -> new RuntimeException("记录不存在"));

        boolean isOwner = record.getUserId().equals(currentUserId);
        boolean isAdmin = userService.isAdmin(currentUserId);

        if (!isOwner && !isAdmin) {
            throw new RuntimeException("无权删除此记录");
        }

        foodRecordRepository.delete(record);
    }

    public void deleteRecords(List<Long> ids, Long currentUserId) {
        boolean isAdmin = userService.isAdmin(currentUserId);
        List<FoodRecord> records = foodRecordRepository.findAllById(ids);

        for (FoodRecord record : records) {
            boolean isOwner = record.getUserId().equals(currentUserId);
            if (!isOwner && !isAdmin) {
                throw new RuntimeException("无权删除记录 ID: " + record.getId());
            }
        }

        foodRecordRepository.deleteAll(records);
    }

    public Page<FoodRecord> getAllRecords(Pageable pageable) {
        return foodRecordRepository.findAllByOrderByCreateTimeDesc(pageable);
    }
}
