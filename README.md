# 🔨 Hệ Thống Đấu Giá Trực Tuyến (Online Auction System - OOD)

Dự án Hệ thống Đấu giá Trực tuyến được thiết kế theo mô hình Hướng đối tượng (Object-Oriented Design - OOD). 

## 🚀 Lịch sử làm việc & Ý tưởng triển khai (Đến giai đoạn hiện tại)

Dưới đây là nhật ký công việc và các quyết định kiến trúc cốt lõi mà tôi đã thực hiện trong giai đoạn khởi tạo dự án:

### 1. Kiến trúc Cơ sở dữ liệu (Phiên bản 5.0 - The Masterpiece)
Thay vì tạo các bảng đơn giản, tôi đã thiết kế một CSDL MySQL (`auction_system`) cực kỳ chặt chẽ và áp dụng tư duy OOP trực tiếp vào SQL:
* **Áp dụng OOP (Joined-Table Strategy):** Xây dựng bảng `products` làm lớp cha (Abstract Item) và các bảng con `product_electronics`, `product_arts`, `product_vehicles` kế thừa từ lớp cha.
* **Ràng buộc dữ liệu siêu chặt (Max Ping):** * Dùng `REGEX` để ép chuẩn định dạng ID ngay từ Database (VD: Customer ID phải là `BD5...`, Auction ID phải là `AU1...`).
  * Dùng `CHECK` constraint để đảm bảo logic nghiệp vụ (Giá hiện tại >= Giá khởi điểm, Thời gian kết thúc > Thời gian bắt đầu).
* **Bảo toàn toàn vẹn dữ liệu:** Thiết lập đầy đủ các Khóa ngoại (`FOREIGN KEY`) với cơ chế `ON DELETE CASCADE` và `RESTRICT` hợp lý, kết hợp `UNIQUE KEY` cho tính năng Auto-bidding.

### 2. Thiết lập Java & Kết nối MySQL
* **Quản lý thư viện bằng Maven:** Khởi tạo file `pom.xml` chuẩn chỉ để quản lý các dependency.
* **Bảo mật mật khẩu (Yêu cầu cốt lõi):** Tích hợp thư viện `jbcrypt` và tạo class `PasswordUtil` để băm mật khẩu (Hash Password) kết hợp "muối" (salt 12) trước khi lưu xuống DB, đảm bảo an toàn tuyệt đối theo tiêu chuẩn công nghiệp.
* **Kết nối CSDL:** Tích hợp `mysql-connector-j` và xây dựng class `DatabaseConnection` dùng JDBC để giao tiếp thành công với MySQL.

### 3. Quản lý Mã nguồn (Git/GitHub)
* Khởi tạo và thiết lập chuẩn luồng làm việc với Git.
* Cấu hình file `.gitignore` để loại bỏ các file rác/file cấu hình IDE (`.idea/`, `target/`), giúp repository luôn sạch sẽ và không bị xung đột khi làm việc nhóm.
* Xử lý thành công các vấn đề về đồng bộ (Pull --rebase) và đẩy code (Push) an toàn lên GitHub.

---

## 📂 Cấu trúc thư mục hiện tại

```text
Auction-System-OOD/
│
├── .gitignore               # Chặn các file rác không cho lên Git
├── database_v5.sql          # Script khởi tạo CSDL hoàn chỉnh
├── pom.xml                  # Cấu hình Maven & Thư viện (MySQL, BCrypt)
├── README.md                # Tài liệu dự án (File này)
│
└── src/
    └── main/
        └── java/
            ├── com.auction.model/  # Chứa các Class/Entity (User, Product...)
            ├── utils/
            │   ├── DatabaseConnection.java  # Class kết nối MySQL
            │   └── PasswordUtil.java        # Class băm mật khẩu BCrypt
            └── server.main.Main.java            # File chạy test hệ thống
