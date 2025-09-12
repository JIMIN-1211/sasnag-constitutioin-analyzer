-- health_app 데이터베이스 생성
CREATE DATABASE IF NOT EXISTS health_app DEFAULT CHARACTER SET utf8mb4;
USE health_app;

-- users 테이블 (회원가입/로그인 테스트용)
CREATE TABLE IF NOT EXISTS users (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  username VARCHAR(50)  NOT NULL UNIQUE,
  email    VARCHAR(120) NOT NULL UNIQUE,
  password_hash VARCHAR(255) NOT NULL,
  name VARCHAR(100) NULL,
  -- 설문/건강정보 활용 시 사용
  age INT NULL,
  height FLOAT NULL,
  weight FLOAT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 설문 응답 (FK 테스트용)
CREATE TABLE IF NOT EXISTS survey_answers (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id     BIGINT NOT NULL,
  question_id BIGINT NOT NULL,
  answer_id   BIGINT NOT NULL,
  submitted_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_answers_user FOREIGN KEY (user_id) REFERENCES users(id)
);
