-- 1. 대학교 테이블
CREATE TABLE university (
    university_id   BIGINT NOT NULL PRIMARY KEY,
    name            VARCHAR(100) NOT NULL,      -- 대학교 이름: 100자 제한
    email_domain    VARCHAR(50) NOT NULL        -- 이메일 도메인: 50자 제한
);

-- 2. 사용자 테이블
CREATE TABLE user (
    user_id           BIGINT NOT NULL PRIMARY KEY,
    university_id     BIGINT NOT NULL,
    username          VARCHAR(30) NOT NULL,         -- 아이디: 30자 제한
    password          VARCHAR(100) NOT NULL,        -- 암호화된 비밀번호: 100자 제한
    email             VARCHAR(100) NOT NULL,        -- 이메일: 100자 제한
    nickname          VARCHAR(30) NOT NULL,         -- 닉네임: 30자 제한
    is_email_verified BOOLEAN NOT NULL,
    profile_image_url VARCHAR(200),                 -- 프로필 이미지 URL: 200자 제한
    manner_score      INT NOT NULL,
    create_at         TIMESTAMP NOT NULL,
    FOREIGN KEY (university_id) REFERENCES university(university_id)
);

-- 3. 카테고리 테이블 (계층형)
CREATE TABLE category (
    category_id         BIGINT NOT NULL PRIMARY KEY,
    parent_category_id  BIGINT,
    category_name       VARCHAR(50) NOT NULL,       -- 카테고리명: 50자 제한
    create_at           TIMESTAMP NOT NULL,
    FOREIGN KEY (parent_category_id) REFERENCES category(category_id)
);

-- 4. 상품 테이블
CREATE TABLE product (
    product_id               BIGINT NOT NULL PRIMARY KEY,
    category_id2             BIGINT NOT NULL,
    user_id                  BIGINT NOT NULL,
    ai_predicted_category_id BIGINT,
    title                    VARCHAR(100) NOT NULL,     -- 상품명: 100자 제한
    description              TEXT,                      -- 상세 설명: TEXT
    price                    DECIMAL(10,0),
    status                   VARCHAR(20) NOT NULL,      -- 상태: 20자 제한(판매중, 예약중 등)
    ai_price_min             DECIMAL(10,0),
    ai_price_max             DECIMAL(10,0),
    view_count               INT,
    chat_count               INT,
    location_info            VARCHAR(100),              -- 위치 정보: 100자 제한
    create_at                TIMESTAMP NOT NULL,
    update_at                TIMESTAMP NOT NULL,
    refreshed_at             TIMESTAMP,
    sold_at                  TIMESTAMP,
    FOREIGN KEY (category_id2) REFERENCES category(category_id),
    FOREIGN KEY (user_id) REFERENCES user(user_id),
    FOREIGN KEY (ai_predicted_category_id) REFERENCES category(category_id)
);

-- 5. 상품 이미지 테이블
CREATE TABLE productImage (
    image_id      BIGINT NOT NULL PRIMARY KEY,
    product_id    BIGINT NOT NULL,
    image_url     VARCHAR(200) NOT NULL,           -- 이미지 URL: 200자 제한
    sequence      INT NOT NULL,
    uploaded_at   TIMESTAMP NOT NULL,
    FOREIGN KEY (product_id) REFERENCES product(product_id)
);

-- 6. 채팅방 테이블
CREATE TABLE chatroom (
    chatroom_id         BIGINT NOT NULL PRIMARY KEY,
    product_id          BIGINT NOT NULL,
    user_id             BIGINT NOT NULL,
    user2_id            BIGINT NOT NULL,
    create_at           TIMESTAMP NOT NULL,
    buyer_unread_count  INT NOT NULL,
    seller_unread_count INT NOT NULL,
    FOREIGN KEY (product_id) REFERENCES product(product_id),
    FOREIGN KEY (user_id) REFERENCES user(user_id),
    FOREIGN KEY (user2_id) REFERENCES user(user_id)
);

-- 7. 채팅 메시지 테이블
CREATE TABLE chatmessage (
    chatmessage_id  BIGINT NOT NULL PRIMARY KEY,
    chatroom_id     BIGINT NOT NULL,
    user_id         BIGINT NOT NULL,
    message_type    VARCHAR(20) NOT NULL,          -- 메시지 타입: 20자 제한
    message_content TEXT,                          -- 메시지 내용: TEXT
    sent_at         TIMESTAMP NOT NULL,
    FOREIGN KEY (chatroom_id) REFERENCES chatroom(chatroom_id),
    FOREIGN KEY (user_id) REFERENCES user(user_id)
);

-- 8. 사용자 활동 로그 테이블
CREATE TABLE useractivitylog (
    log_id        BIGINT NOT NULL PRIMARY KEY,
    user_id       BIGINT NOT NULL,
    session_id    VARCHAR(64) NOT NULL,            -- 세션ID: 64자 제한
    activity_type VARCHAR(30) NOT NULL,            -- 활동 타입: 30자 제한
    target_id     BIGINT,
    activity_detail TEXT,                          -- 상세 내용: TEXT
    create_at     TIMESTAMP NOT NULL,
    FOREIGN KEY (user_id) REFERENCES user(user_id)
);

-- 9. 거래내역 테이블
CREATE TABLE transaction (
    transaction_id   BIGINT NOT NULL PRIMARY KEY,
    product_id       BIGINT NOT NULL,
    user_id          BIGINT NOT NULL,
    final_price      DECIMAL(10,0) NOT NULL,
    transaction_date TIMESTAMP NOT NULL,
    review_id2       BIGINT,
    FOREIGN KEY (product_id) REFERENCES product(product_id),
    FOREIGN KEY (user_id) REFERENCES user(user_id)
    -- review_id2는 review 테이블 생성 후 추가 가능
);

-- 10. 거래 후기 테이블
CREATE TABLE review (
    review_id      BIGINT NOT NULL PRIMARY KEY,
    transaction_id BIGINT NOT NULL,
    user_id        BIGINT NOT NULL,
    rating         INT NOT NULL,
    content        TEXT,
    create_at      TIMESTAMP NOT NULL,
    FOREIGN KEY (transaction_id) REFERENCES transaction(transaction_id),
    FOREIGN KEY (user_id) REFERENCES user(user_id)
);

-- (transaction 테이블의 review_id2에 대한 외래키는 review 테이블 생성 후 아래처럼 추가)
ALTER TABLE transaction
    ADD CONSTRAINT fk_transaction_review
    FOREIGN KEY (review_id2) REFERENCES review(review_id);
