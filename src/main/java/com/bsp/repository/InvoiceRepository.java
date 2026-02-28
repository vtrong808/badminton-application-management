package com.bsp.repository;

import com.bsp.entity.Invoice;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    // Tại sao làm vậy: Khóa cứng dòng dữ liệu này ở mức Database (SELECT ... FOR UPDATE).
    // Thread khác muốn lấy Invoice này để Finalize hoặc Export sẽ bị block chờ tới khi Transaction hiện tại commit xong.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT i FROM Invoice i WHERE i.id = :id")
    Optional<Invoice> findByIdWithPessimisticLock(@Param("id") Long id);

    // Lấy mã hóa đơn lớn nhất trong tháng để sinh mã mới
    @Query("SELECT MAX(i.invoiceNumber) FROM Invoice i WHERE i.invoiceNumber LIKE :prefix%")
    String findMaxInvoiceNumberByPrefix(@Param("prefix") String prefix);
}