package com.bsp.repository;

import com.bsp.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
    // Sẵn sàng cho Phase Invoice: Lấy sản phẩm và lock row (Pessimistic) nếu cần nghiệp vụ hardcore hơn
    // @Lock(LockModeType.PESSIMISTIC_WRITE)
    // @Query("SELECT p FROM Product p WHERE p.id = :id")
    // Optional<Product> findByIdWithPessimisticLock(@Param("id") Long id);
}