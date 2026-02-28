package com.bsp.service.impl;

import com.bsp.audit.AuditAction;
import com.bsp.entity.Invoice;
import com.bsp.entity.InvoiceItem;
import com.bsp.entity.Product;
import com.bsp.entity.User;
import com.bsp.entity.enums.InvoiceStatus;
import com.bsp.export.PdfExportService;
import com.bsp.repository.InvoiceRepository;
import com.bsp.repository.ProductRepository;
import com.bsp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

// Auto-generated — ready to smash bugs like smash shuttlecocks
@Service
@RequiredArgsConstructor
public class InvoiceServiceImpl {

    private final InvoiceRepository invoiceRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final PdfExportService pdfExportService;

    /**
     * THREAD-SAFE INVOICE NUMBER GENERATION
     * Tại sao làm vậy: Phải synchronize (hoặc dùng khóa phân tán Redis) để đảm bảo 2 request cùng lúc
     * không lấy ra chung một số thứ tự (XXXX).
     */
    public synchronized String generateInvoiceNumber() {
        String yearMonth = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
        String prefix = "BSP-" + yearMonth + "-";

        String maxInvoiceNo = invoiceRepository.findMaxInvoiceNumberByPrefix(prefix);
        int sequence = 1;

        if (maxInvoiceNo != null) {
            String seqStr = maxInvoiceNo.replace(prefix, "");
            sequence = Integer.parseInt(seqStr) + 1;
        }
        return prefix + String.format("%04d", sequence);
    }

    /**
     * FINALIZE INVOICE: Chốt đơn và trừ kho.
     * Cực kỳ quan trọng: @Transactional bắt buộc phải có để giữ Lock và Rollback nếu có lỗi.
     */
    @Transactional
    @AuditAction(actionType = "FINALIZE", objectType = "INVOICE")
    public void finalizeInvoice(Long invoiceId) {
        // 1. PESSIMISTIC LOCK Hóa Đơn
        Invoice invoice = invoiceRepository.findByIdWithPessimisticLock(invoiceId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn"));

        if (invoice.getStatus() != InvoiceStatus.DRAFT) {
            throw new RuntimeException("Chỉ có thể Finalize hóa đơn ở trạng thái DRAFT");
        }

        // 2. Trừ tồn kho an toàn
        for (InvoiceItem item : invoice.getItems()) {
            if (item.getProduct() != null) {
                // PESSIMISTIC LOCK Sản Phẩm (tránh race-condition trừ âm kho)
                Product product = productRepository.findByIdWithPessimisticLock(item.getProduct().getId())
                        .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại"));

                if (product.getStock() < item.getQty()) {
                    throw new RuntimeException("Sản phẩm " + product.getName() + " không đủ tồn kho!");
                }

                product.setStock(product.getStock() - item.getQty());
                productRepository.save(product);
            }
        }

        // 3. Cập nhật trạng thái
        invoice.setStatus(InvoiceStatus.FINALIZED);
        invoiceRepository.save(invoice);
    }

    @Transactional
    @AuditAction(actionType = "EXPORT", objectType = "INVOICE") // <-- AOP SẼ BẮT VÀO ĐÂY
    public void exportInvoice(Long invoiceId) {
        Invoice invoice = invoiceRepository.findByIdWithPessimisticLock(invoiceId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn"));

        if (invoice.getStatus() != InvoiceStatus.FINALIZED) {
            throw new RuntimeException("Chỉ được Export khi hóa đơn đã FINALIZED");
        }

        invoice.setStatus(InvoiceStatus.EXPORTED);
        invoice.setExportedAt(LocalDateTime.now());

        try {
            // Sinh PDF và lấy đường dẫn lưu vào DB
            String pdfPath = pdfExportService.generateInvoicePdf(invoice);
            invoice.setExportFilePath(pdfPath);
        } catch (IOException e) {
            throw new RuntimeException("Lỗi xuất PDF: " + e.getMessage());
        }

        invoiceRepository.save(invoice);
    }

    public File getExportedPdfFile(Long invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn"));

        if (invoice.getExportFilePath() == null) {
            throw new RuntimeException("Hóa đơn này chưa được export ra PDF");
        }

        File file = new File(invoice.getExportFilePath());
        if (!file.exists()) {
            throw new RuntimeException("File vật lý không tồn tại trên server");
        }
        return file;
    }

    // Trả về danh sách DTO an toàn
    public java.util.List<com.bsp.dto.response.InvoiceResponse> getAllInvoices() {
        return invoiceRepository.findAll().stream()
                .map(i -> com.bsp.dto.response.InvoiceResponse.builder()
                        .id(i.getId())
                        .invoiceNumber(i.getInvoiceNumber())
                        .status(i.getStatus().name())
                        .totalAmount(i.getTotalAmount())
                        .createdAt(i.getCreatedAt())
                        .build())
                .collect(java.util.stream.Collectors.toList());
    }

    // Trả về DTO sau khi tạo
    @Transactional
    public com.bsp.dto.response.InvoiceResponse createRetailInvoice(com.bsp.dto.request.InvoiceRequest request, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user"));

        Invoice invoice = Invoice.builder()
                .invoiceNumber(generateInvoiceNumber())
                .type(com.bsp.entity.enums.InvoiceType.RETAIL)
                .status(InvoiceStatus.DRAFT)
                .issuedBy(user)
                .paymentMethod(com.bsp.entity.enums.PaymentMethod.valueOf(request.getPaymentMethod()))
                .discount(java.math.BigDecimal.ZERO)
                .tax(java.math.BigDecimal.ZERO)
                .build();

        java.math.BigDecimal total = java.math.BigDecimal.ZERO;

        for (var itemReq : request.getItems()) {
            Product p = productRepository.findById(itemReq.getProductId())
                    .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại"));

            java.math.BigDecimal itemTotal = p.getPrice().multiply(java.math.BigDecimal.valueOf(itemReq.getQty()));
            total = total.add(itemTotal);

            InvoiceItem item = InvoiceItem.builder()
                    .invoice(invoice)
                    .product(p)
                    .qty(itemReq.getQty())
                    .unitPrice(p.getPrice())
                    .total(itemTotal)
                    .build();
            invoice.getItems().add(item);
        }

        invoice.setSubtotal(total);
        invoice.setTotalAmount(total);

        Invoice saved = invoiceRepository.save(invoice);

        return com.bsp.dto.response.InvoiceResponse.builder()
                .id(saved.getId())
                .invoiceNumber(saved.getInvoiceNumber())
                .status(saved.getStatus().name())
                .totalAmount(saved.getTotalAmount())
                // Fallback thời gian phòng khi Hibernate chưa nạp kịp
                .createdAt(saved.getCreatedAt() != null ? saved.getCreatedAt() : java.time.LocalDateTime.now())
                .build();
    }
}