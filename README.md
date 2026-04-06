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
