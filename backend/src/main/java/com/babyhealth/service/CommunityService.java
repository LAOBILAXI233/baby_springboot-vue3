package com.babyhealth.service;

import com.babyhealth.dto.CommunityCommentDto;
import com.babyhealth.dto.CommunityPostDto;
import com.babyhealth.entity.*;
import com.babyhealth.repository.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class CommunityService {

    private final CommunityPostRepository postRepository;
    private final CommunityCommentRepository commentRepository;
    private final CommunityLikeRepository likeRepository;
    private final CommunityFavoriteRepository favoriteRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public CommunityService(CommunityPostRepository postRepository,
                            CommunityCommentRepository commentRepository,
                            CommunityLikeRepository likeRepository,
                            CommunityFavoriteRepository favoriteRepository,
                            UserRepository userRepository) {
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
        this.likeRepository = likeRepository;
        this.favoriteRepository = favoriteRepository;
        this.userRepository = userRepository;
    }

    public CommunityPost createPost(Long userId, String content, String images) {
        CommunityPost post = new CommunityPost();
        post.setUserId(userId);
        post.setContent(content);
        post.setImages(images);
        return postRepository.save(post);
    }

    public Page<CommunityPost> getPosts(Pageable pageable) {
        return postRepository.findAllByOrderByCreateTimeDesc(pageable);
    }

    public List<CommunityPostDto> getPostDtos(Pageable pageable, Long currentUserId) {
        Page<CommunityPost> page = getPosts(pageable);
        List<CommunityPostDto> result = new ArrayList<>();
        for (CommunityPost post : page.getContent()) {
            result.add(toPostDto(post, currentUserId));
        }
        return result;
    }

    public CommunityPostDto toPostDto(CommunityPost post, Long currentUserId) {
        CommunityPostDto dto = new CommunityPostDto();
        dto.setId(post.getId());
        dto.setUserId(post.getUserId());
        dto.setContent(post.getContent());
        dto.setImages(parseImages(post.getImages()));
        dto.setLikeCount(post.getLikeCount());
        dto.setCommentCount(post.getCommentCount());
        dto.setCreateTime(post.getCreateTime());

        User user = userRepository.findById(post.getUserId()).orElse(null);
        if (user != null) {
            dto.setUsername(user.getUsername());
            dto.setNickname(user.getNickname());
            dto.setAvatar(user.getAvatar());
        }

        if (currentUserId != null) {
            dto.setLiked(likeRepository.findByPostIdAndUserId(post.getId(), currentUserId).isPresent());
            dto.setFavorited(favoriteRepository.findByPostIdAndUserId(post.getId(), currentUserId).isPresent());
        } else {
            dto.setLiked(false);
            dto.setFavorited(false);
        }
        return dto;
    }

    public CommunityPost likePost(Long postId, Long userId) {
        CommunityPost post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("帖子不存在"));
        Optional<CommunityLike> existing = likeRepository.findByPostIdAndUserId(postId, userId);
        if (existing.isEmpty()) {
            CommunityLike like = new CommunityLike();
            like.setPostId(postId);
            like.setUserId(userId);
            likeRepository.save(like);
            post.setLikeCount(post.getLikeCount() + 1);
            postRepository.save(post);
        }
        return post;
    }

    public CommunityPost unlikePost(Long postId, Long userId) {
        CommunityPost post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("帖子不存在"));
        Optional<CommunityLike> existing = likeRepository.findByPostIdAndUserId(postId, userId);
        if (existing.isPresent()) {
            likeRepository.delete(existing.get());
            post.setLikeCount(Math.max(0, post.getLikeCount() - 1));
            postRepository.save(post);
        }
        return post;
    }

    public CommunityComment addComment(Long postId, Long userId, String content, String images) {
        CommunityComment comment = new CommunityComment();
        comment.setPostId(postId);
        comment.setUserId(userId);
        comment.setContent(content);
        comment.setImages(images);
        commentRepository.save(comment);

        CommunityPost post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("帖子不存在"));
        post.setCommentCount(post.getCommentCount() + 1);
        postRepository.save(post);

        return comment;
    }

    public List<CommunityCommentDto> getComments(Long postId) {
        List<CommunityComment> comments = commentRepository.findByPostIdOrderByCreateTimeDesc(postId);
        List<CommunityCommentDto> result = new ArrayList<>();
        for (CommunityComment c : comments) {
            CommunityCommentDto dto = new CommunityCommentDto();
            dto.setId(c.getId());
            dto.setPostId(c.getPostId());
            dto.setUserId(c.getUserId());
            dto.setContent(c.getContent());
            dto.setImages(parseImages(c.getImages()));
            dto.setCreateTime(c.getCreateTime());
            User user = userRepository.findById(c.getUserId()).orElse(null);
            if (user != null) {
                dto.setUsername(user.getUsername());
                dto.setNickname(user.getNickname());
                dto.setAvatar(user.getAvatar());
            }
            result.add(dto);
        }
        return result;
    }

    public void deleteComment(Long commentId, Long userId) {
        CommunityComment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("评论不存在"));
        User user = userRepository.findById(userId).orElse(null);
        boolean isAdmin = user != null && user.getRole() != null && user.getRole() == 2;
        if (!comment.getUserId().equals(userId) && !isAdmin) {
            throw new RuntimeException("无权删除评论");
        }
        commentRepository.delete(comment);

        CommunityPost post = postRepository.findById(comment.getPostId()).orElse(null);
        if (post != null) {
            post.setCommentCount(Math.max(0, post.getCommentCount() - 1));
            postRepository.save(post);
        }
    }

    public void favoritePost(Long postId, Long userId) {
        Optional<CommunityFavorite> existing = favoriteRepository.findByPostIdAndUserId(postId, userId);
        if (existing.isEmpty()) {
            CommunityFavorite fav = new CommunityFavorite();
            fav.setPostId(postId);
            fav.setUserId(userId);
            favoriteRepository.save(fav);
        }
    }

    public void unfavoritePost(Long postId, Long userId) {
        Optional<CommunityFavorite> existing = favoriteRepository.findByPostIdAndUserId(postId, userId);
        existing.ifPresent(favoriteRepository::delete);
    }

    public List<CommunityPostDto> getFavorites(Long userId) {
        List<CommunityFavorite> favs = favoriteRepository.findByUserId(userId);
        List<CommunityPostDto> result = new ArrayList<>();
        for (CommunityFavorite fav : favs) {
            postRepository.findById(fav.getPostId()).ifPresent(post -> {
                result.add(toPostDto(post, userId));
            });
        }
        return result;
    }

    @Transactional
    public void deletePost(Long postId, Long currentUserId) {
        CommunityPost post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("帖子不存在"));

        if (post.getUserId() == null) {
            throw new RuntimeException("帖子数据异常，无法删除");
        }

        User user = userRepository.findById(currentUserId).orElse(null);
        boolean isAdmin = user != null && user.getRole() != null && user.getRole() == 2;
        if (!Objects.equals(post.getUserId(), currentUserId) && !isAdmin) {
            throw new RuntimeException("无权删除帖子");
        }

        commentRepository.deleteByPostId(postId);
        likeRepository.deleteByPostId(postId);
        favoriteRepository.deleteByPostId(postId);
        postRepository.delete(post);
    }

    public List<CommunityPostDto> getAllPostDtos(Long currentUserId) {
        List<CommunityPost> posts = postRepository.findAll();
        List<CommunityPostDto> result = new ArrayList<>();
        for (CommunityPost post : posts) {
            result.add(toPostDto(post, currentUserId));
        }
        return result;
    }

    private List<String> parseImages(String images) {
        if (images == null || images.isEmpty()) return new ArrayList<>();
        try {
            return objectMapper.readValue(images, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            List<String> list = new ArrayList<>();
            for (String s : images.split(",")) {
                if (!s.trim().isEmpty()) list.add(s.trim());
            }
            return list;
        }
    }
}
