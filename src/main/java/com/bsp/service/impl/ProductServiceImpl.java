package com.bsp.service.impl;

import com.bsp.dto.request.ProductRequest;
import com.bsp.dto.response.ProductResponse;
import com.bsp.entity.Product;
import com.bsp.repository.ProductRepository;
import com.bsp.service.ProductService;
import lombok.RequiredArgsConstructor;
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
// Auto-generated — ready to smash bugs like smash shuttlecocks
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    // Repository thao tác database
    private final ProductRepository productRepository;

    /**
     * Tạo mới sản phẩm
     * - Mapping từ ProductRequest → Entity
     * - Lưu vào DB
     * - Trả về ProductResponse
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

        // ================= 2. SAVE TO DATABASE =================
        Product savedProduct = productRepository.save(product);

        // ================= 3. RETURN RESPONSE =================
        return mapToResponse(savedProduct);
    }

    /**
     * Cập nhật sản phẩm
     * - Tìm sản phẩm theo id
     * - Update field
     * - Hibernate sẽ check @Version nếu có (Optimistic Lock)
     */
    @Override
    @Transactional
    public ProductResponse updateProduct(Long id, ProductRequest request) {

        // ================= 1. FIND PRODUCT =================
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));
        // TODO: Nên thay bằng CustomException (NotFoundException)

        // ================= 2. UPDATE FIELDS =================
        product.setName(request.getName());
        product.setPrice(request.getPrice());
        product.setStock(request.getStock());
        product.setImageUrl(request.getImageUrl());

        /*
         * Không bắt buộc phải gọi save() nếu entity đang được quản lý (managed)
         * vì Hibernate sẽ auto dirty checking khi transaction commit.
         * Tuy nhiên gọi save() giúp code rõ ràng hơn.
         */
        Product updatedProduct = productRepository.save(product);

        return mapToResponse(updatedProduct);
    }

    /**
     * Lấy toàn bộ sản phẩm
     * readOnly = true giúp tối ưu hiệu năng (Hibernate không cần dirty checking)
     */
    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> getAllProducts() {

        return productRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Mapper thủ công:
     * Trade-off:
     *  + Dễ debug
     *  + Không cần thêm dependency (MapStruct/ModelMapper)
     *  - Khi project lớn sẽ bị lặp code
     */
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