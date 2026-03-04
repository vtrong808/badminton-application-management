package com.bsp.controller;

import com.bsp.dto.response.ApiResponse;
import com.bsp.entity.Racket;
import com.bsp.entity.enums.RacketStatus;
import com.bsp.repository.RacketRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/rackets")
@RequiredArgsConstructor
@Tag(name = "Racket", description = "Quản lý thuê vợt cầu lông")
@SecurityRequirement(name = "bearerAuth")
public class RacketController {

    private final RacketRepository racketRepository;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'BS')")
    @Operation(summary = "Lấy danh sách vợt")
    public ResponseEntity<ApiResponse<List<Racket>>> getAllRackets() {
        return ResponseEntity.ok(ApiResponse.success(racketRepository.findAll(), "Thành công"));
    }

    @PutMapping("/{id}/rent")
    @PreAuthorize("hasAnyRole('ADMIN', 'BS')")
    @Transactional // Đảm bảo tính toàn vẹn khi Update
    @Operation(summary = "Cho thuê vợt (Kích hoạt khóa)")
    public ResponseEntity<ApiResponse<Racket>> rentRacket(@PathVariable Long id) {
        Racket racket = racketRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy vợt"));

        if (racket.getStatus() == RacketStatus.IN_USE) {
            throw new RuntimeException("Vợt này đang có người sử dụng!");
        }

        racket.setStatus(RacketStatus.IN_USE);
        racket.setRentedAt(LocalDateTime.now());
        // Khi lưu, Spring Data JPA tự động tăng cột version.
        // Nếu 2 nhân viên cùng click 1 lúc, người thứ 2 sẽ bị văng lỗi OptimisticLockException!
        return ResponseEntity.ok(ApiResponse.success(racketRepository.save(racket), "Cho thuê thành công"));
    }

    @PutMapping("/{id}/return")
    @PreAuthorize("hasAnyRole('ADMIN', 'BS')")
    @Transactional
    @Operation(summary = "Nhận lại vợt (Mở khóa)")
    public ResponseEntity<ApiResponse<Racket>> returnRacket(@PathVariable Long id) {
        Racket racket = racketRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy vợt"));

        racket.setStatus(RacketStatus.AVAILABLE);
        racket.setRentedAt(null);
        return ResponseEntity.ok(ApiResponse.success(racketRepository.save(racket), "Trả vợt thành công"));
    }
}