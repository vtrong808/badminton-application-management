package com.bsp.controller;

import com.bsp.dto.response.ApiResponse;
import com.bsp.entity.Invoice;
import com.bsp.repository.InvoiceRepository;
import com.bsp.service.impl.InvoiceServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.io.File;

@RestController
@RequestMapping("/api/v1/invoices")
@RequiredArgsConstructor
@Tag(name = "Invoice", description = "Quản lý Hóa Đơn & Thanh Toán")
@SecurityRequirement(name = "bearerAuth")
public class InvoiceController {

    private final InvoiceServiceImpl invoiceService;

    @PostMapping("/{id}/finalize")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF_SALES')")
    @Operation(summary = "Chốt hóa đơn (Finalize) & Trừ tồn kho an toàn")
    public ResponseEntity<ApiResponse<String>> finalizeInvoice(@PathVariable Long id) {
        invoiceService.finalizeInvoice(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Hóa đơn đã được chốt và trừ tồn kho thành công"));
    }

    @PostMapping("/{id}/export")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF_SALES')")
    @Operation(summary = "Đánh dấu xuất hóa đơn (Lock chống double-export)")
    public ResponseEntity<ApiResponse<String>> exportInvoice(@PathVariable Long id) {
        invoiceService.exportInvoice(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Hóa đơn đã chuyển trạng thái Exported"));
    }

    @GetMapping("/{id}/download")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF_SALES', 'USER')")
    @Operation(summary = "Tải file PDF hóa đơn đã export")
    public ResponseEntity<Resource> downloadInvoicePdf(@PathVariable Long id) {

        File file = invoiceService.getExportedPdfFile(id); // Gọi qua Service

        Resource resource = new FileSystemResource(file);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getName() + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(resource);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF_SALES')")
    @Operation(summary = "Lấy danh sách hóa đơn")
    public ResponseEntity<ApiResponse<java.util.List<com.bsp.dto.response.InvoiceResponse>>> getAllInvoices() {
        return ResponseEntity.ok(ApiResponse.success(invoiceService.getAllInvoices(), "Thành công"));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF_SALES')")
    @Operation(summary = "Tạo hóa đơn bán lẻ (DRAFT)")
    public ResponseEntity<ApiResponse<com.bsp.dto.response.InvoiceResponse>> createRetailInvoice(@RequestBody com.bsp.dto.request.InvoiceRequest request) {
        String username = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(ApiResponse.success(invoiceService.createRetailInvoice(request, username), "Tạo DRAFT thành công"));
    }
}