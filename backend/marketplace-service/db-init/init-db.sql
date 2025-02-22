CREATE DATABASE IF NOT EXISTS marketplace;
USE marketplace;

CREATE TABLE listings (
    id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    price DECIMAL(10,2) NOT NULL,
    seller_id INT NOT NULL,
    images VARCHAR(255),
    latitude DECIMAL(9,6),
    longitude DECIMAL(9,6),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
