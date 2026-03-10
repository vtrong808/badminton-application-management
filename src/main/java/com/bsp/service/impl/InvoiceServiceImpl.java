package com.bsp.service.impl;

import com.bsp.audit.AuditAction;
import com.bsp.entity.Invoice;
import com.bsp.entity.InvoiceItem;
import com.bsp.entity.Product;
import com.bsp.entity.User;
import com.bsp.entity.enums.InvoiceStatus;
import com.bsp.entity.enums.InvoiceType;
import com.bsp.export.PdfExportService;
import com.bsp.repository.InvoiceRepository;
import com.bsp.repository.ProductRepository;
import com.bsp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class InvoiceServiceImpl {

    private final InvoiceRepository invoiceRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final PdfExportService pdfExportService;

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

    @Transactional
    @AuditAction(actionType = "FINALIZE", objectType = "INVOICE")
    public void finalizeInvoice(Long invoiceId) {
        Invoice invoice = invoiceRepository.findByIdWithPessimisticLock(invoiceId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn"));

        // FIX 1: Cho phép duyệt cả đơn DRAFT (Tiền mặt) và PENDING_CONFIRMATION (Chuyển khoản)
        if (invoice.getStatus() != InvoiceStatus.DRAFT && invoice.getStatus() != InvoiceStatus.PENDING_CONFIRMATION) {
            throw new RuntimeException("Chỉ có thể Finalize hóa đơn ở trạng thái DRAFT hoặc PENDING_CONFIRMATION");
        }

        // Trừ tồn kho an toàn
        for (InvoiceItem item : invoice.getItems()) {
            if (item.getProduct() != null) {
                Product product = productRepository.findByIdWithPessimisticLock(item.getProduct().getId())
                        .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại"));

                if (product.getStock() < item.getQty()) {
                    throw new RuntimeException("Sản phẩm " + product.getName() + " không đủ tồn kho!");
                }

                product.setStock(product.getStock() - item.getQty());
                productRepository.save(product);
            }
        }

        invoice.setStatus(InvoiceStatus.FINALIZED);
        invoiceRepository.save(invoice);
    }

    @Transactional
    @AuditAction(actionType = "EXPORT", objectType = "INVOICE")
    public void exportInvoice(Long invoiceId) {
        Invoice invoice = invoiceRepository.findByIdWithPessimisticLock(invoiceId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn"));

        if (invoice.getStatus() != InvoiceStatus.FINALIZED) {
            throw new RuntimeException("Chỉ được Export khi hóa đơn đã FINALIZED");
        }

        invoice.setStatus(InvoiceStatus.EXPORTED);
        invoice.setExportedAt(LocalDateTime.now());

        try {
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

    // FIX 2: Bổ sung paymentMethod và proofImageUrl khi trả về danh sách
    public java.util.List<com.bsp.dto.response.InvoiceResponse> getAllInvoices() {
        return invoiceRepository.findAll().stream()
                .map(i -> com.bsp.dto.response.InvoiceResponse.builder()
                        .id(i.getId())
                        .invoiceNumber(i.getInvoiceNumber())
                        .status(i.getStatus().name())
                        .paymentMethod(i.getPaymentMethod() != null ? i.getPaymentMethod().name() : "CASH")
                        .proofImageUrl(i.getProofImageUrl())
                        .totalAmount(i.getTotalAmount())
                        .createdAt(i.getCreatedAt())
                        .build())
                .collect(java.util.stream.Collectors.toList());
    }

    @Transactional
    public com.bsp.dto.response.InvoiceResponse createRetailInvoice(com.bsp.dto.request.InvoiceRequest request, String username) {
        User user = userRepository.findByUsername(username).orElse(null);

        Invoice invoice = Invoice.builder()
                .invoiceNumber(generateInvoiceNumber())
                .type(InvoiceType.RETAIL)
                .status(request.getPaymentMethod().equals("TRANSFER") ? InvoiceStatus.PENDING_CONFIRMATION : InvoiceStatus.DRAFT)
                .issuedBy(user)
                .paymentMethod(com.bsp.entity.enums.PaymentMethod.valueOf(request.getPaymentMethod()))
                .proofImageUrl(request.getProofImageUrl())
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
                .paymentMethod(saved.getPaymentMethod().name())
                .proofImageUrl(saved.getProofImageUrl())
                .totalAmount(saved.getTotalAmount())
                .createdAt(saved.getCreatedAt() != null ? saved.getCreatedAt() : java.time.LocalDateTime.now())
                .build();
    }
}