-- 1. Xử lý lại Role trong bảng Users (Chuyển STAFF cũ thành BS)
UPDATE users SET role = 'ROLE_BS' WHERE role IN ('ROLE_STAFF_BOOKING', 'ROLE_STAFF_SALES');
UPDATE users SET role = 'ROLE_CUSTOMER' WHERE role = 'ROLE_USER';

-- 2. Bổ sung trường Lương cho Nhân viên
ALTER TABLE users
ADD COLUMN base_salary DECIMAL(10, 2) DEFAULT 3000000,
ADD COLUMN bonus_salary DECIMAL(10, 2) DEFAULT 0,
ADD COLUMN penalty_salary DECIMAL(10, 2) DEFAULT 0;

-- 3. Tạo bảng Categories (Danh mục sản phẩm)
CREATE TABLE product_categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(255)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO product_categories (name) VALUES ('Drinks'), ('Shuttlecocks'), ('Accessories'), ('Services');

-- 4. Bổ sung category_id vào bảng Products
ALTER TABLE products
ADD COLUMN category_id BIGINT,
ADD CONSTRAINT fk_product_category FOREIGN KEY (category_id) REFERENCES product_categories(id);

-- 5. Tạo bảng Khách Hàng (Tách biệt khỏi Users)
CREATE TABLE customers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    phone_number VARCHAR(20) NOT NULL UNIQUE,
    full_name VARCHAR(100) NOT NULL,
    is_member BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 6. Bảng Quản lý tài sản Vợt Thuê (Như em đề xuất)
CREATE TABLE rackets (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    racket_code VARCHAR(50) NOT NULL UNIQUE, -- Mã vợt (VD: V01, V02)
    brand VARCHAR(50),
    status VARCHAR(30) DEFAULT 'AVAILABLE', -- AVAILABLE, IN_USE, MAINTENANCE
    rental_price DECIMAL(10, 2) DEFAULT 20000,
    version INT NOT NULL DEFAULT 0 -- Optimistic Locking
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO rackets (racket_code, brand) VALUES ('V01', 'Yonex'), ('V02', 'Lining'), ('V03', 'Victor');

-- 7. Bảng Khách Vãng Lai (Walk-in Sessions)
CREATE TABLE walk_in_sessions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    customer_name VARCHAR(100) NOT NULL,
    phone_number VARCHAR(20),
    age INT,
    play_date DATE NOT NULL,
    start_time TIME NOT NULL,
    price DECIMAL(10, 2) DEFAULT 50000,
    status VARCHAR(30) DEFAULT 'PLAYING', -- PLAYING, FINISHED
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 8. Bảng Quản lý ca làm việc (Shift Schedules)
CREATE TABLE shift_schedules (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    shift_date DATE NOT NULL,
    shift_type VARCHAR(30) NOT NULL, -- MORNING, AFTERNOON, EVENING
    is_attended BOOLEAN DEFAULT FALSE,
    check_in_time DATETIME,
    CONSTRAINT fk_shift_user FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;