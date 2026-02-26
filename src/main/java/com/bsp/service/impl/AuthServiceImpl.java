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

/**
 * Service xử lý authentication:
 * - Login (xác thực + tạo JWT)
 * - Logout (blacklist JWT bằng Redis)
 */
// Auto-generated — ready to smash bugs like smash shuttlecocks
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    // Repository truy xuất user từ database
    private final UserRepository userRepository;

    // Service tạo và xử lý JWT
    private final JwtService jwtService;

    // Spring Security component để authenticate username/password
    private final AuthenticationManager authenticationManager;

    // Redis dùng để lưu blacklist token khi logout
    private final StringRedisTemplate redisTemplate;

    /**
     * Xử lý login:
     * 1. Authenticate bằng Spring Security
     * 2. Lấy thông tin user từ DB
     * 3. Tạo JWT token
     * 4. Trả về AuthResponse
     */
    @Override
    public AuthResponse login(LoginRequest request) {

        // ================= 1. AUTHENTICATION =================
        // Nếu sai username/password → Spring sẽ throw exception
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        // ================= 2. LOAD USER =================
        // Sau khi authenticate thành công, lấy user từ database
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        // TODO: Nên thay bằng CustomException (ví dụ: NotFoundException)

        // ================= 3. GENERATE JWT =================
        String jwtToken = jwtService.generateToken(user);

        // ================= 4. BUILD RESPONSE =================
        return AuthResponse.builder()
                .accessToken(jwtToken)
                .refreshToken("dummy-refresh-token-for-now")
                // TODO: Sau này implement refresh token chuẩn
                .username(user.getUsername())
                .role(user.getRole().name())
                .build();
    }

    /**
     * Logout:
     * - Đưa JWT vào Redis blacklist
     * - Các request sau đó sẽ bị JwtFilter chặn lại nếu token nằm trong blacklist
     */
    @Override
    public void logout(String token) {

        // Bỏ tiền tố "Bearer " trong header Authorization
        String jwt = token.substring(7);

        /*
         * Lưu token vào Redis với key: BL:<jwt>
         * Value không quan trọng (chỉ để đánh dấu tồn tại)
         * TTL hiện tại đang hardcode 1 ngày (nên đồng bộ với expire time của JWT)
         */
        redisTemplate.opsForValue().set(
                "BL:" + jwt,
                "logout",
                1,
                TimeUnit.DAYS
        );

        // TODO:
        // - Nên lấy expire time từ JWT rồi set TTL chính xác thay vì hardcode
        // - Có thể optimize bằng cách chỉ lưu đến khi token hết hạn
    }
}