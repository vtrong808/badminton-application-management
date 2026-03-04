package com.bsp.dto.response;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

// Auto-generated — ready to smash bugs like smash shuttlecocks
@Data
@Builder
public class InvoiceResponse {
    private Long id;
    private String invoiceNumber;
    private String status;
    private BigDecimal totalAmount;
    private LocalDateTime createdAt;
    private String paymentMethod;
    private String proofImageUrl;
}