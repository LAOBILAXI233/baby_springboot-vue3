package com.babyhealth.controller;

import com.babyhealth.dto.ApiResponse;
import com.babyhealth.dto.CommunityCommentDto;
import com.babyhealth.dto.CommunityPostDto;
import com.babyhealth.entity.CommunityPost;
import com.babyhealth.service.CommunityService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/community")
public class CommunityController {

    private final CommunityService communityService;

    public CommunityController(CommunityService communityService) {
        this.communityService = communityService;
    }

    @PostMapping("/post")
    public ApiResponse<CommunityPost> createPost(@RequestBody Map<String, Object> body) {
        Long userId = Long.valueOf(body.get("userId").toString());
        String content = (String) body.get("content");
        String images = (String) body.get("images");
        return ApiResponse.success("发布成功", communityService.createPost(userId, content, images));
    }

    @GetMapping("/posts")
    public ApiResponse<List<CommunityPostDto>> getPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long userId) {
        Pageable pageable = PageRequest.of(page, size);
        return ApiResponse.success(communityService.getPostDtos(pageable, userId));
    }

    @PostMapping("/like/{postId}")
    public ApiResponse<CommunityPost> likePost(@PathVariable Long postId, @RequestParam Long userId) {
        return ApiResponse.success(communityService.likePost(postId, userId));
    }

    @DeleteMapping("/like/{postId}")
    public ApiResponse<CommunityPost> unlikePost(@PathVariable Long postId, @RequestParam Long userId) {
        return ApiResponse.success(communityService.unlikePost(postId, userId));
    }

    @PostMapping("/comment")
    public ApiResponse<CommunityCommentDto> addComment(@RequestBody Map<String, Object> body) {
        Long postId = Long.valueOf(body.get("postId").toString());
        Long userId = Long.valueOf(body.get("userId").toString());
        String content = (String) body.get("content");
        String images = (String) body.getOrDefault("images", "");
        return ApiResponse.success("评论成功", toCommentDto(communityService.addComment(postId, userId, content, images)));
    }

    @GetMapping("/comments/{postId}")
    public ApiResponse<List<CommunityCommentDto>> getComments(@PathVariable Long postId) {
        return ApiResponse.success(communityService.getComments(postId));
    }

    @DeleteMapping("/comment/{commentId}")
    public ApiResponse<Void> deleteComment(@PathVariable Long commentId, @RequestParam Long userId) {
        communityService.deleteComment(commentId, userId);
        return ApiResponse.success("删除成功", null);
    }

    @PostMapping("/favorite/{postId}")
    public ApiResponse<Void> favoritePost(@PathVariable Long postId, @RequestParam Long userId) {
        communityService.favoritePost(postId, userId);
        return ApiResponse.success("收藏成功", null);
    }

    @DeleteMapping("/favorite/{postId}")
    public ApiResponse<Void> unfavoritePost(@PathVariable Long postId, @RequestParam Long userId) {
        communityService.unfavoritePost(postId, userId);
        return ApiResponse.success("取消收藏", null);
    }

    @GetMapping("/favorites")
    public ApiResponse<List<CommunityPostDto>> getFavorites(@RequestParam Long userId) {
        return ApiResponse.success(communityService.getFavorites(userId));
    }

    @DeleteMapping("/post/{postId}")
    public ApiResponse<Void> deletePost(@PathVariable Long postId, @RequestParam Long userId) {
        communityService.deletePost(postId, userId);
        return ApiResponse.success("删除成功", null);
    }

    @GetMapping("/admin/posts")
    public ApiResponse<List<CommunityPostDto>> getAllPosts(@RequestParam Long userId) {
        return ApiResponse.success(communityService.getAllPostDtos(userId));
    }

    private CommunityCommentDto toCommentDto(com.babyhealth.entity.CommunityComment c) {
        CommunityCommentDto dto = new CommunityCommentDto();
        dto.setId(c.getId());
        dto.setPostId(c.getPostId());
        dto.setUserId(c.getUserId());
        dto.setContent(c.getContent());
        dto.setCreateTime(c.getCreateTime());
        return dto;
    }
}
