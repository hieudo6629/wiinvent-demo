package com.wiinvent.demo.controller.dto;

import java.util.List;

public class ListPointHistoryDTO extends MarkAttendanceDTO{
    private List<PointHistoryDTO> data;

    public List<PointHistoryDTO> getData() {
        return data;
    }

    public void setData(List<PointHistoryDTO> data) {
        this.data = data;
    }
}
