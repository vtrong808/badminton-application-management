package com.bsp.controller;

import com.bsp.dto.request.CourtRequest;
import com.bsp.dto.response.ApiResponse;
import com.bsp.entity.Court;
import com.bsp.repository.CourtRepository;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// Viết nhanh Controller gọi thẳng Repository (Trong thực tế nên qua Service như Product)
@RestController
@RequestMapping("/api/v1/courts")
@RequiredArgsConstructor
@Tag(name = "Court", description = "Quản lý Sân Cầu Lông")
@SecurityRequirement(name = "bearerAuth")
public class CourtController {

    private final CourtRepository courtRepository;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'BS', 'USER')")
    public ResponseEntity<ApiResponse<List<Court>>> getAllCourts() {
        return ResponseEntity.ok(ApiResponse.success(courtRepository.findAll(), "Lấy danh sách sân thành công"));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Court>> createCourt(@RequestBody CourtRequest request) {
        Court court = Court.builder().name(request.getName()).type(request.getType())
                .status(request.getStatus()).priceDay(request.getPriceDay())
                .priceNight(request.getPriceNight()).build();
        return ResponseEntity.ok(ApiResponse.success(courtRepository.save(court), "Tạo sân thành công"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Court>> updateCourt(@PathVariable Long id, @RequestBody CourtRequest request) {
        Court court = courtRepository.findById(id).orElseThrow(() -> new RuntimeException("Sân không tồn tại"));
        court.setName(request.getName());
        court.setType(request.getType());
        court.setStatus(request.getStatus());
        court.setPriceDay(request.getPriceDay());
        court.setPriceNight(request.getPriceNight());
        return ResponseEntity.ok(ApiResponse.success(courtRepository.save(court), "Cập nhật sân thành công"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> deleteCourt(@PathVariable Long id) {
        courtRepository.deleteById(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Xóa sân thành công"));
    }
}