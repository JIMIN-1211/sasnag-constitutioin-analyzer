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

-- ✅ users 테이블 누락 컬럼 자동 추가(팀원도 같은 파일만 실행하면 자동 보정)
DELIMITER //
CREATE PROCEDURE patch_users_table()
BEGIN
  -- gender
  IF NOT EXISTS (
    SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME='users' AND COLUMN_NAME='gender'
  ) THEN
    ALTER TABLE users ADD COLUMN gender VARCHAR(10) NULL;
  END IF;

  -- birth_year
  IF NOT EXISTS (
    SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME='users' AND COLUMN_NAME='birth_year'
  ) THEN
    ALTER TABLE users ADD COLUMN birth_year INT NULL;
  END IF;

  -- bmi
  IF NOT EXISTS (
    SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME='users' AND COLUMN_NAME='bmi'
  ) THEN
    ALTER TABLE users ADD COLUMN bmi FLOAT NULL;
  END IF;

  -- provider / provider_id / 유니크키
  IF NOT EXISTS (
    SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME='users' AND COLUMN_NAME='provider'
  ) THEN
    ALTER TABLE users ADD COLUMN provider VARCHAR(50) NOT NULL DEFAULT 'local';
  END IF;

  IF NOT EXISTS (
    SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME='users' AND COLUMN_NAME='provider_id'
  ) THEN
    ALTER TABLE users ADD COLUMN provider_id VARCHAR(255) NULL;
  END IF;

  -- 유니크 키(있으면 건너뜀)
  IF NOT EXISTS (
    SELECT 1 FROM INFORMATION_SCHEMA.STATISTICS
     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME='users' AND INDEX_NAME='ux_provider'
  ) THEN
    ALTER TABLE users ADD UNIQUE KEY ux_provider (provider, provider_id);
  END IF;
END//
DELIMITER ;

CALL patch_users_table();
DROP PROCEDURE patch_users_table;

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

-- ===== calories_burned 컬럼이 없을 때만 추가 (버전 호환용) =====
DELIMITER //
CREATE PROCEDURE add_col_calories_burned()
BEGIN
  IF NOT EXISTS (
    SELECT 1
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME   = 'health_records'
      AND COLUMN_NAME  = 'calories_burned'
  ) THEN
    ALTER TABLE health_records
      ADD COLUMN calories_burned INT NULL;
  END IF;
END//
DELIMITER ;

CALL add_col_calories_burned();
DROP PROCEDURE add_col_calories_burned;

-- 체질별 건강 팁 테이블 생성
CREATE TABLE IF NOT EXISTS constitution_tips (
    id INT PRIMARY KEY AUTO_INCREMENT,
    constitution_type VARCHAR(20) NOT NULL,
    tip_type VARCHAR(20) NOT NULL,
    title VARCHAR(100) NOT NULL,
    content TEXT NOT NULL,
    KEY idx_constitution_type (constitution_type)
);

-- 체질 설명 테이블 생성
CREATE TABLE IF NOT EXISTS constitution_descriptions (
    id INT PRIMARY KEY AUTO_INCREMENT,
    constitution_type VARCHAR(20) NOT NULL,
    title_ko VARCHAR(100) NOT NULL,
    summary TEXT NOT NULL,
    health_trends TEXT NOT NULL,
    life_management TEXT NOT NULL,
    KEY idx_constitution_type (constitution_type)
);

-- 마스터 테이블(운동, 식단) 먼저 생성
CREATE TABLE IF NOT EXISTS master_exercise_types (
    id INT PRIMARY KEY AUTO_INCREMENT,
    exercise_name VARCHAR(100) NOT NULL UNIQUE,
    category VARCHAR(50) NOT NULL,
    cal_per_min FLOAT NOT NULL
);

CREATE TABLE IF NOT EXISTS master_foods (
    id INT PRIMARY KEY AUTO_INCREMENT,
    food_name VARCHAR(100) NOT NULL UNIQUE,
    constitution_suitability VARCHAR(20) NOT NULL,
    cal_per_gram FLOAT NOT NULL,
    unit_gram INT NOT NULL DEFAULT 100
);

-- 개별 운동 기록 테이블
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

-- 개별 식단 기록 테이블
CREATE TABLE IF NOT EXISTS meal_logs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    food_id INT NOT NULL,
    intake_gram FLOAT NOT NULL,
    calories_consumed_session FLOAT NOT NULL,
    recorded_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_meal_log_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_meal_food FOREIGN KEY (food_id) REFERENCES master_foods(id),
    KEY idx_meal_user_time (user_id, recorded_at)
);

-- 1) 태양인 설명
INSERT INTO constitution_descriptions(constitution_type, title_ko, summary, health_trends, life_management) VALUES
('태양인', '태양인 (太陽人)',
 '활동적이고 진취적이며 리더십이 강한 편입니다. 스트레스에 민감하여 과긴장 시 쉽게 피로해질 수 있습니다.',
 '간 기능은 강하고 폐 기능은 약한 경향이 있습니다. 에너지가 많지만 과로하면 호흡기 질환, 두통, 어깨 결림이 생기기 쉽습니다.',
 '휴식과 수면의 균형을 유지하시기 바랍니다. 과도한 경쟁심을 조절하고 명상, 산책 등으로 스트레스를 완화하시는 것이 좋습니다. 가벼운 유산소 활동을 통해 긴장을 풀어주시기 바랍니다.');

-- 2) 소양인 설명
INSERT INTO constitution_descriptions(constitution_type, title_ko, summary, health_trends, life_management) VALUES
('소양인', '소양인 (少陽人)',
 '외향적이고 열정적이며 사람과의 교류를 즐기는 편입니다. 의욕이 높지만 쉽게 열이 오르고 피로해질 수 있습니다.',
 '위 기능은 강하고 신장 기능은 약한 경향이 있습니다. 상열감이 잘 오르고 얼굴이 붉어지거나 부으며 입이 마를 수 있습니다.',
 '체온 조절에 유의하시고 찬물보다는 미지근한 물을 드시기 바랍니다. 규칙적인 수면을 유지하고 야식과 밤샘을 피하시는 것이 좋습니다. 가벼운 스트레칭이나 명상으로 긴장을 완화하시는 것이 좋습니다.');

-- 3) 태음인 설명
INSERT INTO constitution_descriptions(constitution_type, title_ko, summary, health_trends, life_management) VALUES
('태음인', '태음인 (太陰人)',
 '안정적이고 실용적이며 침착하고 인내심이 있는 편입니다. 체력이 좋지만 대사가 느려 체중 증가가 쉽습니다.',
 '폐 기능은 강하고 간 기능은 약한 경향이 있습니다. 비만, 지방간, 고혈압, 당뇨 등의 대사성 질환에 유의하셔야 합니다.',
 '규칙적인 활동으로 대사를 촉진하시기 바랍니다. 기름진 음식과 과식을 피하고 가벼운 식사를 권장드립니다. 호흡 이완이나 반신욕 등으로 순환을 돕는 것이 좋습니다.');

-- 4) 소음인 설명
INSERT INTO constitution_descriptions(constitution_type, title_ko, summary, health_trends, life_management) VALUES
('소음인', '소음인 (少陰人)',
 '내향적이고 예민하며 세심한 편입니다. 소화력이 약하고 한기에 민감합니다.',
 '소화 및 순환 기능이 약한 경향이 있습니다. 복부 팽만, 수족 냉증, 피로가 잦을 수 있습니다.',
 '규칙적인 식사와 충분한 수면이 필요합니다. 따뜻한 음식을 드시고 찬 음식이나 아이스 음료는 피하시기 바랍니다. 무리하지 말고 휴식과 안정을 우선하시기 바랍니다.');

-- 체질별 건강 팁 데이터
INSERT IGNORE INTO constitution_tips (constitution_type, tip_type, title, content) VALUES
('태양인', 'diet', '해산물과 채소로 열 식히기', '몸에 열이 많고 간 기능이 약하므로, 담백하고 지방이 적은 해산물(새우, 전복)과 메밀, 채소 등 시원한 음식을 섭취하세요. 맵거나 기름진 육류는 피하는 것이 좋습니다.'),
('태양인', 'exercise', '하체 강화와 지구력 단련', '하체가 약하므로 등산, 걷기 등 하체를 단련하는 운동이 필수입니다. 짧고 강하게 집중력을 요하는 운동이 효과적이며, 운동 중 휴식 시간을 짧게 가져 지구력을 기르세요.'),

('태음인', 'diet', '과식 금지 및 고른 영양', '소화 흡수 능력이 매우 좋고 식욕이 왕성하여 과식하기 쉽습니다. 포만감이 느껴지기 전에 식사를 멈추는 습관을 들이세요. 쇠고기, 콩 등 고른 영양 섭취가 중요합니다.'),
('태음인', 'exercise', '규칙적인 땀 배출과 전신 운동', '땀을 흘려 몸의 기운을 순환시키는 것이 가장 중요합니다. 달리기, 수영, 자전거 등 전신을 활용하고 땀을 충분히 내는 규칙적인 유산소 운동이 효과적입니다. 꾸준함이 핵심입니다.'),

('소양인', 'diet', '서늘한 성질의 음식 섭취', '비위에 열이 많아 뜨거운 음식은 피해야 합니다. 열을 내리는 신선한 채소, 오이, 돼지고기, 해삼 등 서늘한 성질의 음식을 섭취하여 속을 편안하게 유지하세요. 보양식은 오히려 독이 될 수 있습니다.'),
('소양인', 'exercise', '하체 단련과 상체 이완', '상체가 발달한 반면 하체가 약하므로 스쿼트, 하이킹 등 하체를 중점적으로 단련하는 운동이 좋습니다. 스트레스를 풀기 위한 명상이나 가벼운 산책도 도움이 됩니다.'),

('소음인', 'diet', '따뜻한 음식으로 소화기 보호', '소화기관이 매우 약하고 몸이 차기 쉽습니다. 소화가 잘되는 따뜻한 음식 위주로 소식하세요. 닭고기, 찹쌀, 인삼 등 따뜻한 성질의 음식이 좋으며, 찬 음료나 생식, 냉면 등은 피해야 합니다.'),
('소음인', 'exercise', '기운을 보존하는 가벼운 운동', '기운을 쉽게 소모하고 땀을 많이 흘리면 기력이 저하될 수 있습니다. 땀을 적게 흘리는 요가, 가벼운 걷기, 스트레칭 등 체온을 유지하며 천천히 체력을 기르는 운동이 적합합니다.');

-- 운동 기준 데이터
INSERT IGNORE INTO master_exercise_types (exercise_name, category, cal_per_min) VALUES
('걷기', '생활체육', 4.0),
('달리기', '생활체육', 8.0),
('등산', '생활체육', 6.0),
('자전거', '생활체육', 7.0),
('웨이트 트레이닝', '헬스', 5.0),
('유산소 (일반)', '헬스', 8.0);

-- 체질별 추천 식단 데이터
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
