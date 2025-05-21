package com.wiinvent.demo.repo;

import com.wiinvent.demo.entity.PointHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PointHistoryRepository extends JpaRepository<PointHistory,Long> {
    Page<PointHistory> findAllByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
}
