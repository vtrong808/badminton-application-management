package com.bsp.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

// Auto-generated — ready to smash bugs like smash shuttlecocks
@Entity
@Table(name = "products")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price; // Decimal để tính tiền không bị sai số

    @Column(nullable = false)
    private Integer stock;

    private String imageUrl;

    // Tại sao làm vậy: Bắt buộc cho Optimistic Locking. Hibernate tự tăng khi có update.
    // Nếu 2 thread cùng update bản ghi có version = 1, thread thứ 2 sẽ văng ObjectOptimisticLockingFailureException.
    @Version
    private Integer version;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private ProductCategory category;
}