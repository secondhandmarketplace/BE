import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import styles from "./ItemCard.module.css";
import Tag from "../Tag/Tag.jsx";

function ItemCard({ item, hideCompleted = true }) {
  const navigate = useNavigate();
  const [imageError, setImageError] = useState(false);

  // 거래완료 상품은 렌더링하지 않음
  if (hideCompleted && item.status === "거래완료") return null;

  // ✅ 이미지 URL 처리 함수
  const getImageUrl = () => {
    if (imageError) {
      return "/assets/default-image.png";
    }

    if (item.imageUrl) {
      // 1. 이미 완전한 URL인 경우
      if (item.imageUrl.startsWith("http")) {
        return item.imageUrl;
      }

      // 2. /uploads/로 시작하는 경우 - API 엔드포인트로 변환
      if (item.imageUrl.startsWith("/uploads/")) {
        const filename = item.imageUrl.replace("/uploads/", "");
        const timestamp = new Date().getTime();
        return `http://localhost:8080/api/image/${filename}?t=${timestamp}`;
      }

      // 3. 파일명만 있는 경우
      const timestamp = new Date().getTime();
      return `http://localhost:8080/api/image/${item.imageUrl}?t=${timestamp}`;
    }

    // 4. itemImages 배열이 있는 경우
    if (item.itemImages && item.itemImages.length > 0) {
      const imageUrl = item.itemImages[0];
      if (imageUrl.startsWith("/uploads/")) {
        const filename = imageUrl.replace("/uploads/", "");
        const timestamp = new Date().getTime();
        return `http://localhost:8080/api/image/${filename}?t=${timestamp}`;
      }
    }

    // 5. 기본 이미지
    return "/assets/default-image.png";
  };

  // 이미지 로드 에러 핸들러
  const handleImageError = () => {
    console.error("이미지 로드 실패:", item.imageUrl);
    setImageError(true);
  };

  // 이미지 로드 성공 핸들러
  const handleImageLoad = () => {
    console.log("이미지 로드 성공:", getImageUrl());
  };

  const handleClick = () => {
    const viewed = JSON.parse(localStorage.getItem("viewedItems") || "[]");

    // 중복 방지: 이미 있는 item 은 제거하고 맨 뒤에 다시 추가 (최근 조회 기준)
    const filtered = viewed.filter((v) => v.id !== item.id);
    const updated = [...filtered, item]; // 최신 클릭을 맨 뒤로

    localStorage.setItem("viewedItems", JSON.stringify(updated));

    navigate(`/item/${item.id}`, { state: item });
  };

  // 가격 포맷팅
  const formatPrice = (price) => {
    if (price === 0) return "무료 나눔";
    return `${price.toLocaleString()}원`;
  };

  return (
    <div className={styles.card} onClick={handleClick}>
      <div className={styles.imageWrapper}>
        <img
          src={getImageUrl()}
          alt={item.title || "상품"}
          onError={handleImageError}
          onLoad={handleImageLoad}
          loading="lazy"
          style={{
            width: "100%",
            height: "100%",
            objectFit: "cover",
          }}
        />
        {/* 상품 상태 표시 */}
        {item.status === "거래완료" && (
          <div className={styles.statusOverlay}>거래완료</div>
        )}
      </div>
      <div className={styles.content}>
        <p className={styles.title}>{item.title || "제목 없음"}</p>
        <p className={styles.price}>{formatPrice(item.price || 0)}</p>
        <div className={styles.tags}>
          {(item.tags || []).map((tag, idx) => (
            <Tag key={idx} text={tag} />
          ))}
        </div>
        {/* 추가 정보 */}
        <div className={styles.meta}>
          {item.regdate && (
            <span className={styles.date}>
              {new Date(item.regdate).toLocaleDateString()}
            </span>
          )}
        </div>
      </div>
    </div>
  );
}

export default ItemCard;
