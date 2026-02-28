package com.bsp.exception;

import com.bsp.dto.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

// Auto-generated — ready to smash bugs like smash shuttlecocks
// Tại sao làm vậy: Tập trung xử lý lỗi tại một nơi. Giúp Frontend luôn nhận được HTTP Status Code và JSON format đồng nhất {status: "error", message: "..."}.
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 1. Xử lý lỗi Validation (từ các DTO gắn @Valid)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return ResponseEntity.badRequest().body(
                ApiResponse.<Map<String, String>>builder()
                        .status("error")
                        .message("Dữ liệu đầu vào không hợp lệ")
                        .data(errors)
                        .build()
        );
    }

    // 2. Xử lý lỗi Concurrency (Optimistic Locking Phase 2)
    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ResponseEntity<ApiResponse<Object>> handleOptimisticLockingException(ObjectOptimisticLockingFailureException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(
                ApiResponse.error("Dữ liệu đã bị thay đổi bởi một người dùng khác. Vui lòng tải lại trang và thử lại!")
        );
    }

    // 3. Xử lý lỗi Phân quyền (Spring Security RBAC)
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ResponseEntity<ApiResponse<Object>> handleAccessDeniedException(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                ApiResponse.error("Bạn không có quyền thực hiện hành động này!")
        );
    }

    // 4. Xử lý lỗi Logic Nghiệp vụ chung (từ throw new RuntimeException của ta)
    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST) // Hoặc 400 tùy logic
    public ResponseEntity<ApiResponse<Object>> handleRuntimeException(RuntimeException ex) {
        return ResponseEntity.badRequest().body(
                ApiResponse.error(ex.getMessage())
        );
    }

    // 5. Fallback cho tất cả các lỗi hệ thống chưa biết (NullPointer, SQL Exception...)
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<ApiResponse<Object>> handleGlobalException(Exception ex) {
        // Trong thực tế sẽ log.error(ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ApiResponse.error("Đã xảy ra lỗi hệ thống nghiêm trọng. Vui lòng liên hệ Admin.")
        );
    }
}