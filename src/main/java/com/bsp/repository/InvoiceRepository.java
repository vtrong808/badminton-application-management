package com.bsp.repository;

import com.bsp.entity.Invoice;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    // Tại sao làm vậy: Khóa cứng dòng dữ liệu này ở mức Database (SELECT ... FOR UPDATE).
    // Thread khác muốn lấy Invoice này để Finalize hoặc Export sẽ bị block chờ tới khi Transaction hiện tại commit xong.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT i FROM Invoice i WHERE i.id = :id")
    Optional<Invoice> findByIdWithPessimisticLock(@Param("id") Long id);

    // Lấy mã hóa đơn lớn nhất trong tháng để sinh mã mới
    @Query("SELECT MAX(i.invoiceNumber) FROM Invoice i WHERE i.invoiceNumber LIKE CONCAT(:prefix, '%')")
    String findMaxInvoiceNumberByPrefix(@Param("prefix") String prefix);

    // Tại sao làm vậy: Dùng Native Query để tận dụng hàm DATE() của MySQL, nhóm doanh thu theo ngày rất tốc độ.
    // Chỉ tính tiền các hóa đơn đã chốt (FINALIZED hoặc EXPORTED).
    @Query(value = "SELECT DATE(exported_at) as date, SUM(total_amount) as revenue " +
            "FROM invoices " +
            "WHERE status IN ('FINALIZED', 'EXPORTED') " +
            "AND exported_at IS NOT NULL " +
            "GROUP BY DATE(exported_at) " +
            "ORDER BY date DESC " +
            "LIMIT 30", nativeQuery = true)
    List<Object[]> getRevenueLast30Days();
}