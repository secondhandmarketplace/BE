-- 대학교 데이터
INSERT INTO university (university_id, name, email_domain) VALUES
(1, '서울대학교', 'snu.ac.kr'),
(2, '고려대학교', 'korea.ac.kr');

-- 카테고리 데이터
INSERT INTO category (category_id, parent_category_id, category_name, create_at) VALUES
(1, NULL, '전자기기', NOW()),
(2, 1, '이어폰/헤드폰', NOW()),
(3, 1, '스마트폰', NOW()),
(4, 1, '노트북', NOW());

-- 사용자 데이터
INSERT INTO user (user_id, university_id, username, password, email, nickname, is_email_verified, manner_score, create_at) VALUES
(1, 1, 'user1', '$2a$10$example', 'user1@snu.ac.kr', '판매자1', true, 4, NOW()),
(2, 2, 'user2', '$2a$10$example', 'user2@korea.ac.kr', '판매자2', true, 5, NOW());

-- 상품 데이터
INSERT INTO product (product_id, category_id2, user_id, title, description, price, status, view_count, location_info, create_at, update_at) VALUES
(1, 2, 1, 'AirPods Pro 2세대', '거의 새상품입니다. 사용감 없음', 180000, '판매중', 100, '서울시 강남구 역삼동', NOW(), NOW()),
(2, 2, 1, 'Sony WH-1000XM4', '1년 사용했습니다. 상태 양호', 250000, '판매중', 50, '서울시 서초구 서초동', NOW(), NOW()),
(3, 2, 2, 'AirPods Pro 2세대', '미개봉 새상품입니다', 220000, '판매중', 200, '서울시 강남구 역삼동', NOW(), NOW());

-- 거래 데이터 (review, product, user와 연결)
INSERT INTO transaction (transaction_id, product_id, user_id, final_price, transaction_date) VALUES
(1, 1, 2, 180000, NOW()),
(2, 2, 1, 250000, NOW());

-- 리뷰 데이터
INSERT INTO review (review_id, transaction_id, user_id, rating, content, create_at) VALUES
(1, 1, 1, 5, '매우 만족스러운 거래였습니다', NOW()),
(2, 2, 2, 4, '좋은 거래였습니다', NOW()); 