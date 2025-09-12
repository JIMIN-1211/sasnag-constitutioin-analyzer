/* === 1. 데이터베이스/계정/권한 === */
CREATE DATABASE IF NOT EXISTS sasang
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_general_ci;

CREATE USER IF NOT EXISTS 'sasang'@'localhost' IDENTIFIED BY 'pass1234';
GRANT ALL PRIVILEGES ON sasang.* TO 'sasang'@'localhost';
FLUSH PRIVILEGES;

/* === 2. 스키마 생성 === */
USE sasang;

CREATE TABLE IF NOT EXISTS users (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  username VARCHAR(50) NOT NULL UNIQUE,
  email VARCHAR(255) NOT NULL UNIQUE,
  password_hash VARCHAR(255) NOT NULL,
  name VARCHAR(100),
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS survey_answers (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  survey_id VARCHAR(50) NOT NULL,
  user_id BIGINT NOT NULL,
  question_id VARCHAR(50) NOT NULL,
  answer VARCHAR(255) NOT NULL,
  submitted_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_user_time (user_id, submitted_at)
);

/* === 3. 확인용 === */
SHOW TABLES;
