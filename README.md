Auction-System-OOD/
├── src/main/java/com/auction/          # Gốc của toàn bộ mã nguồn
│   ├── client.controllers/             # Điều hướng giao diện (Frontend)
│   │   ├── AuctionController.java      # Xử lý màn hình đấu giá chính
│   │   └── SellerController.java       # Xử lý màn hình dành cho người bán
│   │
│   ├── common/                         # Các lớp dùng chung cho Client & Server
│   │   ├── dto/                        # Đối tượng truyền tải dữ liệu
│   │   │   └── Message.java            # Gói tin gửi qua Socket
│   │   └── model/                      # Các thực thể OOP (Entities)
│   │       ├── Admin.java              # Vai trò quản trị hệ thống [cite: 37, 115]
│   │       ├── Art.java                # Loại sản phẩm nghệ thuật (Kế thừa Item) [cite: 113, 120]
│   │       ├── AuctionRoom.java        # Quản lý trạng thái phiên (OPEN/RUNNING...) [cite: 55]
│   │       ├── Bidder.java             # Vai trò người tham gia đấu giá [cite: 35, 115]
│   │       ├── BidTransaction.java     # Lưu vết lịch sử đặt giá [cite: 117, 177]
│   │       ├── Entity.java (Interface) # Lớp cơ sở cho toàn bộ đối tượng [cite: 111]
│   │       ├── Item.java (Abstract)    # Lớp cha của các loại sản phẩm [cite: 112, 120]
│   │       ├── Seller.java             # Vai trò người đăng bán [cite: 36, 115]
│   │       └── User.java               # Lớp cha chứa ID (BD5xxxxx) và Pass [cite: 114, 153]
│   │
│   └── server/                         # Logic xử lý tại máy chủ (Backend)
│       ├── dao/                        # Data Access Object - Thao tác Database
│       │   ├── BidDAO.java             # Lưu/lấy dữ liệu đặt giá
│       │   ├── UserDAO.java            # Quản lý tài khoản (BD5xxxxx)
│       │   └── MockDB.java             # Database giả lập để test nhanh
│       ├── main/                       # Khởi chạy hệ thống Server
│       │   ├── AuctionServer.java      # Server Socket chính
│       │   └── Main.java               # Điểm entry của ứng dụng
│       ├── service/                    # Xử lý nghiệp vụ (Business Logic)
│       │   ├── AuctionRoomService.java # Điều phối logic các phòng đấu giá
│       │   ├── AuctionService.java     # Kiểm tra tính hợp lệ của giá Bid
│       │   ├── AuthService.java        # Xử lý Đăng nhập/Đăng ký
│       │   └── ClientConnection.java   # Quản lý luồng kết nối
│       ├── utils/                      # Các công cụ hỗ trợ
│       │   ├── DatabaseConnection.java # Kết nối tới database_v5.sql
│       │   └── PasswordUtil.java       # Mã hóa mật khẩu bảo mật
│       └── server/                     # (Lưu ý) File xử lý kết nối
│           └── ClientHandler.java      # Xử lý request từ Client riêng biệt
│
├── src/main/resources/                 # Tài nguyên phi mã nguồn
│   └── com.auction.client.views/       # File thiết kế giao diện FXML
│       └── auction-view.fxml           # Giao diện đấu giá thời gian thực [cite: 67]
│
├── database_v5.sql                     # Script tạo bảng Database
├── pom.xml                             # Quản lý thư viện Maven
└── README.md                           # Tài liệu hướng dẫn này
