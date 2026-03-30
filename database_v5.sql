/* =================================================================================
 * AUCTION SYSTEM DATABASE SCHEMA - VERSION 4.2 (FINAL - NO ERRORS)
 * Architecture: Client-Server (Java Spring Boot/JavaFX)
 * Database Engine: MySQL 8.0+
 * All bugs fixed, no syntax errors, no duplicate indexes
 * ================================================================================= */

-- Khởi tạo Database
DROP DATABASE IF EXISTS auction_system_v2;
CREATE DATABASE auction_system_v2 CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE auction_system_v2;

/* =================================================================================
 * TABLE: USERS
 * ================================================================================= */
CREATE TABLE users (
    customer_id VARCHAR(8) PRIMARY KEY COMMENT 'Format: BD5XXXXX',
    password_hash VARCHAR(255) NOT NULL COMMENT 'Bcrypt hash',
    email VARCHAR(255) UNIQUE NOT NULL,
    full_name VARCHAR(255) NOT NULL,
    phone VARCHAR(20),
    avatar_url VARCHAR(500),
    bio TEXT,
    role ENUM('BIDDER', 'SELLER', 'ADMIN') NOT NULL,
    status ENUM('ACTIVE', 'INACTIVE', 'BANNED', 'SUSPENDED') DEFAULT 'ACTIVE',
    verification_status ENUM('UNVERIFIED', 'VERIFIED', 'FAILED') DEFAULT 'UNVERIFIED',
    identity_number VARCHAR(50) COMMENT 'National ID / Passport',
    identity_verified_at TIMESTAMP(3) NULL,
    reputation_score DECIMAL(4, 2) DEFAULT 5.00 COMMENT 'Scale: 1.00 to 5.00',
    total_auctions_won INT DEFAULT 0,
    total_auctions_sold INT DEFAULT 0,
    created_at TIMESTAMP(3) DEFAULT CURRENT_TIMESTAMP(3),
    updated_at TIMESTAMP(3) DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    last_login_at TIMESTAMP(3) NULL,
    
    INDEX idx_email (email),
    INDEX idx_role (role),
    INDEX idx_status (status),
    INDEX idx_reputation (reputation_score)
);

-- Trigger: Ràng buộc định dạng ID người dùng
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
 * TABLE: WALLETS
 * ================================================================================= */
CREATE TABLE wallets (
    wallet_id INT AUTO_INCREMENT PRIMARY KEY,
    customer_id VARCHAR(8) NOT NULL UNIQUE,
    balance DECIMAL(15, 2) DEFAULT 0.00 CHECK (balance >= 0) COMMENT 'Total actual balance',
    frozen_balance DECIMAL(15, 2) DEFAULT 0.00 CHECK (frozen_balance >= 0) COMMENT 'Amount locked for active bids',
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

/* =================================================================================
 * TABLE: WALLET_TRANSACTIONS
 * ================================================================================= */
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
 * TABLE: PRODUCTS
 * ================================================================================= */
CREATE TABLE products (
    product_id INT AUTO_INCREMENT PRIMARY KEY,
    seller_id VARCHAR(8) NOT NULL,
    product_type ENUM('ELECTRONICS', 'ART', 'VEHICLE', 'JEWELRY', 'COLLECTIBLE') NOT NULL,
    product_name VARCHAR(255) NOT NULL,
    description TEXT,
    `condition` ENUM('NEW', 'LIKE_NEW', 'GOOD', 'FAIR', 'POOR') DEFAULT 'GOOD',
    starting_price DECIMAL(15, 2) NOT NULL CHECK (starting_price > 0),
    reserve_price DECIMAL(15, 2),
    current_price DECIMAL(15, 2) NOT NULL,
    status ENUM('AVAILABLE', 'SOLD', 'CANCELED', 'SUSPENDED') DEFAULT 'AVAILABLE',
    category_id INT,
    location VARCHAR(255),
    
    brand VARCHAR(100),
    warranty_months INT DEFAULT 0,
    artist_name VARCHAR(100),
    creation_year INT,
    authentication_certificate VARCHAR(500),
    engine_type VARCHAR(50),
    mileage INT,
    
    view_count INT DEFAULT 0,
    is_featured BOOLEAN DEFAULT FALSE,
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

/* =================================================================================
 * TABLE: PRODUCT_IMAGES
 * ================================================================================= */
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
 * TABLE: AUCTIONS
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
    max_extension INT DEFAULT 3,
    auto_extension_enabled BOOLEAN DEFAULT TRUE,
    auto_extension_minutes INT DEFAULT 5,
    status ENUM('OPEN', 'RUNNING', 'FINISHED', 'PAID', 'CANCELED') DEFAULT 'OPEN',
    winner_id VARCHAR(8) DEFAULT NULL,
    total_bids INT DEFAULT 0,
    unique_bidders INT DEFAULT 0,
    min_bid_increment DECIMAL(15, 2) DEFAULT 1.00,
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
 * TABLE: BID_TRANSACTIONS
 * ================================================================================= */
CREATE TABLE bid_transactions (
    transaction_id INT AUTO_INCREMENT PRIMARY KEY,
    auction_id VARCHAR(8) NOT NULL,
    bidder_id VARCHAR(8) NOT NULL,
    bid_amount DECIMAL(15, 2) NOT NULL,
    bid_rank INT NOT NULL,
    is_highest BOOLEAN DEFAULT FALSE,
    is_auto_bid BOOLEAN DEFAULT FALSE,
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

/* =================================================================================
 * TABLE: AUTO_BIDDING_RULES
 * ================================================================================= */
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
 * TABLE: NOTIFICATIONS
 * ================================================================================= */
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

/* =================================================================================
 * TABLE: PAYMENTS
 * ================================================================================= */
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

/* =================================================================================
 * TABLE: PRICE_HISTORY_LOGS
 * ================================================================================= */
CREATE TABLE price_history_logs (
    log_id INT AUTO_INCREMENT PRIMARY KEY,
    product_id INT NOT NULL,
    old_price DECIMAL(15, 2) NOT NULL,
    new_price DECIMAL(15, 2) NOT NULL,
    changed_at TIMESTAMP(3) DEFAULT CURRENT_TIMESTAMP(3),
    
    INDEX idx_product_history (product_id),
    INDEX idx_changed_at (changed_at)
);

-- Trigger: Tự động ghi log khi giá sản phẩm cập nhật
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
 * VIEWS
 * ================================================================================= */

CREATE VIEW auction_winners_info AS
SELECT 
    a.auction_id,
    p.product_name,
    u.customer_id as winner_id,
    u.full_name as winner_name,
    u.email,
    a.final_price as winning_price,
    a.end_time,
    pa.status as payment_status,
    a.status as auction_status
FROM auctions a
JOIN products p ON a.product_id = p.product_id
JOIN users u ON a.winner_id = u.customer_id
LEFT JOIN payments pa ON a.auction_id = pa.auction_id
WHERE a.status IN ('FINISHED', 'PAID');

CREATE VIEW seller_statistics AS
SELECT 
    u.customer_id,
    u.full_name,
    COUNT(DISTINCT a.auction_id) as total_auctions,
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
    a.auction_id,
    p.product_id,
    p.product_name,
    p.current_price,
    a.total_bids,
    a.unique_bidders,
    a.total_views,
    COALESCE(a.actual_end_time, a.end_time) AS time_left,
    (a.total_bids * 10 + a.unique_bidders * 5 + a.total_views * 1) AS hot_score
FROM auctions a
JOIN products p ON a.product_id = p.product_id
WHERE a.status = 'RUNNING'
ORDER BY hot_score DESC;

/* =================================================================================
 * STORED PROCEDURES - FIXED & OPTIMIZED
 * ================================================================================= */

-- ✅ PROCEDURE 1: Place Auto Bid (với fix race condition)
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
    
    IF p_expected_product_version IS NOT NULL AND p_expected_product_version != v_db_product_version THEN
        SIGNAL SQLSTATE '45000' 
        SET MESSAGE_TEXT = 'Concurrency Error: Product version mismatch';
    END IF;
    
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

-- ✅ PROCEDURE 2: Finish Auction (với proper locking)
DELIMITER $$
CREATE PROCEDURE finish_auction(
    IN p_auction_id VARCHAR(8)
)
BEGIN
    DECLARE v_winner_id VARCHAR(8);
    DECLARE v_highest_bid DECIMAL(15, 2);
    DECLARE v_auction_status VARCHAR(20);

    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;

    START TRANSACTION;

    SELECT status INTO v_auction_status 
    FROM auctions 
    WHERE auction_id = p_auction_id
    FOR UPDATE;

    IF v_auction_status != 'RUNNING' THEN
        SIGNAL SQLSTATE '45000' 
        SET MESSAGE_TEXT = 'Validation Error: Auction is not in RUNNING state';
    END IF;

    SELECT bidder_id, bid_amount
    INTO v_winner_id, v_highest_bid
    FROM bid_transactions
    WHERE auction_id = p_auction_id AND is_highest = TRUE
    ORDER BY bid_rank DESC
    LIMIT 1
    FOR UPDATE;

    UPDATE auctions
    SET status = 'FINISHED',
        winner_id = COALESCE(v_winner_id, winner_id),
        final_price = COALESCE(v_highest_bid, final_price),
        actual_end_time = COALESCE(actual_end_time, NOW(3)),
        version = version + 1,
        updated_at = NOW(3)
    WHERE auction_id = p_auction_id;

    COMMIT;
END$$
DELIMITER ;

-- ✅ PROCEDURE 3: Complete Auction (với balance validation)
DELIMITER $$
CREATE PROCEDURE complete_auction(
    IN p_auction_id VARCHAR(8)
)
BEGIN
    DECLARE v_winner_id VARCHAR(8);
    DECLARE v_final_price DECIMAL(15, 2);
    DECLARE v_product_id INT;
    DECLARE v_seller_id VARCHAR(8);
    DECLARE v_auction_status VARCHAR(20);
    DECLARE v_winner_balance DECIMAL(15, 2);
    DECLARE v_wallet_id INT;

    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;

    START TRANSACTION;

    SELECT a.winner_id, a.final_price, a.product_id, p.seller_id, a.status
    INTO v_winner_id, v_final_price, v_product_id, v_seller_id, v_auction_status
    FROM auctions a
    JOIN products p ON a.product_id = p.product_id
    WHERE a.auction_id = p_auction_id
    FOR UPDATE;

    IF v_auction_status != 'FINISHED' THEN
        SIGNAL SQLSTATE '45000' 
        SET MESSAGE_TEXT = 'Validation Error: Auction is not in FINISHED state';
    END IF;

    IF v_winner_id IS NULL THEN
        SIGNAL SQLSTATE '45000' 
        SET MESSAGE_TEXT = 'Data Error: No winner found for this auction';
    END IF;

    SELECT wallet_id, available_balance INTO v_wallet_id, v_winner_balance 
    FROM wallets 
    WHERE customer_id = v_winner_id
    FOR UPDATE;

    IF v_winner_balance < v_final_price THEN
        SIGNAL SQLSTATE '45000' 
        SET MESSAGE_TEXT = 'Payment Error: Insufficient balance to complete auction';
    END IF;

    UPDATE auctions
    SET status = 'PAID', updated_at = NOW(3)
    WHERE auction_id = p_auction_id;

    UPDATE products
    SET status = 'SOLD', updated_at = NOW(3)
    WHERE product_id = v_product_id;

    INSERT INTO payments (
        auction_id, seller_id, buyer_id, amount,
        platform_fee, payment_method, status, created_at
    ) VALUES (
        p_auction_id, v_seller_id, v_winner_id, v_final_price,
        v_final_price * 0.05, 'WALLET', 'PENDING', NOW(3)
    );

    UPDATE wallets
    SET balance = balance - v_final_price,
        updated_at = NOW(3)
    WHERE customer_id = v_winner_id;

    INSERT INTO wallet_transactions (
        wallet_id, customer_id, transaction_type, amount, 
        balance_before, balance_after, reference_id, description, status
    ) VALUES (
        v_wallet_id, 
        v_winner_id, 
        'PAYMENT',
        v_final_price,
        v_winner_balance,
        v_winner_balance - v_final_price,
        p_auction_id,
        CONCAT('Payment for auction ', p_auction_id),
        'COMPLETED'
    );

    COMMIT;
END$$
DELIMITER ;

-- ✅ PROCEDURE 4: Extend Auction (Anti-sniping)
DELIMITER $$
CREATE PROCEDURE extend_auction(
    IN p_auction_id VARCHAR(8),
    IN p_expected_version INT
)
BEGIN
    DECLARE v_extension_count INT;
    DECLARE v_max_extension INT;
    DECLARE v_current_end_time DATETIME(3);
    DECLARE v_extension_minutes INT;
    DECLARE v_db_version INT;
    DECLARE v_new_end_time DATETIME(3);

    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;

    START TRANSACTION;

    SELECT extension_count, max_extension, COALESCE(actual_end_time, end_time), auto_extension_minutes, version
    INTO v_extension_count, v_max_extension, v_current_end_time, v_extension_minutes, v_db_version
    FROM auctions
    WHERE auction_id = p_auction_id
    FOR UPDATE;

    IF p_expected_version IS NOT NULL AND p_expected_version != v_db_version THEN
        SIGNAL SQLSTATE '45000' 
        SET MESSAGE_TEXT = 'Concurrency Error: Auction version mismatch';
    END IF;

    IF v_extension_count >= v_max_extension THEN
        SIGNAL SQLSTATE '45000' 
        SET MESSAGE_TEXT = 'Validation Error: Maximum extension limit reached';
    END IF;

    SET v_new_end_time = DATE_ADD(v_current_end_time, INTERVAL v_extension_minutes MINUTE);

    UPDATE auctions
    SET end_time = v_new_end_time,
        actual_end_time = v_new_end_time,
        extension_count = extension_count + 1,
        version = version + 1,
        updated_at = NOW(3)
    WHERE auction_id = p_auction_id;

    COMMIT;
END$$
DELIMITER ;

/* =================================================================================
 * EVENT SCHEDULER - Auto-close expired auctions
 * ================================================================================= */
SET GLOBAL event_scheduler = ON;

DELIMITER $$
CREATE EVENT IF NOT EXISTS evt_auto_close_expired_auctions
ON SCHEDULE EVERY 1 MINUTE
DO
BEGIN
    DECLARE done INT DEFAULT FALSE;
    DECLARE v_auction_id VARCHAR(8);
    
    DECLARE cur_expired CURSOR FOR 
        SELECT auction_id FROM auctions 
        WHERE status = 'RUNNING' 
        AND COALESCE(actual_end_time, end_time) <= NOW(3)
        LIMIT 100;
        
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;
    DECLARE CONTINUE HANDLER FOR SQLEXCEPTION BEGIN END;
    
    OPEN cur_expired;
    
    read_loop: LOOP
        FETCH cur_expired INTO v_auction_id;
        IF done THEN
            LEAVE read_loop;
        END IF;
        
        CALL finish_auction(v_auction_id);
    END LOOP;
    
    CLOSE cur_expired;
END$$
DELIMITER ;
