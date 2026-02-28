package com.bsp.dto.request;
import com.bsp.entity.enums.CourtStatus;
import com.bsp.entity.enums.CourtType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class CourtRequest {
    @NotBlank(message = "Tên sân không được trống")
    private String name;
    @NotNull(message = "Loại sân không được trống")
    private CourtType type;
    @NotNull(message = "Trạng thái không được trống")
    private CourtStatus status;
    @NotNull(message = "Giá ngày không được trống")
    private BigDecimal priceDay;
    @NotNull(message = "Giá đêm không được trống")
    private BigDecimal priceNight;
}