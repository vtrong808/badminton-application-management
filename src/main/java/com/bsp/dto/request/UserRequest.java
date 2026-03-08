package com.bsp.dto.request;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class UserRequest {
    private String username;
    private String password;
    private BigDecimal baseSalary; // Lương cơ bản
}