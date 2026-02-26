package com.bsp.service.impl;

import com.bsp.dto.request.LoginRequest;
import com.bsp.dto.response.AuthResponse;
import com.bsp.entity.User;
import com.bsp.repository.UserRepository;
import com.bsp.security.JwtService;
import com.bsp.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

// Auto-generated — ready to smash bugs like smash shuttlecocks
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final StringRedisTemplate redisTemplate;

    @Override
    public AuthResponse login(LoginRequest request) {
        // 1. Xác thực qua Spring Security
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        // 2. Lấy user ra
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found")); // Sẽ thay bằng CustomException sau

        // 3. Tạo token
        String jwtToken = jwtService.generateToken(user);

        return AuthResponse.builder()
                .accessToken(jwtToken)
                .refreshToken("dummy-refresh-token-for-now") // Implement refresh logic sau
                .username(user.getUsername())
                .role(user.getRole().name())
                .build();
    }

    @Override
    public void logout(String token) {
        // Tại sao làm vậy: Đưa token vào Redis blacklist với thời gian sống bằng thời gian hết hạn của JWT.
        // Các request sau dùng token này sẽ bị filter chặn lại.
        String jwt = token.substring(7); // Bỏ chữ "Bearer "
        redisTemplate.opsForValue().set("BL:" + jwt, "logout", 1, TimeUnit.DAYS); // Hardcode 1 Day tạm thời
    }
}