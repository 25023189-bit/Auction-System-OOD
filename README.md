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

- JDK 25 để biên dịch và chạy JavaFX 25
- MySQL 8 trở lên
- IntelliJ IDEA hoặc IDE tương đương

## 3.2. Tạo database

Chạy câu lệnh sau trong MySQL:

```sql
CREATE DATABASE auction_system_v2;
```

## 3.3. Import database

Chạy file:

```text
src/main/resources/database/init_db.sql
```

## 3.4. Cấu hình kết nối database

Tạo file:

```text
src/main/resources/db.properties
```

Ví dụ:

```properties
db.url=jdbc:mysql://localhost:3306/auction_system_v2
db.user=root
db.password=
db.driver=com.mysql.cj.jdbc.Driver
```

## 3.5. Chạy chương trình

### Cài đặt Python ChatBot

ChatBot Python cần dùng đúng các version dependency đã pin để tránh lỗi model `LogisticRegression` khác version giữa các máy:

```powershell
python -m pip install -r Auction_AI\ChatBot\requirements.txt
```

Nếu file model bị cũ hoặc không tương thích, ChatBot sẽ tự train lại từ dữ liệu trong `Auction_AI\ChatBot\Logist\data\generated` khi khởi động.

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
├── Auction_AI/
├── tools/
├── src/
│   └── main/
│       ├── java/com/auction/
│       │   ├── client/
│       │   │   ├── app/
│       │   │   │   └── launcher/
│       │   │   ├── chatbot/
│       │   │   ├── core/
│       │   │   │   ├── mvvm/
│       │   │   │   ├── navigation/
│       │   │   │   └── ui/
│       │   │   ├── feature/
│       │   │   │   ├── auth/
│       │   │   │   ├── controllers/
│       │   │   │   ├── lobby/
│       │   │   │   ├── room/
│       │   │   │   └── viewmodel/
│       │   │   ├── network/
│       │   │   │   ├── dispatcher/
│       │   │   │   ├── messaging/
│       │   │   │   ├── protocol/
│       │   │   │   └── socket/
│       │   │   ├── service/
│       │   │   ├── session/
│       │   │   └── shared/
│       │   │       ├── mapper/
│       │   │       └── support/
│       │   ├── common/
│       │   │   ├── dto/
│       │   │   ├── model/
│       │   │   └── role/
│       │   └── server/
│       │       ├── config/
│       │       ├── dao/
│       │       ├── handler/
│       │       ├── main/
│       │       ├── service/
│       │       └── utils/
│       └── resources/
│           ├── application.properties
│           ├── db.properties
│           ├── db.properties.example
│           ├── logback.xml
│           ├── database/
│           │   └── init_db.sql
│           └── com/example/auctionprototype/
│               ├── css/
│               ├── fxml/
│               └── images/
├── *.puml, *.mdj
├── pom.xml
├── mvnw, mvnw.cmd
└── target/
```

---

# 9. Chức năng từng thư mục

- `.github/`: Chứa cấu hình GitHub như workflow hoặc thiết lập phục vụ quản lý mã nguồn.
- `.idea/`: Chứa cấu hình dự án của IntelliJ IDEA.
- `.mvn/`: Chứa Maven Wrapper để chạy project mà không cần cài Maven riêng.
- `Auction_AI/`: Chứa phần AI/chatbot Python dùng để hỗ trợ trả lời trong giao diện client.
- `tools/`: Chứa công cụ phụ trợ cục bộ, hiện có bộ StarUML phục vụ mở/chỉnh sửa sơ đồ.
- `*.puml`, `*.mdj`: Chứa sơ đồ PlantUML và StarUML của hệ thống.
- `pom.xml`: Cấu hình Maven, dependency, plugin và thông tin build project.
- `mvnw`, `mvnw.cmd`: Maven Wrapper để chạy Maven thống nhất trên các máy.
- `src/`: Thư mục chính chứa mã nguồn và tài nguyên của ứng dụng.
- `src/main/`: Chứa mã nguồn chính dùng khi chạy chương trình.
- `src/main/java/`: Chứa toàn bộ source code Java.
- `src/main/java/com/auction/`: Namespace gốc của dự án.

- `client/`: Chứa toàn bộ phần xử lý phía client.
- `client/app/`: Chứa entry point JavaFX của client như `Main` và `Launcher`.
- `client/app/launcher/`: Mở các cửa sổ phụ như Seller Dashboard và Admin Dashboard.
- `client/chatbot/`: Controller chatbot, fallback message và cầu nối sang Python chatbot.
- `client/core/`: Chứa các abstraction lõi của client.
- `client/core/mvvm/`: Thư mục giữ chỗ cho tổ chức MVVM nếu mở rộng thêm.
- `client/core/navigation/`: Điều hướng giữa login, lobby, phòng đấu giá và dashboard.
- `client/core/ui/`: Interface chung cho presenter và binder UI.
- `client/feature/`: Chứa các chức năng nghiệp vụ phía client.
- `client/feature/auth/`: Xử lý form đăng nhập, đăng ký, quên mật khẩu, validator và response xác thực.
- `client/feature/controllers/`: Chứa controller JavaFX để liên kết FXML với logic xử lý.
- `client/feature/lobby/`: Render danh sách phòng, card đấu giá và thông tin user ở lobby.
- `client/feature/room/`: Xử lý UI phòng đấu giá như join, bid, chat, timer và đóng phiên.
- `client/feature/viewmodel/`: Chứa model hiển thị cho auth và lobby.
- `client/network/`: Chứa toàn bộ logic giao tiếp mạng giữa client và server.
- `client/network/dispatcher/`: Thư mục giữ chỗ cho tầng dispatch nếu tách thêm luồng mạng.
- `client/network/messaging/`: Router và fallback handler xử lý `Message` server trả về.
- `client/network/protocol/`: Thư mục giữ chỗ cho hằng số action/protocol nếu chuẩn hóa thêm.
- `client/network/socket/`: Quản lý socket client, gửi request và lắng nghe response server.
- `client/service/`: API phía client đóng gói các request gửi lên server.
- `client/session/`: Lưu trạng thái người dùng hiện tại trên client.
- `client/shared/`: Chứa các lớp dùng chung phía client.
- `client/shared/mapper/`: Chuyển đổi dữ liệu giữa DTO, model và dữ liệu hiển thị.
- `client/shared/support/`: Chứa các tiện ích hỗ trợ khác cho client.

- `common/`: Chứa các thành phần dùng chung giữa client và server.
- `common/dto/`: Chứa các đối tượng truyền dữ liệu giữa các tầng.
- `common/model/`: Chứa các model nghiệp vụ chính của hệ thống.
- `common/role/`: Chứa enum hoặc lớp quản lý vai trò người dùng.

- `server/`: Chứa toàn bộ phần xử lý phía server.
- `server/ClientHandler.java`: Xử lý một kết nối socket client, đọc message và gửi response.
- `server/config/`: Đọc cấu hình ứng dụng như email, SMTP và rate limit.
- `server/dao/`: Chứa lớp truy xuất dữ liệu tới database.
- `server/handler/`: Router và handler xử lý từng nhóm action từ client như auth, room, seller, admin.
- `server/main/`: Chứa lớp khởi động server.
- `server/service/`: Chứa logic nghiệp vụ chính như xác thực, tạo phiên, đấu giá, chốt phiên, email và reset mật khẩu.
- `server/utils/`: Chứa các lớp tiện ích hỗ trợ phía server.

- `src/main/resources/`: Chứa tài nguyên không phải mã Java.
- `resources/application.properties`: Cấu hình chung của ứng dụng.
- `resources/db.properties`: Cấu hình kết nối database đang dùng khi chạy.
- `resources/db.properties.example`: File mẫu cho cấu hình database.
- `resources/logback.xml`: Cấu hình logging.
- `resources/com/example/auctionprototype/css/`: Chứa CSS cho các màn hình JavaFX.
- `resources/com/example/auctionprototype/fxml/`: Chứa FXML của login, lobby, phòng đấu giá, admin, seller và product view.
- `resources/com/example/auctionprototype/images/`: Chứa hình ảnh phục vụ giao diện JavaFX.
- `resources/database/`: Chứa file SQL khởi tạo database.
- `target/`: Chứa các file sinh ra sau khi build bằng Maven.

---

# 10. Tài liệu chi tiết từng lớp Java

## 10.1. Lớp Model (com.auction.common.model)

### User.java
**Mục đích**: Đại diện cho người dùng trong hệ thống (Bidder, Seller, Admin).

**Chức năng chính**:
- Lưu trữ thông tin tài khoản: `customer_id`, `username`, `password_hash`, `role`, `balance`, `organization`
- `role` có 3 giá trị: BIDDER (người mua), SELLER (người bán), ADMIN (quản trị viên)
- `organization` chỉ có ý nghĩa khi `role = SELLER`
- `balance` là số tiền hiện tại của người dùng trong tài khoản
- Hỗ trợ getter/setter cho tất cả các thuộc tính

**Luồng sử dụng**:
1. UserDAO tạo User từ dữ liệu database
2. AuthService kiểm tra User trong quá trình login
3. AuctionRoomService validate User có quyền join room hay bid hay không

### AuctionRoom.java
**Mục đích**: Đại diện cho một phiên đấu giá realtime.

**Chức năng chính**:
- Lưu trữ thông tin phiên: `auctionId`, `itemName`, `sellerName`, `currentPrice`, `highestBidder`
- `status`: trạng thái phiên (OPEN, RUNNING, SOLD, UNSOLD, CLOSED_BY_SELLER, CANCELED_BY_ADMIN)
- `startTime`, `endTime`, `duration_minutes`: thời gian và thời lượng phiên
- `starting_price`, `bid_step`: giá khởi điểm và bước giá
- `minimum_join_amount`: số tiền tối thiểu để join phiên
- `extensionSeconds`: số giây mỗi lần gia hạn khi có bid trong 30 giây cuối
- `entryLocked`: cờ cho biết liệu có thể join thêm người mới không
- `scheduledEndTime`: thời gian kết thúc thực tế (tính cả phần extend)
- `participantCount`: số người đang tham gia

**Luồng sử dụng**:
1. AuctionDAO query từ database rồi map thành AuctionRoom
2. AuctionRoomService kiểm tra điều kiện join room hay bid
3. Dữ liệu được trả về client để hiển thị UI

### Item.java & Art.java
**Mục đích**: Đại diện cho sản phẩm được đấu giá.

**Chức năng chính**:
- Item: lớp cha chứa thông tin chung của sản phẩm (id, name, description, startingPrice)
- Art: lớp con của Item, thêm field `artistName` cho sản phẩm nghệ thuật
- Mỗi sản phẩm có 1 lần được đấu giá duy nhất

**Luồng sử dụng**:
1. ItemDAO tạo Item từ database
2. Seller chọn Item khi tạo phiên đấu giá
3. AuctionRoom chứa tham chiếu đến Item để hiển thị thông tin sản phẩm trên UI

### BidTransaction.java
**Mục đích**: Lưu lại từng lần bid của người dùng.

**Chức năng chính**:
- `transactionId`: mã giao dịch bid duy nhất
- `auctionId`: phiên đấu giá mà bid này thuộc về
- `bidderId`: người mua (Bidder) đã bid
- `bidAmount`: số tiền của bid
- `bidTime`: thời điểm bid được phát hành

**Luồng sử dụng**:
1. Khi Bidder đặt giá, BidDAO tạo BidTransaction mới
2. Lưu vào bảng `bid_transactions` của database
3. Được dùng để tính giá cao nhất, tìm người thắng cuộc sau khi phiên hết hạn

### PendingAuctionRequest.java
**Mục đích**: Đại diện yêu cầu tạo phiên của Seller chưa được duyệt bởi Admin.

**Chức năng chính**:
- Giữ tất cả thông tin từ form tạo phiên của Seller
- `requestId`: mã yêu cầu duy nhất
- `sellerId`, `sellerOrganization`: thông tin seller
- `itemName`, `itemDescription`: thông tin sản phẩm
- `startingPrice`, `bidStep`, `minimumJoinAmount`: các thông số giá
- `startTime`, `durationMinutes`, `extensionSeconds`: thông số thời gian
- `status`: PENDING, APPROVED, REJECTED
- Tạo timestamp để biết Seller yêu cầu lúc nào

**Luồng sử dụng**:
1. SellerActionHandler phân tích form tạo phiên từ client
2. Tạo PendingAuctionRequest và lưu vào hàng chờ
3. AdminActionHandler duyệt danh sách pending, nếu approve thì tạo AuctionRoom thực
4. Nếu reject thì xóa request khỏi hàng chờ

### Bidder.java, Seller.java, Admin.java
**Mục đích**: Lớp kế thừa từ User để đại diện cho từng loại vai trò cụ thể.

**Chức năng chính**:
- Bidder: đại diện người mua, có thể bid trong các phiên
- Seller: đại diện người bán, có thể tạo phiên mới
- Admin: đại diện quản trị viên, có thể duyệt phiên và quản lý hệ thống

---

## 10.2. Lớp DAO (com.auction.server.dao)

DAO (Data Access Object) chịu trách nhiệm truy cập database. Mỗi DAO xử lý một bảng hoặc một tập hợp thao tác liên quan.

### UserDAO.java
**Mục đích**: Quản lý tất cả thao tác đọc, ghi user từ/vào database.

**Chức năng chính**:

| Method | Mô tả |
|--------|-------|
| `getUserById(customerId)` | Lấy User theo customer_id (được normalize thành chữ hoa) |
| `getUserByUsername(username)` | Lấy User theo username |
| `login(loginIdentifier, rawPassword)` | Xác thực người dùng, so sánh mật khẩu bằng BCrypt |
| `registerUser(user, rawPassword)` | Đăng ký tài khoản mới, check trùng trước, hash password trước khi lưu |
| `resetPassword(customerId, newPassword, confirmPassword)` | Reset mật khẩu cũ (yêu cầu nhập confirm) |
| `resetPasswordWithNewPassword(username, newPassword)` | Reset mật khẩu từ quên mật khẩu (không yêu cầu confirm) |
| `getAllUsers()` | Lấy danh sách tất cả user |
| `deleteUser(customerId)` | Xóa user khỏi hệ thống |
| `generateNextCustomerId()` | Tự sinh mã bidder tiếp theo (BD500001, BD500002, ...) |

**Business Rules**:
- Mật khẩu luôn được hash bằng BCrypt (cost 12) trước khi lưu, không bao giờ lưu mật khẩu thô
- `customer_id` được chuẩn hóa thành chữ hoa để tránh sai khác khi query
- `role` chỉ nhận 3 giá trị: BIDDER, SELLER, ADMIN
- `organization` chỉ có giá trị khi role = SELLER
- Hỗ trợ login bằng cả username hoặc customer_id

**Luồng sử dụng chính**:
1. AuthService gọi `login()` khi người dùng nhập username/password
2. ForgotPasswordService gọi `getUserByUsernameWithEmail()` để lấy email gửi token reset
3. AuctionRoomService gọi `getUserById()` để kiểm tra user có quyền bid/join không
4. AdminActionHandler gọi để quản lý user

### AuctionDAO.java
**Mục đích**: Quản lý tất cả thao tác đấu giá từ/vào database.

**Chức năng chính**:

| Method | Mô tả |
|--------|-------|
| `createAuction(auction)` | Tạo phiên đấu giá mới sau khi Admin duyệt |
| `getAuctionById(auctionId)` | Lấy thông tin phiên theo ID |
| `getAllActiveAuctions()` | Lấy danh sách tất cả phiên đang hoạt động (status = OPEN hoặc RUNNING) |
| `closeAuctionByTime(auctionId)` | Chốt phiên khi hết hạn: cập nhật status, tính tiền cho winner và seller |
| `closeAuctionBySellerRequest(auctionId)` | Seller chủ động đóng phiên: set status = CLOSED_BY_SELLER |
| `cancelAuctionByAdmin(auctionId, reason)` | Admin hủy phiên vì lý do nào đó |
| `getSellerStats(sellerId)` | Lấy thống kê hiệu quả của seller: số phiên, tỉ lệ thành công, tỉ lệ hủy |

**Business Rules**:
- Trạng thái phiên chỉ được thay đổi theo sơ đồ nhất định: OPEN → RUNNING → (SOLD/UNSOLD/CLOSED_BY_SELLER/CANCELED_BY_ADMIN)
- Khi phiên kết thúc, nếu có bidder thắng (có bid):
  - Trừ tiền từ tài khoản bidder (số tiền = bid cao nhất)
  - Cộng tiền vào tài khoản seller
  - Set status = SOLD
- Nếu không có bidder nào bid, set status = UNSOLD, không chuyển tiền
- Nếu seller đóng hoặc admin hủy, không chuyển tiền

**Luồng sử dụng chính**:
1. SellerActionHandler gọi `createAuction()` sau khi Admin duyệt
2. AuctionRoomService gọi `getAuctionById()` khi Bidder join room
3. AuctionRoomService gọi `closeAuctionByTime()` khi hết hạn
4. Watcher thread gọi `getAllActiveAuctions()` để lấy danh sách phiên cần check hết hạn

### BidDAO.java
**Mục đích**: Quản lý tất cả thao tác bid từ/vào database.

**Chức năng chính**:

| Method | Mô tả |
|--------|-------|
| `placeBid(auctionId, bidderId, amount)` | Đặt bid mới: xóa bid cũ của người này, insert bid mới, update current_price |
| `getHighestBid(auctionId)` | Lấy bid cao nhất của phiên |
| `getBidHistory(auctionId)` | Lấy lịch sử tất cả bid của phiên (để hiển thị trên UI) |
| `getBidsByBidder(bidderId)` | Lấy tất cả bid của một người (để kiểm tra lịch sử bid) |

**Business Rules**:
- Mỗi người trong một phiên chỉ có thể có 1 bid "cao nhất" đang active
- Khi Bidder đặt bid mới, bid cũ của người đó trong phiên này bị xóa
- Bid mới phải cao hơn current_price hoặc cao hơn bid cao nhất hiện tại

**Luồng sử dụng chính**:
1. AuctionRoomService gọi `placeBid()` khi Bidder đặt giá mới
2. AuctionDAO gọi `getHighestBid()` khi chốt phiên để xác định người thắng
3. Client gọi qua handler để lấy `getBidHistory()` hiển thị lịch sử bid

### ItemDAO.java
**Mục đích**: Quản lý thông tin sản phẩm.

**Chức năng chính**:

| Method | Mô tả |
|--------|-------|
| `createItem(item)` | Tạo sản phẩm mới |
| `getItemById(itemId)` | Lấy thông tin sản phẩm theo ID |
| `getAllItems()` | Lấy danh sách tất cả sản phẩm |

### TransactionDAO.java
**Mục đích**: Lưu lại các giao dịch tiền (payment transactions).

**Chức năng chính**:

| Method | Mô tả |
|--------|-------|
| `recordTransaction(transaction)` | Ghi lại giao dịch tiền (winner -balance, seller +balance) |
| `getTransactionsByUser(userId)` | Lấy lịch sử giao dịch của user |

---

## 10.3. Lớp Service (com.auction.server.service)

Service chứa logic nghiệp vụ phức tạp, điều phối giữa các DAO và xử lý các quy tắc business.

### AuctionRoomService.java
**Mục đích**: Điều phối logic phòng đấu giá realtime.

**Chức năng chính**:

| Method | Mô tả |
|--------|-------|
| `joinRoom(roomId, userId)` | Xử lý Bidder vào phòng: validate status, check balance, lock entry nếu < 30s cuối |
| `placeNewBid(roomId, userId, amount)` | Xử lý Bidder đặt giá: validate status, ghi bid, extend time nếu cần |
| `finalizeExpiredAuctionIfNeeded(roomId)` | Kiểm tra xem phiên đã hết hạn chưa, nếu có thì chốt phiên |

**Business Rules**:
- **Anti-sniping**: Trong 30 giây cuối, người mới không được join nhưng participant đã join vẫn được bid
- Khi có bid trong 30 giây cuối, phiên được gia hạn thêm `extensionSeconds`
- Bid hợp lệ phải có người trong participant list
- Bid hợp lệ phải cao hơn current_price

**Ghi chú kỹ thuật**:
- Thread-safe bằng cách synchronized trên `AuctionRuntimeState` của từng phòng
- Tránh race condition giữa join/bid/finalize

**Luồng sử dụng chính**:
1. ClientHandler gọi `joinRoom()` khi Bidder click "Join"
2. ClientHandler gọi `placeNewBid()` khi Bidder click "Bid"
3. Watcher thread gọi `finalizeExpiredAuctionIfNeeded()` để chốt phiên hết hạn

### AuctionCreationValidator.java
**Mục đích**: Validate form tạo phiên của Seller trước khi đưa vào hàng chờ Admin duyệt.

**Chức năng chính**:

| Method | Mô tả |
|--------|-------|
| `validateAuction(...)` | Kiểm tra tất cả điều kiện trong form tạo phiên |
| `getErrorMessage()` | Trả lại thông báo lỗi chi tiết nếu validate fail |

**Các điều kiện validate**:
1. `sellerId` không được rỗng
2. `sellerOrganization` không được rỗng (Seller phải có tên tổ chức)
3. `itemName` không được rỗng
4. `itemDesc` không được rỗng
5. `startingPrice` > 0
6. `bidStep` > 0
7. `minimumJoinAmount` < 75% của `startingPrice` (để tránh minimum quá cao)
8. `minimumJoinAmount` > 10% của `startingPrice` (để tránh minimum quá thấp)
9. `durationMinutes` > 0
10. `extensionSeconds` nằm trong khoảng 1-120 giây
11. `startTime` phải hiện tại hoặc trong tương lai (không được trong quá khứ)
12. `sellerReputation` nằm trong 0-5
13. `successfulAuctionRate` nằm trong 0-1
14. `adminCancellationRate` nằm trong 0-1

**Luồng sử dụng chính**:
1. SellerActionHandler nhận form từ client
2. Gọi `validateAuction()` để kiểm tra
3. Nếu fail, trả `getErrorMessage()` về client
4. Nếu pass, tạo `PendingAuctionRequest` đưa vào hàng chờ Admin duyệt

### AuctionRuntimeState.java
**Mục đích**: Lưu trạng thái phòng đấu giá trong runtime (bộ nhớ).

**Chức năng chính**:

| Method | Mô tả |
|--------|-------|
| `addParticipant(userId)` | Thêm người vào danh sách participant |
| `hasParticipant(userId)` | Kiểm tra người có trong participant list không |
| `getParticipants()` | Lấy danh sách tất cả participant |
| `lockEntry(now)` | Khóa entry khi vào 30 giây cuối |
| `isEntryLocked()` | Kiểm tra entry có bị khóa không |
| `extendBySeconds(seconds)` | Gia hạn phiên thêm n giây |
| `getTotalExtendedSeconds()` | Lấy tổng số giây đã được gia hạn |

**Luồng sử dụng chính**:
1. Khi Bidder join room, gọi `addParticipant()`
2. Khi entry bị khóa (< 30s cuối), set `isEntryLocked = true`
3. Khi Bidder bid trong 30s cuối, gọi `extendBySeconds()`

### AuctionStateManager.java
**Mục đích**: Quản lý `AuctionRuntimeState` của tất cả phòng đấu giá hiện tại.

**Chức năng chính**:

| Method | Mô tả |
|--------|-------|
| `getState(roomId)` | Lấy runtime state của một phòng, nếu chưa có thì tạo mới |
| `removeState(roomId)` | Xóa runtime state khi phòng kết thúc |

### AuthService.java
**Mục đích**: Xử lý logic xác thực người dùng.

**Chức năng chính**:

| Method | Mô tả |
|--------|-------|
| `authenticate(username, password)` | Kiểm tra username/password hợp lệ rồi trả User |
| `register(user, password)` | Đăng ký tài khoản mới |

**Luồng sử dụng chính**:
1. Client gửi username + password
2. AuthService gọi `UserDAO.login()` để verify
3. Trả User nếu hợp lệ, null nếu sai

### EmailService.java
**Mục đích**: Gửi email (dùng cho quên mật khẩu, notification v.v.).

**Chức năng chính**:

| Method | Mô tả |
|--------|-------|
| `sendPasswordResetEmail(email, token)` | Gửi email reset password với token |
| `sendNotificationEmail(email, subject, message)` | Gửi email notification |

**Luồng sử dụng chính**:
1. ForgotPasswordService tạo token reset
2. Gọi `sendPasswordResetEmail()` để gửi link reset về email
3. User nhấn link và reset password

### ForgotPasswordService.java
**Mục đích**: Xử lý luồng quên mật khẩu.

**Chức năng chính**:

| Method | Mô tả |
|--------|-------|
| `initiatePasswordReset(username)` | Tạo token reset, gửi email |
| `resetPasswordWithToken(token, newPassword)` | Verify token rồi reset password |

### PasswordResetTokenGenerator.java
**Mục đích**: Tạo và manage token reset password.

**Chức năng chính**:

| Method | Mô tả |
|--------|-------|
| `generateToken(userId)` | Tạo token reset mới, lưu vào map với expiration time |
| `validateToken(token)` | Kiểm tra token còn hợp lệ không (chưa hết hạn) |
| `getTokenData(token)` | Lấy dữ liệu token (userId, etc.) |

### PasswordStrengthValidator.java
**Mục đích**: Validate độ mạnh của mật khẩu.

**Chức năng chính**:

| Method | Mô tả |
|--------|-------|
| `validatePassword(password)` | Kiểm tra password đạt tiêu chuẩn độ mạnh |
| `getErrorMessage()` | Trả lại lý do password yếu |

**Tiêu chuẩn mật khẩu mạnh**:
- Tối thiểu 8 ký tự
- Phải có chữ hoa, chữ thường, số, ký tự đặc biệt

### ClientConnection.java
**Mục đích**: Đại diện cho một kết nối socket của client tới server.

**Chức năng chính**:
- Lưu trữ socket, input/output stream
- Gửi/nhận message từ client
- Liên kết với userId của client
- Callback khi client disconnect

**Luồng sử dụng chính**:
1. ClientHandler tạo ClientConnection khi client kết nối
2. Lưu vào map `connectedClients` trong AuctionServer
3. Khi broadcast, AuctionServer gửi message qua `ClientConnection.send()`

### AuditLogger.java
**Mục đích**: Ghi lại các hành động quan trọng của hệ thống (audit log).

**Chức năng chính**:

| Method | Mô tả |
|--------|-------|
| `logUserAction(userId, action, details)` | Ghi lại action của user (login, create auction, etc.) |
| `logAuctionChange(auctionId, oldStatus, newStatus)` | Ghi lại thay đổi status của phiên |
| `logBalanceChange(userId, oldBalance, newBalance, reason)` | Ghi lại thay đổi balance của user |

### PendingAuctionApprovalService.java
**Mục đích**: Quản lý hàng chờ phiên đấu giá chờ Admin duyệt.

**Chức năng chính**:

| Method | Mô tả |
|--------|-------|
| `addPendingRequest(request)` | Thêm yêu cầu tạo phiên vào hàng chờ |
| `getPendingRequests()` | Lấy danh sách tất cả yêu cầu chưa duyệt |
| `approvePending(requestId)` | Duyệt yêu cầu, tạo AuctionRoom thực |
| `rejectPending(requestId, reason)` | Từ chối yêu cầu |

### ProductDetailService.java
**Mục đích**: Lấy thông tin chi tiết sản phẩm trong phiên đấu giá.

**Chức năng chính**:

| Method | Mô tả |
|--------|-------|
| `getProductDetailForAuction(auctionId)` | Lấy thông tin chi tiết item của phiên |

### RateLimiter.java
**Mục đích**: Giới hạn tốc độ request từ client để tránh spam/attack.

**Chức năng chính**:

| Method | Mô tả |
|--------|-------|
| `isAllowed(clientId)` | Kiểm tra client có được phép gửi request không |
| `recordRequest(clientId)` | Ghi lại request từ client |

**Business Rules**:
- Mỗi client chỉ được phép N request trong M phút
- Nếu vượt quá, reject request có thông báo "Too many requests"

---

## 10.4. Lớp Utility (com.auction.server.utils)

### DatabaseConnection.java
**Mục đích**: Factory để tạo JDBC connection tới MySQL database.

**Chức năng chính**:

| Method | Mô tả |
|--------|-------|
| `getConnection()` | Lấy connection mới từ pool (hoặc tạo mới) |

**Cấu hình ưu tiên**:
1. Environment variable: `AUCTION_DB_URL`, `AUCTION_DB_USER`, `AUCTION_DB_PASSWORD`, `AUCTION_DB_DRIVER`
2. System property: `-Dauction.db.url`, `-Dauction.db.user`, etc.
3. File resource: `db.properties` hoặc `db.properties.example`
4. Default: `com.mysql.cj.jdbc.Driver`

**Luồng sử dụng chính**:
1. DAO gọi `DatabaseConnection.getConnection()` mỗi khi cần query
2. Connection được return và close sau khi xong (try-with-resources)

### PasswordUtil.java
**Mục đích**: Hash và verify mật khẩu bằng BCrypt.

**Chức năng chính**:

| Method | Mô tả |
|--------|-------|
| `hashPassword(plainTextPassword)` | Hash mật khẩu với salt, cost 12 |
| `checkPassword(plainTextPassword, hashedPassword)` | So sánh mật khẩu thô với hash |

**Luồng sử dụng chính**:
1. UserDAO gọi `hashPassword()` khi đăng ký hoặc reset password
2. UserDAO gọi `checkPassword()` khi login để verify password

---

## 10.5. Main Entry Point

### AuctionServer.java
**Mục đích**: Điểm khởi động server, quản lý socket listener và broadcast message.

**Chức năng chính**:
- Tạo ServerSocket lắng nghe port (default 5000)
- Chấp nhận kết nối từ client, tạo ClientHandler cho mỗi client
- Quản lý danh sách connected clients
- Broadcast message tới tất cả client (ROOM_LIST, UPDATE_BALANCE, etc.)
- Chạy Watcher thread để check phiên hết hạn

**Luồng sử dụng chính**:
1. Run AuctionServer.java
2. Lắng nghe port 5000
3. Khi client connect, tạo thread ClientHandler mới
4. Mỗi lần có thay đổi (bid success, room closed, etc.) gọi `broadcast()`

### Main.java (Client)
**Mục đích**: Điểm khởi động client, load login UI.

**Chức năng chính**:
- Load FXML login-view.fxml từ resource
- Tạo primaryStage với size tối thiểu 1000x600
- Maximize cửa sổ

---

# 11. Các chỗ có thể chỉnh sửa

## 11.1. Thời gian phiên

```java
// Trong AuctionRoom hoặc form tạo phiên
int durationMinutes = 10; // thay đổi số phút ở đây
```

## 11.2. Thời gian gia hạn

```java
// Trong AuctionRoomService
int extensionSeconds = 30; // mỗi lần có bid trong 30s cuối
```

## 11.3. Cửa sổ anti-sniping

```java
// Trong AuctionRoomService.java
private static final long FINAL_WINDOW_SECONDS = 30L; // thay đổi ở đây
```

## 11.4. Số dư ban đầu

```java
// Trong UserDAO.registerUser() hoặc init_db.sql
double initialBalance = 1000000; // thay đổi số tiền ban đầu cho bidder
```

## 11.5. Port server

```java
// Trong AuctionServer.java
int PORT = 5000; // thay đổi port ở đây
```

## 11.6. Database configuration

```properties
# Trong src/main/resources/db.properties
db.url=jdbc:mysql://localhost:3306/auction_system_v2
db.user=root
db.password=
db.driver=com.mysql.cj.jdbc.Driver
```

---

# 12. Lỗi thường gặp

## 12.1. Không kết nối được database

- MySQL chưa chạy
- Sai tài khoản hoặc mật khẩu database
- Chưa tạo file `db.properties`

## 12.2. Không đăng nhập được

- Chưa import database
- Dữ liệu user chưa tồn tại

## 12.3. Không thấy room

- Chưa chạy server
- Room đã kết thúc hoặc bị đóng

## 12.4. Giá không cập nhật realtime

- Lỗi handler phía client
- Message từ server chưa được xử lý đúng

## 12.5. Số dư không thay đổi

- Chưa broadcast `UPDATE_BALANCE`
- Phiên chưa được chốt hợp lệ

---

# 13. Lưu ý

- Phải chạy server trước rồi mới chạy client
- Không nên sửa trực tiếp database khi hệ thống đang chạy
- Các thay đổi logic nghiệp vụ nên tập trung ở:
  - `AuctionRoomService`
  - `ClientHandler`
  - `AuctionDAO`
  - `BidDAO`
  - `AuctionCreationValidator`

# 14. Tham khảo ý tưởng, thiết kế:
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

# 15. Phân công công việc:

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
  + Database (wallet, sản phẩm, Vòng đời phiên đấu giá, lịch sử đặt giá, thông báo, thanh toán, lịch sử đặt giá, báo cáo)
  + Kết nối database
  + Kiểm thử và sửa lỗi nếu xuất hiện hoặc chưa tối ưu
  + Chức năng lấy lại mật khẩu bằng email
- Đặng Đức Anh:
  + Login view (background, login card)
  + Bidder, Seller, Admin, Lobbies, chatbot (background, giao diện của các nút và chức năng)
