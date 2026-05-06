package com.babyhealth.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "tb_community_post")
public class CommunityPost {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    @Lob
    private String content;

    @Lob
    private String images;

    private Integer likeCount;

    private Integer commentCount;

    private LocalDateTime createTime;

    @PrePersist
    protected void onCreate() {
        likeCount = 0;
        commentCount = 0;
        createTime = LocalDateTime.now();
    }
}
