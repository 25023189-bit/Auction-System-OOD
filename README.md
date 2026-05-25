# Auction System - OOD (Object-Oriented Design)

## 📋 Mô Tả Dự Án

### Bài Toán
Đây là project hệ thống đấu giá realtime sử dụng **JavaFX** cho client, **Socket** cho server và **MySQL** cho database.

### Phạm Vi Hệ Thống

Project mô phỏng một hệ thống đấu giá với các chức năng chính:

- ✅ Đăng ký, đăng nhập
- ✅ Tạo phiên đấu giá cho seller
- ✅ Tham gia đấu giá cho bidder
- ✅ Đặt giá realtime
- ✅ Tự động gia hạn phiên ở thời điểm cuối (Anti-sniping)
- ✅ Tự động chốt phiên và xử lý tiền
- ✅ Tự động đóng phòng và đưa người dùng về sảnh khi phiên kết thúc

---

## 🛠 Công Nghệ & Môi Trường

### Công Nghệ Sử Dụng

| Công Nghệ | Tỷ Lệ | Mục Đích |
|-----------|-------|---------|
| Java | 85.3% | Backend server, client logic |
| Python | 12.8% | AI ChatBot |
| CSS | 1.8% | UI styling cho JavaFX |
| Batchfile | 0.1% | Build scripts |

**Tech Stack chính:**
- **JavaFX**: Giao diện người dùng
- **Java Socket**: Giao tiếp realtime client-server
- **MySQL**: Cơ sở dữ liệu
- **Maven**: Build tool
- **BCrypt**: Mã hóa mật khẩu
- **Python ML**: AI auto-approve phiên (Logistic Regression)

### Môi Trường & Yêu Cầu

- **JDK**: 25 (để biên dịch và chạy JavaFX 25)
- **MySQL**: 8.0 trở lên
- **IDE**: IntelliJ IDEA hoặc tương đương
- **Python**: 3.8+ (cho AI ChatBot)

### Yêu Cầu Cài Đặt

```bash
# 1. Clone repository
git clone https://github.com/25023189-bit/Auction-System-OOD.git
cd Auction-System-OOD

# 2. Tạo database
mysql -u root -p -e "CREATE DATABASE auction_system_v2;"

# 3. Import database schema
mysql -u root -p auction_system_v2 < src/main/resources/database/init_db.sql

# 4. Cấu hình database (tạo file db.properties)
# Xem chi tiết ở phần 3.4

# 5. Cài Python dependencies (cho ChatBot)
pip install -r Auction_AI/ChatBot/requirements.txt
```

---

## 📁 Cấu Trúc Thư Mục & Module Chính

```
AuctionSystem/
├── src/main/java/com/auction/
│   ├── client/                # Client-side logic (JavaFX GUI)
│   │   ├── app/               # Entry point (Launcher.java)
│   │   ├── feature/           # Login, Lobby, Room, Dashboard
│   │   ├── network/           # Socket communication
│   │   └── service/           # API gửi request tới server
│   │
│   ├── server/                # Server-side logic
│   │   ├── main/              # AuctionServer.java (entry point)
│   │   ├── handler/           # ClientHandler, xử lý request
│   │   ├── service/           # Business logic
│   │   ├── dao/               # Database operations
│   │   └── config/            # Configuration
│   │
│   └── common/                # Shared code
│       ├── dto/               # Data Transfer Objects
│       ├── model/             # User, AuctionRoom, Item, etc.
│       └── role/              # BIDDER, SELLER, ADMIN
│
├── src/main/resources/
│   ├── database/              # init_db.sql
│   ├── db.properties          # Database config
│   └── fxml/, css/, images/   # UI resources
│
├── Auction_AI/
│   ├── ChatBot/               # Python chatbot
│   └── AutoApprove/           # ML model dự đoán auto-approve phiên
│
└── pom.xml
```

### Module Chính

1. **Client Module** (`com.auction.client`)
   - JavaFX GUI interface
   - Login, Lobby (danh sách phòng), Room (phòng đấu giá)
   - Giao tiếp Socket realtime

2. **Server Module** (`com.auction.server`)
   - Lắng nghe kết nối từ client
   - Xử lý business logic phiên đấu giá
   - Quản lý database
   - Broadcast realtime updates

3. **Common Module** (`com.auction.common`)
   - Models dùng chung
   - DTOs truyền giữa client-server
   - Roles và enums

4. **AI Module** (`Auction_AI`)
   - ChatBot Python (NLP)
   - Auto-approve model (ML)

---

## 📦 Vị Trí File JAR

Sau khi build bằng Maven, các file JAR sẽ nằm trong thư mục:

```
target/
├── Auction-System-OOD-1.0-SNAPSHOT.jar          # Main executable
└── lib/                                          # Dependencies
```

**Hoặc build với IDE:**
```
out/artifacts/
├── AuctionServer.jar                            # Server JAR (nếu build riêng)
└── AuctionClient.jar                            # Client JAR (nếu build riêng)
```

---

## 🚀 Hướng Dẫn Chạy Ứng Dụng

### Bước 1: Tạo & Import Database

```bash
# Tạo database
mysql -u root -p -e "CREATE DATABASE auction_system_v2;"

# Import schema
mysql -u root -p auction_system_v2 < src/main/resources/database/init_db.sql
```

### Bước 2: Cấu Hình Database (db.properties)

**Tạo file**: `src/main/resources/db.properties`

**Nội dung mẫu**:
```properties
db.url=jdbc:mysql://localhost:3306/auction_system_v2
db.user=root
db.password=your_mysql_password
db.driver=com.mysql.cj.jdbc.Driver
```

*(Nếu chưa có file, copy từ `db.properties.example` và chỉnh sửa)*

### Bước 3: Cài Đặt Python ChatBot (Tuỳ Chọn)

```bash
# Navigate to ChatBot directory
cd Auction_AI/ChatBot

# Install dependencies
pip install -r requirements.txt
```

*Lưu ý: ChatBot sẽ tự train lại model nếu file cũ hoặc không tương thích*

### Bước 4: Chạy Server ⭐ (CHẠY TRƯỚC)

**Option 1: Từ IDE**
```
1. Mở project trong IntelliJ IDEA
2. Tìm class: com.auction.server.main.AuctionServer
3. Right-click → Run 'AuctionServer.main()'
```

**Option 2: Từ terminal**
```bash
mvn exec:java -Dexec.mainClass="com.auction.server.main.AuctionServer"
```

**Option 3: Từ JAR**
```bash
java -jar target/Auction-System-OOD-1.0-SNAPSHOT.jar
```

**Output mong đợi:**
```
[Server] Listening on port 5000...
[Database] Connection established successfully
[Server] Ready to accept connections
```

### Bước 5: Chạy Client ⭐ (CHẠY SAU)

**⚠️ QUAN TRỌNG: Chờ Server khởi động xong rồi mới chạy Client!**

**Option 1: Từ IDE**
```
1. Tìm class: com.auction.client.app.Launcher
2. Right-click → Run 'Launcher.main()'
```

**Option 2: Từ terminal**
```bash
mvn exec:java -Dexec.mainClass="com.auction.client.app.Launcher"
```

**Option 3: Từ JAR**
```bash
java -jar target/Auction-System-OOD-1.0-SNAPSHOT.jar
```

### Bước 6: Đăng Nhập & Sử Dụng

Sau khi Client khởi động, bạn sẽ thấy giao diện Login.

**Tài khoản Demo:**

| Role | Username | Password |
|------|----------|----------|
| Seller | hephaestus | 123 |
| Bidder | cerberus | 123 |
| Admin | hades | 123 |

---

## ✅ Danh Sách Chức Năng Hoàn Thành

### Chức Năng Người Dùng (General)
- [x] Đăng ký tài khoản mới
- [x] Đăng nhập / Đăng xuất
- [x] Quên mật khẩu + reset bằng email
- [x] Quản lý thông tin tài khoản cá nhân
- [x] Xem lịch sử giao dịch

### Chức Năng Người Bán (Seller)
- [x] Xem danh sách sản phẩm
- [x] Tạo phiên đấu giá (gửi yêu cầu Admin duyệt)
- [x] Xem danh sách phiên đang hoạt động
- [x] Xem danh sách phiên đã kết thúc
- [x] Chủ động đóng phiên
- [x] Xem lịch sử các phiên đã tạo

### Chức Năng Người Mua (Bidder)
- [x] Xem danh sách các phiên đấu giá đang chạy
- [x] Lọc và tìm kiếm phiên theo tiêu chí
- [x] Xem chi tiết sản phẩm và phiên
- [x] Tham gia phòng đấu giá (join room)
- [x] Đặt giá realtime (bidding)
- [x] Xem giá cao nhất và người thắng hiện tại
- [x] Xem lịch sử tất cả bid của mình
- [x] Xem danh sách phiên đã thắng

### Chức Năng Quản Trị (Admin)
- [x] Xem danh sách yêu cầu tạo phiên chờ duyệt
- [x] Duyệt hoặc từ chối yêu cầu phiên
- [x] Quản lý người dùng (view, suspend, activate)
- [x] Quản lý sản phẩm
- [x] Hủy phiên nếu cần
- [x] Xem báo cáo thống kê
- [x] Xem nhật ký hoạt động (audit log)

### Chức Năng Kỹ Thuật
- [x] Kiểm soát đồng thời truy cập (Thread-safe)
- [x] Anti-sniping (gia hạn tự động trong 30s cuối nếu có bid)
- [x] Xác thực & phân quyền người dùng
- [x] Mã hóa mật khẩu BCrypt
- [x] Lưu trữ dữ liệu an toàn trên MySQL
- [x] Xử lý lỗi và exception handling
- [x] Logging và monitoring (Audit Log)
- [x] Rate limiting để tránh spam
- [x] Email notification (quên mật khẩu)
- [x] AI ChatBot hỗ trợ trả lời câu hỏi
- [x] AI auto-approve phiên (ML model)

---

## 📄 Tài Liệu & Demo

### Link Báo Cáo & Tài Liệu
- 📖 **Báo cáo chi tiết**: [Tải PDF](./docs/report.pdf)
- 📚 **Tài liệu API**: [API Documentation](./docs/API_documentation.md)
- 📊 **Sơ đồ PlantUML/StarUML**: [Xem trong repository](./Auction_AI/)

### Link Video Demo
- 🎥 **Video hướng dẫn sử dụng**: [Xem Demo Video](#)
- 🎥 **Video demo Server/Client startup**: [Xem Demo Video](#)
- 🎥 **Video demo Anti-sniping & realtime bid**: [Xem Demo Video](#)

*Thay thế link video bằng link thực tế trên YouTube/Google Drive*

---

## 🔄 Quy Tắc Nghiệp Vụ Chính

### 1. Trạng Thái Phiên Đấu Giá

| Trạng thái | Ý nghĩa |
|-----------|---------|
| OPEN | Phiên đã tạo nhưng chưa bắt đầu |
| RUNNING | Phiên đang diễn ra |
| SOLD | Phiên kết thúc và bán thành công |
| UNSOLD | Phiên kết thúc nhưng không có người mua |
| CLOSED_BY_SELLER | Seller chủ động đóng phiên |
| CANCELED_BY_ADMIN | Admin hủy phiên |

### 2. Quy Tắc Tiền

| Trường hợp | Kết Quả |
|----------|--------|
| Seller đóng phiên | Không chuyển tiền |
| Admin hủy phiên | Không chuyển tiền |
| Hết giờ + có người thắng | Trừ tiền bidder thắng, cộng tiền cho seller |
| Hết giờ + không có ai bid | Không chuyển tiền |

### 3. Anti-Sniping

- Trong **30 giây cuối** của phiên, hệ thống **chặn người mới** tham gia
- Nếu có bid hợp lệ ở giai đoạn cuối, phiên sẽ được **gia hạn** thêm (mặc định 30-120s)
- **Số lần gia hạn không bị giới hạn** nếu vẫn có bid hợp lệ
- Các **participant đã join** vẫn được phép bid cho đến khi phiên thực sự kết thúc

### 4. Quy Tắc Tạo Phiên

- Seller nhập **giá khởi điểm** (starting_price)
- Seller nhập **bước giá** (bid_step) - mức tăng tối thiểu cho mỗi bid
- Seller nhập **số tiền tối thiểu** để tham gia (minimum_join_amount)
- **Constraint**: 
  - `minimum_join_amount` > 10% của `starting_price`
  - `minimum_join_amount` < 75% của `starting_price`
- Validation được thực hiện bởi lớp `AuctionCreationValidator`

### 5. Khi Hết Thời Gian

- Hệ thống **tự động chốt phiên**
- Dữ liệu phiên vẫn được **lưu trong database**
- **Toàn bộ seller & bidder** trong phiên được **đưa về sảnh**
- Phiên **biến mất khỏi danh sách** đang hoạt động trên UI

---

## 🔧 Troubleshooting

### Vấn đề kết nối Server
```
Error: Connection refused
Solution: 
- Kiểm tra server đã khởi động chưa (log sẽ hiển thị "Listening on port 5000")
- Kiểm tra port 5000 không bị chiếm bởi ứng dụng khác
- Kiểm tra firewall có chặn port không
```

### Vấn đề Database
```
Error: No suitable driver found / SQLNonTransientConnectionException
Solution:
- Kiểm tra MySQL service đang chạy
- Kiểm tra file db.properties có cấu hình đúng
- Kiểm tra có tạo database "auction_system_v2" chưa
- Kiểm tra user/password MySQL đúng
- Kiểm tra JDBC driver trong pom.xml (com.mysql:mysql-connector-java)
```

### Vấn đề GUI Client
```
Error: Exception in thread "main" java.awt.HeadlessException
Solution:
- Kiểm tra bạn có display available (không phải SSH headless)
- Hoặc sử dụng X11 forwarding nếu SSH
```

### Vấn đề JavaFX
```
Error: module javafx.fxml not found
Solution:
- Kiểm tra JDK 25 có cài module JavaFX
- Hoặc chỉnh sửa pom.xml javafx-maven-plugin version
```

### Không thấy Room trong Lobby
```
Reason: 
- Server chưa tạo phiên (hoặc phiên chưa được Admin duyệt)
- Phiên đã kết thúc/bị đóng
- Bidder không có đủ tiền để join phiên
Solution:
- Seller tạo phiên mới → Admin duyệt
- Bidder check số dư tài khoản
```

---

## 👨‍💻 Thông Tin Tác Giả & Phân Công

### Thông Tin Dự Án
- **Repository**: [25023189-bit/Auction-System-OOD](https://github.com/25023189-bit/Auction-System-OOD)
- **Bài tập**: Lập Trình Nâng Cao - Object-Oriented Design

### Phân Công Công Việc

| Tác Giả | Trách Nhiệm |
|--------|-----------|
| **Nguyễn Hữu Hùng** | Đăng nhập, Đăng ký, AI ChatBot, Tạo phiên, Biến động số dư, Đặt giá, Kết thúc phiên, Phân quyền Admin |
| **Tô Bảo Hân** | Lập trình mạng, Xử lý Server, Đa luồng (Multithreading), Quản trị trạng thái hệ thống, Quản lý mã nguồn |
| **Bùi Thế Dũng** | Database design, Kết nối database, Lấy lại mật khẩu bằng email, Testing & debugging |
| **Đặng Đức Anh** | UI/UX JavaFX (Login, Lobby, Room, Dashboard), Thiết kế giao diện |

---

## 📜 License

This project is part of a university assignment. All rights reserved.

---

## 🔗 Tham Khảo Ý Tưởng & Thiết Kế

**Auction Platform**:
- https://www.bpiauctions.com/
- https://www.namecheap.com/
- https://dgts.moj.gov.vn/
- https://daugiaviet.vn/home
- https://www.ebay.com/
- https://www.catawiki.com/en
- https://www.dealdash.com/

**Social & Commerce**:
- https://www.facebook.com/
- https://www.tiktok.com/
- https://www.instagram.com/
- https://www.amazon.com/

---

**Cập nhật lần cuối**: 25/05/2026
