-- Auto-generated — ready to smash bugs like smash shuttlecocks
-- V2__Insert_Sample_Data.sql

-- =====================================================
-- 1. USERS (Tài khoản mẫu)
-- Password mặc định:
-- admin         -> admin123
-- staff_booking -> staff123
-- user01        -> user123
-- =====================================================
INSERT INTO users (username, password, role, shift, active) VALUES
('admin', '$2a$10$wWeTBVgVZ6m3I7lgM0E/tu30bqlQO74AHHTo.SYl7ciYmiSDMg0hG', 'ROLE_ADMIN', 'MORNING', TRUE),
('staff_booking', '$2a$10$lUki7WgAAJHih0VuYY9usOcH/l0GJ5T8JGzHJ/Ye9kDFkXS0BX8ba', 'ROLE_STAFF_BOOKING', 'AFTERNOON', TRUE),
('user01', '$2a$10$OBN5cIDGhQyBa0MzA4CSMOyTAk86Rgk//Ikl3nrgSeOqMqJ57V/nu', 'ROLE_USER', NULL, TRUE);


-- =====================================================
-- 2. COURTS (10 Sân cầu lông)
-- Gồm 8 sân tiêu chuẩn và 2 sân VIP (Thảm xịn, đèn xịn hơn)
-- =====================================================
INSERT INTO courts (name, type, status, price_day, price_night) VALUES
('Sân 1 (Tiêu Chuẩn)', 'STANDARD', 'AVAILABLE', 70000, 90000),
('Sân 2 (Tiêu Chuẩn)', 'STANDARD', 'AVAILABLE', 70000, 90000),
('Sân 3 (Tiêu Chuẩn)', 'STANDARD', 'AVAILABLE', 70000, 90000),
('Sân 4 (Tiêu Chuẩn)', 'STANDARD', 'AVAILABLE', 70000, 90000),
('Sân 5 (Tiêu Chuẩn)', 'STANDARD', 'AVAILABLE', 70000, 90000),
('Sân 6 (Tiêu Chuẩn)', 'STANDARD', 'AVAILABLE', 70000, 90000),
('Sân 7 (Tiêu Chuẩn)', 'STANDARD', 'AVAILABLE', 70000, 90000),
('Sân 8 (Tiêu Chuẩn)', 'STANDARD', 'MAINTENANCE', 70000, 90000), -- Giả lập 1 sân đang bảo trì để test UI
('Sân 9 (VIP)', 'VIP', 'AVAILABLE', 100000, 130000),
('Sân 10 (VIP)', 'VIP', 'AVAILABLE', 100000, 130000);


-- =====================================================
-- 3. PRODUCTS (Hàng hóa & Dịch vụ)
-- Bao gồm nước giải khát, dụng cụ và dịch vụ cho thuê
-- =====================================================
INSERT INTO products (name, price, stock, image_url, version) VALUES
-- Nhóm Nước uống
('Nước suối Aquafina 500ml', 10000, 100, 'https://cdn-icons-png.flaticon.com/512/3105/3105804.png', 0),
('Nước tăng lực Redbull', 15000, 80, 'https://cdn-icons-png.flaticon.com/512/3050/3050212.png', 0),
('Nước bù khoáng Revive', 15000, 60, 'https://cdn-icons-png.flaticon.com/512/3105/3105804.png', 0),
('Trà đá (Ly)', 5000, 999, 'https://cdn-icons-png.flaticon.com/512/3050/3050212.png', 0),

-- Nhóm Phụ kiện cầu lông
('Ống cầu Yonex AS-40', 550000, 20, 'https://cdn-icons-png.flaticon.com/512/4609/4609351.png', 0),
('Ống cầu Vina Star', 230000, 50, 'https://cdn-icons-png.flaticon.com/512/4609/4609351.png', 0),
('Quấn cán vợt VS', 15000, 200, 'https://cdn-icons-png.flaticon.com/512/2853/2853364.png', 0),

-- Nhóm Dịch vụ (Không có tồn kho vật lý, set số lớn)
('Thuê vợt cầu lông (Ca)', 30000, 10, 'https://cdn-icons-png.flaticon.com/512/103/103444.png', 0);