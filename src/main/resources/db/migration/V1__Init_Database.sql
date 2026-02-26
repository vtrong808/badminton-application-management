-- Auto-generated — ready to smash bugs like smash shuttlecocks
-- V1__Init_Database.sql

-- 1. Table: users (Quản lý nhân viên và khách hàng)
-- Tại sao làm vậy: username phải UNIQUE, role dùng VARCHAR để map với Enum trong Spring (USER, STAFF_BOOKING, STAFF_SALES, ADMIN).
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(30) NOT NULL,
    shift VARCHAR(30), -- MORNING, AFTERNOON, EVENING (nullable cho khách hàng)
    active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. Table: courts (Quản lý sân cầu lông)
-- Tại sao làm vậy: Tách giá ngày/đêm rõ ràng để tính tiền tự động theo khung giờ (Pricing Rule).
CREATE TABLE courts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    type VARCHAR(50) NOT NULL, -- STANDARD, VIP
    status VARCHAR(30) NOT NULL, -- AVAILABLE, MAINTENANCE
    price_day DECIMAL(10, 2) NOT NULL,   -- Giá 06:00 - 17:00
    price_night DECIMAL(10, 2) NOT NULL, -- Giá 17:00 - 23:00
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. Table: bookings (Lịch đặt sân)
-- Tại sao làm vậy: Lưu trữ status (PENDING, CONFIRMED, CANCELLED, COMPLETED).
CREATE TABLE bookings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    court_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    start_time DATETIME NOT NULL,
    end_time DATETIME NOT NULL,
    total_price DECIMAL(10, 2) NOT NULL,
    status VARCHAR(30) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_booking_court FOREIGN KEY (court_id) REFERENCES courts(id),
    CONSTRAINT fk_booking_user FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. Table: products (Hàng hóa/Dịch vụ như nước, thuê vợt, cầu)
-- Tại sao làm vậy: BẮT BUỘC có cột `version` cho cơ chế Optimistic Locking (@Version) để chống race-condition khi bán hàng.
CREATE TABLE products (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    price DECIMAL(10, 2) NOT NULL,
    stock INT NOT NULL DEFAULT 0,
    image_url VARCHAR(255),
    version INT NOT NULL DEFAULT 0, -- Dùng cho Hibernate @Version
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 5. Table: invoices (Hóa đơn phức tạp)
-- Tại sao làm vậy: Lưu invoice_number duy nhất, index để search nhanh. status (DRAFT, FINALIZED, EXPORTED, CANCELLED).
CREATE TABLE invoices (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    invoice_number VARCHAR(50) NOT NULL UNIQUE, -- Định dạng: BSP-YYYYMM-XXXX
    type VARCHAR(30) NOT NULL, -- BOOKING, RETAIL, COMBINED
    status VARCHAR(30) NOT NULL,
    issued_by BIGINT NOT NULL,
    related_booking_id BIGINT,
    subtotal DECIMAL(10, 2) NOT NULL DEFAULT 0,
    discount DECIMAL(10, 2) NOT NULL DEFAULT 0,
    tax DECIMAL(10, 2) NOT NULL DEFAULT 0,
    total_amount DECIMAL(10, 2) NOT NULL DEFAULT 0,
    payment_method VARCHAR(30), -- CASH, TRANSFER, CARD
    exported_at DATETIME,
    export_file_path VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_invoice_issuer FOREIGN KEY (issued_by) REFERENCES users(id),
    CONSTRAINT fk_invoice_booking FOREIGN KEY (related_booking_id) REFERENCES bookings(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 6. Table: invoice_items (Chi tiết hóa đơn)
-- Tại sao làm vậy: product_id có thể NULL (trong trường hợp charge tiền phụ phí dịch vụ không có trong bảng products).
CREATE TABLE invoice_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    invoice_id BIGINT NOT NULL,
    product_id BIGINT,
    service_desc VARCHAR(255), -- Mô tả dịch vụ nếu product_id NULL
    qty INT NOT NULL,
    unit_price DECIMAL(10, 2) NOT NULL,
    total DECIMAL(10, 2) NOT NULL,
    CONSTRAINT fk_item_invoice FOREIGN KEY (invoice_id) REFERENCES invoices(id) ON DELETE CASCADE,
    CONSTRAINT fk_item_product FOREIGN KEY (product_id) REFERENCES products(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 7. Table: audit_logs (Lưu vết hệ thống)
-- Tại sao làm vậy: details để kiểu JSON/TEXT lưu state before/after. Không khóa ngoại cứng để tránh rác DB khi xóa object.
CREATE TABLE audit_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    actor_id BIGINT, -- Ai thực hiện (có thể NULL nếu là system)
    action_type VARCHAR(50) NOT NULL, -- CREATE, UPDATE, DELETE, EXPORT_INVOICE
    object_type VARCHAR(50) NOT NULL, -- INVOICE, BOOKING, PRODUCT...
    object_id BIGINT NOT NULL,
    timestamp DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    details TEXT, -- Lưu JSON của dữ liệu thay đổi
    INDEX idx_audit_object (object_type, object_id),
    INDEX idx_audit_actor (actor_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 8. Tạo Indexes cho các trường hay truy vấn (Performance Optimization)
CREATE INDEX idx_booking_dates ON bookings(start_time, end_time);
CREATE INDEX idx_invoice_status ON invoices(status);