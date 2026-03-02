package com.bsp.controller;

import com.bsp.dto.response.ApiResponse;
import com.bsp.entity.ProductCategory;
import com.bsp.repository.ProductCategoryRepository;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
@Tag(name = "Category", description = "Danh mục sản phẩm")
@SecurityRequirement(name = "bearerAuth")
public class ProductCategoryController {

    private final ProductCategoryRepository categoryRepository;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ProductCategory>>> getAllCategories() {
        return ResponseEntity.ok(ApiResponse.success(categoryRepository.findAll(), "Thành công"));
    }
}