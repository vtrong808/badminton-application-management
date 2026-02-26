package com.bsp.service;

import com.bsp.dto.request.LoginRequest;
import com.bsp.dto.response.AuthResponse;

public interface AuthService {
    AuthResponse login(LoginRequest request);
    void logout(String token);
}