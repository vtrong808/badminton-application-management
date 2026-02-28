package com.bsp.dto.response;

import com.bsp.entity.enums.CourtStatus;
import com.bsp.entity.enums.CourtType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class CourtResponse {
    private Long id;
    private String name;
    private CourtType type;
    private CourtStatus status;
    private BigDecimal priceDay;
    private BigDecimal priceNight;
}