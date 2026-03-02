package com.bsp.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class ProductRequest {
    @NotBlank(message = "Tên sản phẩm không được trống")
    private String name;

    @NotNull(message = "Giá không được trống")
    @Min(value = 0, message = "Giá không được âm")
    private BigDecimal price;

    @NotNull(message = "Số lượng tồn kho không được trống")
    @Min(value = 0, message = "Tồn kho không được âm")
    private Integer stock;

    private String imageUrl;

    private Long categoryId;
}