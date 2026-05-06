package com.babyhealth.repository;

import com.babyhealth.entity.CommunityComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommunityCommentRepository extends JpaRepository<CommunityComment, Long> {
    List<CommunityComment> findByPostIdOrderByCreateTimeDesc(Long postId);
    void deleteByPostId(Long postId);
}
