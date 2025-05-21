package com.wiinvent.demo.controller;

import com.wiinvent.demo.controller.dto.*;
import com.wiinvent.demo.entity.AttendanceConfig;
import com.wiinvent.demo.entity.PointHistory;
import com.wiinvent.demo.entity.User;
import com.wiinvent.demo.service.AttendanceService;
import com.wiinvent.demo.service.LotusPointLoader;
import com.wiinvent.demo.service.UserService;
import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/v1/lotus")
public class LotusController {
    private static final Logger log = LogManager.getLogger(LotusController.class);
    @Autowired
    private LotusPointLoader lotusPointLoader;
    @Autowired
    private AttendanceService attendanceService;
    @Autowired
    private UserService userService;

    @GetMapping("/list/attendance")
    public ResponseEntity<?> getListAttendance(@RequestParam("userId") long userId) {
        try {
            long start = System.currentTimeMillis();
            log.info("getListAttendance|UserId|" + userId + "|START");
            ListAttendanceDTO listAttendanceDTO = new ListAttendanceDTO();
            if (!userService.checkExistUser(userId)) {
                listAttendanceDTO.setStatusCode(404);
                listAttendanceDTO.setDesc("UserId does not exist!");
                listAttendanceDTO.setData(new ArrayList<>());
                return ResponseEntity.badRequest().body(listAttendanceDTO);
            }
            List<AttendanceDTO> list = attendanceService.getListAttendance(userId);
            listAttendanceDTO.setStatusCode(200);
            listAttendanceDTO.setDesc("Successfully!");
            listAttendanceDTO.setData(list);
            long proc = System.currentTimeMillis() - start;
            log.info("getListAttendance|UserId|" + userId + "|ExecuteTime|" + proc);
            return ResponseEntity.ok().body(listAttendanceDTO);
        } catch (Exception e) {
            log.error("getListAttendance|UserId|" + userId + "|Exception|" + e.getMessage(), e);
            ListAttendanceDTO listAttendanceDTO = new ListAttendanceDTO();
            listAttendanceDTO.setStatusCode(500);
            listAttendanceDTO.setDesc("Internal error, please try again later!");
            listAttendanceDTO.setData(new ArrayList<>());
            return ResponseEntity.internalServerError().body(listAttendanceDTO);
        }
    }

    @PostMapping("/mark/attendance")
    public ResponseEntity<?> markAttendance(@RequestParam("userId") Long userId) {
        try {
            long start = System.currentTimeMillis();
            log.info("markAttendance|UserId|" + userId + "|START");
            MarkAttendanceDTO markAttendanceDTO = new MarkAttendanceDTO();
            if (!isCheckinTime(LocalTime.now())) {
                markAttendanceDTO.setStatusCode(400);
                markAttendanceDTO.setDesc("Not checkin time!");
                return ResponseEntity.badRequest().body(markAttendanceDTO);
            }
            if (!userService.checkExistUser(userId)) {
                markAttendanceDTO.setStatusCode(404);
                markAttendanceDTO.setDesc("UserId does not exist!");
                return ResponseEntity.badRequest().body(markAttendanceDTO);
            }
            markAttendanceDTO = attendanceService.markAttendance(userId);
            long proc = System.currentTimeMillis() - start;
            log.info("markAttendance|UserId|" + userId + "|ExecuteTime|" + proc);
            return switch (markAttendanceDTO.getStatusCode()) {
                case 200 -> ResponseEntity.ok().body(markAttendanceDTO);
                case 400 -> ResponseEntity.badRequest().body(markAttendanceDTO);
                default -> ResponseEntity.internalServerError().body(markAttendanceDTO);
            };
        } catch (Exception e) {
            log.error("markAttendance|UserId|" + userId + "|Exception|" + e.getMessage(), e);
            MarkAttendanceDTO markAttendanceDTO = new MarkAttendanceDTO();
            markAttendanceDTO.setStatusCode(500);
            markAttendanceDTO.setDesc("Internal error, please try again later!");
            return ResponseEntity.internalServerError().body(markAttendanceDTO);
        }
    }

    public boolean isCheckinTime(LocalTime checkinTime) {
        LocalTime morningStart = LocalTime.of(9, 0);
        LocalTime morningEnd = LocalTime.of(11, 0);
        LocalTime eveningStart = LocalTime.of(19, 0);
        LocalTime eveningEnd = LocalTime.of(21, 0);

        return (checkinTime.isAfter(morningStart) && checkinTime.isBefore(morningEnd)) ||
                (checkinTime.isAfter(eveningStart) && checkinTime.isBefore(eveningEnd));
    }


    @PostMapping("/sub/point")
    public ResponseEntity<?> subtractPoint(@RequestParam("userId") Long userId,
                                           @RequestParam("point") int point) {
        try {
            long start = System.currentTimeMillis();
            log.info("subtractPoint|UserId|" + userId + "|Point|" + point + "|START");
            MarkAttendanceDTO markAttendanceDTO = new MarkAttendanceDTO();
            if (point < 1) {
                markAttendanceDTO.setStatusCode(400);
                markAttendanceDTO.setDesc("Point must be greater than 0!");
                return ResponseEntity.badRequest().body(markAttendanceDTO);
            }
            markAttendanceDTO = attendanceService.subtractPoint(userId, point);
            long proc = System.currentTimeMillis() - start;
            log.info("subtractPoint|UserId|" + userId + "|Point|" + point + "|ExecuteTime|" + proc);
            return switch (markAttendanceDTO.getStatusCode()) {
                case 200 -> ResponseEntity.ok().body(markAttendanceDTO);
                case 400 -> ResponseEntity.badRequest().body(markAttendanceDTO);
                default -> ResponseEntity.internalServerError().body(markAttendanceDTO);
            };
        } catch (Exception e) {
            log.error("subtractPoint|UserId|" + userId + "|Point|" + point + "|Exception|" + e.getMessage(), e);
            MarkAttendanceDTO markAttendanceDTO = new MarkAttendanceDTO();
            markAttendanceDTO.setStatusCode(500);
            markAttendanceDTO.setDesc("Internal error, please try again later!");
            return ResponseEntity.internalServerError().body(markAttendanceDTO);
        }
    }

    @GetMapping("/history/point")
    public ResponseEntity<?> getPointHistory(@RequestParam("userId") Long userId,
                                             @RequestParam("page") int page,
                                             @RequestParam("size") int size) {
        try {
            long start = System.currentTimeMillis();
            log.info("getPointHistory|UserId|" + userId + "|Page|" + page + "|size|" + size + "|START");
            ListPointHistoryDTO listPointHistoryDTO = new ListPointHistoryDTO();

            if (!userService.checkExistUser(userId)) {
                listPointHistoryDTO.setStatusCode(404);
                listPointHistoryDTO.setDesc("UserId does not exist!");
                listPointHistoryDTO.setData(new ArrayList<>());
                return ResponseEntity.badRequest().body(listPointHistoryDTO);
            }
            List<PointHistoryDTO> historyDTOList = attendanceService.getPointHistoryDTO(userId, page, size);
            if (historyDTOList != null) {
                listPointHistoryDTO.setStatusCode(200);
                listPointHistoryDTO.setDesc("success");
                listPointHistoryDTO.setData(historyDTOList);
            } else {
                listPointHistoryDTO.setStatusCode(404);
                listPointHistoryDTO.setDesc("empty");
                listPointHistoryDTO.setData(new ArrayList<>());
            }
            long proc = System.currentTimeMillis() - start;
            log.info("getPointHistory|UserId|" + userId + "|Page|" + page + "|size|" + size + "|ExecuteTime|" + proc);
            return ResponseEntity.ok().body(listPointHistoryDTO);
        } catch (Exception e) {
            log.info("getPointHistory|UserId|" + userId + "|Page|" + page + "|size|" + size + "|Exception|" + e.getMessage(), e);
            ListPointHistoryDTO listPointHistoryDTO = new ListPointHistoryDTO();
            listPointHistoryDTO.setStatusCode(500);
            listPointHistoryDTO.setDesc("Internal error, please try again later!");
            listPointHistoryDTO.setData(new ArrayList<>());
            return ResponseEntity.internalServerError().body(listPointHistoryDTO);
        }
    }
}
