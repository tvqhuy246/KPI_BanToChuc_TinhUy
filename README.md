# HỆ THỐNG QUẢN LÝ PHÂN CÔNG NHIỆM VỤ & ĐÁNH GIÁ KPI THEO VỊ TRÍ VIỆC LÀM

[![Java](https://img.shields.io/badge/Java-21%20LTS-orange.svg?logo=openjdk)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.4-brightgreen.svg?logo=springboot)](https://spring.io/projects/spring-boot)
[![Database](https://img.shields.io/badge/Database-H2%20Persistent%20(PostgreSQL%20Mode)-blue.svg?logo=postgresql)](https://www.h2database.com/)
[![Security](https://img.shields.io/badge/Security-C%E1%BA%A5p%20%C4%91%E1%BB%99%203-red.svg)](https://thuvienphapluat.vn/)

---

## 📌 GIỚI THIỆU TỔNG QUAN

Ứng dụng Web hỗ trợ công tác quản lý phân công nhiệm vụ, theo dõi tiến độ và đánh giá kết quả thực hiện nhiệm vụ (KPI) định kỳ theo vị trí việc làm:

1. **Bộ tiêu chí định lượng 100 điểm:** Đánh giá cá nhân và tập thể định kỳ hằng tháng, quý, năm; tự động khống chế tỷ lệ Hoàn thành xuất sắc $\le 20\%$ theo quy định.
2. **Quy trình phân cấp 5 tầng:** Quản lý vị trí việc làm, phân công nhiệm vụ, cập nhật tiến độ, thẩm định phê duyệt tuần tự từ Cấp 5 đến Cấp 1, và tự động tạo dự thảo quyết định xếp loại khổ A4.
3. **Bảo đảm an toàn thông tin:** Thiết kế bảo đảm an toàn hệ thống thông tin Cấp độ 3 (phân quyền vai trò RBAC, đóng dấu chìm bảo mật, mã hóa dữ liệu và lưu vết nhật ký kiểm toán).

---

## 🏗️ KIẾN TRÚC HỆ THỐNG (SYSTEM ARCHITECTURE)

Hệ thống được thiết kế theo mô hình kiến trúc phân tầng doanh nghiệp (Enterprise Multi-tier Architecture):

```
[ Client Browser / Desktop Web ]
            │  HTTPS / Web Traffic
            ▼
[ TẦNG TRÌNH DIỄN (Presentation Layer) ]
  • Thymeleaf 3 (Server-Side Rendering) kết hợp Bootstrap 5.3.2 Responsive
  • Dynamic Security Watermark: Cơ chế đóng dấu chìm bảo mật hiển thị theo phiên làm việc
            │
            ▼
[ TẦNG BẢO MẬT & ĐIỀU HƯỚNG (Security Layer) ]
  • AuthInterceptor: Kiểm soát phiên truy cập, phân quyền theo 5 cấp bậc chức vụ (RBAC)
  • Password Security Engine: Cơ chế băm mật khẩu một chiều an toàn (BCrypt)
  • Application Firewall: Lớp kiểm soát và ngăn chặn các nguy cơ tấn công Web / Prompt Injection
            │
            ▼
[ TẦNG NGHIỆP VỤ (Business Logic Layer) ]
  • KpiCalculationService: Thuật toán tổng hợp điểm số định lượng, tự động áp trần thi đua ≤ 20%
  • ApprovalSmartRouting: Định tuyến luồng thẩm định và phê duyệt phân cấp không vượt cấp
  • EvidenceIntegrityService: Cơ chế kiểm tra toàn vẹn tài liệu và tệp số hóa bằng thuật toán SHA-256
  • Assistant Support Service: Mô-đun hỗ trợ tra cứu thông tin quy chế và nghiệp vụ nội bộ
            │
            ▼
[ TẦNG DỮ LIỆU & LƯU TRỮ (Persistence Layer) ]
  • Spring Data JPA & Hibernate 6 ORM: Tương tác CSDL qua truy vấn tham số hóa an toàn
  • Persistent Database Engine: Lưu trữ bền vững, tuân thủ chuẩn giao dịch ACID
  • Audit Trail Logging: Ghi nhận và lưu vết lịch sử mọi thao tác thay đổi dữ liệu
```

---

## 🚀 HƯỚNG DẪN KHỞI ĐỘNG HỆ THỐNG

### 1. Khởi động nhanh (Windows):
- Nhấp đúp chuột vào file: `chay_he_thong.bat`
- Hệ thống tự động khởi chạy tại cổng mặc định **8080**.

### 2. Khởi động bằng dòng lệnh (Maven):
```bash
mvn clean package -DskipTests
java -jar target/kpi-management-1.0.0.jar
```

### 3. Truy cập ứng dụng:
- **Trang đăng nhập:** [http://localhost:8080/login](http://localhost:8080/login)

---

## 👥 CÁC VAI TRÒ MÔ PHỎNG TRONG HỆ THỐNG (ROLES)

Hệ thống thiết lập sẵn các nhóm vai trò mô phỏng tương ứng với 5 cấp bậc quản trị để kiểm thử quy trình nghiệp vụ:

| Cấp bậc | Vai trò phân cấp | Tên đăng nhập demo | Thẩm quyền mô phỏng |
| :---: | :--- | :---: | :--- |
| **Cấp 1** | Lãnh đạo cấp cao | `admin` | Phê duyệt kết quả cuối cùng, ký ban hành quyết định xếp loại |
| **Cấp 2** | Lãnh đạo phụ trách | `huy2` | Phụ trách khối, thẩm định hồ sơ cấp phòng |
| **Cấp 3** | Lãnh đạo phòng | `huy3` | Giao việc theo danh mục vị trí việc làm, thẩm định cấp phòng |
| **Cấp 4** | Phó trưởng phòng | `huy4` | Đôn đốc tiến độ, rà soát hồ sơ chuyên viên |
| **Cấp 5** | Chuyên viên thực thi | `huy5` | Nhận việc, cập nhật tiến độ %, tự chấm điểm định kỳ |

> *(Lưu ý: Mật khẩu của các tài khoản được quản lý nội bộ theo chính sách bảo mật của hệ thống).*

---

## 🛠️ CÔNG NGHỆ SỬ DỤNG (TECH STACK)

- **Backend:** Java 21 LTS, Spring Boot 3.3.4 (Spring MVC, Spring Data JPA)
- **Frontend:** Thymeleaf 3, Bootstrap 5.3.2, HTML5/CSS3, JavaScript
- **Cơ sở dữ liệu:** H2 Persistent Engine (chế độ PostgreSQL tương thích, giao dịch ACID)
- **An toàn thông tin:** BCrypt, SHA-256 Digest, Dynamic Watermarking, Audit Trail Logging

---
*Tác giả: **Trần Võ Quang Huy***
