package com.wiinvent.demo.repo;

import com.wiinvent.demo.entity.AttendanceConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AttendanceConfigRepository extends JpaRepository<AttendanceConfig, Long> {
    List<AttendanceConfig> findAllByOrderByDayNumberAsc();
}
