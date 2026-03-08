-- Thêm cột mật khẩu và loại khách hàng
ALTER TABLE customers ADD COLUMN password VARCHAR(255);
ALTER TABLE customers ADD COLUMN customer_type VARCHAR(30) DEFAULT 'REGULAR';

-- Xóa bỏ cột is_member cũ
ALTER TABLE customers DROP COLUMN is_member;