# 🔨 Hệ Thống Đấu Giá Trực Tuyến (Online Auction System)

**Bài tập lớn môn Lập trình nâng cao (LTNC) - Học kỳ II, 2025-2026**

Dự án xây dựng một hệ thống đấu giá trực tuyến theo kiến trúc Client-Server, sử dụng ngôn ngữ Java, giao diện JavaFX và mô hình MVC. Dự án hỗ trợ đấu giá thời gian thực (Real-time), xử lý tranh chấp đồng thời (Concurrency) và các tính năng nâng cao như Auto-Bidding, Anti-sniping.

---

## 🛠 Công nghệ sử dụng
- **Ngôn ngữ:** Java 17+
- **Giao diện (Client):** JavaFX, FXML
- **Kiến trúc:** Client - Server (Socket Networking)
- **Mô hình thiết kế:** MVC (Model-View-Controller), DAO Pattern, Singleton, Factory, Observer
- **Cơ sở dữ liệu:** MySQL 8.0+
- **Quản lý dự án/Build:** Maven 
- **Kiểm thử & CI/CD:** JUnit, GitHub Actions

---

## 📂 Cấu trúc thư mục dự án (Project Structure)
Anh em chú ý cấu trúc thư mục dưới đây để biết mình cần làm việc ở file nào, thư mục nào nhé:

```text
Auction-System-OOD/
├── src/main/java/com/auction/
│   ├── client.controllers/             # Frontend: Điều hướng giao diện
│   │   ├── AuctionController.java      # Màn hình đấu giá chính
│   │   └── SellerController.java       # Màn hình người bán
│   │
│   ├── common/                         # Dùng chung Client & Server
│   │   ├── dto/                        # Data Transfer Objects
│   │   │   └── Message.java            # Gói tin Socket
│   │   └── model/                      # Các thực thể OOP
│   │       ├── Admin.java              #
│   │       ├── Art.java                #
│   │       ├── AuctionRoom.java        #
│   │       ├── Bidder.java             #
│   │       ├── BidTransaction.java     #
│   │       ├── Entity.java (Interface) #
│   │       ├── Item.java (Abstract)    #
│   │       ├── Seller.java             #
│   │       └── User.java               # Chứa ID BD5xxxxx
│   │
│   └── server/                         # Backend: Xử lý tại máy chủ
│       ├── dao/                        # Thao tác Database
│       │   ├── BidDAO.java             #
│       │   ├── UserDAO.java            #
│       │   └── MockDB.java             #
│       ├── main/                       # Khởi chạy Server
│       │   ├── AuctionServer.java      #
│       │   └── Main.java               #
│       ├── service/                    # Logic nghiệp vụ
│       │   ├── AuctionRoomService.java #
│       │   ├── AuctionService.java     #
│       │   ├── AuthService.java        #
│       │   └── ClientConnection.java   #
│       ├── utils/                      # Tiện ích
│       │   ├── DatabaseConnection.java #
│       │   └── PasswordUtil.java       #
│       └── server/                     
│           └── ClientHandler.java      # Xử lý luồng Client
├── src/main/resources/                 # Resources
│   └── com.auction.client.views/       
│       └── auction-view.fxml           # Giao diện FXML
├── database_v5.sql                     # Script SQL
├── pom.xml                             # Maven config
└── README.md                           # Tài liệu này
