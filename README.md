# Auction System

Đây là project hệ thống đấu giá realtime sử dụng JavaFX cho client, Socket cho server và MySQL cho database.

---

# 1. Giới thiệu

Project mô phỏng một hệ thống đấu giá với các chức năng chính:

- Đăng ký, đăng nhập
- Tạo phiên đấu giá cho seller
- Tham gia đấu giá cho bidder
- Đặt giá realtime
- Tự động gia hạn phiên ở thời điểm cuối
- Tự động chốt phiên và xử lý tiền
- Tự động đóng phòng và đưa người dùng về sảnh khi phiên kết thúc

---

# 2. Công nghệ sử dụng

- JavaFX
- Java Socket
- MySQL
- Maven

---

# 3. Cách chạy project

## 3.1. Yêu cầu

- JDK 17 trở lên để chạy runtime JavaFX hiện tại
- MySQL 8 trở lên
- IntelliJ IDEA hoặc IDE tương đương

## 3.2. Tạo database

Chạy câu lệnh sau trong MySQL:

```sql
CREATE DATABASE auction_system;
```

## 3.3. Import database

Chạy file:

```text
src/main/resources/database/init_db.sql
```

## 3.4. Cấu hình kết nối database

Tạo file:

```text
src/main/resources/database.properties
```

Ví dụ:

```properties
db.url=jdbc:mysql://localhost:3306/auction_system
db.user=root
db.password=
db.driver=com.mysql.cj.jdbc.Driver
```

## 3.5. Chạy chương trình

### Chạy server

Mở lớp:

```text
com.auction.server.main.AuctionServer
```

và chạy.

### Chạy client

Mở lớp:

```text
com.auction.client.app.Launcher
```

và chạy.

---

# 4. Tài khoản mẫu

| Role | Username | Password |
| ---- | -------- | -------- |
| Seller | hephaestus | 123 |
| Bidder | cerberus | 123 |
| Admin | hades | 123 |

---

# 5. Nghiệp vụ chính

## 5.1. Đăng nhập

- Người dùng đăng nhập bằng `username`
- Hệ thống quản lý người dùng theo `customer_id`

## 5.2. Trạng thái phiên đấu giá

| Trạng thái | Ý nghĩa |
| ---------- | ------- |
| OPEN | Phiên đã tạo nhưng chưa bắt đầu |
| RUNNING | Phiên đang diễn ra |
| SOLD | Phiên kết thúc và bán thành công |
| UNSOLD | Phiên kết thúc nhưng không có người mua |
| CLOSED_BY_SELLER | Seller chủ động đóng phiên |
| CANCELED_BY_ADMIN | Admin hủy phiên |

## 5.3. Quy tắc tiền

| Trường hợp | Kết quả |
| ---------- | ------- |
| Seller đóng phiên | Không chuyển tiền |
| Admin hủy phiên | Không chuyển tiền |
| Hết giờ và có người thắng | Trừ tiền bidder thắng, cộng tiền cho seller |
| Hết giờ nhưng không có ai bid | Không chuyển tiền |

## 5.4. Quy tắc tham gia phòng

- Trước khi phiên bắt đầu, chỉ seller của phiên đó được vào phòng
- Bidder không được vào phòng trước giờ bắt đầu
- Bidder phải có số dư lớn hơn hoặc bằng mức tiền tối thiểu để tham gia phiên

## 5.5. Quy tắc tạo phiên

- Seller nhập giá khởi điểm
- Seller nhập bước giá
- Seller nhập số tiền tối thiểu để tham gia phiên
- Số tiền tối thiểu phải lớn hơn 10 phần trăm giá khởi điểm
- Số tiền tối thiểu phải nhỏ hơn 75 phần trăm giá khởi điểm
- Việc kiểm tra dữ liệu tạo phiên được gom vào lớp `AuctionCreationValidator`

## 5.6. Anti-sniping

- Trong khoảng thời gian cuối của phiên, hệ thống chặn người mới tham gia
- Nếu có bid hợp lệ ở giai đoạn cuối, phiên sẽ được gia hạn
- Số lần gia hạn không bị giới hạn nếu vẫn có bid hợp lệ

## 5.7. Tự động đóng phiên

- Khi hết thời gian, hệ thống tự động chốt phiên
- Hệ thống vẫn lưu dữ liệu phiên trong database
- Toàn bộ seller và bidder đang ở trong phiên được đưa trở lại sảnh
- Phiên biến mất khỏi danh sách đang hoạt động trên UI

---

# 6. Các action chính giữa client và server

| Action | Mô tả |
| ------ | ----- |
| ROOM_LIST | Danh sách phòng đấu giá |
| ROOM_JOINED | Tham gia phòng thành công |
| BID_SUCCESS | Đặt giá thành công |
| UPDATE_PRICE | Cập nhật giá theo thời gian thực |
| UPDATE_BALANCE | Cập nhật số dư tài khoản |
| AUCTION_CLOSED_NOTIFY | Thông báo phiên đã đóng |

---

# 7. Cấu trúc database

## 7.1. Bảng `users`

- `customer_id`
- `username`
- `password_hash`
- `role`
- `balance`
- `organization`

Ghi chú:

- `organization` chỉ có giá trị khi user là seller
- bidder và admin để `null`

## 7.2. Bảng `items`

- `item_id`
- `name`
- `description`
- `current_price`

## 7.3. Bảng `auctions`

- `auction_id`
- `item_id`
- `seller_id`
- `status`
- `start_time`
- `duration_minutes`
- `actual_end_time`
- `extension_seconds`
- `starting_price`
- `bid_step`
- `minimum_join_amount`

## 7.4. Bảng `bid_transactions`

- `transaction_id`
- `auction_id`
- `bidder_id`
- `bid_amount`
- `bid_time`

---

# 8. Sơ đồ thư mục

```text
AuctionSystem/
├── .github/
├── .idea/
├── .mvn/
├── Auction-System-OOD/
├── Auction_AI/
├── src/
│   └── main/
│       ├── java/
│       │   └── com/
│       │       └── auction/
│       │           ├── client/
│       │           │   ├── app/
│       │           │   │   └── launcher/
│       │           │   ├── core/
│       │           │   │   ├── mvvm/
│       │           │   │   ├── navigation/
│       │           │   │   └── ui/
│       │           │   ├── feature/
│       │           │   │   ├── auth/
│       │           │   │   ├── controllers/
│       │           │   │   ├── lobby/
│       │           │   │   ├── room/
│       │           │   │   └── viewmodel/
│       │           │   ├── network/
│       │           │   │   ├── dispatcher/
│       │           │   │   ├── messaging/
│       │           │   │   ├── protocol/
│       │           │   │   └── socket/
│       │           │   ├── session/
│       │           │   └── shared/
│       │           │       ├── mapper/
│       │           │       └── support/
│       │           ├── common/
│       │           │   ├── dto/
│       │           │   ├── model/
│       │           │   └── role/
│       │           └── server/
│       │               ├── dao/
│       │               ├── main/
│       │               ├── service/
│       │               └── utils/
│       └── resources/
│           ├── com/
│           │   └── example/
│           │       └── auctionprototype/
│           ├── database/
│           └── Image/
└── target/
```

---

# 9. Chức năng từng thư mục

- `.github/`: Chứa cấu hình GitHub như workflow hoặc thiết lập phục vụ quản lý mã nguồn.
- `.idea/`: Chứa cấu hình dự án của IntelliJ IDEA.
- `.mvn/`: Chứa Maven Wrapper để chạy project mà không cần cài Maven riêng.
- `Auction-System-OOD/`: Chứa tài liệu hoặc phần thiết kế hướng đối tượng của hệ thống.
- `Auction_AI/`: Chứa tài liệu hoặc phần mở rộng liên quan đến AI nếu có sử dụng.
- `src/`: Thư mục chính chứa mã nguồn và tài nguyên của ứng dụng.
- `src/main/`: Chứa mã nguồn chính dùng khi chạy chương trình.
- `src/main/java/`: Chứa toàn bộ source code Java.
- `src/main/java/com/auction/`: Namespace gốc của dự án.

- `client/`: Chứa toàn bộ phần xử lý phía client.
- `client/app/`: Chứa điểm khởi chạy và phần khởi tạo ứng dụng client.
- `client/app/launcher/`: Chứa các lớp chuyên mở các màn hình giao diện.
- `client/core/`: Chứa hạ tầng lõi của client.
- `client/core/mvvm/`: Hỗ trợ tổ chức mã theo mô hình MVVM.
- `client/core/navigation/`: Quản lý điều hướng giữa các màn hình.
- `client/core/ui/`: Chứa thành phần giao diện dùng chung.
- `client/feature/`: Chứa các chức năng nghiệp vụ phía client.
- `client/feature/auth/`: Xử lý đăng ký, đăng nhập, kiểm tra form xác thực.
- `client/feature/controllers/`: Chứa controller JavaFX để liên kết FXML với logic xử lý.
- `client/feature/lobby/`: Xử lý logic sảnh chờ và danh sách phiên đấu giá.
- `client/feature/room/`: Xử lý logic trong phòng đấu giá như vào phòng, đặt giá, đóng phiên.
- `client/feature/viewmodel/`: Chứa ViewModel trung gian giữa UI và logic nghiệp vụ.
- `client/network/`: Chứa toàn bộ logic giao tiếp mạng giữa client và server.
- `client/network/dispatcher/`: Điều phối và phân loại message từ server.
- `client/network/messaging/`: Hỗ trợ xử lý dữ liệu thông điệp.
- `client/network/protocol/`: Định nghĩa action, format message và giao thức trao đổi.
- `client/network/socket/`: Quản lý kết nối socket và luồng gửi nhận dữ liệu.
- `client/session/`: Lưu trạng thái người dùng hiện tại trên client.
- `client/shared/`: Chứa các lớp dùng chung phía client.
- `client/shared/mapper/`: Chuyển đổi dữ liệu giữa DTO, model và dữ liệu hiển thị.
- `client/shared/support/`: Chứa các tiện ích hỗ trợ khác cho client.

- `common/`: Chứa các thành phần dùng chung giữa client và server.
- `common/dto/`: Chứa các đối tượng truyền dữ liệu giữa các tầng.
- `common/model/`: Chứa các model nghiệp vụ chính của hệ thống.
- `common/role/`: Chứa enum hoặc lớp quản lý vai trò người dùng.

- `server/`: Chứa toàn bộ phần xử lý phía server.
- `server/dao/`: Chứa lớp truy xuất dữ liệu tới database.
- `server/main/`: Chứa lớp khởi động server.
- `server/service/`: Chứa logic nghiệp vụ chính như tạo phiên, kiểm tra điều kiện, xử lý đấu giá và chốt phiên.
- `server/utils/`: Chứa các lớp tiện ích hỗ trợ phía server.

- `src/main/resources/`: Chứa tài nguyên không phải mã Java.
- `resources/com/example/auctionprototype/`: Chứa file FXML, CSS và tài nguyên giao diện JavaFX.
- `resources/database/`: Chứa file SQL khởi tạo database.
- `resources/Image/`: Chứa hình ảnh phục vụ giao diện.
- `target/`: Chứa các file sinh ra sau khi build bằng Maven.

---

# 10. Các chỗ có thể chỉnh sửa

## 10.1. Thời gian phiên

```java
duration_minutes
```

## 10.2. Thời gian gia hạn

```java
extension_seconds
```

## 10.3. Cửa sổ anti-sniping

```java
FINAL_WINDOW_SECONDS = 30
```

## 10.4. Số dư ban đầu

```java
1000000
```

---

# 11. Lỗi thường gặp

## 11.1. Không kết nối được database

- MySQL chưa chạy
- Sai tài khoản hoặc mật khẩu database
- Chưa tạo file `database.properties`

## 11.2. Không đăng nhập được

- Chưa import database
- Dữ liệu user chưa tồn tại

## 11.3. Không thấy room

- Chưa chạy server
- Room đã kết thúc hoặc bị đóng

## 11.4. Giá không cập nhật realtime

- Lỗi handler phía client
- Message từ server chưa được xử lý đúng

## 11.5. Số dư không thay đổi

- Chưa broadcast `UPDATE_BALANCE`
- Phiên chưa được chốt hợp lệ

---

# 12. Lưu ý

- Phải chạy server trước rồi mới chạy client
- Không nên sửa trực tiếp database khi hệ thống đang chạy
- Các thay đổi logic nghiệp vụ nên tập trung ở:
  - `AuctionRoomService`
  - `ClientHandler`
  - `AuctionDAO`
  - `BidDAO`
  - `AuctionCreationValidator`

# 13.Tham khảo ý tưởng, thiết kế:
- https://www.bpiauctions.com/
- https://www.namecheap.com/
- https://dgts.moj.gov.vn/
- https://daugiaviet.vn/home

- https://www.facebook.com/
- https://www.tiktok.com/
- https://www.instagram.com/
- https://www.amazon.com/

- https://www.ebay.com/
- https://www.catawiki.com/en
- https://www.dealdash.com/

# 14. Phân công công việc:
- Nguyễn Hữu Hùng:
  + Đăng nhập, Đăng ký
  + AI
  + Tạo phiên
  + Biến động số dư, Đặt giá, Kết thúc phiên, Chatting
  + Phân quyền Admin
- Tô Bảo Hân:
  + Lập trình mạng & Xử lý Server
  + Xử lý đa luồng (Multithreading)
  + Quản trị trạng thái hệ thống
  + Tính năng bổ sung
  + Quản lý mã nguồn
- Bùi Thế Dũng:
  + Database (wallet, sản phẩm, Vòng đời phiên đấu giá,lịch sử đặt giá,thông báo, thanh toán, lịch sử đặt giá, báo cáo)
  + Kết nối database
  + Kiểm thử và sửa lỗi nếu xuất hiện hoặc chưa tối ưu
  + Chức năng lấy lại mật khẩu bằng email
- Đặng Đức Anh:
  + Login view (background, login card)
  + Bidder,Seller,Admin,Lobbies, chatbot (background, giao diện của các nút và chức năng)



