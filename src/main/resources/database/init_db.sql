/* =================================================================================
 * AUCTION SYSTEM DATABASE SCHEMA - VERSION 4.3 (FIXED FOR JAVA DAO)
 * Architecture: Client-Server (Java Spring Boot/JavaFX)
 * Database Engine: MySQL 8.0+
 * Người thiết kế: Quản lý Database & GitHub của Team
 * ================================================================================= */

-- Khởi tạo Database với chuẩn mã hóa UTF-8 để lưu tiếng Việt có dấu mượt mà
DROP DATABASE IF EXISTS auction_system_v2;
CREATE DATABASE auction_system_v2 CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE auction_system_v2;

/* =================================================================================
 * TABLE: USERS (BẢNG NGƯỜI DÙNG)
 * Quản lý thông tin tài khoản, phân quyền và điểm uy tín của người dùng.
 * ĐÃ THÊM: username, organization, balance để khớp với UserDAO.java
 * ================================================================================= */
CREATE TABLE users (
                       customer_id VARCHAR(8) PRIMARY KEY COMMENT 'Format: BD5XXXXX',
                       username VARCHAR(50) UNIQUE COMMENT 'Tên đăng nhập (Thêm theo yêu cầu Frontend)',
                       password_hash VARCHAR(255) NOT NULL COMMENT 'Bcrypt hash',
                       email VARCHAR(255) UNIQUE NOT NULL,
                       full_name VARCHAR(255) NOT NULL,
                       phone VARCHAR(20),
                       avatar_url VARCHAR(500),
                       bio TEXT,

    -- 3 Quyền cơ bản: BIDDER (Chỉ mua), SELLER (Được bán), ADMIN (Quản trị hệ thống)
                       role ENUM('BIDDER', 'SELLER', 'ADMIN') NOT NULL,
                       organization VARCHAR(255) COMMENT 'Tên tổ chức/công ty (Dành riêng cho SELLER)',
                       balance DECIMAL(15, 2) DEFAULT 0.00 COMMENT 'Số dư tài khoản cơ bản',

                       status ENUM('ACTIVE', 'INACTIVE', 'BANNED', 'SUSPENDED') DEFAULT 'ACTIVE',
                       verification_status ENUM('UNVERIFIED', 'VERIFIED', 'FAILED') DEFAULT 'UNVERIFIED',
                       identity_number VARCHAR(50) COMMENT 'National ID / Passport',
                       identity_verified_at TIMESTAMP(3) NULL,

    -- Điểm uy tín: Nếu bỏ bom không thanh toán, bị trừ điểm. Điểm thấp không cho đấu giá.
                       reputation_score DECIMAL(4, 2) DEFAULT 5.00 COMMENT 'Scale: 1.00 to 5.00',
                       total_auctions_won INT DEFAULT 0,
                       total_auctions_sold INT DEFAULT 0,
                       created_at TIMESTAMP(3) DEFAULT CURRENT_TIMESTAMP(3),
                       updated_at TIMESTAMP(3) DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
                       last_login_at TIMESTAMP(3) NULL,

    -- Đánh Index để tìm kiếm nhanh theo email, username hoặc lọc theo vai trò
                       INDEX idx_username (username),
                       INDEX idx_email (email),
                       INDEX idx_role (role),
                       INDEX idx_status (status),
                       INDEX idx_reputation (reputation_score)
);

-- Trigger: Canh gác ngay cổng Database, nếu ID không đúng chuẩn BD5xxxxx là chặn ngay (Quăng lỗi 45000)
DELIMITER $$
CREATE TRIGGER trg_check_customer_id_format
    BEFORE INSERT ON users FOR EACH ROW
BEGIN
    IF NEW.customer_id NOT REGEXP '^BD5[0-9]{5}$' THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Invalid customer_id format. Expected format: BD5XXXXX';
    END IF;
END$$
DELIMITER ;

/* =================================================================================
 * TABLE: WALLETS (VÍ TIỀN ĐIỆN TỬ)
 * Tách biệt số dư thực và số dư đang bị "đóng băng" (khi đang đặt giá).
 * Tránh việc 1 người cầm 100k đi bid 10 cái điện thoại cùng lúc.
 * ================================================================================= */
CREATE TABLE wallets (
                         wallet_id INT AUTO_INCREMENT PRIMARY KEY,
                         customer_id VARCHAR(8) NOT NULL UNIQUE,
                         balance DECIMAL(15, 2) DEFAULT 0.00 CHECK (balance >= 0) COMMENT 'Tổng tiền nạp vào',
                         frozen_balance DECIMAL(15, 2) DEFAULT 0.00 CHECK (frozen_balance >= 0) COMMENT 'Tiền cọc đang bị giam vì bid giá cao nhất',

    -- Cột ảo (Generated Column): Tự động tính số tiền thực tế có thể dùng = balance - frozen
                         available_balance DECIMAL(15, 2) GENERATED ALWAYS AS (balance - frozen_balance) STORED COMMENT 'Usable balance',

                         total_deposited DECIMAL(15, 2) DEFAULT 0.00,
                         total_withdrawn DECIMAL(15, 2) DEFAULT 0.00,
                         created_at TIMESTAMP(3) DEFAULT CURRENT_TIMESTAMP(3),
                         updated_at TIMESTAMP(3) DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),

                         FOREIGN KEY (customer_id) REFERENCES users(customer_id) ON DELETE CASCADE,
                         INDEX idx_customer_id (customer_id),
                         INDEX idx_balance (available_balance),
                         INDEX idx_customer_available_balance (customer_id, available_balance)
);

/* Bảng này (wallet_transactions) lưu lịch sử biến động số dư (Sao kê ví) */
CREATE TABLE wallet_transactions (
                                     transaction_id INT AUTO_INCREMENT PRIMARY KEY,
                                     wallet_id INT NOT NULL,
                                     customer_id VARCHAR(8) NOT NULL,
                                     transaction_type ENUM('DEPOSIT', 'WITHDRAWAL', 'BID_FREEZE', 'BID_RELEASE', 'PAYMENT', 'REFUND', 'BONUS') NOT NULL,
                                     amount DECIMAL(15, 2) NOT NULL CHECK (amount > 0),
                                     balance_before DECIMAL(15, 2) NOT NULL,
                                     balance_after DECIMAL(15, 2) NOT NULL,
                                     reference_id VARCHAR(100) COMMENT 'References auction_id or external payment_id',
                                     description VARCHAR(500),
                                     status ENUM('PENDING', 'COMPLETED', 'FAILED', 'REVERSED') DEFAULT 'PENDING',
                                     payment_method ENUM('CREDIT_CARD', 'BANK_TRANSFER', 'PAYPAL', 'E_WALLET', 'CASH') DEFAULT NULL,
                                     ip_address VARCHAR(45),
                                     created_at TIMESTAMP(3) DEFAULT CURRENT_TIMESTAMP(3),

                                     FOREIGN KEY (wallet_id) REFERENCES wallets(wallet_id) ON DELETE CASCADE,
                                     FOREIGN KEY (customer_id) REFERENCES users(customer_id) ON DELETE CASCADE,

                                     INDEX idx_wallet_id (wallet_id),
                                     INDEX idx_customer_id (customer_id),
                                     INDEX idx_status (status),
                                     INDEX idx_created_at (created_at)
);

/* =================================================================================
 * TABLE: PRODUCTS (SẢN PHẨM)
 * Chứa thông tin chi tiết về sản phẩm được đem ra đấu giá.
 * ================================================================================= */
CREATE TABLE products (
                          product_id INT AUTO_INCREMENT PRIMARY KEY,
                          seller_id VARCHAR(8) NOT NULL,

    -- Phân loại để dùng Factory Pattern trong Java Backend
                          product_type ENUM('ELECTRONICS', 'ART', 'VEHICLE', 'JEWELRY', 'COLLECTIBLE') NOT NULL,

                          product_name VARCHAR(255) NOT NULL,
                          description TEXT,
                          `condition` ENUM('NEW', 'LIKE_NEW', 'GOOD', 'FAIR', 'POOR') DEFAULT 'GOOD',
                          starting_price DECIMAL(15, 2) NOT NULL CHECK (starting_price > 0),
                          reserve_price DECIMAL(15, 2) COMMENT 'Giá kỳ vọng tối thiểu, nếu kết thúc mà chưa đạt giá này thì Seller có quyền ko bán',
                          current_price DECIMAL(15, 2) NOT NULL,
                          status ENUM('AVAILABLE', 'SOLD', 'CANCELED', 'SUSPENDED') DEFAULT 'AVAILABLE',
                          category_id INT,
                          location VARCHAR(255),

    -- Các thuộc tính mở rộng tùy theo loại sản phẩm
                          brand VARCHAR(100),
                          warranty_months INT DEFAULT 0,
                          artist_name VARCHAR(100),
                          creation_year INT,
                          authentication_certificate VARCHAR(500),
                          engine_type VARCHAR(50),
                          mileage INT,

                          view_count INT DEFAULT 0,
                          is_featured BOOLEAN DEFAULT FALSE,

    -- CỘT CHỐNG CONCURRENCY (Lạc quan - Optimistic)
                          version INT DEFAULT 0 COMMENT 'Optimistic locking version field',

                          created_at TIMESTAMP(3) DEFAULT CURRENT_TIMESTAMP(3),
                          updated_at TIMESTAMP(3) DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),

                          FOREIGN KEY (seller_id) REFERENCES users(customer_id) ON DELETE RESTRICT,
                          CONSTRAINT chk_valid_price CHECK (current_price >= starting_price),

                          INDEX idx_seller_status (seller_id, status),
                          INDEX idx_product_type (product_type),
                          INDEX idx_created_at (created_at),
                          INDEX idx_version (version),
                          FULLTEXT INDEX ft_name_description (product_name, description)
);

CREATE TABLE product_images (
                                image_id INT AUTO_INCREMENT PRIMARY KEY,
                                product_id INT NOT NULL,
                                image_url VARCHAR(500) NOT NULL,
                                display_order INT DEFAULT 0,
                                is_primary BOOLEAN DEFAULT FALSE,
                                uploaded_at TIMESTAMP(3) DEFAULT CURRENT_TIMESTAMP(3),

                                FOREIGN KEY (product_id) REFERENCES products(product_id) ON DELETE CASCADE,
                                INDEX idx_product_id (product_id)
);

/* =================================================================================
 * TABLE: AUCTIONS (PHIÊN ĐẤU GIÁ)
 * Quản lý vòng đời thời gian thực của một cuộc đấu giá.
 * ================================================================================= */
CREATE TABLE auctions (
                          auction_id VARCHAR(8) PRIMARY KEY COMMENT 'Format: AU1XXXXX',
                          product_id INT NOT NULL UNIQUE,
                          created_by VARCHAR(8) NOT NULL,
                          start_time DATETIME(3) NOT NULL,
                          end_time DATETIME(3) NOT NULL,

                          actual_end_time DATETIME(3) COMMENT 'Updated dynamically if anti-sniping is triggered',

                          final_price DECIMAL(15, 2) DEFAULT NULL,
                          extension_count INT DEFAULT 0,
                          max_extension INT DEFAULT 3 COMMENT 'Chỉ cho phép gia hạn tối đa 3 lần để tránh lạm dụng',
                          auto_extension_enabled BOOLEAN DEFAULT TRUE,
                          auto_extension_minutes INT DEFAULT 5 COMMENT 'Gia hạn thêm 5 phút nếu có người bid giây chót',

                          status ENUM('OPEN', 'RUNNING', 'FINISHED', 'PAID', 'CANCELED') DEFAULT 'OPEN',
                          winner_id VARCHAR(8) DEFAULT NULL,
                          total_bids INT DEFAULT 0,
                          unique_bidders INT DEFAULT 0,
                          min_bid_increment DECIMAL(15, 2) DEFAULT 1.00 COMMENT 'Bước giá tối thiểu (ví dụ: mỗi lần phải bid thêm ít nhất 10k)',
                          total_views INT DEFAULT 0,

                          version INT DEFAULT 0 COMMENT 'Optimistic locking version field',

                          created_at TIMESTAMP(3) DEFAULT CURRENT_TIMESTAMP(3),
                          updated_at TIMESTAMP(3) DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),

                          FOREIGN KEY (product_id) REFERENCES products(product_id) ON DELETE CASCADE,
                          FOREIGN KEY (created_by) REFERENCES users(customer_id) ON DELETE RESTRICT,
                          FOREIGN KEY (winner_id) REFERENCES users(customer_id) ON DELETE SET NULL,

                          CONSTRAINT chk_auction_time CHECK (end_time > start_time),
                          CONSTRAINT chk_auction_id_format CHECK (auction_id REGEXP '^AU1[0-9]{5}$'),
                          INDEX idx_auction_status (status),
                          INDEX idx_auction_winner (winner_id),
                          INDEX idx_auction_dates (start_time, end_time),
                          INDEX idx_product_id (product_id),
                          INDEX idx_version (version)
);

/* =================================================================================
 * TABLE: BID_TRANSACTIONS (LỊCH SỬ ĐẶT GIÁ)
 * ================================================================================= */
CREATE TABLE bid_transactions (
                                  transaction_id INT AUTO_INCREMENT PRIMARY KEY,
                                  auction_id VARCHAR(8) NOT NULL,
                                  bidder_id VARCHAR(8) NOT NULL,
                                  bid_amount DECIMAL(15, 2) NOT NULL,
                                  bid_rank INT NOT NULL COMMENT 'Thứ tự lượt bid (1, 2, 3...)',
                                  is_highest BOOLEAN DEFAULT FALSE COMMENT 'Chỉ có 1 dòng duy nhất mang giá trị TRUE (Người đang giữ top)',
                                  is_auto_bid BOOLEAN DEFAULT FALSE COMMENT 'Đánh dấu xem đây là người tự bấm hay máy (Auto-bid) tự đánh hộ',
                                  ip_address VARCHAR(45),
                                  device_info VARCHAR(255),
                                  bid_time TIMESTAMP(3) DEFAULT CURRENT_TIMESTAMP(3),

                                  FOREIGN KEY (auction_id) REFERENCES auctions(auction_id) ON DELETE CASCADE,
                                  FOREIGN KEY (bidder_id) REFERENCES users(customer_id) ON DELETE RESTRICT,

                                  CONSTRAINT chk_valid_bid CHECK (bid_amount > 0),
                                  UNIQUE KEY uk_auction_bid_rank (auction_id, bid_rank),
                                  INDEX idx_bidder_auction (bidder_id, auction_id),
                                  INDEX idx_auction_bid_time (auction_id, bid_time),
                                  INDEX idx_auction_is_highest (auction_id, is_highest)
);

/* Bảng này lưu cấu hình cho người lười (Nhập giá tối đa xong để máy tự đấu nhau) */
CREATE TABLE auto_bidding_rules (
                                    rule_id INT AUTO_INCREMENT PRIMARY KEY,
                                    auction_id VARCHAR(8) NOT NULL,
                                    bidder_id VARCHAR(8) NOT NULL,
                                    max_bid DECIMAL(15, 2) NOT NULL CHECK (max_bid > 0),
                                    increment_step DECIMAL(15, 2) NOT NULL CHECK (increment_step > 0),
                                    is_active BOOLEAN DEFAULT TRUE,
                                    auto_rebid_enabled BOOLEAN DEFAULT TRUE,
                                    registered_at TIMESTAMP(3) DEFAULT CURRENT_TIMESTAMP(3),

                                    FOREIGN KEY (auction_id) REFERENCES auctions(auction_id) ON DELETE CASCADE,
                                    FOREIGN KEY (bidder_id) REFERENCES users(customer_id) ON DELETE CASCADE,
                                    UNIQUE KEY uk_auction_bidder (auction_id, bidder_id),
                                    INDEX idx_bidder (bidder_id),
                                    INDEX idx_active (is_active)
);

/* =================================================================================
 * CÁC BẢNG KHÁC (NOTIFICATIONS, PAYMENTS, LOGS)
 * ================================================================================= */
-- Bảng Thông báo
CREATE TABLE notifications (
                               notification_id INT AUTO_INCREMENT PRIMARY KEY,
                               user_id VARCHAR(8) NOT NULL,
                               auction_id VARCHAR(8),
                               related_user_id VARCHAR(8),
                               message TEXT NOT NULL,
                               notification_type ENUM(
                                   'BID_OUTBID', 'AUCTION_WON', 'AUCTION_ENDED',
                                   'PAYMENT_REMINDER', 'ITEM_SHIPPED', 'AUCTION_EXTENDED',
                                   'AUTO_BID_ACTIVATED', 'SYSTEM_MESSAGE'
                                   ) NOT NULL,
                               is_read BOOLEAN DEFAULT FALSE,
                               read_at TIMESTAMP(3) NULL,
                               priority ENUM('LOW', 'MEDIUM', 'HIGH', 'URGENT') DEFAULT 'MEDIUM',
                               notification_channel ENUM('IN_APP', 'EMAIL', 'SMS', 'BOTH') DEFAULT 'IN_APP',
                               sent_at TIMESTAMP(3),
                               created_at TIMESTAMP(3) DEFAULT CURRENT_TIMESTAMP(3),

                               FOREIGN KEY (user_id) REFERENCES users(customer_id) ON DELETE CASCADE,
                               FOREIGN KEY (auction_id) REFERENCES auctions(auction_id) ON DELETE SET NULL,
                               FOREIGN KEY (related_user_id) REFERENCES users(customer_id) ON DELETE SET NULL,

                               INDEX idx_user_unread (user_id, is_read),
                               INDEX idx_created_at (created_at),
                               INDEX idx_priority (priority)
);

-- Bảng Thanh Toán
CREATE TABLE payments (
                          payment_id INT AUTO_INCREMENT PRIMARY KEY,
                          auction_id VARCHAR(8) NOT NULL,
                          seller_id VARCHAR(8) NOT NULL,
                          buyer_id VARCHAR(8) NOT NULL,
                          amount DECIMAL(15, 2) NOT NULL CHECK (amount > 0),
                          platform_fee DECIMAL(15, 2) DEFAULT 0.00,
                          seller_amount DECIMAL(15, 2) GENERATED ALWAYS AS (amount - platform_fee) STORED,
                          payment_method ENUM('CREDIT_CARD', 'BANK_TRANSFER', 'PAYPAL', 'WALLET', 'OTHER') NOT NULL,
                          status ENUM('PENDING', 'PROCESSING', 'COMPLETED', 'FAILED', 'REFUNDED', 'DISPUTED') DEFAULT 'PENDING',
                          transaction_ref VARCHAR(100) UNIQUE,
                          provider_response JSON,
                          dispute_status ENUM('NONE', 'OPEN', 'CLOSED', 'RESOLVED') DEFAULT 'NONE',
                          dispute_reason VARCHAR(255),
                          paid_at TIMESTAMP(3),
                          completed_at TIMESTAMP(3),
                          refunded_at TIMESTAMP(3),
                          created_at TIMESTAMP(3) DEFAULT CURRENT_TIMESTAMP(3),
                          updated_at TIMESTAMP(3) DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),

                          FOREIGN KEY (auction_id) REFERENCES auctions(auction_id) ON DELETE RESTRICT,
                          FOREIGN KEY (seller_id) REFERENCES users(customer_id) ON DELETE RESTRICT,
                          FOREIGN KEY (buyer_id) REFERENCES users(customer_id) ON DELETE RESTRICT,

                          CONSTRAINT chk_fee_valid CHECK (platform_fee >= 0 AND platform_fee <= amount),
                          INDEX idx_payment_status (status),
                          INDEX idx_buyer_auction (buyer_id, auction_id),
                          INDEX idx_seller_id (seller_id),
                          INDEX idx_created_at (created_at),
                          INDEX idx_status_refunded_at (status, refunded_at)
);

CREATE TABLE price_history_logs (
                                    log_id INT AUTO_INCREMENT PRIMARY KEY,
                                    product_id INT NOT NULL,
                                    old_price DECIMAL(15, 2) NOT NULL,
                                    new_price DECIMAL(15, 2) NOT NULL,
                                    changed_at TIMESTAMP(3) DEFAULT CURRENT_TIMESTAMP(3),

                                    INDEX idx_product_history (product_id),
                                    INDEX idx_changed_at (changed_at)
);

-- Trigger: Lịch sử giá
DELIMITER $$
CREATE TRIGGER trg_after_product_price_update
    AFTER UPDATE ON products
    FOR EACH ROW
BEGIN
    IF OLD.current_price <> NEW.current_price THEN
        INSERT INTO price_history_logs (product_id, old_price, new_price, changed_at)
        VALUES (NEW.product_id, OLD.current_price, NEW.current_price, NOW(3));
    END IF;
END$$
DELIMITER ;

/* =================================================================================
 * VIEWS - VIEW BÁO CÁO
 * ================================================================================= */
CREATE VIEW auction_winners_info AS
SELECT
    a.auction_id, p.product_name, u.customer_id as winner_id, u.full_name as winner_name,
    u.email, a.final_price as winning_price, a.end_time, pa.status as payment_status, a.status as auction_status
FROM auctions a
         JOIN products p ON a.product_id = p.product_id
         JOIN users u ON a.winner_id = u.customer_id
         LEFT JOIN payments pa ON a.auction_id = pa.auction_id
WHERE a.status IN ('FINISHED', 'PAID');

CREATE VIEW seller_statistics AS
SELECT
    u.customer_id, u.full_name, COUNT(DISTINCT a.auction_id) as total_auctions,
    SUM(CASE WHEN a.status IN ('FINISHED', 'PAID') THEN 1 ELSE 0 END) as completed_auctions,
    AVG(p.current_price) as avg_selling_price,
    SUM(CASE WHEN a.status IN ('FINISHED', 'PAID') THEN a.final_price ELSE 0 END) as total_revenue,
    u.reputation_score
FROM users u
         LEFT JOIN products p ON u.customer_id = p.seller_id
         LEFT JOIN auctions a ON p.product_id = a.product_id
WHERE u.role IN ('SELLER', 'ADMIN')
GROUP BY u.customer_id, u.full_name, u.reputation_score;

CREATE VIEW trending_auctions AS
SELECT
    a.auction_id, p.product_id, p.product_name, p.current_price,
    a.total_bids, a.unique_bidders, a.total_views, COALESCE(a.actual_end_time, a.end_time) AS time_left,
    (a.total_bids * 10 + a.unique_bidders * 5 + a.total_views * 1) AS hot_score
FROM auctions a
         JOIN products p ON a.product_id = p.product_id
WHERE a.status = 'RUNNING'
ORDER BY hot_score DESC;

/* =================================================================================
 * STORED PROCEDURES
 * ================================================================================= */
DELIMITER $$
CREATE PROCEDURE place_auto_bid(
    IN p_auction_id VARCHAR(8),
    IN p_bidder_id VARCHAR(8),
    IN p_max_bid DECIMAL(15, 2),
    IN p_expected_product_version INT,
    IN p_expected_auction_version INT
)
BEGIN
    DECLARE v_current_highest DECIMAL(15, 2);
    DECLARE v_next_bid DECIMAL(15, 2);
    DECLARE v_product_id INT;
    DECLARE v_min_increment DECIMAL(15, 2);
    DECLARE v_starting_price DECIMAL(15, 2);
    DECLARE v_db_auction_version INT;
    DECLARE v_db_product_version INT;
    DECLARE v_new_bid_rank INT;
    DECLARE v_auction_status VARCHAR(20);

    DECLARE EXIT HANDLER FOR SQLEXCEPTION
        BEGIN
            ROLLBACK;
            RESIGNAL;
        END;

    START TRANSACTION;

    SELECT a.product_id, a.version, a.min_bid_increment, a.status
    INTO v_product_id, v_db_auction_version, v_min_increment, v_auction_status
    FROM auctions a
    WHERE a.auction_id = p_auction_id
        FOR UPDATE;

    IF v_auction_status NOT IN ('OPEN', 'RUNNING') THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Validation Error: Auction is not open or running';
    END IF;

    IF p_expected_auction_version IS NOT NULL AND p_expected_auction_version != v_db_auction_version THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Concurrency Error: Auction version mismatch';
    END IF;

    SELECT p.starting_price, p.current_price, p.version
    INTO v_starting_price, v_current_highest, v_db_product_version
    FROM products p
    WHERE p.product_id = v_product_id
        FOR UPDATE;

    IF v_current_highest IS NULL OR v_current_highest < v_starting_price THEN
        SET v_next_bid = v_starting_price + v_min_increment;
    ELSE
        SET v_next_bid = v_current_highest + v_min_increment;
    END IF;

    IF p_max_bid < v_next_bid THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Validation Error: Insufficient max bid amount';
    END IF;

    SELECT COALESCE(MAX(bid_rank), 0) + 1
    INTO v_new_bid_rank
    FROM bid_transactions
    WHERE auction_id = p_auction_id
        FOR UPDATE;

    UPDATE bid_transactions
    SET is_highest = FALSE
    WHERE auction_id = p_auction_id AND is_highest = TRUE;

    INSERT INTO bid_transactions (
        auction_id, bidder_id, bid_amount, bid_rank,
        is_highest, is_auto_bid, bid_time
    ) VALUES (
                 p_auction_id, p_bidder_id, v_next_bid, v_new_bid_rank,
                 TRUE, TRUE, NOW(3)
             );

    UPDATE products
    SET current_price = v_next_bid,
        version = version + 1,
        updated_at = NOW(3)
    WHERE product_id = v_product_id;

    UPDATE auctions
    SET total_bids = total_bids + 1,
        version = version + 1,
        updated_at = NOW(3)
    WHERE auction_id = p_auction_id;

    COMMIT;
END$$
DELIMITER ;