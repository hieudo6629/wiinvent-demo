package com.wiinvent.demo.controller.dto;

public class UserInfoDTO extends MarkAttendanceDTO{
    private UserLotusDTO data;

    public UserLotusDTO getData() {
        return data;
    }

    public void setData(UserLotusDTO data) {
        this.data = data;
    }

}
