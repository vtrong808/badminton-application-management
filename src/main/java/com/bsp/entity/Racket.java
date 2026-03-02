package com.bsp.entity;

import com.bsp.entity.enums.RacketStatus;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "rackets")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Racket {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "racket_code", nullable = false, unique = true, length = 50)
    private String racketCode;

    @Column(length = 50)
    private String brand;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RacketStatus status;

    @Column(name = "rental_price")
    private BigDecimal rentalPrice;

    @Version
    private Integer version;
}