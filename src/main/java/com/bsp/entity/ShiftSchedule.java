package com.bsp.entity;

import com.bsp.entity.enums.ShiftType;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "shift_schedules")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ShiftSchedule {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "shift_date", nullable = false)
    private LocalDate shiftDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "shift_type", nullable = false)
    private ShiftType shiftType;

    @Column(name = "is_attended")
    private Boolean isAttended;

    @Column(name = "check_in_time")
    private LocalDateTime checkInTime;
}