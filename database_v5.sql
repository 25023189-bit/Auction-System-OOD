-- ==============================================================================
-- DATABASE: HỆ THỐNG ĐẤU GIÁ TRỰC TUYẾN (VERSION 5.0 - THE MASTERPIECE)
-- ==============================================================================
CREATE DATABASE IF NOT EXISTS auction_db;
USE auction_db;
DROP DATABASE IF EXISTS auction_system;
CREATE DATABASE auction_system CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE auction_system;

-- ==========================================
-- 1. BẢNG NGƯỜI DÙNG (Ràng buộc chặt chẽ ID)
-- ==========================================
CREATE TABLE users (
    customer_id VARCHAR(8) PRIMARY KEY,       
    password VARCHAR(255) NOT NULL,
    role ENUM('BIDDER', 'SELLER', 'ADMIN') NOT NULL,
    created_at TIMESTAMP(3) DEFAULT CURRENT_TIMESTAMP(3),
    
    -- NÂNG CẤP MAX PING: Ép buộc chuẩn định dạng ngay từ CSDL
    CONSTRAINT chk_customer_id_format CHECK (customer_id REGEXP '^BD5[0-9]{5}$')
);

-- ==========================================
-- 2. CẤU TRÚC KẾ THỪA OOP (JOINED-TABLE STRATEGY)
-- Phục vụ yêu cầu Abstract Item -> Electronics, Art, Vehicle
-- ==========================================
-- Lớp Cha (Abstract Item)
CREATE TABLE products (
    product_id INT AUTO_INCREMENT PRIMARY KEY,
    seller_id VARCHAR(8) NOT NULL,
    product_name VARCHAR(255) NOT NULL,
    description TEXT,
    starting_price DECIMAL(15, 2) NOT NULL CHECK (starting_price >= 0),
    current_price DECIMAL(15, 2) NOT NULL,
    version INT DEFAULT 0,                    
    
    FOREIGN KEY (seller_id) REFERENCES users(customer_id) ON DELETE RESTRICT,
    -- Nâng cấp: Giá hiện tại không bao giờ được thấp hơn giá khởi điểm
    CONSTRAINT chk_valid_price CHECK (current_price >= starting_price)
);

-- Lớp Con 1: Đồ Điện Tử (Electronics)
CREATE TABLE product_electronics (
    product_id INT PRIMARY KEY,
    brand VARCHAR(100) NOT NULL,
    warranty_months INT DEFAULT 0,
    FOREIGN KEY (product_id) REFERENCES products(product_id) ON DELETE CASCADE
);

-- Lớp Con 2: Nghệ thuật (Art)
CREATE TABLE product_arts (
    product_id INT PRIMARY KEY,
    artist_name VARCHAR(100) NOT NULL,
    creation_year INT,
    FOREIGN KEY (product_id) REFERENCES products(product_id) ON DELETE CASCADE
);

-- Lớp Con 3: Phương tiện (Vehicle)
CREATE TABLE product_vehicles (
    product_id INT PRIMARY KEY,
    brand VARCHAR(100) NOT NULL,
    engine_type VARCHAR(50),
    FOREIGN KEY (product_id) REFERENCES products(product_id) ON DELETE CASCADE
);

-- ==========================================
-- 3. BẢNG PHIÊN ĐẤU GIÁ (Ràng buộc logic thời gian & ID)
-- ==========================================
CREATE TABLE auctions (
    auction_id VARCHAR(8) PRIMARY KEY,        
    product_id INT NOT NULL UNIQUE,
    start_time DATETIME(3) NOT NULL,          
    end_time DATETIME(3) NOT NULL,            
    extension_count INT DEFAULT 0,            
    status ENUM('OPEN', 'RUNNING', 'FINISHED', 'PAID', 'CANCELED') NOT NULL DEFAULT 'OPEN',
    winner_id VARCHAR(8) DEFAULT NULL,        
    
    FOREIGN KEY (product_id) REFERENCES products(product_id) ON DELETE CASCADE,
    FOREIGN KEY (winner_id) REFERENCES users(customer_id) ON DELETE SET NULL,
    
    -- NÂNG CẤP MAX PING: Ép buộc chuẩn định dạng ID và Logic Thời Gian
    CONSTRAINT chk_auction_id_format CHECK (auction_id REGEXP '^AU1[0-9]{5}$'),
    CONSTRAINT chk_auction_time CHECK (end_time > start_time)
);

-- ==========================================
-- 4. BẢNG LỊCH SỬ GIAO DỊCH
-- ==========================================
CREATE TABLE bid_transactions (
    transaction_id INT AUTO_INCREMENT PRIMARY KEY,
    auction_id VARCHAR(8) NOT NULL,
    bidder_id VARCHAR(8) NOT NULL,
    bid_amount DECIMAL(15, 2) NOT NULL,
    bid_time TIMESTAMP(3) DEFAULT CURRENT_TIMESTAMP(3),
    
    FOREIGN KEY (auction_id) REFERENCES auctions(auction_id) ON DELETE CASCADE,
    FOREIGN KEY (bidder_id) REFERENCES users(customer_id) ON DELETE RESTRICT
);

-- ==========================================
-- 5. BẢNG AUTO-BIDDING VÀ THÔNG BÁO (Giữ nguyên tính hiệu quả)
-- ==========================================
CREATE TABLE auto_bidding_rules (
    rule_id INT AUTO_INCREMENT PRIMARY KEY,
    auction_id VARCHAR(8) NOT NULL,
    bidder_id VARCHAR(8) NOT NULL,
    max_bid DECIMAL(15, 2) NOT NULL,          
    increment_step DECIMAL(15, 2) NOT NULL,   
    registered_at TIMESTAMP(3) DEFAULT CURRENT_TIMESTAMP(3), 
    FOREIGN KEY (auction_id) REFERENCES auctions(auction_id) ON DELETE CASCADE,
    FOREIGN KEY (bidder_id) REFERENCES users(customer_id) ON DELETE CASCADE,
    UNIQUE KEY (auction_id, bidder_id)
);

CREATE TABLE notifications (
    notification_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id VARCHAR(8) NOT NULL,              
    message TEXT NOT NULL,                    
    is_read BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP(3) DEFAULT CURRENT_TIMESTAMP(3),
    FOREIGN KEY (user_id) REFERENCES users(customer_id) ON DELETE CASCADE
);

-- TẠO INDEX TỐI ƯU TỐC ĐỘ
CREATE INDEX idx_auction_bid_time ON bid_transactions(auction_id, bid_time);
CREATE INDEX idx_auction_status ON auctions(status);