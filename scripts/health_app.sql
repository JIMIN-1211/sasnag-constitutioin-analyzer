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

-- 'calories_burned' (소비 칼로리) 필드 추가
ALTER TABLE health_records
ADD COLUMN IF NOT EXISTS calories_burned INT NULL;

-- 체질별 건강 팁 테이블 생성
CREATE TABLE IF NOT EXISTS constitution_tips (
    id INT PRIMARY KEY AUTO_INCREMENT,
    constitution_type VARCHAR(20) NOT NULL,
    tip_type VARCHAR(20) NOT NULL,
    title VARCHAR(100) NOT NULL,
    content TEXT NOT NULL,
    
    KEY idx_constitution_type (constitution_type)
);

-- 개별 운동 기록 테이블 (운동 타입, 시간, 소모 칼로리 기록)
CREATE TABLE IF NOT EXISTS exercise_logs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    exercise_type_id INT NOT NULL,
    duration_min INT NOT NULL,
    calories_burned_session FLOAT NOT NULL,
    recorded_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_exercise_log_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_exercise_type FOREIGN KEY (exercise_type_id) REFERENCES master_exercise_types(id),
    KEY idx_exercise_user_time (user_id, recorded_at)
);

-- 개별 식단 기록 테이블 (섭취 음식, 양, 섭취 칼로리 기록)
CREATE TABLE IF NOT EXISTS meal_logs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    food_id INT NOT NULL,
    intake_gram FLOAT NOT NULL,
    calories_consumed_session FLOAT NOT NULL
    recorded_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_meal_log_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_meal_food FOREIGN KEY (food_id) REFERENCES master_foods(id),
    KEY idx_meal_user_time (user_id, recorded_at)
);

-- 운동 기준 정보 테이블 (Master Data - 유지)
CREATE TABLE IF NOT EXISTS master_exercise_types (
    id INT PRIMARY KEY AUTO_INCREMENT,
    exercise_name VARCHAR(100) NOT NULL UNIQUE,
    category VARCHAR(50) NOT NULL,
    cal_per_min FLOAT NOT NULL
);

-- 식단 기준 정보 테이블 (Master Data - 유지)
CREATE TABLE IF NOT EXISTS master_foods (
    id INT PRIMARY KEY AUTO_INCREMENT,
    food_name VARCHAR(100) NOT NULL UNIQUE,
    constitution_suitability VARCHAR(20) NOT NULL,
    cal_per_gram FLOAT NOT NULL,
    unit_gram INT NOT NULL DEFAULT 100
);

-- 태양인 (Tae-Yang In): 폐(肺)가 크고 간(肝)이 작음. 상체 발달, 하체 약함.
INSERT IGNORE INTO constitution_tips (constitution_type, tip_type, title, content) VALUES
('태양인', 'diet', '해산물과 채소로 열 식히기', '몸에 열이 많고 간 기능이 약하므로, 담백하고 지방이 적은 해산물(새우, 전복)과 메밀, 채소 등 시원한 음식을 섭취하세요. 맵거나 기름진 육류는 피하는 것이 좋습니다.'),
('태양인', 'exercise', '하체 강화와 지구력 단련', '하체가 약하므로 등산, 걷기 등 하체를 단련하는 운동이 필수입니다. 짧고 강하게 집중력을 요하는 운동이 효과적이며, 운동 중 휴식 시간을 짧게 가져 지구력을 기르세요.');

-- 태음인 (Tae-Eum In): 간(肝)이 크고 폐(肺)가 작음. 허리 발달, 비만 경향.
INSERT IGNORE INTO constitution_tips (constitution_type, tip_type, title, content) VALUES
('태음인', 'diet', '과식 금지 및 고른 영양', '소화 흡수 능력이 매우 좋고 식욕이 왕성하여 과식하기 쉽습니다. 포만감이 느껴지기 전에 식사를 멈추는 습관을 들이세요. 쇠고기, 콩 등 고른 영양 섭취가 중요합니다.'),
('태음인', 'exercise', '규칙적인 땀 배출과 전신 운동', '땀을 흘려 몸의 기운을 순환시키는 것이 가장 중요합니다. 달리기, 수영, 자전거 등 전신을 활용하고 땀을 충분히 내는 규칙적인 유산소 운동이 효과적입니다. 꾸준함이 핵심입니다.');

-- 소양인 (So-Yang In): 비(脾)가 크고 신(腎)이 작음. 가슴 발달, 하체 약함.
INSERT IGNORE INTO constitution_tips (constitution_type, tip_type, title, content) VALUES
('소양인', 'diet', '서늘한 성질의 음식 섭취', '비위에 열이 많아 뜨거운 음식은 피해야 합니다. 열을 내리는 신선한 채소, 오이, 돼지고기, 해삼 등 서늘한 성질의 음식을 섭취하여 속을 편안하게 유지하세요. 보양식은 오히려 독이 될 수 있습니다.'),
('소양인', 'exercise', '하체 단련과 상체 이완', '상체가 발달한 반면 하체가 약하므로 스쿼트, 하이킹 등 하체를 중점적으로 단련하는 운동이 좋습니다. 스트레스를 풀기 위한 명상이나 가벼운 산책도 도움이 됩니다.');

-- 소음인 (So-Eum In): 신(腎)이 크고 비(脾)가 작음. 하체 발달, 소화기관 약함.
INSERT IGNORE INTO constitution_tips (constitution_type, tip_type, title, content) VALUES
('소음인', 'diet', '따뜻한 음식으로 소화기 보호', '소화기관이 매우 약하고 몸이 차기 쉽습니다. 소화가 잘되는 따뜻한 음식 위주로 소식하세요. 닭고기, 찹쌀, 인삼 등 따뜻한 성질의 음식이 좋으며, 찬 음료나 생식, 냉면 등은 피해야 합니다.'),
('소음인', 'exercise', '기운을 보존하는 가벼운 운동', '기운을 쉽게 소모하고 땀을 많이 흘리면 기력이 저하될 수 있습니다. 땀을 적게 흘리는 요가, 가벼운 걷기, 스트레칭 등 체온을 유지하며 천천히 체력을 기르는 운동이 적합합니다.');



-- 운동 기준 데이터 (분당 칼로리 기준)
INSERT IGNORE INTO master_exercise_types (exercise_name, category, cal_per_min) VALUES
('걷기', '생활체육', 4.0),
('달리기', '생활체육', 8.0),
('등산', '생활체육', 6.0),
('자전거', '생활체육', 7.0),
('웨이트 트레이닝', '헬스', 5.0),
('유산소 (일반)', '헬스', 8.0);

-- 체질별 추천 식단 데이터 (예시, 1g당 칼로리 기준)
INSERT IGNORE INTO master_foods (food_name, constitution_suitability, cal_per_gram) VALUES
('메밀', '태양인', 0.092),
('전복', '태양인', 0.082),
('쇠고기 (안심)', '태음인', 0.137),
('콩 (대두)', '태음인', 0.418),
('돼지고기 (목살)', '소양인', 0.205),
('해삼', '소양인', 0.045),
('닭고기 (가슴살)', '소음인', 0.165),
('찹쌀', '소음인', 0.370);

-- 확인용
SHOW TABLES;
DESC users;
DESC survey_answers;
DESC user_constitution;
DESC health_records;

INSERT INTO users (id, username, password_hash, email, name, gender, birth_year, height, weight) VALUES
(10, 'taeyangin_tester', '$2b$10$dummyHashForTest.vH3WJ', 'taeyang@test.com', '이태양', 'male', 1990, 180, 75),
(11, 'soumin_tester', '$2b$10$dummyHashForTest.vH3WJ', 'soumin@test.com', '박소민', 'female', 1995, 160, 50);

-- 3. 체질 정보 (user_constitution) 삽입
-- 태양인 (ID 10) 및 소음인 (ID 11) 체질 확정
INSERT INTO user_constitution (user_id, constitution_type, score) VALUES
(10, '태양인', 15),
(11, '소음인', 15);

-- 4. 오늘의 건강 기록 (health_records) 삽입
INSERT INTO health_records (user_id, calories, exercise_duration, sleep_records, calories_burned, record_date) VALUES
-- 태양인
(10, 2000, 120, 6.5, 700, CURDATE()),
-- 소음인
(11, 1500, 30, 8.0, 150, CURDATE());