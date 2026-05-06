package com.babyhealth.repository;

import com.babyhealth.entity.CommunityFavorite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommunityFavoriteRepository extends JpaRepository<CommunityFavorite, Long> {
    Optional<CommunityFavorite> findByPostIdAndUserId(Long postId, Long userId);
    List<CommunityFavorite> findByUserId(Long userId);
    void deleteByPostIdAndUserId(Long postId, Long userId);
    void deleteByPostId(Long postId);
}
