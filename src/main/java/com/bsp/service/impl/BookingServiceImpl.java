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
// Auto-generated — ready to smash bugs like smash shuttlecocks
@Service
@RequiredArgsConstructor
public class BookingServiceImpl {

    private final BookingRepository bookingRepository;
    private final CourtRepository courtRepository;
    private final UserRepository userRepository;

    /**
     * Tạo mới một booking.
     *
     * Flow xử lý:
     * 1. Validate thời gian
     * 2. Kiểm tra sân tồn tại
     * 3. Kiểm tra trạng thái sân
     * 4. Kiểm tra trùng lịch
     * 5. Tính tổng tiền
     * 6. Lấy user hiện tại
     * 7. Lưu database
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

        return bookingRepository.save(booking);
    }

    // ================= VALIDATION =================

    /**
     * Kiểm tra thời gian hợp lệ.
     * startTime phải nhỏ hơn endTime.
     */
    private void validateBookingTime(BookingRequest request) {
        if (!request.getStartTime().isBefore(request.getEndTime())) {
            throw new IllegalArgumentException("Thời gian bắt đầu phải trước thời gian kết thúc");
        }
    }

    /**
     * Lấy sân theo ID.
     * Nếu không tồn tại -> throw exception.
     */
    private Court getCourtOrThrow(Long courtId) {
        return courtRepository.findById(courtId)
                .orElseThrow(() -> new IllegalArgumentException("Sân không tồn tại"));
    }

    /**
     * Kiểm tra trạng thái sân.
     * Nếu đang bảo trì -> không cho đặt.
     */
    private void checkCourtAvailability(Court court) {
        if (court.getStatus() == CourtStatus.MAINTENANCE) {
            throw new IllegalStateException("Sân đang bảo trì");
        }
    }

    /**
     * Kiểm tra trùng lịch đặt sân.
     * Nếu có booking chồng lấn thời gian -> throw exception.
     */
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

    /**
     * Lấy thông tin user đang đăng nhập từ SecurityContext.
     * Dùng username để truy vấn DB.
     */
    private User getCurrentUser() {
        String username = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        return userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("User không hợp lệ"));
    }

    // ================= PRICING LOGIC =================

    /**
     * Thuật toán tính tổng tiền.
     *
     * Logic:
     * - Chia booking thành từng block 30 phút
     * - Nếu thời điểm < 17:00 -> giá ngày
     * - Nếu >= 17:00 -> giá đêm
     * - Cộng dồn theo từng block
     *
     * Ưu điểm:
     * - Dễ unit test
     * - Linh hoạt thay đổi business rule
     */
    public BigDecimal calculateTotalPrice(LocalDateTime start,
                                          LocalDateTime end,
                                          Court court) {

        LocalTime dayShiftEnd = LocalTime.of(17, 0); // Mốc chuyển ca
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