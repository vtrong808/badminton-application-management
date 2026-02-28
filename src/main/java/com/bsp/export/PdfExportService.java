package com.bsp.export;

import com.bsp.entity.Invoice;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

// Auto-generated — ready to smash bugs like smash shuttlecocks
@Service
public class PdfExportService {

    @Value("${app.storage.local-path}")
    private String exportDir;

    // Tại sao làm vậy: Tách biệt logic gen PDF ra Service riêng.
    // Dùng OpenPDF tạo file vật lý lưu vào thư mục local (hoặc S3 sau này).
    public String generateInvoicePdf(Invoice invoice) throws IOException {
        File directory = new File(exportDir);
        if (!directory.exists()) {
            directory.mkdirs(); // Tự động tạo thư mục nếu chưa có
        }

        String fileName = invoice.getInvoiceNumber() + ".pdf";
        String filePath = exportDir + fileName;

        Document document = new Document(PageSize.A4);
        try {
            PdfWriter.getInstance(document, new FileOutputStream(filePath));
            document.open();

            // Tiêu đề
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20);
            Paragraph title = new Paragraph("BADMINTON INVOICE (BSP)", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            document.add(Chunk.NEWLINE);

            // Thông tin chung
            document.add(new Paragraph("Invoice No: " + invoice.getInvoiceNumber()));
            document.add(new Paragraph("Date: " + invoice.getExportedAt()));
            document.add(new Paragraph("Status: " + invoice.getStatus().name()));
            document.add(Chunk.NEWLINE);

            // Bảng Chi tiết
            PdfPTable table = new PdfPTable(4);
            table.setWidthPercentage(100);
            table.addCell(new PdfPCell(new Phrase("Description")));
            table.addCell(new PdfPCell(new Phrase("Qty")));
            table.addCell(new PdfPCell(new Phrase("Unit Price")));
            table.addCell(new PdfPCell(new Phrase("Total")));

            // Duyệt qua Invoice Items (nếu có)
            invoice.getItems().forEach(item -> {
                String desc = item.getProduct() != null ? item.getProduct().getName() : item.getServiceDesc();
                table.addCell(desc);
                table.addCell(String.valueOf(item.getQty()));
                table.addCell(item.getUnitPrice().toString());
                table.addCell(item.getTotal().toString());
            });

            document.add(table);
            document.add(Chunk.NEWLINE);

            // Tổng tiền
            Font totalFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);
            Paragraph totalParagraph = new Paragraph("Total Amount: " + invoice.getTotalAmount() + " VND", totalFont);
            totalParagraph.setAlignment(Element.ALIGN_RIGHT);
            document.add(totalParagraph);

        } catch (DocumentException e) {
            throw new RuntimeException("Lỗi khi tạo file PDF", e);
        } finally {
            document.close();
        }

        return filePath;
    }
}