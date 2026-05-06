package com.babyhealth.repository;

import com.babyhealth.entity.FoodRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface FoodRecordRepository extends JpaRepository<FoodRecord, Long> {
    Page<FoodRecord> findByUserIdOrderByCreateTimeDesc(Long userId, Pageable pageable);
    List<FoodRecord> findByUserIdOrderByCreateTimeDesc(Long userId);
    Page<FoodRecord> findAllByOrderByCreateTimeDesc(Pageable pageable);
}
