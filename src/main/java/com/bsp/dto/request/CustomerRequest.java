package com.bsp.dto.request;

import lombok.Data;

@Data
public class CustomerRequest {
    private String fullName;
    private String phoneNumber;
    private String password;
    private String customerType;
}