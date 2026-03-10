package com.bsp.dto.request;

import lombok.Data;

@Data
public class ShiftRequest {
    private Long userId;
    private String shiftDate;
    private String shiftType;
}