package com.wiinvent.demo.repo;

import com.wiinvent.demo.entity.Attendance;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    // Kiểm tra đã điểm danh hôm nay chưa
    boolean existsByUserIdAndCheckinDate(Long userId, LocalDate checkinDate);

    // Đếm số lần checkin trong tháng
    @Cacheable(cacheNames = "rc24h", key = "'CheckinInMonth:'+#userId")
    @Query("SELECT COUNT(c) FROM Attendance c WHERE c.user.id = :userId AND c.checkinDate BETWEEN :start AND :end")
    long countCheckinInMonth(@Param("userId") Long userId,
                             @Param("start") LocalDate start,
                             @Param("end") LocalDate end);

    // Lấy danh sách ngày đã checkin trong tháng
    @Query("SELECT c.checkinDate FROM Attendance c WHERE c.user.id = :userId AND c.checkinDate BETWEEN :start AND :end ORDER BY id ASC")
    List<LocalDate> findCheckinDatesInMonth(@Param("userId") Long userId,
                                            @Param("start") LocalDate start,
                                            @Param("end") LocalDate end);

    @Modifying
    @Query(value = "CALL update_points(:userId, :actionType, :point)", nativeQuery = true)
    void updatePoints(@Param("userId") Long userId, @Param("actionType") String actionType, @Param("point") int point);
}
