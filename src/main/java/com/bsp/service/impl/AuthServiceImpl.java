package com.bsp.service.impl;

import com.bsp.dto.request.LoginRequest;
import com.bsp.dto.response.AuthResponse;
import com.bsp.entity.Customer;
import com.bsp.entity.User;
import com.bsp.repository.CustomerRepository;
import com.bsp.repository.UserRepository;
import com.bsp.security.JwtService;
import com.bsp.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Override
    public AuthResponse login(LoginRequest request) {
        // 1. Tìm trong bảng Users (Admin, Staff)
        Optional<User> userOpt = userRepository.findByUsername(request.getUsername());

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                throw new RuntimeException("Sai mật khẩu");
            }
            String token = jwtService.generateToken(user);
            return AuthResponse.builder()
                    .accessToken(token) // FIX 1: Sửa thành accessToken
                    .username(user.getUsername())
                    .role(user.getRole().name())
                    .build();
        }

        // 2. Nếu không thấy User, tìm trong bảng Customers (Số điện thoại)
        Optional<Customer> customerOpt = customerRepository.findAll().stream()
                .filter(c -> c.getPhoneNumber().equals(request.getUsername())) // Dùng SĐT làm username
                .findFirst();

        if (customerOpt.isPresent()) {
            Customer customer = customerOpt.get();
            if (!passwordEncoder.matches(request.getPassword(), customer.getPassword())) {
                throw new RuntimeException("Sai mật khẩu khách hàng");
            }

            // Xây dựng Token cho Khách Hàng
            String token = jwtService.generateToken(
                    org.springframework.security.core.userdetails.User.builder()
                            .username(customer.getPhoneNumber())
                            .password(customer.getPassword())
                            .roles("CUSTOMER") // Tạo Role giả định cho Spring Security
                            .build()
            );
            return AuthResponse.builder()
                    .accessToken(token)
                    .username(customer.getFullName())
                    .role("ROLE_CUSTOMER")
                    .build();
        }

        throw new RuntimeException("Không tìm thấy tài khoản");
    }

    @Override
    public void logout(String token) {
    }
}