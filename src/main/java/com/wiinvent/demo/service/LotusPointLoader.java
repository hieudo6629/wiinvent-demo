package com.wiinvent.demo.service;

import com.wiinvent.demo.entity.AttendanceConfig;
import com.wiinvent.demo.entity.PointHistory;
import com.wiinvent.demo.repo.AttendanceConfigRepository;
import jakarta.annotation.PostConstruct;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class LotusPointLoader {
    private static final Logger log = LogManager.getLogger(LotusPointLoader.class);
    private Map<Integer, AttendanceConfig> attendanceConfigMap = new HashMap<>();
    private List<AttendanceConfig> attendanceConfigs = new ArrayList<>();
    @Autowired
    private AttendanceConfigRepository configRepository;

    @PostConstruct
    public void loadConfig() {
        long start = System.currentTimeMillis();
        attendanceConfigs = configRepository.findAllByOrderByDayNumberAsc();
        attendanceConfigs.forEach(config -> attendanceConfigMap.put(config.getDayNumber(), config));
        long proc = System.currentTimeMillis() - start;
        log.info("Load config success|ExecuteTime|" + proc);
    }

    public void reloadConfig() {
        attendanceConfigMap.clear();
        loadConfig();
    }

    public AttendanceConfig getAttendanceInfo(int dayNumber) {
        return attendanceConfigMap.getOrDefault(dayNumber, new AttendanceConfig());
    }

    public List<AttendanceConfig> getListAttendanceConfig() {
        return attendanceConfigs;
    }

    public Map<Integer, AttendanceConfig> getAttendanceConfigMap() {
        return attendanceConfigMap;
    }

}
