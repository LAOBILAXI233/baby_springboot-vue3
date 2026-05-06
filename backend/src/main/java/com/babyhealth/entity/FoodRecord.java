package com.babyhealth.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "tb_food_record")
public class FoodRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    @Column(length = 500)
    private String imageUrl;

    @Column(columnDefinition = "TEXT")
    private String aiResult;

    @Column(length = 200)
    private String foodName;

    private Integer healthScore;

    @Column(length = 500)
    private String suggestion;

    @Column(length = 50)
    private String mealType;

    @Column(columnDefinition = "TEXT")
    private String nutrients;

    private Integer confidence;

    @Column(length = 500)
    private String feedback;

    private LocalDateTime createTime;

    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
        if (nutrients == null) nutrients = "[]";
    }
}
