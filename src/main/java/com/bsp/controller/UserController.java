package com.bsp.controller;

import com.bsp.dto.request.SalaryUpdateRequest;
import com.bsp.dto.request.UserRequest;
import com.bsp.dto.response.ApiResponse;
import com.bsp.entity.User;
import com.bsp.entity.enums.UserRole;
import com.bsp.repository.UserRepository;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @GetMapping("/staff")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<User>>> getStaff() {
        List<User> staff = userRepository.findAll().stream()
                .filter(u -> u.getRole() == UserRole.ROLE_BS)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(staff, "Thành công"));
    }

    // --- CRUD NHÂN VIÊN ---
    @PostMapping("/staff")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<User>> createStaff(@RequestBody UserRequest request) {
        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword())) // Mã hóa Password
                .role(UserRole.ROLE_BS)
                .baseSalary(request.getBaseSalary() != null ? request.getBaseSalary() : new BigDecimal("3000000"))
                .bonusSalary(BigDecimal.ZERO)
                .penaltySalary(BigDecimal.ZERO)
                .build();
        return ResponseEntity.ok(ApiResponse.success(userRepository.save(user), "Tạo nhân viên thành công"));
    }

    @PutMapping("/staff/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<User>> updateStaff(@PathVariable Long id, @RequestBody UserRequest request) {
        User user = userRepository.findById(id).orElseThrow();
        user.setUsername(request.getUsername());

        // Nếu admin có nhập pass mới thì đổi pass
        if (request.getPassword() != null && !request.getPassword().trim().isEmpty()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        if (request.getBaseSalary() != null) user.setBaseSalary(request.getBaseSalary());

        return ResponseEntity.ok(ApiResponse.success(userRepository.save(user), "Cập nhật thành công"));
    }

    @DeleteMapping("/staff/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> deleteStaff(@PathVariable Long id) {
        userRepository.deleteById(id);
        return ResponseEntity.ok(ApiResponse.success("OK", "Xóa thành công"));
    }

    // --- CẬP NHẬT THƯỞNG PHẠT (Giữ nguyên) ---
    @PutMapping("/staff/{id}/salary")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<User>> updateSalary(@PathVariable Long id, @RequestBody SalaryUpdateRequest request) {
        User user = userRepository.findById(id).orElseThrow();
        user.setBonusSalary(request.getBonusSalary());
        user.setPenaltySalary(request.getPenaltySalary());
        return ResponseEntity.ok(ApiResponse.success(userRepository.save(user), "Cập nhật lương thành công"));
    }
}