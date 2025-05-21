package com.wiinvent.demo.controller.dto;

public class AttendanceDTO {
    private String name;
    private int extraPoint;
    private int status; // 1 - đã điểm danh, 0 - chưa điểm danh

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getExtraPoint() {
        return extraPoint;
    }

    public void setExtraPoint(int extraPoint) {
        this.extraPoint = extraPoint;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
