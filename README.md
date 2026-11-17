# HỆ THỐNG QUẢN LÝ PHÂN CÔNG NHIỆM VỤ & ĐÁNH GIÁ CHẤT LƯỢNG CÁN BỘ THEO VỊ TRÍ VIỆC LÀM (KPI)

[![Nền tảng](https://img.shields.io/badge/N%E1%BB%81n%20t%E1%BA%A3ng-Java%2021%20LTS%20%7C%20Spring%20Boot%203-red.svg)](https://spring.io/)
[![Kiến trúc](https://img.shields.io/badge/Ki%E1%BA%BFn%20tr%C3%BAc-Ph%C3%A2n%20t%E1%BA%A7ng%20Doanh%20nghi%E1%BB%87p%20(4%20L%E1%BB%9Bp)-gold.svg)](#)
[![An toàn thông tin](https://img.shields.io/badge/An%20to%C3%A0n%20th%C3%B4ng%20tin-C%E1%BA%A5p%20%C4%91%E1%BB%99%203%20(N%C4%90%2085%2F2016)-crimson.svg)](#)

---

## I. MỤC TIÊU & PHẠM VI ỨNG DỤNG

Phần mềm được nghiên cứu và xây dựng nhằm phục vụ công tác chuyển đổi số trong quản lý cán bộ, công chức; lượng hóa kết quả thực hiện chức trách, nhiệm vụ được giao theo từng vị trí việc làm:

1. **Bộ tiêu chí định lượng 100 điểm:** Đánh giá kết quả công tác của tập thể và cá nhân định kỳ hằng tháng, hằng quý và hằng năm; bảo đảm nguyên tắc kiểm điểm, đánh giá công tâm, khách quan và thực hiện nghiêm quy định về khống chế tỷ lệ Hoàn thành xuất sắc nhiệm vụ không vượt quá 20%.
2. **Quy trình phân cấp quản lý chặt chẽ:** Quản lý danh mục vị trí việc làm, giao nhiệm vụ có thời hạn và định mức giờ công chuẩn; thẩm định, đánh giá đa cấp theo đúng thẩm quyền hành chính từ cơ sở đến lãnh đạo đơn vị; hỗ trợ tự động tổng hợp và kết xuất dự thảo quyết định công nhận kết quả xếp loại thi đua theo thể thức quy định.
3. **Bảo đảm an toàn thông tin & bảo vệ dữ liệu:** Ứng dụng các giải pháp kỹ thuật đáp ứng tiêu chuẩn an toàn hệ thống thông tin Cấp độ 3; thực hiện phân quyền vai trò người dùng (RBAC), đóng dấu chìm định danh điện tử, mã hóa dữ liệu lưu trữ và lưu vết toàn diện nhật ký kiểm toán hệ thống.

---

## II. KIẾN TRÚC HỆ THỐNG

Hệ thống được thiết kế theo mô hình phân tầng khép kín, tối ưu hóa cho môi trường mạng truyền dẫn chuyên dùng và mạng nội bộ:

```
[ GIAO DIỆN NGƯỜI DÙNG ]
  • Công nghệ Server-Side Rendering (Thymeleaf 3) kết hợp giao diện chuẩn mực, trang nghiêm
  • Cơ chế đóng dấu chìm bảo mật động (Dynamic Security Watermark) hỗ trợ chống lộ lọt thông tin
            │
            ▼
[ PHÂN HỆ AN TOÀN & BẢO VỆ PHIÊN TRUY CẬP ]
  • Kiểm soát truy cập nghiêm ngặt theo phân cấp thẩm quyền (Role-Based Access Control)
  • Cơ chế mật mã học lưu trữ thông tin xác thực an toàn (BCrypt)
  • Lớp kiểm soát và sàng lọc dữ liệu đầu vào, phòng chống các nguy cơ an ninh mạng ứng dụng
            │
            ▼
[ PHÂN HỆ NGHIỆP VỤ & TỰ ĐỘNG HÓA ]
  • Xử lý thuật toán tổng hợp điểm số định lượng theo khung năng lực vị trí việc làm
  • Chu trình thẩm định, điều hướng hồ sơ tuần tự theo thẩm quyền chỉ đạo điều hành
  • Kiểm soát tính toàn vẹn của hồ sơ, tài liệu số hóa bằng thuật toán băm an toàn (SHA-256)
  • Mô-đun hỗ trợ tra cứu thông tin phục vụ công tác chuyên môn
            │
            ▼
[ PHÂN HỆ LƯU TRỮ & TOÀN VẸN DỮ LIỆU ]
  • Tương tác cơ sở dữ liệu qua tầng đối tượng bảo đảm tính toàn vẹn tham số (JPA/Hibernate ORM)
  • Cơ chế lưu trữ bền vững trên tệp dữ liệu, bảo đảm đầy đủ 4 thuộc tính giao dịch ACID
  • Nhật ký kiểm toán (Audit Trail) ghi nhận bất khả chối bỏ mọi biến động dữ liệu
```

---

## III. PHÂN ĐỊNH THẨM QUYỀN TRONG HỆ THỐNG

Phần mềm thiết lập các nhóm vai trò nghiệp vụ phản ánh đúng nguyên tắc tập trung dân chủ và chế độ thủ trưởng trong công tác quản lý cán bộ:

* **Cấp lãnh đạo đơn vị:** Chỉ đạo toàn diện; xem xét, phê duyệt kết quả đánh giá cuối cùng và ban hành quyết định công nhận xếp loại.
* **Cấp lãnh đạo phụ trách khối:** Theo dõi, chỉ đạo tiến độ các phòng chuyên môn; thẩm định kết quả đánh giá của các đơn vị trực thuộc.
* **Cấp trưởng phòng chuyên môn:** Trực tiếp phân công, giao nhiệm vụ theo vị trí việc làm; kiểm tra, đánh giá bước đầu đối với cán bộ thuộc phòng.
* **Cấp phó trưởng phòng:** Đôn đốc, giám sát việc thực hiện nhiệm vụ; hỗ trợ thẩm tra tiến độ và kết quả công việc.
* **Cấp chuyên viên, cán bộ nghiệp vụ:** Tiếp nhận nhiệm vụ, cập nhật nhật ký công tác, nộp sản phẩm minh chứng và thực hiện tự chấm điểm định kỳ.
* **Bộ phận quản trị hệ thống:** Bảo đảm kỹ thuật vận hành, sao lưu an toàn dữ liệu và giám sát an ninh hệ thống.

---

## IV. NỀN TẢNG CÔNG NGHỆ CHÍNH

* **Ngôn ngữ & Khung phát triển Backend:** Java 21 LTS, Spring Boot 3
* **Giao diện & Trình diễn Frontend:** Thymeleaf 3, Bootstrap 5, Modern CSS, JavaScript
* **Quản trị Cơ sở dữ liệu:** H2 Persistent Engine (Tương thích chuẩn SQL tiêu chuẩn, giao dịch ACID)
* **Giải pháp An toàn dữ liệu:** Mật mã hóa khóa an toàn, mã băm toàn vẹn SHA-256, Audit Trail Logging

---

## V. TRẢI NGHIỆM TRỰC TUYẾN & TRIỂN KHAI ĐÁM MÂY (DEMO)

Nhằm phục vụ công tác nghiệm thu, trải nghiệm và đánh giá nhanh các tính năng mà không yêu cầu máy chủ cục bộ phải mở liên tục, hệ thống đã được chuẩn hóa theo kiến trúc Cloud-Native:

* **Trải nghiệm trực tuyến (Cloud Demo):** Hệ thống sẵn sàng vận hành trên nền tảng đám mây (Cloud Web Service) thông qua liên kết kho mã nguồn tự động.
* **Tự động triển khai (CI/CD Ready):** Dự án tích hợp sẵn tệp cấu hình `Dockerfile` và `render.yaml`, hỗ trợ triển khai tức thì (1-Click Deployment) lên các nền tảng đám mây chuẩn Container (Render, Railway, Docker Engine). Khi kho mã nguồn cập nhật, quy trình build tự động sẽ tái thiết lập môi trường mới nhất.

> [!NOTE]
> Thông tin tài khoản trải nghiệm thử nghiệm phục vụ công tác đánh giá chuyên môn được phân cấp và bàn giao nội bộ theo đúng nguyên tắc bảo đảm an toàn thông tin.

---

## VI. HƯỚNG DẪN CÀI ĐẶT & KHỞI CHẠY TẠI MÔI TRƯỜNG NỘI BỘ (LOCAL)

### 1. Yêu cầu môi trường
* **Hệ điều hành:** Windows / Linux / macOS
* **Java Development Kit:** Java 21 LTS trở lên
* **Công cụ đóng gói:** Apache Maven 3.8+ (hoặc môi trường Docker)

### 2. Các phương thức khởi chạy

#### Cách 1: Sử dụng kịch bản tự động (Khuyên dùng trên Windows)
Thực hiện nhấp đúp hoặc chạy tệp lệnh trong thư mục gốc dự án:
```cmd
chay_he_thong.bat
```
*Kịch bản sẽ tự động kiểm tra biến môi trường Java, biên dịch mã nguồn và khởi động dịch vụ.*

#### Cách 2: Biên dịch và chạy thủ công bằng Maven / Java CLI
```bash
# 1. Tải kho mã nguồn
git clone -b qhuy https://github.com/tvqhuy246/KPI_BanToChuc_TinhUy.git
cd KPI_BanToChuc_TinhUy

# 2. Đóng gói ứng dụng (bỏ qua kiểm thử tự động)
mvn clean package -DskipTests

# 3. Khởi chạy ứng dụng
java -jar target/kpi-btctu-daklak-1.0.0.jar
```

#### Cách 3: Chạy nhanh bằng Docker (Không cần cài đặt Java/Maven)
```bash
# 1. Đóng gói Container Image
docker build -t kpi-btctu-daklak .

# 2. Khởi chạy Container dịch vụ
docker run -d -p 8080:8080 --name kpi-app kpi-btctu-daklak
```

### 3. Truy cập hệ thống
Sau khi khởi động thành công, mở trình duyệt web và truy cập theo địa chỉ:
* **Giao diện làm việc:** `http://localhost:8080` (hoặc cổng dịch vụ được cấu hình)
* **Trang đăng nhập:** Tự động điều hướng đến màn hình xác thực người dùng.

---
*Tài liệu kỹ thuật hệ thống phần mềm — Thực hiện: **Trần Võ Quang Huy***
