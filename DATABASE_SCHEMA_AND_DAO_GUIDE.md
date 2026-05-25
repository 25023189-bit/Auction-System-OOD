# Hướng Dẫn Hoàn Thành DAO Layer - BidDAO và AuctionDAO

## Mục lục
1. [Lược đồ cơ sở dữ liệu](#lược-đồ-cơ-sở-dữ-liệu)
2. [Các câu lệnh SQL](#các-câu-lệnh-sql)
3. [BidDAO - Lịch sử đấu giá](#bindao---lịch-sử-đấu-giá)
4. [AuctionDAO - Quản lý phiên đấu giá](#auctiondao---quản-lý-phiên-đấu-giá)
5. [Xử lý Transaction An Toàn](#xử-lý-transaction-an-toàn)
6. [Tránh Lost Update & Concurrent Bidding](#tránh-lost-update--concurrent-bidding)
7. [Test Cases & Ví dụ](#test-cases--ví-dụ)

---

## Lược đồ cơ sở dữ liệu

### Bảng: `bid_transactions` (Lịch sử đấu giá)
```sql
CREATE TABLE bid_transactions (
    transaction_id INT AUTO_INCREMENT PRIMARY KEY,
    auction_id VARCHAR(20) NOT NULL,
    bidder_id VARCHAR(20) NOT NULL,
    bid_amount DECIMAL(15, 2) NOT NULL,
    bid_rank INT DEFAULT 0,
    is_highest TINYINT DEFAULT 0,
    bid_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (auction_id) REFERENCES auctions(auction_id),
    FOREIGN KEY (bidder_id) REFERENCES users(customer_id),
    INDEX idx_auction_id (auction_id),
    INDEX idx_bidder_id (bidder_id),
    INDEX idx_is_highest (is_highest),
    INDEX idx_bid_time (bid_time)
);
```

### Bảng: `auctions` (Phiên đấu giá)
```sql
CREATE TABLE auctions (
    auction_id VARCHAR(20) PRIMARY KEY,
    product_id INT NOT NULL,
    created_by VARCHAR(20) NOT NULL,
    status ENUM('OPEN', 'RUNNING', 'FINISHED', 'PAID', 'CANCELED') DEFAULT 'OPEN',
    start_time TIMESTAMP,
    end_time TIMESTAMP,
    actual_end_time TIMESTAMP,
    min_bid_increment DECIMAL(15, 2) DEFAULT 1.0,
    winner_id VARCHAR(20),
    final_price DECIMAL(15, 2),
    
    FOREIGN KEY (product_id) REFERENCES products(product_id),
    FOREIGN KEY (created_by) REFERENCES users(customer_id),
    FOREIGN KEY (winner_id) REFERENCES users(customer_id),
    INDEX idx_status (status),
    INDEX idx_start_time (start_time),
    INDEX idx_created_by (created_by)
);
```

### Bảng: `products` (Sản phẩm)
```sql
CREATE TABLE products (
    product_id INT AUTO_INCREMENT PRIMARY KEY,
    product_name VARCHAR(255) NOT NULL,
    description TEXT,
    starting_price DECIMAL(15, 2) NOT NULL,
    current_price DECIMAL(15, 2) NOT NULL,
    seller_id VARCHAR(20),
    product_type VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (seller_id) REFERENCES users(customer_id),
    INDEX idx_seller_id (seller_id)
);
```

---

## Các câu lệnh SQL

### 1. INSERT - Thêm một lượt đặt giá mới

```sql
INSERT INTO bid_transactions 
(auction_id, bidder_id, bid_amount, bid_rank, is_highest, bid_time)
VALUES (?, ?, ?, (
    SELECT COALESCE(MAX(bid_rank), 0) + 1 
    FROM bid_transactions 
    WHERE auction_id = ?
), 1, NOW());
```

**Giải thích:**
- `bid_rank` tự động tính bằng MAX(bid_rank) + 1 để đảm bảo thứ tự đặt giá
- `is_highest = 1` đánh dấu đây là lượt đặt giá cao nhất hiện tại
- `bid_time` lưu thời gian server, không dùng client time để tránh giả mạo

### 2. SELECT - Lấy lịch sử đấu giá

```sql
-- Lấy toàn bộ lịch sử bid của một phiên (sắp xếp theo giá cao nhất trước)
SELECT transaction_id, auction_id, bidder_id, bid_amount, bid_rank, is_highest, bid_time
FROM bid_transactions
WHERE auction_id = ?
ORDER BY bid_amount DESC, bid_time ASC;

-- Lấy lượt đặt giá cao nhất hiện tại
SELECT bidder_id, bid_amount, bid_time
FROM bid_transactions
WHERE auction_id = ? AND is_highest = 1
LIMIT 1;

-- Lấy N lượt đặt giá gần nhất của một người dùng
SELECT transaction_id, auction_id, bidder_id, bid_amount, bid_time
FROM bid_transactions
WHERE bidder_id = ? AND auction_id = ?
ORDER BY bid_time DESC
LIMIT 10;
```

### 3. UPDATE - Cập nhật trạng thái highest

```sql
-- Xóa cờ highest cũ (chỉ một bid có highest = 1 tại một thời điểm)
UPDATE bid_transactions
SET is_highest = 0
WHERE auction_id = ? AND is_highest = 1;

-- Cập nhật giá hiện tại của sản phẩm sau khi có bid mới
UPDATE products
SET current_price = ?
WHERE product_id = ?;
```

### 4. SELECT - Lấy thông tin phiên đấu giá

```sql
-- Lấy đầy đủ thông tin phiên (gom từ auctions và products)
SELECT 
    a.auction_id, 
    a.product_id, 
    a.created_by AS seller_id, 
    a.status,
    a.start_time, 
    a.end_time, 
    a.actual_end_time, 
    a.min_bid_increment,
    a.winner_id,
    a.final_price,
    p.product_name, 
    p.description, 
    p.current_price, 
    p.starting_price
FROM auctions a
JOIN products p ON a.product_id = p.product_id
WHERE a.auction_id = ?;

-- Lấy danh sách phiên đang chạy hoặc sắp bắt đầu
SELECT a.auction_id, a.product_id, a.created_by, a.status,
       a.start_time, a.end_time, a.min_bid_increment,
       p.product_name, p.current_price
FROM auctions a
JOIN products p ON a.product_id = p.product_id
WHERE a.status IN ('OPEN', 'RUNNING')
ORDER BY a.start_time ASC;
```

### 5. Transaction - Chốt phiên đấu giá (Atomicity)

```sql
-- Bắt đầu transaction
START TRANSACTION;

-- Khóa bảng auctions để tránh chốt đồng thời
SELECT auction_id, created_by, status
FROM auctions
WHERE auction_id = ? 
FOR UPDATE;

-- Tìm lượt đặt giá cao nhất
SELECT bidder_id, bid_amount
FROM bid_transactions
WHERE auction_id = ? AND is_highest = 1
ORDER BY bid_amount DESC
LIMIT 1;

-- Cập nhật trạng thái phiên
UPDATE auctions
SET status = 'PAID', 
    winner_id = ?, 
    final_price = ?,
    actual_end_time = NOW(3)
WHERE auction_id = ?;

-- Trừ tiền từ người thắng
UPDATE users
SET balance = balance - ?
WHERE customer_id = ? AND balance >= ?;

-- Cộng tiền cho seller
UPDATE users
SET balance = balance + ?
WHERE customer_id = ?;

-- Commit nếu mọi câu lệnh thành công, ngược lại ROLLBACK
COMMIT;
```

---

## BidDAO - Lịch sử đấu giá

File hiện tại đã có sẵn tại: `src/main/java/com/auction/server/dao/BidDAO.java`

**Chức năng chính:**
- `placeBid(auctionId, bidderId, bidAmount)` - Đặt giá mới với transaction lock
- `BidResult` - DTO chứa kết quả (success, status, message)
- `BidStatus` - Enum định nghĩa trạng thái (SUCCESS, BID_TOO_LOW, INSUFFICIENT_BALANCE, etc.)

**Điểm an toàn:**
- Sử dụng `FOR UPDATE` để khóa phiên đấu giá khi kiểm tra giá
- Transaction với `setAutoCommit(false)` để atomicity
- Validate giá trước khi insert (bid phải >= current_price + bid_step)
- Validate số dư trước khi insert

---

## AuctionDAO - Quản lý phiên đấu giá

File hiện tại đã có sẵn tại: `src/main/java/com/auction/server/dao/AuctionDAO.java`

**Chức năng chính:**
- `createAuctionWithItem(room, item, sellerId)` - Tạo phiên + sản phẩm (transaction)
- `getAuctionById(roomId)` - Lấy chi tiết phiên
- `getAllActiveAuctions()` - Lấy danh sách phiên đang chạy
- `closeAuctionByTime(roomId)` - Chốt phiên hết hạn với xử lý thanh toán

**Điểm an toàn:**
- `createAuctionWithItem()` dùng transaction để insert products + auctions atomically
- `closeAuctionByTime()` dùng `FOR UPDATE` + transaction để chốt phiên an toàn
- Xử lý rollback nếu có lỗi ở bất kỳ bước nào

---

## Xử lý Transaction An Toàn

### 1. Preventive Locking (Khóa trước)

```java
// BidDAO.placeBid() sử dụng FOR UPDATE để khóa phiên
String selectAuctionSql = """
    SELECT a.product_id, p.current_price, u.balance, a.min_bid_increment
    FROM auctions a
    JOIN products p ON a.product_id = p.product_id
    JOIN users u ON u.customer_id = ?
    WHERE a.auction_id = ?
    FOR UPDATE  // <-- Khóa auctions cho đến khi transaction kết thúc
    """;
```

**Hiệu ứng:**
- Khi User A đang đặt giá, User B không thể đọc giá cũ để validate
- Đảm bảo tất cả thao tác của User A hoàn thành trước User B

### 2. Atomicity (Nguyên tử hóa)

```java
try (Connection conn = DatabaseConnection.getConnection()) {
    conn.setAutoCommit(false);  // Tắt auto-commit
    
    // Thực hiện các thao tác
    pstmt1.executeUpdate();
    pstmt2.executeUpdate();
    pstmt3.executeUpdate();
    
    conn.commit();  // Commit tất cả hoặc không commit gì
} catch (SQLException e) {
    // Nếu có lỗi, rollback tự động
}
```

### 3. Isolation Level (Mức cô lập)

```java
// MySQL mặc định: REPEATABLE READ
// Đủ để tránh dirty read, non-repeatable read, phantom read
// Với FOR UPDATE, tránh bất kỳ race condition nào
```

---

## Tránh Lost Update & Concurrent Bidding

### Kịch bản: Hai người đặt giá cùng lúc

#### ❌ Sai (No Transaction)
```
T1: User A đọc current_price = 100
T2: User B đọc current_price = 100
T3: User A đặt 150 → ghi current_price = 150
T4: User B đặt 120 → ghi current_price = 120 (LOST UPDATE!)
Kết quả: current_price = 120 (sai, phải là 150)
```

#### ✅ Đúng (BidDAO.placeBid with FOR UPDATE)
```
T1: User A lock auctions → đọc current_price = 100
T2: User B cố lock auctions → CHỜ (blocked)
T3: User A validate 150 > 100 + bidStep ✓ → insert bid → update current_price = 150 → commit
T4: Lock được release, User B khóa auctions → đọc current_price = 150 (mới)
T5: User B validate 120 > 150 + bidStep ✗ → BID_TOO_LOW
```

### Code Demo: Concurrent Bidding Safety

```java
public BidResult placeBid(String auctionId, String bidderId, double bidAmount) {
    String selectAuctionSql = """
            SELECT a.product_id, p.current_price, u.balance, a.min_bid_increment
            FROM auctions a
            JOIN products p ON a.product_id = p.product_id
            JOIN users u ON u.customer_id = ?
            WHERE a.auction_id = ?
            FOR UPDATE  // ← Khóa tới khi commit/rollback
            """;
    
    try (Connection conn = DatabaseConnection.getConnection()) {
        conn.setAutoCommit(false);  // ← Bắt đầu transaction
        
        // Đọc với khóa
        double currentPrice = readCurrentPrice(conn, selectAuctionSql, bidderId, auctionId);
        
        // Validate
        if (bidAmount < currentPrice + bidStep) {
            conn.rollback();
            return BidResult.fail(BidStatus.BID_TOO_LOW, "...");
        }
        
        // Ghi bid mới
        insertBid(conn, auctionId, bidderId, bidAmount);
        
        // Cập nhật current_price
        updateItemPrice(conn, productId, bidAmount);
        
        conn.commit();  // ← Unlock và commit
        return BidResult.success();
    } catch (SQLException e) {
        // Connection tự động rollback khi close nếu chưa commit
        return BidResult.fail(BidStatus.ERROR, "...");
    }
}
```

---

## Test Cases & Ví dụ

### Test 1: Placent Bid Thành công

```
Phiên: AU100001
- Giá hiện tại: 100
- Bid step: 10

User A đặt 150
→ 150 > 100 + 10 ✓
→ Ghi bid, cập nhật current_price = 150
→ BID_SUCCESS
```

### Test 2: Bid Thấp hơn mức yêu cầu

```
Phiên: AU100001
- Giá hiện tại: 150
- Bid step: 10

User B đặt 155
→ 155 > 150 + 10 ✗ (chỉ 155 > 160)
→ BID_TOO_LOW
```

### Test 3: Không đủ số dư

```
Phiên: AU100001
- Giá hiện tại: 100
- Bid step: 10
- User C balance: 50

User C đặt 200
→ 200 > 100 + 10 ✓ (giá hợp lệ)
→ Nhưng 200 > 50 (số dư) ✗
→ INSUFFICIENT_BALANCE
```

### Test 4: Concurrent Bidding (Không mất cập nhật)

```
T1: User A lock → đọc current_price = 100
T2: User B try lock → CHỜ
T3: User A: validate 150, insert, update price = 150, commit
T4: User B: khóa được, đọc current_price = 150 (mới)
T5: User B: validate 140 > 150 + 10 ✗
→ BID_TOO_LOW (không mất update)
```

### Test 5: Chốt phiên với thanh toán (Atomicity)

```
Phiên AU100001: 2 lượt bid
- User A: 150
- User B: 200 (highest)

closeAuctionByTime("AU100001"):
1. Lock auctions (FOR UPDATE)
2. Tìm bidder = User B, final_price = 200
3. Update auctions: status=PAID, winner_id=User B, final_price=200
4. Debit User B: balance -= 200
5. Credit User A (seller): balance += 200 * commission_rate (nếu có)
6. Commit tất cả hoặc ROLLBACK nếu có lỗi

Result: Nguyên tử - hoặc thành công hoàn toàn, hoặc không thay đổi gì
```

---

## Kết luận

Hệ thống DAO hiện tại của bạn đã áp dụng:

✅ **DatabaseConnection** - Quản lý connection từ Aiven thống nhất  
✅ **Transaction with FOR UPDATE** - Khóa phiên trước khi validate  
✅ **Atomicity (ACID)** - Toàn bộ hoặc không gì cả  
✅ **ResultSet Mapping** - Gom dữ liệu vào Entity classes  
✅ **Error Handling** - Log lỗi và rollback tự động  

**Tiếp theo:** Triển khai BidTransaction DAO và test concurrent bidding trên Aiven!
