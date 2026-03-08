package com.bsp.controller;

import com.bsp.dto.request.WalkInRequest;
import com.bsp.dto.response.ApiResponse;
import com.bsp.entity.WalkInSession;
import com.bsp.service.WalkInService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/walk-ins")
@RequiredArgsConstructor
@Tag(name = "Walk-in Session", description = "Quản lý khách vãng lai")
@SecurityRequirement(name = "bearerAuth")
public class WalkInController {

    private final WalkInService walkInService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'BS', 'CUSTOMER')") // ĐÃ THÊM CUSTOMER
    @Operation(summary = "Lấy danh sách khách vãng lai hôm nay")
    public ResponseEntity<ApiResponse<List<WalkInSession>>> getTodaySessions() {
        List<WalkInSession> sessions = walkInService.getTodaySessions();
        return ResponseEntity.ok(ApiResponse.success(sessions, "Thành công"));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'BS', 'CUSTOMER')") // ĐÃ THÊM CUSTOMER
    @Operation(summary = "Tạo phiên chơi cho khách vãng lai")
    public ResponseEntity<ApiResponse<WalkInSession>> createSession(
            @RequestBody WalkInRequest request) {
        WalkInSession session = walkInService.createSession(request);
        return ResponseEntity.ok(ApiResponse.success(session, "Tạo thành công"));
    }

    @PutMapping("/{id}/finish")
    @PreAuthorize("hasAnyRole('ADMIN', 'BS')") // Chỉ nhân viên mới có quyền kết thúc giờ
    @Operation(summary = "Kết thúc phiên chơi")
    public ResponseEntity<ApiResponse<WalkInSession>> finishSession(
            @PathVariable Long id) {
        WalkInSession session = walkInService.finishSession(id);
        return ResponseEntity.ok(ApiResponse.success(session, "Đã kết thúc"));
    }
}