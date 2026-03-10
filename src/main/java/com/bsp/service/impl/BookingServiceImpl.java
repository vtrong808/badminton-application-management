package com.bsp.service.impl;

import com.bsp.dto.request.BookingRequest;
import com.bsp.entity.Booking;
import com.bsp.entity.Court;
import com.bsp.entity.User;
import com.bsp.entity.enums.BookingStatus;
import com.bsp.entity.enums.CourtStatus;
import com.bsp.repository.BookingRepository;
import com.bsp.repository.CourtRepository;
import com.bsp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.messaging.simp.SimpMessagingTemplate; // Thêm thư viện WebSocket

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * BookingServiceImpl
 *
 * Xử lý toàn bộ business logic liên quan đến đặt sân:
 * - Validate thời gian
 * - Kiểm tra sân tồn tại
 * - Kiểm tra sân bảo trì
 * - Kiểm tra trùng lịch
 * - Tính tiền theo khung giờ (Dynamic Pricing)
 * - Lấy user hiện tại từ SecurityContext
 * - Lưu booking vào database
 */
@Service
@RequiredArgsConstructor
public class BookingServiceImpl { // KHÔNG CẦN IMPLEMENTS

    private final BookingRepository bookingRepository;
    private final CourtRepository courtRepository;
    private final UserRepository userRepository;

    // THÊM CÔNG CỤ PHÁT SÓNG THÔNG BÁO WEBSOCKET
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Tạo mới một booking.
     */
    @Transactional
    public Booking createBooking(BookingRequest request) {

        validateBookingTime(request);

        Court court = getCourtOrThrow(request.getCourtId());

        checkCourtAvailability(court);

        checkBookingConflict(court, request);

        BigDecimal totalPrice = calculateTotalPrice(
                request.getStartTime(),
                request.getEndTime(),
                court
        );

        User currentUser = getCurrentUser();

        Booking booking = Booking.builder()
                .court(court)
                .user(currentUser)
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .totalPrice(totalPrice)
                .status(BookingStatus.CONFIRMED)
                .build();

        // Lưu dữ liệu vào DB
        Booking savedBooking = bookingRepository.save(booking);

        try {
            String username = currentUser != null ? currentUser.getUsername() : "Khách hàng";
            messagingTemplate.convertAndSend("/topic/notifications",
                    "🔔 CÓ LỊCH ĐẶT SÂN MỚI: Khách hàng " + username + " vừa đặt " + court.getName());
        } catch (Exception e) {
            System.err.println("Lỗi khi bắn thông báo WebSocket: " + e.getMessage());
        }

        return savedBooking;
    }

    private void validateBookingTime(BookingRequest request) {
        if (!request.getStartTime().isBefore(request.getEndTime())) {
            throw new IllegalArgumentException("Thời gian bắt đầu phải trước thời gian kết thúc");
        }
    }

    private Court getCourtOrThrow(Long courtId) {
        return courtRepository.findById(courtId)
                .orElseThrow(() -> new IllegalArgumentException("Sân không tồn tại"));
    }

    private void checkCourtAvailability(Court court) {
        if (court.getStatus() == CourtStatus.MAINTENANCE) {
            throw new IllegalStateException("Sân đang bảo trì");
        }
    }

    private void checkBookingConflict(Court court, BookingRequest request) {
        boolean isConflict = bookingRepository.existsOverlappingBooking(
                court.getId(),
                request.getStartTime(),
                request.getEndTime()
        );

        if (isConflict) {
            throw new IllegalStateException("Sân đã có người đặt trong khoảng thời gian này");
        }
    }

    private User getCurrentUser() {
        String username = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        return userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("User không hợp lệ"));
    }

    public BigDecimal calculateTotalPrice(LocalDateTime start,
                                          LocalDateTime end,
                                          Court court) {

        LocalTime dayShiftEnd = LocalTime.of(17, 0);
        BigDecimal total = BigDecimal.ZERO;
        LocalDateTime current = start;

        while (current.isBefore(end)) {

            LocalDateTime nextStep = current.plusMinutes(30);

            if (nextStep.isAfter(end)) {
                nextStep = end;
            }

            long minutes = Duration.between(current, nextStep).toMinutes();

            BigDecimal hours = BigDecimal.valueOf(minutes)
                    .divide(BigDecimal.valueOf(60), 4, RoundingMode.HALF_UP);

            if (current.toLocalTime().isBefore(dayShiftEnd)) {
                total = total.add(court.getPriceDay().multiply(hours));
            } else {
                total = total.add(court.getPriceNight().multiply(hours));
            }

            current = nextStep;
        }

        return total.setScale(2, RoundingMode.HALF_UP);
    }
}