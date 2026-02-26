BADMINTON MANAGEMENT SYSTEM

---Giới thiệu dự án---

Badminton Management System là hệ thống quản lý sân cầu lông được xây dựng nhằm mô phỏng quy trình vận hành thực tế của một cơ sở kinh doanh sân cầu lông.

Dự án tập trung vào:

  + Quản lý tài khoản & phân quyền

  + Quản lý đặt sân
  
  + Quản lý khách hàng
  
  + Quản lý hóa đơn & chi tiết hóa đơn
  
  + Theo dõi lịch sử hoạt động
  
  + Xây dựng hệ thống bảo mật JWT
  
  + Thiết kế kiến trúc DAO tổng quát
  
  + Dự án được xây dựng theo định hướng phát triển backend chuyên nghiệp và tư duy phân tích hệ thống (Business Analysis mindset).

---Mục tiêu dự án---

-> Áp dụng kiến trúc phân tầng (Layered Architecture)

-> Thiết kế cơ sở dữ liệu có quan hệ chặt chẽ

-> Triển khai xác thực & phân quyền bằng JWT

-> Xây dựng RESTful API theo chuẩn thực tế

-> Tách biệt rõ Business Logic – Data Access – Security

-> Mô phỏng quy trình vận hành thực tế của doanh nghiệp

---Công nghệ sử dụng---

- Thành phần	Công nghệ

- Ngôn ngữ	Java 21

- Framework	Spring Boot 3+

- Bảo mật	Spring Security + JWT

- ORM	Spring Data JPA (Hibernate)

- Cơ sở dữ liệu	SQL Server

- Server	Tomcat 10

- Build Tool	Maven

---Kiến trúc hệ thống---

Hệ thống được xây dựng theo mô hình:

      Controller
      
          ⬇
      
      Service
      
          ⬇
      
      DAO (Generic CrudDAO<T, ID>)
      
          ⬇
      
      Database

Đặc điểm nổi bật:

- Sử dụng UserDetailsService tùy chỉnh

- JWT Authentication Filter tích hợp vào Security Filter Chain

- Thiết kế Generic DAO để tái sử dụng CRUD

- Cấu trúc project rõ ràng, dễ mở rộng

- Phân quyền theo Role

---Thiết kế cơ sở dữ liệu---

Hệ thống bao gồm các bảng chính:

+ TaiKhoan

+ PhanCa

+ LichSuHoatDong

+ KhachHang

+ San

+ GiaThueSan

+ DatSan

+ LichSuDatSan

+ SanPham

+ HoaDon

+ ChiTietHoaDon

+ Thiết kế database đảm bảo:

+ Ràng buộc khóa ngoại chặt chẽ

+ Chuẩn hóa dữ liệu

+ Dễ mở rộng theo mô hình thực tế

---Bảo mật---

  Sử dụng JWT để xác thực người dùng

  Phân quyền theo Role
  
  Password được mã hóa bằng BCrypt
  
  Security Filter Chain cấu hình theo chuẩn Spring Boot 3

---Cách chạy dự án---

1️⃣ Clone project

  git clone <repository-url>

2️⃣ Cấu hình database

  Tạo database trong MySQL:
  
  CREATE DATABASE badminton_management;
  
  Cập nhật file:
  
  src/main/resources/application.properties
  
  Ví dụ:
  
    spring.datasource.url=jdbc:mysql://localhost:3307/bsp_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true&characterEncoding=UTF-8
    
    spring.datasource.username=bsp_user
    
    spring.datasource.password=bsp_password
    
    app.jwt.secret=BadmintonManagementSecretKey2026VeryStrongKey
    
    app.jwt.expiration=86400000

3️⃣ Chạy project

  Chạy bằng Maven:
  
  mvn spring-boot:run
  
  Hoặc chạy trực tiếp trong IDE.

---API Authentication---

  Ví dụ đăng nhập:
  
    POST /api/v1/auth/login
    
    Body:
    
    {
    
      "username": "admin",
      
      "password": "admin123"
    
    }
  
  Hệ thống sẽ trả về JWT token để sử dụng cho các request tiếp theo.

---Định hướng phát triển---

  - Tích hợp giao diện frontend (React / Thymeleaf)
  
  - Thống kê doanh thu
  
  - Dashboard quản lý
  
  - Phân quyền nâng cao
  
  - Logging & Monitoring
  
  - Triển khai Docker

---Tác giả---

Văn Trọng

Sinh viên ngành Phát triển Phần mềm

Định hướng: Business Analyst & Backend Developer

