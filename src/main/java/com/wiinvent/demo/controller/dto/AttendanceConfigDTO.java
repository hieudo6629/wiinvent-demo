package com.wiinvent.demo.controller.dto;

import com.wiinvent.demo.entity.AttendanceConfig;
import java.util.List;

public class AttendanceConfigDTO extends MarkAttendanceDTO{

    private List<AttendanceConfig> data;

    public List<AttendanceConfig> getData() {
        return data;
    }

    public void setData(List<AttendanceConfig> data) {
        this.data = data;
    }
}
