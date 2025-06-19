-- 1. 대학교 테이블: 대학교 정보를 저장하는 테이블
CREATE TABLE university (
    university_id   BIGINT NOT NULL PRIMARY KEY AUTO_INCREMENT,    -- 대학교 고유 식별자
    name            VARCHAR(100) NOT NULL,          -- 대학교 이름 (100자 제한)
    email_domain    VARCHAR(50) NOT NULL            -- 대학교 이메일 도메인 (50자 제한)
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. 사용자 테이블: 회원 정보를 저장하는 테이블
CREATE TABLE user (
    user_id           BIGINT NOT NULL PRIMARY KEY AUTO_INCREMENT,  -- 사용자 고유 식별자
    university_id     BIGINT NOT NULL,              -- 소속 대학교 ID
    username          VARCHAR(30) NOT NULL,         -- 로그인 아이디 (30자 제한)
    password          VARCHAR(100) NOT NULL,        -- 암호화된 비밀번호 (100자 제한)
    email             VARCHAR(100) NOT NULL,        -- 이메일 주소 (100자 제한)
    nickname          VARCHAR(30) NOT NULL,         -- 닉네임 (30자 제한)
    is_email_verified BOOLEAN NOT NULL,             -- 이메일 인증 여부
    profile_image_url VARCHAR(200),                 -- 프로필 이미지 URL (200자 제한)
    manner_score      INT NOT NULL,                 -- 매너 점수
    create_at         TIMESTAMP NOT NULL,           -- 계정 생성 시간
    FOREIGN KEY (university_id) REFERENCES university(university_id)
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. 카테고리 테이블: 상품 카테고리 정보를 저장하는 테이블 (계층형 구조)
CREATE TABLE category (
    category_id         BIGINT NOT NULL PRIMARY KEY AUTO_INCREMENT,  -- 카테고리 고유 식별자
    parent_category_id  BIGINT,                       -- 상위 카테고리 ID (계층 구조)
    category_name       VARCHAR(50) NOT NULL,         -- 카테고리명 (50자 제한)
    create_at           TIMESTAMP NOT NULL,           -- 카테고리 생성 시간
    FOREIGN KEY (parent_category_id) REFERENCES category(category_id)
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. 상품 테이블: 판매 상품 정보를 저장하는 테이블
CREATE TABLE product (
    product_id               BIGINT NOT NULL PRIMARY KEY AUTO_INCREMENT,  -- 상품 고유 식별자
    category_id2             BIGINT NOT NULL,              -- 상품 카테고리 ID
    user_id                  BIGINT NOT NULL,              -- 판매자 ID
    ai_predicted_category_id BIGINT,                       -- AI가 예측한 카테고리 ID
    title                    VARCHAR(100) NOT NULL,        -- 상품명 (100자 제한)
    description              TEXT,                         -- 상품 상세 설명
    price                    DECIMAL(10,0),                -- 상품 가격
    status                   VARCHAR(20) NOT NULL,         -- 상품 상태 (판매중, 예약중 등)
    ai_price_min             DECIMAL(10,0),                -- AI 예측 최소 가격
    ai_price_max             DECIMAL(10,0),                -- AI 예측 최대 가격
    view_count               INT,                          -- 조회수
    chat_count               INT,                          -- 채팅 수
    location_info            VARCHAR(100),                 -- 위치 정보 (100자 제한)
    create_at                TIMESTAMP NOT NULL,           -- 상품 등록 시간
    update_at                TIMESTAMP NOT NULL,           -- 상품 정보 수정 시간
    refreshed_at             TIMESTAMP,                    -- 상품 새로고침 시간
    sold_at                  TIMESTAMP,                    -- 판매 완료 시간
    FOREIGN KEY (category_id2) REFERENCES category(category_id),
    FOREIGN KEY (user_id) REFERENCES user(user_id),
    FOREIGN KEY (ai_predicted_category_id) REFERENCES category(category_id)
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 5. 상품 이미지 테이블: 상품의 이미지 정보를 저장하는 테이블
CREATE TABLE productImage (
    image_id      BIGINT NOT NULL PRIMARY KEY AUTO_INCREMENT,  -- 이미지 고유 식별자
    product_id    BIGINT NOT NULL,              -- 상품 ID
    image_url     VARCHAR(200) NOT NULL,        -- 이미지 URL (200자 제한)
    sequence      INT NOT NULL,                 -- 이미지 순서
    uploaded_at   TIMESTAMP NOT NULL,           -- 이미지 업로드 시간
    FOREIGN KEY (product_id) REFERENCES product(product_id)
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 6. 채팅방 테이블: 상품 거래 관련 채팅방 정보를 저장하는 테이블
CREATE TABLE chatroom (
    chatroom_id         BIGINT NOT NULL PRIMARY KEY AUTO_INCREMENT,  -- 채팅방 고유 식별자
    product_id          BIGINT NOT NULL,              -- 관련 상품 ID
    user_id             BIGINT NOT NULL,              -- 첫 번째 사용자 ID
    user2_id            BIGINT NOT NULL,              -- 두 번째 사용자 ID
    create_at           TIMESTAMP NOT NULL,           -- 채팅방 생성 시간
    buyer_unread_count  INT NOT NULL,                 -- 구매자 미읽음 메시지 수
    seller_unread_count INT NOT NULL,                 -- 판매자 미읽음 메시지 수
    FOREIGN KEY (product_id) REFERENCES product(product_id),
    FOREIGN KEY (user_id) REFERENCES user(user_id),
    FOREIGN KEY (user2_id) REFERENCES user(user_id)
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 7. 채팅 메시지 테이블: 채팅방의 메시지 정보를 저장하는 테이블
CREATE TABLE chatmessage (
    chatmessage_id  BIGINT NOT NULL PRIMARY KEY AUTO_INCREMENT,  -- 메시지 고유 식별자
    chatroom_id     BIGINT NOT NULL,              -- 채팅방 ID
    user_id         BIGINT NOT NULL,              -- 발신자 ID
    message_type    VARCHAR(20) NOT NULL,         -- 메시지 타입 (20자 제한)
    message_content TEXT,                         -- 메시지 내용
    sent_at         TIMESTAMP NOT NULL,           -- 메시지 전송 시간
    FOREIGN KEY (chatroom_id) REFERENCES chatroom(chatroom_id),
    FOREIGN KEY (user_id) REFERENCES user(user_id)
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 8. 사용자 활동 로그 테이블: 사용자의 활동 내역을 저장하는 테이블
CREATE TABLE useractivitylog (
    log_id        BIGINT NOT NULL PRIMARY KEY AUTO_INCREMENT,  -- 로그 고유 식별자
    user_id       BIGINT NOT NULL,              -- 사용자 ID
    session_id    VARCHAR(64) NOT NULL,         -- 세션 ID (64자 제한)
    activity_type VARCHAR(30) NOT NULL,         -- 활동 타입 (30자 제한)
    target_id     BIGINT,                       -- 활동 대상 ID
    activity_detail TEXT,                       -- 활동 상세 내용
    create_at     TIMESTAMP NOT NULL,           -- 활동 발생 시간
    FOREIGN KEY (user_id) REFERENCES user(user_id)
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 9. 거래내역 테이블: 상품 거래 정보를 저장하는 테이블
CREATE TABLE transaction (
    transaction_id   BIGINT NOT NULL PRIMARY KEY AUTO_INCREMENT,  -- 거래 고유 식별자
    product_id       BIGINT NOT NULL,              -- 상품 ID
    user_id          BIGINT NOT NULL,              -- 구매자 ID
    final_price      DECIMAL(10,0) NOT NULL,       -- 최종 거래 가격
    transaction_date TIMESTAMP NOT NULL,           -- 거래 완료 시간
    review_id2       BIGINT,                       -- 리뷰 ID
    FOREIGN KEY (product_id) REFERENCES product(product_id),
    FOREIGN KEY (user_id) REFERENCES user(user_id)
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 10. 거래 후기 테이블: 거래 후 리뷰 정보를 저장하는 테이블
CREATE TABLE review (
    review_id      BIGINT NOT NULL PRIMARY KEY AUTO_INCREMENT,  -- 리뷰 고유 식별자
    transaction_id BIGINT NOT NULL,              -- 거래 ID
    user_id        BIGINT NOT NULL,              -- 리뷰 작성자 ID
    rating         INT NOT NULL,                 -- 평점
    content        TEXT,                         -- 리뷰 내용
    create_at      TIMESTAMP NOT NULL,           -- 리뷰 작성 시간
    FOREIGN KEY (transaction_id) REFERENCES transaction(transaction_id),
    FOREIGN KEY (user_id) REFERENCES user(user_id)
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- (transaction 테이블의 review_id2에 대한 외래키는 review 테이블 생성 후 아래처럼 추가)
ALTER TABLE transaction
    ADD CONSTRAINT fk_transaction_review
    FOREIGN KEY (review_id2) REFERENCES review(review_id);

show databases;

create database marketplace_db;