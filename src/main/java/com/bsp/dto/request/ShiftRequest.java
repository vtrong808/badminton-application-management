package com.bsp.dto.request;
import lombok.Data;
import java.time.LocalDate;

@Data
public class ShiftRequest {
    private Long userId;
    private LocalDate shiftDate;
    private String shiftType; // MORNING, AFTERNOON, EVENING
}