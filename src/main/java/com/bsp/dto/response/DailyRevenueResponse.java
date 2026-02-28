package com.bsp.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class DailyRevenueResponse {
    private String date; // Định dạng YYYY-MM-DD
    private BigDecimal revenue;
}