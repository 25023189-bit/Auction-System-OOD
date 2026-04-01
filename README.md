Auction-System-OOD/
├── src/main/java/com/auction/
│   ├── client/                 # Chứa toàn bộ mã nguồn phía người dùng
│   │   ├── controllers/        # Xử lý logic giao diện (Action listeners, UI logic) [cite: 128]
│   │   │   ├── AuctionController.java
│   │   │   └── SellerController.java
│   │   └── views/              # (Tùy chọn) Các lớp bổ trợ hiển thị
│   │
│   ├── server/                 # Chứa logic xử lý trung tâm và Database [cite: 129, 130]
│   │   ├── dao/                # Data Access Object - Lớp tương tác trực tiếp SQL [cite: 129]
│   │   │   ├── UserDAO.java    # Xử lý Đăng ký/Đăng nhập (BD5xxxxx) [cite: 152, 171]
│   │   │   ├── BidDAO.java     # Lưu trữ lịch sử đặt giá 
│   │   │   └── MockDB.java     # Dữ liệu mẫu để test nhanh
│   │   ├── service/            # Business Logic - Xử lý "luật chơi" [cite: 129]
│   │   │   ├── AuctionService.java  # Điều phối phiên đấu giá (OPEN -> FINISHED) [cite: 55]
│   │   │   ├── AuthService.java     # Kiểm tra quyền Bidder/Seller [cite: 152, 173]
│   │   │   └── ClientConnection.java # Quản lý kết nối Socket [cite: 126]
│   │   ├── utils/              # Tiện ích hệ thống
│   │   │   ├── DatabaseConnection.java # Kết nối JDBC tới database_v5.sql [cite: 60]
│   │   │   └── PasswordUtil.java       # Mã hóa mật khẩu người dùng
│   │   └── main/               # Điểm khởi chạy Server
│   │       └── AuctionServer.java
│   │
│   └── common/                 # Các lớp dùng chung cho cả Client và Server
│       ├── dto/                # Data Transfer Object - Gói tin gửi qua mạng [cite: 126, 173]
│       │   └── Message.java    # Cấu trúc thông điệp trao đổi giữa Client-Server
│       └── model/              # Các thực thể (Entities) theo thiết kế OOP [cite: 107, 109]
│           ├── Entity.java     # Interface/Abstract class cơ sở [cite: 111]
│           ├── User.java       # Lớp cha cho Bidder, Seller, Admin [cite: 114, 115]
│           ├── Item.java       # Lớp cha cho các loại sản phẩm (Art,...) [cite: 112, 113]
│           ├── AuctionRoom.java# Quản lý trạng thái phiên [cite: 116]
│           └── BidTransaction.java # Chi tiết một lần đặt giá [cite: 117]
│
├── src/main/resources/         # Tài nguyên hệ thống [cite: 128]
│   └── com/auction/client/views/
│       └── auction-view.fxml   # Giao diện thiết kế bằng Scene Builder [cite: 62, 63]
│
├── database_v5.sql             # File kịch bản tạo bảng và dữ liệu mẫu [cite: 175, 177]
├── pom.xml                     # Cấu hình thư viện Maven (JavaFX, MySQL Driver, JUnit) [cite: 133]
└── README.md                   # Hướng dẫn này
