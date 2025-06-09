import React, { useState, useEffect, useRef } from "react";
import axios from "axios";
import styles from "./home.module.css";
import Header from "../../../components/Header/Header.jsx";
import Footer from "../../../components/Footer/Footer.jsx";
import ItemCard from "../../../components/ItemCard/ItemCard.jsx";
import { categories } from "../../../utils/categories.js";

// axios 인스턴스 생성 (기본 설정)
const api = axios.create({
  baseURL: "http://localhost:8080/api",
  timeout: 10000,
  headers: {
    "Content-Type": "application/json",
  },
  withCredentials: true,
});

function HomePage() {
  const [items, setItems] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [priceFilter, setPriceFilter] = useState("all");
  const [categoryFilter, setCategoryFilter] = useState("all");
  const [isSearching, setIsSearching] = useState(false);
  const [searchKeyword, setSearchKeyword] = useState("");

  const results = items.filter((item) =>
    item.title.toLowerCase().includes(searchKeyword.toLowerCase())
  );

  // HomePage.jsx에서 getImageUrl 함수 수정
  const getImageUrl = (item) => {
    // 1. itemImages 배열이 있는 경우
    if (item.itemImages && item.itemImages.length > 0) {
      const imageUrl = item.itemImages[0];
      const filename = imageUrl.replace("/uploads/", "");
      // ✅ API 엔드포인트로 통일
      return `http://localhost:8080/api/image/${filename}`;
    }

    // 2. imageUrl이 있는 경우
    if (item.imageUrl) {
      const filename = item.imageUrl.replace("/uploads/", "");
      // ✅ API 엔드포인트로 통일
      return `http://localhost:8080/api/image/${filename}`;
    }

    // 3. thumbnail이 있는 경우
    if (item.thumbnail) {
      const filename = item.thumbnail.replace("/uploads/", "");
      return `http://localhost:8080/api/image/${filename}`;
    }

    // 4. 기본 이미지
    return "/assets/default-image.png";
  };

  // 필터링 로직
  let filteredItems = items.filter((item) => {
    const priceOk =
      priceFilter === "all"
        ? true
        : priceFilter === "free"
        ? item.price === 0
        : true;
    const categoryOk =
      categoryFilter === "all" ? true : item.category === categoryFilter;
    return priceOk && categoryOk;
  });

  if (priceFilter === "max") {
    filteredItems = [...filteredItems].sort((a, b) => b.price - a.price);
  } else if (priceFilter === "min") {
    filteredItems = [...filteredItems].sort((a, b) => a.price - b.price);
  }

  const homeContainerRef = useRef(null);

  const fetchItems = async () => {
    try {
      setLoading(true);
      setError(null);

      console.log("API 요청 시작: /items");
      const response = await api.get("/items");
      console.log("API 응답:", response.data);

      const transformedItems = response.data.map((item) => ({
        id: item.itemid || item.id,
        title: item.title || "제목 없음",
        price: item.price || 0,
        imageUrl: getImageUrl(item),
        tags: item.tags || [],
        condition: item.value || item.condition || "상태 없음",
        status: item.status || "판매중",
        category: item.category || "기타",
        description: item.description || "",
        regdate: item.regDate || item.regdate || new Date().toISOString(),
        seller: item.seller,
      }));

      // ✅ 프론트엔드에서 최근 등록순으로 정렬
      const sortedItems = transformedItems.sort((a, b) => {
        const dateA = new Date(a.regdate);
        const dateB = new Date(b.regdate);
        return dateB - dateA; // 내림차순 (최신순)
      });

      console.log("정렬된 아이템 (최근 등록순):", sortedItems);
      console.log(
        "첫 번째 아이템:",
        sortedItems[0]?.title,
        sortedItems[0]?.regdate
      );

      setItems(sortedItems);
    } catch (err) {
      console.error("아이템 조회 실패:", err);
      // 에러 처리...
    } finally {
      setLoading(false);
    }
  };

  // 검색 기능 (axios 사용)
  const handleSearch = async (keyword) => {
    if (!keyword.trim()) {
      fetchItems(); // 전체 데이터 다시 로드
      return;
    }

    try {
      setLoading(true);
      setError(null);

      const response = await api.get("/items/search", {
        params: {
          keyword: keyword,
        },
      });

      const transformedItems = response.data.map((item) => ({
        id: item.itemid || item.id,
        title: item.title,
        price: item.price,
        imageUrl: getImageUrl(item), // ✅ 개선된 이미지 URL 처리
        tags: item.tags || [],
        condition: item.value || item.condition,
        status: item.status,
        category: item.category,
        description: item.description,
        regdate: item.regDate || item.regdate,
        seller: item.seller,
      }));

      setItems(transformedItems);
    } catch (err) {
      console.error("검색 실패:", err);
      setError(
        err.response?.data?.message || err.message || "검색에 실패했습니다."
      );
    } finally {
      setLoading(false);
    }
  };

  // 카테고리별 필터링 (axios 사용)
  const handleCategoryFilter = async (category) => {
    if (category === "all") {
      fetchItems();
      return;
    }

    try {
      setLoading(true);
      setError(null);

      const response = await api.get("/items/category", {
        params: {
          category: category,
        },
      });

      const transformedItems = response.data.map((item) => ({
        id: item.itemid || item.id,
        title: item.title,
        price: item.price,
        imageUrl: getImageUrl(item), // ✅ 개선된 이미지 URL 처리
        tags: item.tags || [],
        condition: item.value || item.condition,
        status: item.status,
        category: item.category,
        description: item.description,
        regdate: item.regDate || item.regdate,
        seller: item.seller,
      }));

      setItems(transformedItems);
    } catch (err) {
      console.error("카테고리 필터링 실패:", err);
      setError(
        err.response?.data?.message || err.message || "필터링에 실패했습니다."
      );
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchItems();
  }, []);

  // 검색어 변경 시 디바운싱
  useEffect(() => {
    const timeoutId = setTimeout(() => {
      if (isSearching) {
        handleSearch(searchKeyword);
      }
    }, 300);

    return () => clearTimeout(timeoutId);
  }, [searchKeyword, isSearching]);

  return (
    <>
      <div className={styles.container}>
        <Header
          onSearchClick={() => {
            setIsSearching(true);
            setSearchKeyword("");
            setPriceFilter("all");
            setCategoryFilter("all");
          }}
          isSearching={isSearching}
          onExitSearch={() => {
            setIsSearching(false);
            setSearchKeyword("");
            fetchItems(); // 검색 종료 시 전체 데이터 다시 로드
          }}
        />
        <div className={styles.homeContainer} ref={homeContainerRef}>
          {loading && <div className={styles.emptyMessage}>로딩 중...</div>}

          {error && (
            <div className={styles.error}>
              오류 발생: {error}
              <button onClick={fetchItems} className={styles.retryButton}>
                다시 시도
              </button>
            </div>
          )}

          {!loading && !error && (
            <>
              {isSearching ? (
                <>
                  <div className={styles.result}>
                    <input
                      className={styles.input}
                      placeholder="검색어를 입력하세요"
                      value={searchKeyword}
                      onChange={(e) => setSearchKeyword(e.target.value)}
                    />
                    {searchKeyword.trim() === "" ? (
                      <div className={styles.emptyMessage}>
                        검색어를 입력해주세요.
                      </div>
                    ) : results.length === 0 ? (
                      <div className={styles.emptyMessage}>
                        검색 결과가 없습니다.
                      </div>
                    ) : (
                      results.map((item) => (
                        <ItemCard
                          key={item.id}
                          item={item}
                          hideCompleted={true}
                        />
                      ))
                    )}
                  </div>
                </>
              ) : (
                <>
                  <div className={styles.buttonContainer}>
                    <button
                      className={`${styles.button} ${
                        priceFilter === "all" && categoryFilter === "all"
                          ? styles.active
                          : ""
                      }`}
                      onClick={() => {
                        setPriceFilter("all");
                        setCategoryFilter("all");
                        fetchItems();
                        homeContainerRef.current?.scrollTo({
                          top: 0,
                          behavior: "smooth",
                        });
                      }}>
                      전체 보기
                    </button>
                    <button
                      className={`${styles.button} ${
                        priceFilter === "free" ? styles.active : ""
                      }`}
                      onClick={() => {
                        setPriceFilter("free");
                        setCategoryFilter("all");
                        homeContainerRef.current?.scrollTo({
                          top: 0,
                          behavior: "smooth",
                        });
                      }}>
                      무료 나눔
                    </button>

                    {categories.map((category) => (
                      <button
                        key={category}
                        className={`${styles.button} ${
                          categoryFilter === category ? styles.active : ""
                        }`}
                        onClick={() => {
                          setCategoryFilter(category);
                          setPriceFilter("all");
                          handleCategoryFilter(category);
                          homeContainerRef.current?.scrollTo({
                            top: 0,
                            behavior: "smooth",
                          });
                        }}>
                        {category}
                      </button>
                    ))}
                    <button
                      className={`${styles.button} ${
                        priceFilter === "max" ? styles.active : ""
                      }`}
                      onClick={() => {
                        setPriceFilter("max");
                        setCategoryFilter("all");
                        homeContainerRef.current?.scrollTo({
                          top: 0,
                          behavior: "smooth",
                        });
                      }}>
                      최고가
                    </button>
                    <button
                      className={`${styles.button} ${
                        priceFilter === "min" ? styles.active : ""
                      }`}
                      onClick={() => {
                        setPriceFilter("min");
                        setCategoryFilter("all");
                        homeContainerRef.current?.scrollTo({
                          top: 0,
                          behavior: "smooth",
                        });
                      }}>
                      최저가
                    </button>
                  </div>
                  <div className={styles.content}>
                    {filteredItems.length === 0 ? (
                      <div className={styles.emptyMessage}>
                        등록된 물품이 없습니다.
                      </div>
                    ) : (
                      filteredItems.map((item) => (
                        <div
                          key={item.id}
                          className={`${
                            item.status === "거래완료" ? styles.disabled : ""
                          }`}>
                          <ItemCard item={item} />
                        </div>
                      ))
                    )}
                  </div>
                </>
              )}
            </>
          )}
        </div>
        <Footer />
      </div>
    </>
  );
}

export default HomePage;
