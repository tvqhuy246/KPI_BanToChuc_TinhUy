# HỆ THỐNG QUẢN LÝ PHÂN CÔNG NHIỆM VỤ & ĐÁNH GIÁ CÁN BỘ THEO VỊ TRÍ VIỆC LÀM (KPI)
## BAN TỔ CHỨC TỈNH ỦY ĐẮK LẮK

[![Java](https://img.shields.io/badge/Java-21%20LTS-orange.svg?logo=openjdk)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.4-brightgreen.svg?logo=springboot)](https://spring.io/projects/spring-boot)
[![Database](https://img.shields.io/badge/Database-H2%20Persistent%20(PostgreSQL%20Mode)-blue.svg?logo=postgresql)](https://www.h2database.com/)
[![Security](https://img.shields.io/badge/Security-C%E1%BA%A5p%20%C4%91%E1%BB%99%203%20(N%C4%90%2085%2F2016)-red.svg)](https://thuvienphapluat.vn/)
[![UI Style](https://img.shields.io/badge/UI-Chu%E1%BA%A9n%20Q%C4%90%2099--Q%C4%90%2FTW-crimson.svg)](https://tulieuvankien.dangcongsan.vn/)

---

## 📌 GIỚI THIỆU TỔNG QUAN

Hệ thống được thiết kế và xây dựng nhằm giải quyết trọn vẹn 3 yêu cầu cốt lõi trong công tác quản lý và đánh giá cán bộ tại **Ban Tổ chức Tỉnh ủy Đắk Lắk** (quy mô 39 cán bộ công chức, 05 đầu mối phòng/văn phòng và 49 vị trí việc làm):

1. **Chuẩn hóa thước đo định lượng (KPI 100 điểm):** Đánh giá cá nhân và tập thể các phòng định kỳ hằng tháng, quý, năm; tự động khóa trần Hoàn thành xuất sắc $\le 20\%$ theo **Quy định số 366-QĐ/TW ngày 30/8/2025 của Bộ Chính trị** và **Nghị định số 90/2020/NĐ-CP (sửa đổi bởi NĐ 48/2023/NĐ-CP)**.
2. **Số hóa quy trình phân cấp 5 tầng:** Quản lý cán bộ, phòng chuyên môn, 49 Vị trí việc làm, phân công nhiệm vụ, cập nhật tiến độ, thẩm định phê duyệt không vượt cấp (`Cấp 5 -> 4 -> 3 -> 2 -> 1`), và tự động sinh **Dự thảo Quyết định A4** đúng thể thức Đảng (**Quy định số 399-QĐ/TW**).
3. **Bảo đảm An toàn thông tin Cấp độ 3:** Đáp ứng **Khoản 1 Điều 9 Nghị định số 85/2016/NĐ-CP**, **Luật Bảo vệ bí mật nhà nước 2018**, **Nghị định 63/2026/NĐ-CP** và **Luật Dữ liệu 2024** (RBAC 5 cấp, Watermark chống chụp màn hình, mã hóa BCrypt, vân tay số SHA-256, Audit Trail và tường lửa AI chống Web LLM Attacks).

---

## 🚀 HƯỚNG DẪN KHỞI ĐỘNG HỆ THỐNG

### 1. Khởi động nhanh (Khuyên dùng trên Windows):
- Nhấp đúp chuột vào file: `chay_he_thong.bat`
- Ứng dụng tự động khởi chạy tại cổng **8080**.

### 2. Khởi động bằng dòng lệnh (Maven):
```bash
# Biên dịch và đóng gói ứng dụng
mvn clean package -DskipTests

# Chạy ứng dụng
java -jar target/kpi-management-1.0.0.jar
```

### 3. Đường dẫn truy cập các phân hệ:
- **Trang đăng nhập:** [http://localhost:8080/login](http://localhost:8080/login)
- **Bàn làm việc chỉ đạo điều hành:** [http://localhost:8080/dashboard](http://localhost:8080/dashboard)
- **Quản lý phân công nhiệm vụ (49 VTVL):** [http://localhost:8080/tasks](http://localhost:8080/tasks)
- **Báo cáo tổng hợp & Dự thảo Quyết định A4:** [http://localhost:8080/reports](http://localhost:8080/reports)
- **Nhật ký kiểm toán an toàn thông tin (Audit Logs):** [http://localhost:8080/audit-logs](http://localhost:8080/audit-logs)
- **H2 Database Console:** [http://localhost:8080/h2-console](http://localhost:8080/h2-console)
  - *JDBC URL:* `jdbc:h2:file:./data/kpidb;AUTO_SERVER=TRUE`
  - *Username:* `sa` | *Password:* `password`

---

## 👥 DANH SÁCH TÀI KHOẢN ĐĂNG NHẬP & PHÂN QUYỀN 5 CẤP

Tất cả tài khoản trên hệ thống đều sử dụng mật khẩu mặc định: **`password123`**

### 1. Tài khoản Lãnh đạo Ban & Đại diện các cấp chính thức (Ban Tổ chức Tỉnh ủy):
| Cấp bậc | Họ và tên | Chức danh / Vai trò | Tên đăng nhập | Thẩm quyền trên hệ thống |
| :---: | :--- | :--- | :---: | :--- |
| **Cấp 1** | **Nguyễn Thượng Hải** | Trưởng ban Tổ chức Tỉnh ủy | `nguyen_thuong_hai` | **Lãnh đạo cao nhất:** Chỉ đạo toàn cơ quan, phê duyệt kết quả cuối cùng, ký ban hành Quyết định A4. |
| **Cấp 2** | **Lưu Vĩnh Hưng** | Phó Trưởng ban thường trực | `luu_vinh_hung` | Phụ trách các khối phòng, thẩm định kết quả Cấp 3 trước khi trình Trưởng ban. |
| **Cấp 3** | **Trần Hải Triều** | Trưởng phòng Tổ chức cán bộ | `tran_hai_trieu` | Quản trị phòng, giao việc theo 49 VTVL, chấm điểm Cấp 4 và Cấp 5 trong phòng. |
| **Cấp 4** | **Nguyễn Trần Thi** | Phó Trưởng phòng TCCB | `nguyen_tran_thi` | Đôn đốc công việc, theo dõi hồ sơ MẬT, thẩm định bước 1 phiếu KPI của chuyên viên. |
| **Cấp 5** | **Hồ Lê Nhân** | Chuyên viên Văn phòng | `ho_le_nhan` | Nhận việc, cập nhật tiến độ %, tự chấm điểm KPI hằng tháng/quý/năm, nộp minh chứng. |
| **Admin** | **Quản trị viên** | Quản trị An toàn hệ thống | `admin` | Quản trị CSDL, giám sát Audit Logs, phân quyền an ninh mạng. |

### 2. Bộ 4 tài khoản kiểm thử mang tên tác giả (Trần Võ Quang Huy):
Được phân bổ chuẩn mực từ Cấp 2 đến Cấp 5 trực thuộc khối chỉ đạo của Trưởng ban (Cấp 1):
| Cấp bậc | Tên hiển thị | Tên đăng nhập | Chức vụ & Đơn vị | Điểm KPI mẫu |
| :---: | :--- | :---: | :--- | :---: |
| **Cấp 2** | Trần Võ Quang Huy 2 (Cấp 2) | **`huy2`** | Phó Trưởng ban — Lãnh đạo Ban | 48.0đ *(Không HT)* |
| **Cấp 3** | Trần Võ Quang Huy 3 (Cấp 3) | **`huy3`** | Trưởng phòng — Văn phòng Ban | 47.0đ *(Không HT)* |
| **Cấp 4** | Trần Võ Quang Huy 4 (Cấp 4) | **`huy4`** | Phó Trưởng phòng — Văn phòng Ban | 46.0đ *(Không HT)* |
| **Cấp 5** | Trần Võ Quang Huy 5 (Cấp 5) | **`huy5`** | Chuyên viên — Văn phòng Ban | 45.0đ *(Không HT)* |

---

## 🛠️ KIẾN TRÚC HỆ THỐNG & CÔNG NGHỆ ÁP DỤNG (TECH STACK)

Hệ thống được thiết kế theo mô hình phân tầng **Enterprise Multi-tier Architecture**:

```
[ Client Browser / Mobile Web ]
            │  HTTPS / TLS 1.3
            ▼
[ TẦNG TRÌNH DIỄN (Presentation Layer) ]
  • Thymeleaf 3 (Server-Side Rendering) + Bootstrap 5.3.2 + CSS Glassmorphism
  • Nhận diện Cờ Đảng - Cờ Tổ quốc chuẩn Quy định 99-QĐ/TW
  • Dynamic Security Watermark (In chìm Họ tên, Mã CB, IP, Thời gian)
            │
            ▼
[ TẦNG BẢO MẬT & KIỂM SOÁT PHIÊN (Security Layer) ]
  • AuthInterceptor: Kiểm soát truy cập RBAC 5 cấp, chống BOLA / IDOR 2.0
  • BCrypt Engine (Cost Factor = 10, Salt 128-bit) mã hóa mật khẩu một chiều
  • AI Security Firewall: Chống Prompt Injection & Web LLM Attacks
            │
            ▼
[ TẦNG NGHIỆP VỤ (Business Logic Layer) ]
  • KpiCalculationService: Thuật toán 100 điểm, khóa cứng trần Xuất sắc ≤ 20%
  • ApprovalSmartRouting: Định tuyến phê duyệt tuần tự 5 bước
  • EvidenceSecurityService: Băm toàn vẹn tệp minh chứng SHA-256 (Luật Dữ liệu 2024)
  • Hybrid AI Chatbot Service: Local RAG (Nội bộ) + Google Gemini API (gemini-2.0-flash)
            │
            ▼
[ TẦNG DỮ LIỆU & LƯU TRỮ (Persistence Layer) ]
  • Spring Data JPA & Hibernate 6 ORM (Truy vấn tham số chống SQL Injection)
  • CSDL H2 Persistent Engine (File `./data/kpidb.mv.db`), tương thích PostgreSQL
  • Chuẩn toàn vẹn giao dịch ACID, cơ chế sao lưu 3-2-1 Air-Gapped chống Ransomware
```

---

## ⚖️ HỆ THỐNG CĂN CỨ PHÁP LÝ & QUY CHUẨN ĐÃ TUÂN THỦ

1. **Về công tác đánh giá, xếp loại cán bộ:**
   - **Quy định số 366-QĐ/TW ngày 30/8/2025 của Bộ Chính trị:** Về kiểm điểm và đánh giá, xếp loại chất lượng đối với tập thể, cá nhân trong hệ thống chính trị (khống chế tỷ lệ Hoàn thành xuất sắc $\le 20\%$).
   - **Nghị định số 90/2020/NĐ-CP (sửa đổi bởi Nghị định số 48/2023/NĐ-CP):** Về đánh giá, xếp loại chất lượng cán bộ, công chức, viên chức.
2. **Về thể thức văn bản & công tác văn thư:**
   - **Quy định số 399-QĐ/TW ngày 09/01/2026 của Ban Bí thư:** Về thể loại, thẩm quyền ban hành và thể thức văn bản của Đảng (ứng dụng sinh Dự thảo Quyết định A4 tự động).
   - **Nghị định số 30/2020/NĐ-CP ngày 05/3/2020 của Chính phủ:** Về công tác văn thư.
3. **Về An toàn thông tin, An ninh mạng & Bảo vệ Bí mật nhà nước:**
   - **Khoản 1 Điều 9 Nghị định số 85/2016/NĐ-CP & Thông tư 12/2022/TT-BTTTT:** Xác định hệ thống xử lý thông tin nội bộ của cơ quan Đảng có dữ liệu bí mật nhà nước thuộc **Cấp độ 3**.
   - **Luật Bảo vệ bí mật nhà nước 2018 & Quyết định số 1630/QĐ-TTg ngày 30/7/2025 của Thủ tướng Chính phủ:** Quy định danh mục bí mật nhà nước của Đảng thuộc độ MẬT (hồ sơ nhân sự, quy hoạch cán bộ, thẩm tra chính trị nội bộ).
   - **Nghị định số 63/2026/NĐ-CP ngày 28/02/2026 của Chính phủ:** Quy định chi tiết biện pháp thi hành Luật Bảo vệ bí mật nhà nước trên môi trường số.
   - **Luật Dữ liệu số 60/2024/QH15:** Bảo vệ tính toàn vẹn dữ liệu bằng mã băm SHA-256 và mã hóa kênh truyền TLS 1.3.
   - **Chỉ thị số 14/CT-TTg của Thủ tướng Chính phủ:** Mô hình bảo đảm an toàn thông tin 4 lớp.
4. **Về nghi thức cờ Đảng, cờ Tổ quốc:**
   - **Quy định số 99-QĐ/TW ngày 27/02/2023 của Ban Bí thư Trung ương Đảng:** Chuẩn hóa quy cách cờ Đảng (tỷ lệ 2:3, Búa Liềm hướng lên trên), cờ Đảng đặt bên trái, cờ Tổ quốc đặt bên phải.

---

## 🔒 HƯỚNG DẪN BẢO MẬT KHI TRIỂN KHAI

Để đảm bảo an toàn tuyệt đối khi đưa lên môi trường sản xuất (Production):
1. **API Key Trợ lý AI:** Được cô lập trong file `src/main/resources/application-secret.properties` (đã khai báo trong `.gitignore`). Khi triển khai trên máy chủ, có thể truyền qua biến môi trường hệ thống:
   ```bash
   export AI_CHATBOT_API_KEY="your-gemini-api-key"
   ```
2. **Cơ sở dữ liệu:** File CSDL `./data/kpidb.mv.db` được bảo vệ ngắt truy cập từ bên ngoài bằng `H2SecurityFilter`, chỉ cho phép ứng dụng truy xuất nội bộ.
3. **Mã nguồn:** Toàn bộ lịch sử commit đã được rà soát sạch, không để lộ thông tin mật hay chứng chỉ số cá nhân.

---
*Tác giả thực hiện: **Trần Võ Quang Huy** — Đề bài kiểm tra năng lực ứng dụng CNTT Ban Tổ chức Tỉnh ủy Đắk Lắk.*
