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

    /**
     * Tạo mới sản phẩm
     */
    @Override
    @Transactional
    public ProductResponse createProduct(ProductRequest request) {

        // ================= 1. MAP REQUEST → ENTITY =================
        Product product = Product.builder()
                .name(request.getName())
                .price(request.getPrice())
                .stock(request.getStock())
                .imageUrl(request.getImageUrl())
                .build();

        if (request.getCategoryId() != null) {
            com.bsp.entity.ProductCategory category =
                    categoryRepository.findById(request.getCategoryId())
                            .orElseThrow(() -> new RuntimeException("Danh mục không tồn tại"));
            product.setCategory(category);
        }

        // ================= 2. SAVE TO DATABASE =================
        Product savedProduct = productRepository.save(product);

        // ================= 3. RETURN RESPONSE =================
        return mapToResponse(savedProduct);
    }

    /**
     * Cập nhật sản phẩm
     */
    @Override
    @Transactional
    public ProductResponse updateProduct(Long id, ProductRequest request) {

        // ================= 1. FIND PRODUCT =================
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));

        // ================= 2. UPDATE FIELDS =================
        product.setName(request.getName());
        product.setPrice(request.getPrice());
        product.setStock(request.getStock());
        product.setImageUrl(request.getImageUrl());

        if (request.getCategoryId() != null) {
            com.bsp.entity.ProductCategory category =
                    categoryRepository.findById(request.getCategoryId())
                            .orElseThrow(() -> new RuntimeException("Danh mục không tồn tại"));
            product.setCategory(category);
        }

        Product updatedProduct = productRepository.save(product);

        return mapToResponse(updatedProduct);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> getAllProducts() {

        return productRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private ProductResponse mapToResponse(Product product) {

        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .price(product.getPrice())
                .stock(product.getStock())
                .imageUrl(product.getImageUrl())
                .build();
    }

    @Override
    @Transactional
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm để xóa"));
        productRepository.delete(product);
    }
}