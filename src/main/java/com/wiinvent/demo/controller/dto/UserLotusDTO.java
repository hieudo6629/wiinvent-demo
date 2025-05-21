package com.wiinvent.demo.controller.dto;

public class UserLotusDTO {
    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public int getLotusPoint() {
        return lotusPoint;
    }

    public void setLotusPoint(int lotusPoint) {
        this.lotusPoint = lotusPoint;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    private String fullName;
    private int lotusPoint;
    private String avatar;
}
