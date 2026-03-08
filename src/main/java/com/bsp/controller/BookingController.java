package com.bsp.controller;

import com.bsp.dto.request.BookingRequest;
import com.bsp.dto.response.ApiResponse;
import com.bsp.service.impl.BookingServiceImpl;
import com.bsp.repository.BookingRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/bookings")
@RequiredArgsConstructor
@Tag(name = "Booking", description = "Quản lý Đặt Sân")
@SecurityRequirement(name = "bearerAuth")
public class BookingController {

    private final BookingServiceImpl bookingService;
    private final BookingRepository bookingRepository; // Inject thêm repository để lấy lịch sử

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'BS', 'CUSTOMER')") // CẤP QUYỀN CUSTOMER
    @Operation(summary = "Lấy danh sách đặt sân")
    public ResponseEntity<ApiResponse<Object>> getAllBookings() {
        return ResponseEntity.ok(ApiResponse.success(bookingRepository.findAll(), "Thành công"));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'BS', 'CUSTOMER')") // CẤP QUYỀN CUSTOMER
    @Operation(summary = "Đặt sân mới")
    public ResponseEntity<ApiResponse<Object>> createBooking(@RequestBody BookingRequest request) {
        return ResponseEntity.ok(ApiResponse.success(bookingService.createBooking(request), "Đặt sân thành công"));
    }
}