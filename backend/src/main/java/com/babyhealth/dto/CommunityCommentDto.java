package com.babyhealth.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class CommunityCommentDto {
    private Long id;
    private Long postId;
    private Long userId;
    private String content;
    private List<String> images;
    private LocalDateTime createTime;
    private String username;
    private String nickname;
    private String avatar;
}
