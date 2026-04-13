CREATE DATABASE IF NOT EXISTS auction_system;
USE auction_system;

DROP TABLE IF EXISTS bid_transactions;
DROP TABLE IF EXISTS auctions;
DROP TABLE IF EXISTS items;
DROP TABLE IF EXISTS users;

CREATE TABLE users (
                       customer_id VARCHAR(50) PRIMARY KEY,
                       username VARCHAR(50) UNIQUE NOT NULL,
                       password_hash VARCHAR(255) NOT NULL,
                       role ENUM('BIDDER', 'SELLER','ADMIN') NOT NULL,
                       balance DOUBLE DEFAULT 100000
);

CREATE TABLE items (
                       item_id VARCHAR(50) PRIMARY KEY,
                       name VARCHAR(255) NOT NULL,
                       description TEXT,
                       current_price DOUBLE DEFAULT 0.0
);

CREATE TABLE auctions (
                          auction_id VARCHAR(50) PRIMARY KEY,
                          item_id VARCHAR(50) NOT NULL,
                          seller_id VARCHAR(50) NOT NULL,
                          status VARCHAR(20) DEFAULT 'RUNNING',
                          start_time DATETIME NOT NULL,
                          duration_minutes INT NOT NULL DEFAULT 30,
                          actual_end_time DATETIME NOT NULL,
                          extension_seconds INT NOT NULL DEFAULT 60,

                          FOREIGN KEY (item_id) REFERENCES items(item_id) ON DELETE CASCADE,
                          FOREIGN KEY (seller_id) REFERENCES users(customer_id) ON DELETE CASCADE
);

CREATE TABLE bid_transactions (
                                  transaction_id INT AUTO_INCREMENT PRIMARY KEY,
                                  auction_id VARCHAR(50) NOT NULL,
                                  bidder_id VARCHAR(50) NOT NULL,
                                  bid_amount DOUBLE NOT NULL,
                                  bid_rank INT NOT NULL,
                                  is_highest BOOLEAN DEFAULT TRUE,
                                  bid_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

                                  FOREIGN KEY (auction_id) REFERENCES auctions(auction_id) ON DELETE CASCADE,
                                  FOREIGN KEY (bidder_id) REFERENCES users(customer_id) ON DELETE CASCADE
);