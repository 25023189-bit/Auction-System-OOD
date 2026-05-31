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
| Java | 8x.x% | Backend server, client logic |
| Python | 1x.x% | AI ChatBot |
| CSS | 1.x% | UI styling cho JavaFX |
| Batchfile | 0.x% | Build scripts |

**Tech Stack chính:**
- **JavaFX**: Giao diện người dùng
- **Java Socket**: Giao tiếp realtime client-server
- **MySQL**: Cơ sở dữ liệu
- **Maven**: Build tool (Multi-module)
- **BCrypt**: Mã hóa mật khẩu
- **Python ML**: AI auto-approve phiên (Logistic Regression)
- **HikariCP**: Connection pooling

### Môi Trường & Yêu Cầu

- **JDK**: 25 (để biên dịch và chạy JavaFX 25)
- **MySQL**: 8.0 trở lên
- **IDE**: IntelliJ IDEA hoặc tương đương
- **Python**: 3.8+ (cho AI ChatBot)
- **Maven**: 3.6+

### Yêu Cầu Cài Đặt

```bash
# 1. Clone repository
git clone https://github.com/25023189-bit/Auction-System-OOD.git
cd Auction-System-OOD

# 2. Tạo database
mysql -u root -p -e "CREATE DATABASE auction_system_v2;"

# 3. Import database schema
mysql -u root -p auction_system_v2 < auction-server/src/main/resources/database/init_db.sql

# 4. Cấu hình database (tạo file application.properties)
# Xem chi tiết ở phần Cấu Hình Database

# 5. Cài Python dependencies (cho ChatBot)
pip install -r Auction_AI/ChatBot/requirements.txt

# 6. Build all modules
mvn clean package
```

---

## 📁 Cấu Trúc Thư Mục & Module Chi Tiết

```
Auction-System-OOD/                                   [Root Project]
│
├── 📄 pom.xml                                         [Parent POM - Multi-module]
├── 📄 README.md
├── 📄 DATABASE_SCHEMA_AND_DAO_GUIDE.md
├── 📄 auction_system_functions.json
│
├── 📁 .github/                                        [GitHub Actions]
├── 📁 .mvn/                                           [Maven Wrapper]
├── 📁 .idea/                                          [IntelliJ Config]
│
│
├── 📦 AUCTION-COMMON MODULE
│   ├── 📄 pom.xml
│   ├── src/main/java/com/auction/common/
│   │   ├── dto/
│   │   │   ├── Message.java                          # Socket message wrapper
│   │   │   ├── AuctionEndNotificationPayload.java    # Auction end event
│   │   │   ├── AutoBidRequest.java                   # Auto-bid request
│   │   │   ├── BidHistoryDTO.java                    # Bid history data
│   │   │   └── ...
│   │   │
│   │   ├── model/
│   │   │   ├── Entity.java                           # Base entity
│   │   │   ├── User.java                             # Base user class
│   │   │   ├── Seller.java                           # Seller subclass
│   │   │   ├── Bidder.java                           # Bidder subclass
│   │   │   ├── Admin.java                            # Admin subclass
│   │   │   ├── Item.java                             # Product/Item
│   │   │   ├── AuctionRoom.java                      # Auction room entity
│   │   │   ├── Art.java                              # Art item type
│   │   │   ├── BidTransaction.java                   # Bid transaction log
│   │   │   ├── AutoBidAgent.java                     # Auto-bid agent
│   │   │   ├── AutoBiddingRule.java                  # Auto-bid rules
│   │   │   ├── Notification.java                     # Notification entity
│   │   │   ├── NotificationType.java                 # ENUM
│   │   │   ├── PendingAuctionRequest.java            # Pending request
│   │   │   ├── ProductDetailResponse.java            # Response model
│   │   │   ├── PaymentStatus.java                    # ENUM
│   │   │   ├── TransactionType.java                  # ENUM
│   │   │   └── ...
│   │   │
│   │   └── role/
│   │       └── UserRole.java                         # BIDDER, SELLER, ADMIN
│   │
│   └── src/test/java/                                [Unit Tests]
│
│
├── 📦 AUCTION-SERVER MODULE
│   ├── 📄 pom.xml
│   ├── src/main/java/com/auction/server/
│   │   ├── main/
│   │   │   └── AuctionServer.java                    # Entry point (port 5000)
│   │   │
│   │   ├── handler/
│   │   │   ├── ClientHandler.java                    # Process client requests
│   │   │   ├── ClientConnection.java                 # Client connection wrapper
│   │   │   ├── MessageProcessor.java                 # Message processing
│   │   │   └── MessageRouter.java                    # Route messages to handlers
│   │   │
│   │   ├── network/
│   │   │   ├── ServerSocket.java                     # TCP server
│   │   │   ├── MessageCodec.java                     # Serialize/deserialize
│   │   │   └── NetworkManager.java                   # Network management
│   │   │
│   │   ├── service/
│   │   │   ├── AuthService.java                      # Login/Register/Password
│   │   │   ├── AuctionRoomService.java               # Auction room logic
│   │   │   ├── AuctionCreationValidator.java         # Validation rules
│   │   │   ├── AuctionStateManager.java              # State management
│   │   │   ├── AuctionRuntimeState.java              # Runtime state holder
│   │   │   ├── AutoBidManager.java                   # Auto-bidding logic
│   │   │   ├── PendingAuctionApprovalService.java    # Admin approval
│   │   │   ├── ProductDetailService.java             # Product details
│   │   │   ├── PasswordStrengthValidator.java        # Password validation
│   │   │   ├── RateLimiter.java                      # Rate limiting
│   │   │   └── ...
│   │   │
│   │   ├── dao/
│   │   │   ├── Interfaces:
│   │   │   │   ├── IUserDAO.java
│   │   │   │   ├── IAuctionDAO.java
│   │   │   │   ├── IBidDAO.java
│   │   │   │   └── ...
│   │   │   ├── Implementations:
│   │   │   │   ├── UserDAO.java                      # User DB operations
│   │   │   │   ├── AuctionDAO.java                   # Auction DB operations
│   │   │   │   ├── BidDAO.java                       # Bid DB operations
│   │   │   │   ├── ItemDAO.java                      # Item DB operations
│   │   │   │   ├── TransactionDAO.java               # Transaction logs
│   │   │   │   └── ...
│   │   │
│   │   ├── config/
│   │   │   ├── DatabaseConfig.java                   # DB connection pool
│   │   │   ├── ServerConfig.java                     # Server configuration
│   │   │   ├── ConnectionPool.java                   # HikariCP pool
│   │   │   └── ...
│   │   │
│   │   ├── AI/
│   │   │   ├── AutoApprovalModel.java                # ML model integration
│   │   │   ├── ChatBotIntegration.java               # ChatBot API
│   │   │   └── ...
│   │   │
│   │   └── utils/
│   │       ├── PasswordUtil.java                     # BCrypt hashing
│   │       ├── DateUtil.java
│   │       ├── ValidationUtil.java
│   │       └── ...
│   │
│   ├── src/main/resources/
│   │   ├── 📄 application.properties                 # Server config
│   │   ├── 📄 logback.xml                            # Logging config
│   │   └── database/
│   │       └── 📄 init_db.sql                        # MySQL schema
│   │
│   └── src/test/java/                                [Unit Tests]
│
│
├── 📦 AUCTION-CLIENT MODULE
│   ├── 📄 pom.xml
│   ├── src/main/java/com/auction/client/
│   │   ├── app/
│   │   │   └── Launcher.java                         # JavaFX entry point
│   │   │
│   │   ├── core/
│   │   │   ├── Application.java                      # App main class
│   │   │   ├── SceneManager.java                     # Scene navigation
│   │   │   ├── StageManager.java                     # Window management
│   │   │   └── ...
│   │   │
│   │   ├── feature/
│   │   │   ├── auth/
│   │   │   │   ├── LoginController.java              # Login screen
│   │   │   │   ├── RegisterController.java           # Register screen
│   │   │   │   ├── ForgotPasswordController.java     # Password recovery
│   │   │   │   └── ...
│   │   │   │
│   │   │   ├── lobby/
│   │   │   │   ├── LobbyController.java              # Auction list
│   │   │   │   ├── LobbyViewModel.java               # Business logic
│   │   │   │   ├── AuctionListView.java              # List component
│   │   │   │   └── ...
│   │   │   │
│   │   │   ├── room/
│   │   │   │   ├── RoomController.java               # Auction room UI
│   │   │   │   ├── RoomViewModel.java                # Room logic
│   │   │   │   ├── BidPanelController.java           # Bidding UI
│   │   │   │   ├── ParticipantListController.java    # Participant list
│   │   │   │   └── ...
│   │   │   │
│   │   │   ├── controllers/
│   │   │   │   ├── DashboardController.java          # User dashboard
│   │   │   │   ├── ProfileController.java            # User profile
│   │   │   │   ├── AdminPanelController.java         # Admin panel
│   │   │   │   ├── SellerPanelController.java        # Seller panel
│   │   │   │   ├── BidderPanelController.java        # Bidder panel
│   │   │   │   └── ...
│   │   │   │
│   │   │   └── viewmodel/
│   │   │       ├── DashboardViewModel.java
│   │   │       ├── RoomViewModel.java
│   │   │       └── ...
│   │   │
│   │   ├── session/
│   │   │   ├── SessionManager.java                   # User session
│   │   │   ├── UserContext.java                      # Current user context
│   │   │   └── ...
│   │   │
│   │   ├── network/
│   │   │   ├── SocketClient.java                     # TCP client
│   │   │   ├── MessageHandler.java                   # Handle messages
│   │   │   ├── ConnectionManager.java                # Manage connection
│   │   │   └── ...
│   │   │
│   │   ├── service/
│   │   │   ├── AuthService.java                      # Auth API calls
│   │   │   ├── AuctionService.java                   # Auction API
│   │   │   ├── BidService.java                       # Bidding API
│   │   │   ├── UserService.java                      # User API
│   │   │   └── ...
│   │   │
│   │   ├── shared/
│   │   │   ├── Utils.java                            # Utility functions
│   │   │   ├── Constants.java                        # Constants
│   │   │   ├── Validators.java                       # Client validation
│   │   │   └── ...
│   │   │
│   │   └── AI/
│   │       ├── ChatBotUI.java                        # ChatBot integration
│   │       └── ...
│   │
│   ├── src/main/resources/
│   │   ├── 📄 logback.xml
│   │   ├── com/auction/client/feature/controllers/
│   │   │   ├── 📄 login.fxml                         # Login scene
│   │   │   ├── 📄 lobby.fxml                         # Lobby scene
│   │   │   ├── 📄 room.fxml                          # Auction room scene
│   │   │   ├── 📄 dashboard.fxml                     # Dashboard scene
│   │   │   ├── 📄 admin_panel.fxml                   # Admin panel
│   │   │   ├── 📄 seller_panel.fxml                  # Seller panel
│   │   │   ├── 📄 bidder_panel.fxml                  # Bidder panel
│   │   │   ├── 📄 profile.fxml                       # Profile scene
│   │   │   ├── 📄 bid_history.fxml                   # Bid history
│   │   │   └── ...
│   │   │
│   │   ├── com/auction/client/feature/css/
│   │   │   ├── 📄 style.css                          # Global styles
│   │   │   ├── 📄 login.css                          # Login styles
│   │   │   ├── 📄 lobby.css                          # Lobby styles
│   │   │   ├── 📄 room.css                           # Room styles
│   │   │   └── ...
│   │   │
│   │   ├── com/auction/client/feature/images/
│   │   │   ├── icons/                                # UI icons
│   │   │   ├── logos/                                # App logos
│   │   │   ├── backgrounds/                          # Background images
│   │   │   └── ...
│   │   │
│   │   └── com/auction/client/AI/
│   │       └── chatbot_resources/
│   │
│   └── src/test/java/                                [Unit Tests]
│
│
├── 📁 Auction_AI/
│   ├── ChatBot/
│   │   ├── 🐍 chatbot.py                             # Main ChatBot
│   │   ├── 🐍 train_model.py                         # Training script
│   │   ├── 📦 model.pkl                              # Trained model
│   │   ├── 📄 intents.json                           # Intent definitions
│   │   ├── 📄 training_data.json                     # Training data
│   │   ├── 📄 requirements.txt                       # Python dependencies
│   │   └── 📄 README.md
│   │
│   └── AutoApprove/
│       ├── 🐍 train_logistic_regression.py           # Training script
│       ├── 🐍 model_tester.py                        # GUI tester
│       ├── 🐍 predict_from_input.py                  # Batch prediction
│       ├── 📦 auction_model.pkl                      # Trained ML model
│       ├── 📊 Training_Data.csv                      # Training dataset
│       ├── 📄 input_data.json                        # Test input
│       ├── 📄 requirements.txt                       # Python dependencies
│       └── 📄 README.md
│
│
├── 📁 target/                                        [Build Output - Maven]
│   ├── auction-common-1.0-SNAPSHOT.jar
│   ├── auction-server-1.0-SNAPSHOT.jar
│   ├── auction-client-1.0-SNAPSHOT.jar
│   ├── classes/
│   ├── lib/                                          # Dependencies
│   └── ...
│
│
├── 📊 DOCUMENTATION FILES
│   ├── 📄 AuctionSystem_AllClasses_StarUML_Final.mdj # Class diagram
│   ├── 📄 AuctionSystem_Presentation_StarUML.mdj     # Presentation diagram
│   ├── 📄 system-overview.puml                       # System overview
│   ├── 📄 server-core.puml                           # Server architecture
│   ├── 📄 client-mvc.puml                            # Client MVC
│   ├── 📄 service-layer.puml                         # Service layer
│   ├── 📄 model-dto-layer.puml                       # Model/DTO layer
│   ├── 📄 auction_system_functions.json              # Function definitions
│   └── 📄 Open_AllClasses_StarUML.cmd                # Open diagram tool
│
│
├── 🛠 BUILD & CONFIG FILES
│   ├── 📄 mvnw, mvnw.cmd                             # Maven wrapper
│   ├── 📄 .gitignore
│   ├── 📄 .gitmodules
│   ├── 📄 qodana.yaml                                # Code quality config
│   └── 📄 pom.xml                                    # Main pom.xml
```

---

## Vị trí file JAR

Các file JAR nằm trong thư mục:
release/
├── auction-server.jar
├── auction-client.jar
└── client.properties

## Cấu hình kết nối client

File `client.properties` dùng để cấu hình địa chỉ server.

Chạy local:

server.host=localhost
server.port=8080

Chạy client từ máy khác cùng mạng LAN:

server.host=<IPv4 của máy chạy server>
server.port=8080

## 🚀 Hướng Dẫn Chạy Ứng Dụng

### Bước 1: Tạo & Import Database

```bash
# Tạo database
mysql -u root -p -e "CREATE DATABASE auction_system_v2;"

# Import schema
mysql -u root -p auction_system_v2 < auction-server/src/main/resources/database/init_db.sql
```

### Bước 2: Cấu Hình Database (application.properties)

**Tạo file**: `auction-server/src/main/resources/application.properties`

**Nội dung mẫu**:
```properties
# Database Configuration
db.url=jdbc:mysql://localhost:3306/auction_system_v2
db.user=root
db.password=your_mysql_password
db.driver=com.mysql.cj.jdbc.Driver

# Connection Pool
db.pool.maxSize=10
db.pool.minIdle=5

# Server Configuration
server.port=5000
server.host=localhost
```

### Bước 3: Cài Đặt Python ChatBot (Tuỳ Chọn)

```bash
# Navigate to ChatBot directory
cd Auction_AI/ChatBot

# Install dependencies
pip install -r requirements.txt
```

*Lưu ý: ChatBot sẽ tự train lại model nếu file cũ hoặc không tương thích*

### Bước 4: Build All Modules

```bash
mvn clean package
```

### Bước 5: Chạy Server ⭐ (CHẠY TRƯỚC)

**Từ JAR**
```bash
java -jar target/auction-server.jar
```

**Output mong đợi:**
```
[Server] Listening on port 8080...
[Database] Connection established successfully
[Server] Ready to accept connections
```

### Bước 6: Chạy Client ⭐ (CHẠY SAU)

**⚠️ QUAN TRỌNG: Chờ Server khởi động xong rồi mới chạy Client!**

**Từ JAR**
```bash
java -jar target/auction-client.jar
```

### Bước 7: Đăng Nhập & Sử Dụng

Sau khi Client khởi động, bạn sẽ thấy giao diện Login.

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
- 📖 **Báo cáo chi tiết**: [DATABASE_SCHEMA_AND_DAO_GUIDE.md](./DATABASE_SCHEMA_AND_DAO_GUIDE.md)
- 📚 **PlantUML Diagrams**: 
  - [system-overview.puml](./system-overview.puml)
  - [server-core.puml](./server-core.puml)
  - [client-mvc.puml](./client-mvc.puml)
- 📊 **StarUML Diagrams**: [AuctionSystem_AllClasses_StarUML_Final.mdj](./AuctionSystem_AllClasses_StarUML_Final.mdj)

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
- Kiểm tra file application.properties có cấu hình đúng
- Kiểm tra có tạo database "auction_system_v2" chưa
- Kiểm tra user/password MySQL đúng
- Kiểm tra JDBC driver trong pom.xml
```

## 11.5. Port server

Server lắng nghe port `8080`. Client lấy địa chỉ kết nối từ file `client.properties` đặt cạnh JAR khi chạy:

```properties
server.host=localhost //Có thể thay đổi để kết nối máy chủ
server.port=8080
```

Nếu file không tồn tại hoặc cấu hình không hợp lệ, client dùng mặc định `localhost:8080`.

## 11.6. Database configuration

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
- **Architecture**: Multi-module Maven (auction-common, auction-server, auction-client)

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
