package com.bsp.service.impl;

import com.bsp.entity.Invoice;
import com.bsp.entity.InvoiceItem;
import com.bsp.entity.Product;
import com.bsp.entity.User;
import com.bsp.entity.enums.InvoiceStatus;
import com.bsp.repository.InvoiceRepository;
import com.bsp.repository.ProductRepository;
import com.bsp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

// Auto-generated — ready to smash bugs like smash shuttlecocks
@Service
@RequiredArgsConstructor
public class InvoiceServiceImpl {

    private final InvoiceRepository invoiceRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

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

        // TODO: Lưu Audit Log (Phase 4)
    }

    /**
     * EXPORT INVOICE: Xuất PDF (Chuẩn bị cho Phase 4).
     */
    @Transactional
    public void exportInvoice(Long invoiceId) {
        // PESSIMISTIC LOCK để tránh 2 người cùng bấm Export 1 lúc tạo ra 2 file rác
        Invoice invoice = invoiceRepository.findByIdWithPessimisticLock(invoiceId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn"));

        if (invoice.getStatus() != InvoiceStatus.FINALIZED) {
            throw new RuntimeException("Chỉ được Export khi hóa đơn đã FINALIZED");
        }

        // Logic xuất PDF và Upload MinIO sẽ nằm ở đây (Phase 4)

        invoice.setStatus(InvoiceStatus.EXPORTED);
        invoice.setExportedAt(LocalDateTime.now());
        invoice.setExportFilePath("/storage/exports/" + invoice.getInvoiceNumber() + ".pdf");

        invoiceRepository.save(invoice);
    }
}