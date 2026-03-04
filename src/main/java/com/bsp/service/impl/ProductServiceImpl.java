package com.bsp.service.impl;

import com.bsp.dto.request.ProductRequest;
import com.bsp.dto.response.ProductResponse;
import com.bsp.entity.Product;
import com.bsp.repository.ProductRepository;
import com.bsp.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired; // ✅ thêm
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service xử lý nghiệp vụ liên quan đến Product:
 * - Tạo sản phẩm
 * - Cập nhật sản phẩm
 * - Lấy danh sách sản phẩm
 */
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    // Repository thao tác database
    private final ProductRepository productRepository;

    @Autowired
    private com.bsp.repository.ProductCategoryRepository categoryRepository;

    // Hàm phụ trợ: Chuyển từ Entity sang Response (Mấu chốt nằm ở đây)
    private ProductResponse mapToResponse(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .price(product.getPrice())
                .stock(product.getStock())
                .imageUrl(product.getImageUrl())
                // Gán categoryId để nhả về cho Vue 3
                .categoryId(product.getCategory() != null ? product.getCategory().getId() : null)
                .build();
    }

    @Override
    @Transactional
    public ProductResponse createProduct(com.bsp.dto.request.ProductRequest request) {
        Product product = Product.builder()
                .name(request.getName())
                .price(request.getPrice())
                .stock(request.getStock())
                .imageUrl(request.getImageUrl())
                .build();

        // Map Danh mục vào Product
        if (request.getCategoryId() != null) {
            com.bsp.entity.ProductCategory category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Danh mục không tồn tại"));
            product.setCategory(category);
        }

        Product savedProduct = productRepository.save(product);
        return mapToResponse(savedProduct); // Gọi hàm map ở trên
    }

    @Override
    @Transactional
    public ProductResponse updateProduct(Long id, com.bsp.dto.request.ProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại"));

        product.setName(request.getName());
        product.setPrice(request.getPrice());
        product.setStock(request.getStock());
        product.setImageUrl(request.getImageUrl());

        // Map cập nhật Danh mục
        if (request.getCategoryId() != null) {
            com.bsp.entity.ProductCategory category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Danh mục không tồn tại"));
            product.setCategory(category);
        } else {
            product.setCategory(null);
        }

        Product updatedProduct = productRepository.save(product);
        return mapToResponse(updatedProduct); // Gọi hàm map ở trên
    }

    @Override
    public java.util.List<ProductResponse> getAllProducts() {
        return productRepository.findAll().stream()
                .map(this::mapToResponse) // Sử dụng hàm map chung cho toàn bộ List
                .collect(java.util.stream.Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm để xóa"));
        productRepository.delete(product);
    }
}