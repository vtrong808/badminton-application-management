package com.bsp.service;

import com.bsp.dto.request.ProductRequest;
import com.bsp.dto.response.ProductResponse;
import java.util.List;

public interface ProductService {
    ProductResponse createProduct(ProductRequest request);
    ProductResponse updateProduct(Long id, ProductRequest request);
    List<ProductResponse> getAllProducts();
}