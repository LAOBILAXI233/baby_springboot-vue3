package com.babyhealth.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class CommunityPostDto {
    private Long id;
    private Long userId;
    private String content;
    private List<String> images;
    private Integer likeCount;
    private Integer commentCount;
    private Boolean liked;
    private Boolean favorited;
    private LocalDateTime createTime;
    private String username;
    private String nickname;
    private String avatar;
}
