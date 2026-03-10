package com.bsp.controller;

import com.bsp.dto.request.ProductRequest;
import com.bsp.dto.response.ApiResponse;
import com.bsp.dto.response.ProductResponse;
import com.bsp.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Tag(name = "Product", description = "Quản lý Hàng hóa / Dịch vụ")
@SecurityRequirement(name = "bearerAuth") // Swagger bắt buộc truyền Token
public class ProductController {

    private final ProductService productService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'BS')")
    @Operation(summary = "Tạo mới sản phẩm")
    public ResponseEntity<ApiResponse<ProductResponse>> createProduct(@Valid @RequestBody ProductRequest request) {
        ProductResponse response = productService.createProduct(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Tạo sản phẩm thành công"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'BS')")
    @Operation(summary = "Cập nhật sản phẩm (Có kiểm tra Concurrency)")
    public ResponseEntity<ApiResponse<ProductResponse>> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductRequest request) {
        ProductResponse response = productService.updateProduct(id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Cập nhật sản phẩm thành công"));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'BS', 'CUSTOMER')")
    @Operation(summary = "Lấy danh sách sản phẩm")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getAllProducts() {
        List<ProductResponse> products = productService.getAllProducts();
        return ResponseEntity.ok(ApiResponse.success(products, "Lấy danh sách thành công"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'BS')")
    @Operation(summary = "Xóa sản phẩm")
    public ResponseEntity<ApiResponse<String>> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Xóa sản phẩm thành công"));
    }
}