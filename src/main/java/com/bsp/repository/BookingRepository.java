package com.bsp.repository;

import com.bsp.entity.Booking;
import com.bsp.entity.enums.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    // Tại sao làm vậy: Thuật toán check Overlap Time chuẩn nhất:
    // (StartA < EndB) AND (EndA > StartB) -> Trùng nhau.
    // Trừ các đơn đã CANCELLED ra.
    @Query("SELECT COUNT(b) > 0 FROM Booking b WHERE b.court.id = :courtId " +
            "AND b.status != 'CANCELLED' " +
            "AND b.startTime < :endTime AND b.endTime > :startTime")
    boolean existsOverlappingBooking(
            @Param("courtId") Long courtId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);
}