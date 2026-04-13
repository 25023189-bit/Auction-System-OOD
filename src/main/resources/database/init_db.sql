CREATE DATABASE IF NOT EXISTS auction_system CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
USE auction_system;

/* Dọn dẹp bảng cũ */
SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS bids;
DROP TABLE IF EXISTS auctions;
DROP TABLE IF EXISTS products;
DROP TABLE IF EXISTS users;
SET FOREIGN_KEY_CHECKS = 1;

/* 1. TABLE: USERS (Chuẩn đặc tả: Chỉ gồm ID, Password, Role) */
CREATE TABLE users (
                       customer_id VARCHAR(8) PRIMARY KEY COMMENT 'Format: BD5XXXXX',
                       password VARCHAR(255) NOT NULL,
                       role ENUM('BIDDER', 'SELLER', 'ADMIN') NOT NULL
);

/* 2. TABLE: PRODUCTS */
CREATE TABLE products (
                          product_id INT AUTO_INCREMENT PRIMARY KEY,
                          seller_id VARCHAR(8) NOT NULL,
                          product_name VARCHAR(255) NOT NULL,
                          description TEXT,
                          starting_price DECIMAL(15, 2) NOT NULL,
                          FOREIGN KEY (seller_id) REFERENCES users(customer_id) ON DELETE RESTRICT
);

/* 3. TABLE: AUCTIONS (Quản lý phiên đấu giá cơ bản) */
CREATE TABLE auctions (
                          auction_id VARCHAR(8) PRIMARY KEY COMMENT 'Format: AU1XXXXX',
                          product_id INT NOT NULL UNIQUE,
                          start_time DATETIME NOT NULL,
                          end_time DATETIME NOT NULL,
                          current_price DECIMAL(15, 2) NOT NULL,
                          winner_id VARCHAR(8) DEFAULT NULL,
                          status ENUM('OPEN', 'RUNNING', 'FINISHED', 'CANCELED') DEFAULT 'OPEN',
                          FOREIGN KEY (product_id) REFERENCES products(product_id) ON DELETE CASCADE,
                          FOREIGN KEY (winner_id) REFERENCES users(customer_id) ON DELETE SET NULL
);

/* 4. TABLE: BIDS (Lịch sử đặt giá) */
CREATE TABLE bids (
                      bid_id INT AUTO_INCREMENT PRIMARY KEY,
                      auction_id VARCHAR(8) NOT NULL,
                      bidder_id VARCHAR(8) NOT NULL,
                      bid_amount DECIMAL(15, 2) NOT NULL,
                      bid_time DATETIME DEFAULT CURRENT_TIMESTAMP,
                      FOREIGN KEY (auction_id) REFERENCES auctions(auction_id) ON DELETE CASCADE,
                      FOREIGN KEY (bidder_id) REFERENCES users(customer_id) ON DELETE RESTRICT
);

/* DATA MẪU ĐỂ TEST */
INSERT INTO users (customer_id, password, role) VALUES
                                                    ('BD50001', '123', 'SELLER'),
                                                    ('BD50002', '123', 'BIDDER'),
                                                    ('BD59999', '123', 'ADMIN');