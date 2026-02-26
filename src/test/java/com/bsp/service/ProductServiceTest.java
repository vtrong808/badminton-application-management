package com.bsp.service;

import com.bsp.dto.request.ProductRequest;
import com.bsp.dto.response.ProductResponse;
import com.bsp.entity.Product;
import com.bsp.repository.ProductRepository;
import com.bsp.service.impl.ProductServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductServiceImpl productService;

    @Test
    void createProduct_ShouldReturnProductResponse() {
        // Arrange
        ProductRequest request = new ProductRequest();
        request.setName("Nước suối Aquafina");
        request.setPrice(new BigDecimal("10000"));
        request.setStock(50);

        Product savedProduct = Product.builder()
                .id(1L)
                .name("Nước suối Aquafina")
                .price(new BigDecimal("10000"))
                .stock(50)
                .version(0)
                .build();

        when(productRepository.save(any(Product.class))).thenReturn(savedProduct);

        // Act
        ProductResponse response = productService.createProduct(request);

        // Assert
        assertNotNull(response);
        assertEquals("Nước suối Aquafina", response.getName());
        assertEquals(1L, response.getId());
    }
}