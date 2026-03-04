package com.bsp.dto.request;
import lombok.Data;
import java.util.List;

@Data
public class InvoiceRequest {
    private String paymentMethod;
    private String proofImageUrl;
    private List<CartItem> items;

    @Data
    public static class CartItem {
        private Long productId;
        private Integer qty;
    }
}