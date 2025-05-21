package com.wiinvent.demo.controller.dto;

public class JwtTokenDTO extends MarkAttendanceDTO{
    private String data;

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}
