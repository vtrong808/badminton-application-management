package com.bsp.controller;

import com.bsp.dto.response.ApiResponse;
import com.bsp.entity.User;
import com.bsp.entity.enums.UserRole;
import com.bsp.repository.UserRepository;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class UserController {
    private final UserRepository userRepository;

    @GetMapping("/staff")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<User>>> getStaff() {
        // Chỉ lấy nhân viên (BS)
        List<User> staff = userRepository.findAll().stream()
                .filter(u -> u.getRole() == UserRole.ROLE_BS)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(staff, "Thành công"));
    }
}