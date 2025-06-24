import React, { useState, useEffect } from "react";
import { useLocation, useNavigate, useParams } from "react-router-dom";
import axios from "axios";
import Header from "../../../components/Header/Header.jsx";
import Footer from "../../../components/Footer/Footer.jsx";
import styles from "./itemDetail.module.css";
import { makeRoomIdFromItem } from "../../../utils/chatUtils.js";
import ItemCard from "../../../components/ItemCard/ItemCard.jsx";

// axios 인스턴스 생성
const api = axios.create({
  baseURL: "http://localhost:8080/api",
  timeout: 10000,
  headers: {
    "Content-Type": "application/json",
  },
});

function ItemDetailPage() {
  const location = useLocation();
  const navigate = useNavigate();
  const { id } = useParams(); // URL에서 아이템 ID 가져오기

  const [item, setItem] = useState(location.state || null);
  const [relatedItems, setRelatedItems] = useState([]);
  const [loading, setLoading] = useState(!location.state);
  const [error, setError] = useState(null);
  const [current, setCurrent] = useState(0);

  // 아이템 상세 정보 가져오기
  const fetchItemDetail = async (itemId) => {
    try {
      setLoading(true);
      setError(null);

      const response = await api.get(`/items/${itemId}`);

      // 백엔드 데이터를 프론트엔드 형식에 맞게 변환
      const transformedItem = {
        id: response.data.itemid || response.data.id,
        title: response.data.title,
        price: response.data.price,
        imageUrl: response.data.imageUrl,
        images: response.data.itemImages || [response.data.imageUrl],
        tags: response.data.tags || [],
        value: response.data.value || response.data.condition,
        status: response.data.status,
        category: response.data.category,
        description: response.data.description,
        place: response.data.place || response.data.location,
        regdate: response.data.regDate || response.data.regdate,
        seller: response.data.seller,
      };

      setItem(transformedItem);

      // 연관 상품도 함께 가져오기
      fetchRelatedItems(transformedItem.category, transformedItem.id);
    } catch (err) {
      console.error("아이템 상세 조회 실패:", err);
      setError(
        err.response?.data?.message ||
          err.message ||
          "상품 정보를 불러오는데 실패했습니다."
      );
    } finally {
      setLoading(false);
    }
  };

  // 연관 상품 가져오기
  const fetchRelatedItems = async (category, excludeId) => {
    try {
      const response = await api.get("/items/related", {
        params: {
          category: category,
          excludeId: excludeId,
          limit: 5,
        },
      });

      const transformedRelatedItems = response.data.map((item) => ({
        id: item.itemid || item.id,
        title: item.title,
        price: item.price,
        imageUrl:
          item.imageUrl ||
          (item.itemImages && item.itemImages.length > 0
            ? item.itemImages[0]
            : "/assets/default-image.png"),
        tags: item.tags || [],
        value: item.value || item.condition,
        status: item.status,
        category: item.category,
        description: item.description,
        regdate: item.regDate || item.regdate,
        seller: item.seller,
      }));

      setRelatedItems(transformedRelatedItems);
    } catch (err) {
      console.error("연관 상품 조회 실패:", err);
      // 연관 상품 실패는 치명적이지 않으므로 에러 상태는 설정하지 않음
      setRelatedItems([]);
    }
  };

  // 찜하기 기능
  const handleLikeClick = async () => {
    try {
      const userId = localStorage.getItem("userId") || "guest";

      const response = await api.post("/items/like", {
        itemId: item.id,
        userId: userId,
      });

      if (response.data.success) {
        alert("찜 목록에 추가되었습니다.");
      } else {
        alert(response.data.message || "이미 찜한 상품입니다.");
      }
    } catch (err) {
      console.error("찜하기 실패:", err);

      // 백엔드 실패 시 localStorage 백업 사용
      const likedItems = JSON.parse(localStorage.getItem("likedItems") || "[]");
      const exists = likedItems.some((i) => i.id === item.id);
      if (exists) {
        alert("이미 찜한 상품입니다.");
        return;
      }
      likedItems.push(item);
      localStorage.setItem("likedItems", JSON.stringify(likedItems));
      alert("찜 목록에 추가되었습니다.");
    }
  };

  // 조회수 증가
  const incrementViewCount = async (itemId) => {
    try {
      await api.post(`/items/${itemId}/view`);
    } catch (err) {
      console.error("조회수 증가 실패:", err);
      // 조회수 증가 실패는 사용자에게 알리지 않음
    }
  };

  useEffect(() => {
    if (id && !item) {
      // URL에서 ID를 가져온 경우 (직접 접근)
      fetchItemDetail(id);
    } else if (item) {
      // location.state로 데이터를 받은 경우
      fetchRelatedItems(item.category, item.id);
      incrementViewCount(item.id);
    }
  }, [id, item]);

  // 이미지 처리
  const images = item?.images || [item?.imageUrl] || [
      "/assets/default-image.png",
    ];

  // 슬라이더 핸들러
  const prevImg = () =>
    setCurrent((current - 1 + images.length) % images.length);
  const nextImg = () => setCurrent((current + 1) % images.length);

  const handleChatClick = () => {
    // ✅ 채팅방 생성 API를 호출하여 실제 roomId를 받아서 이동
    handleStartChat();
  };

  if (loading) {
    return (
      <div className={styles.container}>
        <Header />
        <div className={styles.loading}>로딩 중...</div>
        <Footer />
      </div>
    );
  }

  if (error) {
    return (
      <div className={styles.container}>
        <Header />
        <div className={styles.error}>
          오류 발생: {error}
          <button
            onClick={() => fetchItemDetail(id)}
            className={styles.retryButton}>
            다시 시도
          </button>
        </div>
        <Footer />
      </div>
    );
  }

  if (!item) {
    return (
      <div className={styles.container}>
        <Header />
        <div className={styles.error}>상품 정보를 찾을 수 없습니다.</div>
        <Footer />
      </div>
    );
  }

  return (
    <div className={styles.container}>
      <Header />
      <div className={styles.detailContainer}>
        {/* 이미지 슬라이더 */}
        <div className={styles["image-slider"]}>
          {images.map((img, idx) => (
            <img
              key={idx}
              src={img.startsWith("http") ? img : `http://localhost:8080${img}`}
              alt={`상품이미지${idx + 1}`}
              className={`${styles["slider-image"]} ${
                current === idx ? styles["fade-in"] : styles["fade-out"]
              }`}
              style={{ left: 0, right: 0 }}
              onError={(e) => {
                e.target.src = "/assets/default-image.png";
              }}
            />
          ))}
          {images.length > 1 && (
            <>
              <button
                className={`${styles["slider-button"]} ${styles.left}`}
                onClick={prevImg}>
                &lt;
              </button>
              <button
                className={`${styles["slider-button"]} ${styles.right}`}
                onClick={nextImg}>
                &gt;
              </button>
            </>
          )}
        </div>
        {images.length > 1 && (
          <div className={styles["indicator-dots"]}>
            {images.map((_, idx) => (
              <div
                key={idx}
                className={`${styles.dot} ${
                  current === idx ? styles.active : ""
                }`}
                onClick={() => setCurrent(idx)}
              />
            ))}
          </div>
        )}

        {/* 카테고리 태그 */}
        <div className={styles["category-tag"]}>{item.category}</div>

        {/* 판매자 정보 */}
        <div className={styles["seller-info"]}>
          <span>판매자:</span>
          <span className={styles["seller-name"]}>
            {item.seller?.name || item.seller?.username || "학생1"}
          </span>
        </div>

        {/* 제목/가격/설명/사용감 */}
        <div className={styles["item-title"]}>{item.title}</div>
        <div className={styles.price}>{item.price?.toLocaleString()}원</div>
        <div className={styles["description-text"]}>{item.description}</div>
        <div className={styles["item-condition"]}>{item.value}</div>
        <div className={styles["info-row"]}>
          <span>거래 장소: {item.place || "미정"}</span>
          <span>태그: {item.tags?.join(", ") || "없음"}</span>
        </div>

        <button className={styles["chat-button"]} onClick={handleChatClick}>
          채팅하기
        </button>
        <button className={styles["like-button"]} onClick={handleLikeClick}>
          찜하기
        </button>

        {/* 연관 상품 */}
        <div className={styles.relatedItem}>
          <div className={styles["related-title"]}>연관된 상품</div>
          {relatedItems.length > 0 ? (
            relatedItems.map((related) => (
              <ItemCard key={related.id} item={related} hideCompleted={true} />
            ))
          ) : (
            <div className={styles.noRelated}>관련 상품이 없습니다.</div>
          )}
        </div>
      </div>
      <Footer />
    </div>
  );
}

export default ItemDetailPage;
