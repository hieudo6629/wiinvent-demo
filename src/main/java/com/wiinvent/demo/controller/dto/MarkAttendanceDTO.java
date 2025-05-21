package com.wiinvent.demo.controller.dto;

public class MarkAttendanceDTO {
    public MarkAttendanceDTO() {
    }

    public MarkAttendanceDTO(int statusCode, String desc) {
        this.statusCode = statusCode;
        this.desc = desc;
    }

    private int statusCode;
    private String desc;

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
