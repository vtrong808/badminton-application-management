package com.bsp.dto.request;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class WalkInRequest {
    private String customerName;
    private String phoneNumber;
    private Integer age;
    private BigDecimal price;
}