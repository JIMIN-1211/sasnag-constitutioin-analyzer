-- health_app 데이터베이스 생성
CREATE DATABASE IF NOT EXISTS health_app DEFAULT CHARACTER SET utf8mb4;
USE health_app;

-- users 테이블 생성 (로컬 + 소셜 통합)
CREATE TABLE IF NOT EXISTS users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(120) NULL UNIQUE,          -- 소셜 로그인 대비: NULL 허용
    password_hash VARCHAR(255) NULL,         -- 소셜 로그인 대비: NULL 허용
    name VARCHAR(100) NULL,
    gender VARCHAR(10) NULL,
    birth_year INT NULL,
    age INT NULL,
    height FLOAT NULL,
    weight FLOAT NULL,
    bmi FLOAT NULL,
    alert_enabled BOOLEAN NOT NULL DEFAULT FALSE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- ✅ 소셜 로그인 전용
    provider VARCHAR(50) NOT NULL DEFAULT 'local',
    provider_id VARCHAR(255) NULL,
    UNIQUE KEY ux_provider (provider, provider_id)  -- provider + provider_id 조합은 유일
);

-- 설문 응답 (FK 포함)
CREATE TABLE IF NOT EXISTS survey_answers (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    question_id INT NOT NULL,
    answer_id INT NOT NULL,
    submitted_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_survey_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    KEY idx_user_time (user_id, submitted_at)
);

-- 체질 결과 저장
CREATE TABLE IF NOT EXISTS user_constitution (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    constitution_type VARCHAR(20) NOT NULL,
    score FLOAT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    description TEXT NULL,
    CONSTRAINT fk_constitution_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 건강 기록
CREATE TABLE IF NOT EXISTS health_records (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    exercise_records INT NULL,
    sleep_records INT NULL,
    calories INT NULL,
    stress_level INT NULL,
    water_intake_ml INT NULL,
    recorded_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_records_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    KEY idx_records_user_time (user_id, recorded_at)
);

-- 확인용
SHOW TABLES;
DESC users;
DESC survey_answers;
DESC user_constitution;
DESC health_records;
