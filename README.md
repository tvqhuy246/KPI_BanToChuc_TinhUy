# KPI_BanToChuc_TinhUy
Phần mềm Quản lý và Đánh giá KPI Cán bộ, Công chức, Người lao động - Ban Tổ chức Tỉnh ủy Đắk Lắk

## 1. Giới thiệu tổng quan
Hệ thống chuyển đổi số toàn diện công tác quản lý cán bộ, theo dõi tiến độ công việc và đánh giá xếp loại thi đua theo **Quy định số 01-QĐ/BTCTU ngày 28/11/2025**.

## 2. Công nghệ sử dụng
- **Backend:** Java Spring Boot 3.3.4 (Spring Data JPA, Hibernate, Spring MVC)
- **Database:** H2 Database Engine / MySQL / PostgreSQL (Đã có sẵn schema.sql và data.sql)
- **Frontend:** Thymeleaf, Vanilla CSS (Thiết kế phong cách cơ quan Đảng: Đỏ đô & Xanh navy), Chart.js
- **Bảo mật & Phân quyền:** RBAC 3 cấp (Chuyên viên cá nhân, Trưởng phòng, Lãnh đạo Ban) & Audit Log