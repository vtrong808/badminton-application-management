package com.bsp.dto.response;

import lombok.Builder;
import lombok.Data;

// Auto-generated — ready to smash bugs like smash shuttlecocks
// Tại sao làm vậy: Ép toàn bộ API trả về cùng một format chuẩn {status, data, message}, giúp Frontend (Vue 3 + Axios interceptor) dễ dàng parse và xử lý lỗi đồng nhất.
@Data
@Builder
public class ApiResponse<T> {
    private String status;
    private T data;
    private String message;

    public static <T> ApiResponse<T> success(T data, String message) {
        return ApiResponse.<T>builder()
                .status("success")
                .data(data)
                .message(message)
                .build();
    }

    public static <T> ApiResponse<T> error(String message) {
        return ApiResponse.<T>builder()
                .status("error")
                .data(null)
                .message(message)
                .build();
    }
}