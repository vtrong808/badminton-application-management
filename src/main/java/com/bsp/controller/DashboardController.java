package com.bsp.controller;

import com.bsp.dto.response.ApiResponse;
import com.bsp.dto.response.DailyRevenueResponse;
import com.bsp.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard", description = "Thống kê & Báo cáo Analytics")
@SecurityRequirement(name = "bearerAuth")
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/revenue/daily")
    @PreAuthorize("hasRole('ADMIN')") // Nghiệp vụ nhạy cảm, chỉ ADMIN được xem
    @Operation(summary = "Lấy thống kê doanh thu 30 ngày gần nhất")
    public ResponseEntity<ApiResponse<List<DailyRevenueResponse>>> getDailyRevenue() {
        List<DailyRevenueResponse> revenueData = dashboardService.getRevenueLast30Days();
        return ResponseEntity.ok(ApiResponse.success(revenueData, "Lấy thống kê thành công"));
    }
}