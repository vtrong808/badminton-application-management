package com.bsp.dto.request;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class SalaryUpdateRequest {
    private BigDecimal bonusSalary;
    private BigDecimal penaltySalary;
}