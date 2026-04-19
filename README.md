# Auction System

Đây là project hệ thống đấu giá realtime sử dụng JavaFX (client), Socket (server) và MySQL (database).

---

# 📌 1. Giới thiệu

Project mô phỏng một hệ thống đấu giá với các chức năng:

* Đăng ký / đăng nhập
* Tạo phiên đấu giá (seller)
* Tham gia đấu giá (bidder)
* Đặt giá realtime
* Tự động gia hạn phiên (anti-sniping)
* Tự động chốt phiên và xử lý tiền

---

# ⚙️ 2. Cách chạy project

## 2.1. Yêu cầu

* Java JDK 17+
* MySQL 8+
* IDE (khuyên dùng IntelliJ)

---

## 2.2. Tạo database

Mở MySQL và chạy:

```sql
CREATE DATABASE auction_system;
```

---

## 2.3. Import database

Chạy file:

```bash
init_db.sql
```

---

## 2.4. Cấu hình kết nối DB

Tạo file:

```bash
src/main/resources/database.properties
```

Nội dung ví dụ:

```properties
db.url=jdbc:mysql://localhost:3306/auction_systemdb.user=root
db.user=root
db.password=
db.driver=com.mysql.cj.jdbc.Driver
```

👉 Nhớ sửa `user` và `password` cho đúng máy.

---

## 2.5. Chạy chương trình

### Bước 1: chạy server

Mở file:

```
AuctionServer.java
```

→ Run

### Bước 2: chạy client

Mở file:

```
Main.java
```

→ Run

---

# 👤 3. Tài khoản mẫu (nếu có)

| Role   | Username   | Password |
| ------ | ---------- | -------- |
| Seller | hephaestus | 123      |
| Bidder | cerberus   | 123      |
| Admin  | hades      | 123      |

---

# 🧠 4. Nghiệp vụ chính

## 🔐 Đăng nhập

* Đăng nhập bằng **username**
* `customer_id` được hệ thống tự sinh (BD5xxxxx)

---

## 🏷️ Trạng thái phiên đấu giá

| Trạng thái        | Ý nghĩa            |
| ----------------- | ------------------ |
| OPEN / RUNNING    | đang hoạt động     |
| SOLD              | bán thành công     |
| UNSOLD            | không có người mua |
| CLOSED_BY_SELLER  | seller đóng        |
| CANCELED_BY_ADMIN | admin hủy          |

---

## 💸 Quy tắc chuyển tiền

| Trường hợp             | Kết quả                                |
| ---------------------- | -------------------------------------- |
| Seller đóng            | ❌ không chuyển tiền                    |
| Admin hủy              | ❌ không chuyển tiền                    |
| Hết giờ + có người bid | ✅ bidder bị trừ tiền, seller được cộng |
| Hết giờ + không ai bid | ❌ không chuyển tiền                    |

---

## ⏱️ Anti-sniping (30 giây cuối)

* Trong 30 giây cuối:

    * ❌ không cho người mới vào
* Nếu có người đặt giá:

    * ⏱️ phiên được cộng thêm thời gian
* Không giới hạn số lần gia hạn

---

# 📡 5. Các action (message)

Một số action quan trọng giữa client-server:

| Action                | Mô tả                   |
| --------------------- | ----------------------- |
| ROOM_LIST             | danh sách phòng         |
| ROOM_JOINED           | vào phòng               |
| BID_SUCCESS           | đặt giá thành công      |
| UPDATE_PRICE          | cập nhật giá ngoài sảnh |
| UPDATE_BALANCE        | cập nhật số dư          |
| AUCTION_CLOSED_NOTIFY | thông báo đóng phiên    |

---

# 💾 6. Database

## users

* customer_id
* username
* password_hash
* role
* balance

## items

* item_id
* name
* description
* current_price

## auctions

* auction_id
* item_id
* seller_id
* status
* start_time
* duration_minutes
* actual_end_time
* extension_seconds

## bid_transactions

* transaction_id
* auction_id
* bidder_id
* bid_amount
* bid_time

---

# 🔧 7. Các chỗ có thể chỉnh sửa

## Thời gian phiên

```java
duration_minutes
```

## Thời gian gia hạn

```java
extension_seconds
```

## 30 giây cuối

```java
FINAL_WINDOW_SECONDS = 30
```

## Số dư ban đầu

```java
1000000
```

---

# 🐞 8. Lỗi thường gặp

## Không connect được DB

* MySQL chưa chạy
* sai password

## Không login được

* chưa import database

## Không thấy room

* chưa chạy server

## Giá không cập nhật

* lỗi handler hoặc chưa nhận message

## Số dư không đổi

* chưa broadcast UPDATE_BALANCE

---

# 📌 9. Lưu ý

* Phải chạy **server trước client**
* Không sửa trực tiếp DB khi đang chạy
* Các thay đổi logic nên sửa ở:

    * `AuctionRoomService`
    * `ClientHandler`
    * `AuctionDAO`

---

