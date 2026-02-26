package com.bsp.controller;

import com.bsp.dto.request.BookingRequest;
import com.bsp.dto.response.ApiResponse;
import com.bsp.service.impl.BookingServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/bookings")
@RequiredArgsConstructor
@Tag(name = "Booking", description = "Quản lý Đặt Sân")
@SecurityRequirement(name = "bearerAuth")
public class BookingController {

    private final BookingServiceImpl bookingService;

    @PostMapping
    @Operation(summary = "Đặt sân mới")
    public ResponseEntity<ApiResponse<Object>> createBooking(@RequestBody BookingRequest request) {
        // Trong thực tế sẽ map ra BookingResponse, ở đây anh trả về Object entity luôn cho lẹ để em test
        return ResponseEntity.ok(ApiResponse.success(bookingService.createBooking(request), "Đặt sân thành công"));
    }
}