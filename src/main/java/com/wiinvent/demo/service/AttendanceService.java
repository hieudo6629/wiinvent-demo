package com.wiinvent.demo.service;

import com.wiinvent.demo.controller.dto.*;
import com.wiinvent.demo.entity.Attendance;
import com.wiinvent.demo.entity.AttendanceConfig;
import com.wiinvent.demo.entity.PointHistory;
import com.wiinvent.demo.entity.User;
import com.wiinvent.demo.repo.AttendanceRepository;
import com.wiinvent.demo.repo.PointHistoryRepository;
import com.wiinvent.demo.repo.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.ParameterMode;
import jakarta.persistence.PersistenceException;
import jakarta.persistence.StoredProcedureQuery;
import jakarta.transaction.Transactional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.*;

@Service
public class AttendanceService {
    private static final Logger log = LogManager.getLogger(AttendanceService.class);
    @Autowired
    private AttendanceRepository attendanceRepository;
    @Autowired
    private LotusPointLoader lotusPointLoader;
    @Autowired
    private EntityManager entityManager;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PointHistoryRepository pointHistoryRepository;
    @Autowired
    private CacheService cacheService;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    @Autowired
    private UserService userService;

    // check user đã điểm danh
    public boolean checkAttendance(Long userId, LocalDate today) {
        return attendanceRepository.existsByUserIdAndCheckinDate(userId, today);
    }

    // điểm danh
    @Transactional
    public MarkAttendanceDTO markAttendance(Long userId) {
        MarkAttendanceDTO markAttendanceDTO = new MarkAttendanceDTO();
        User user = entityManager.getReference(User.class, userId);
        if (!acquireLock(user.getId(), "mark")) {
            // check khóa, tránh click quá nhanh
            markAttendanceDTO.setStatusCode(400);
            markAttendanceDTO.setDesc("Checkin fail, please try again later!");
            return markAttendanceDTO;
        }
        LocalDate today = LocalDate.now();
        if (checkAttendance(user.getId(), today)) {
            markAttendanceDTO.setStatusCode(400);
            markAttendanceDTO.setDesc("You have checked in today!");
            return markAttendanceDTO;
        }
        LocalDate firstDay = today.with(TemporalAdjusters.firstDayOfMonth());
        LocalDate lastDay = today.with(TemporalAdjusters.lastDayOfMonth());
        long checkinInMonth = attendanceRepository.countCheckinInMonth(user.getId(), firstDay, lastDay);
        AttendanceConfig attendanceConfig = lotusPointLoader.getAttendanceInfo((int) (checkinInMonth + 1));
        try {
//            callUpdatePoints(
//                    userId,
//                    attendanceConfig.getExtraPoint(),
//                    "update_points"
//            );
            // lưu log điểm danh
            int point = attendanceConfig.getExtraPoint();
            Attendance attendance = new Attendance(user, today, point);
            attendanceRepository.save(attendance);
            // cộng điểm cho user
            UserLotusDTO userLotusDTO = userService.getUserLotusDTO(userId);
            user.setLotusPoint(userLotusDTO.getLotusPoint() + point);
            // lưu lịch sử cộng điểm
            PointHistory pointHistory = new PointHistory(user, PointHistory.ActionType.ADD, point, "Cập nhật điểm: +" + point);
            pointHistoryRepository.save(pointHistory);

            checkinInMonth++;
            markAttendanceDTO.setStatusCode(200);
            markAttendanceDTO.setDesc("Checkin successful!");
            // update cache
            cacheService.updateCheckinInMonth(user.getId(), checkinInMonth);
            cacheService.updateUserInfo(user.getId(), user.getLotusPoint());
            cacheService.updateListAttendance(user.getId());
            cacheService.removeCacheHistory(user.getId());
            // xóa khóa
            releaseLock(user.getId(), "mark");
            return markAttendanceDTO;
        } catch (
                Exception e) {
            log.error("markAttendance|UserId|" + user.getId() + "|Point|" + attendanceConfig.getExtraPoint() + "|Exception|" + e.getMessage(), e);
            markAttendanceDTO.setStatusCode(500);
            markAttendanceDTO.setDesc("Internal error, please try again later!");
            return markAttendanceDTO;
        }

    }

    public void callUpdatePoints(Long userId, int point, String transactionName) {
        StoredProcedureQuery query = entityManager
                .createStoredProcedureQuery(transactionName)
                .registerStoredProcedureParameter("p_user_id", Integer.class, ParameterMode.IN)
                .registerStoredProcedureParameter("p_point", Integer.class, ParameterMode.IN);

        query.setParameter("p_user_id", userId.intValue());
        query.setParameter("p_point", point);

        try {
            query.execute();
        } catch (PersistenceException ex) {
            Throwable cause = ex.getCause();
            String msg = (cause != null) ? cause.getMessage() : ex.getMessage();
            throw new RuntimeException("DB error: " + msg, ex);
        }
    }

    public boolean acquireLock(Long userId, String action) {
        String key = "lock:" + action + ":" + userId;
        Boolean success = redisTemplate.opsForValue().setIfAbsent(key, "locked", Duration.ofSeconds(5));
        return Boolean.TRUE.equals(success);
    }

    public void releaseLock(Long userId, String action) {
        String key = "lock:" + action + ":" + userId;
        redisTemplate.delete(key);
    }

    // lấy danh sách điểm danh
    @Cacheable(cacheNames = "rc24h", key = "'ListAttendance:'+#userId", unless = "#result==null")
    public List<AttendanceDTO> getListAttendance(Long userId) {
        LocalDate today = LocalDate.now();

        LocalDate firstDay = today.with(TemporalAdjusters.firstDayOfMonth());
        LocalDate lastDay = today.with(TemporalAdjusters.lastDayOfMonth());
        List<LocalDate> attendanceInMonth = attendanceRepository.findCheckinDatesInMonth(userId, firstDay, lastDay);
        List<AttendanceConfig> attendanceConfigs = lotusPointLoader.getListAttendanceConfig();
        List<AttendanceDTO> attendanceDTOS = new ArrayList<>();
        int checkinInMonth = attendanceInMonth.size();
        if (checkinInMonth < 1) { // chưa điểm danh ngày nào
            attendanceConfigs.forEach(config -> {
                AttendanceDTO attendanceDTO = new AttendanceDTO();
                attendanceDTO.setExtraPoint(config.getExtraPoint());
                attendanceDTO.setName(config.getName());
                attendanceDTO.setStatus(0);
                attendanceDTOS.add(attendanceDTO);
            });
            attendanceDTOS.get(0).setName("Hôm nay");
        } else { // đã điểm danh
            attendanceConfigs.forEach(config -> {
                AttendanceDTO attendanceDTO = new AttendanceDTO();
                attendanceDTO.setExtraPoint(config.getExtraPoint());
                attendanceDTO.setName(config.getName());
                if (config.getDayNumber() <= checkinInMonth) {
                    attendanceDTO.setStatus(1);
                } else {
                    attendanceDTO.setStatus(0);
                }
                attendanceDTOS.add(attendanceDTO);
            });
            if (attendanceInMonth.get(checkinInMonth - 1).isBefore(today)) { // nếu ngày điểm danh gần nhất không phải hôm nay
                if (checkinInMonth < attendanceDTOS.size()) {
                    attendanceDTOS.get(checkinInMonth).setName("Hôm nay");
                }
            } else {
                attendanceDTOS.get(checkinInMonth - 1).setName("Hôm nay");
            }
        }
        return attendanceDTOS;
    }

    // trừ điểm
    @Transactional
    public MarkAttendanceDTO subtractPoint(Long userId, int point) {
        MarkAttendanceDTO markAttendanceDTO = new MarkAttendanceDTO();
        User user = entityManager.getReference(User.class, userId);
        if (!acquireLock(user.getId(), "sub")) {
            // check lock, tránh click quá nhanh
            markAttendanceDTO.setStatusCode(400);
            markAttendanceDTO.setDesc("Subtract point fail, please try again later!");
            return markAttendanceDTO;
        }
        if (user.getLotusPoint() < point) {
            markAttendanceDTO.setStatusCode(400);
            markAttendanceDTO.setDesc("You don't have enough points!");
            return markAttendanceDTO;
        }
        try {
//            callUpdatePoints(
//                    userId,
//                    point,
//                    "subtract_points"
//            );
            // trừ điểm user
            UserLotusDTO userLotusDTO = userService.getUserLotusDTO(userId);
            user.setLotusPoint(userLotusDTO.getLotusPoint() - point);
            // lưu lịch sử trừ điểm
            PointHistory pointHistory = new PointHistory(user, PointHistory.ActionType.SUBTRACT, point, "Cập nhật điểm: -" + point);
            pointHistoryRepository.save(pointHistory);
            markAttendanceDTO.setStatusCode(200);
            markAttendanceDTO.setDesc("Subtract points successful!");
            // update cache
            cacheService.removeCacheHistory(user.getId());
            cacheService.updateUserInfo(user.getId(), user.getLotusPoint());
            // xóa khóa
            releaseLock(user.getId(), "sub");
            return markAttendanceDTO;
        } catch (Exception e) {
            log.error("subtractPoint|UserId|" + user.getId() + "|Point|" + point + "|Exception|" + e.getMessage(), e);
            markAttendanceDTO.setStatusCode(500);
            markAttendanceDTO.setDesc("Internal error, please try again later!");
            return markAttendanceDTO;
        }
    }


    public List<PointHistory> getPointHistory(Long userId, int page, int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<PointHistory> pointHistories = pointHistoryRepository.findAllByUserIdOrderByCreatedAtDesc(userId, pageable);
            if (!pointHistories.isEmpty()) {
                return pointHistories.stream().toList();
            } else {
                return null;
            }
        } catch (Exception e) {
            log.error("getPointHistory|UserId|" + userId + "|Page|" + page + "|Size|" + size + "|Exception|" + e.getMessage(), e);
            return null;
        }
    }

    // lấy lịch sử cộng điểm
    @Cacheable(cacheNames = "rc24h", key = "'PointHistory:'+#userId+':'+#page+':'+#size", unless = "#result==null")
    public List<PointHistoryDTO> getPointHistoryDTO(Long userId, int page, int size) {
        List<PointHistory> pointHistoryList = getPointHistory(userId, page, size);
        List<PointHistoryDTO> historyDTOList = new ArrayList<>();
        if (pointHistoryList != null) {
            pointHistoryList.forEach(p -> {
                PointHistoryDTO pointHistoryDTO1 = new PointHistoryDTO();
                pointHistoryDTO1.setId(p.getId());
                pointHistoryDTO1.setPoint(p.getPoint());
                pointHistoryDTO1.setNote(p.getNote());
                pointHistoryDTO1.setActionType(String.valueOf(p.getActionType()));
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                pointHistoryDTO1.setCreatedAt(p.getCreatedAt().format(formatter));
                historyDTOList.add(pointHistoryDTO1);
            });
        } else {
            return null;
        }
        return historyDTOList;
    }
}
