package com.bsp.repository;

import com.bsp.entity.ShiftSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface ShiftScheduleRepository extends JpaRepository<ShiftSchedule, Long> {

    Optional<ShiftSchedule> findByUserIdAndShiftDate(Long userId, LocalDate shiftDate);
}