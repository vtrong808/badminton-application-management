package com.bsp.entity;

import com.bsp.entity.enums.CourtStatus;
import com.bsp.entity.enums.CourtType;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

// Auto-generated — ready to smash bugs like smash shuttlecocks
@Entity
@Table(name = "courts")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Court {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CourtType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CourtStatus status;

    @Column(name = "price_day", nullable = false)
    private BigDecimal priceDay;

    @Column(name = "price_night", nullable = false)
    private BigDecimal priceNight;
}