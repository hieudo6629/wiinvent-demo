package com.wiinvent.demo.controller.dto;

import com.wiinvent.demo.entity.AttendanceConfig;

import java.util.List;

public class ListAttendanceDTO extends MarkAttendanceDTO {

    private List<AttendanceDTO> data;

    public List<AttendanceDTO> getData() {
        return data;
    }

    public void setData(List<AttendanceDTO> data) {
        this.data = data;
    }
}
