package com.bsp.repository;

import com.bsp.entity.ShiftSchedule;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShiftScheduleRepository extends JpaRepository<ShiftSchedule, Long> {
}
