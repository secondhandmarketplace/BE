import React, { useState, useEffect } from "react";
import { Link, useNavigate } from "react-router-dom";
import axios from "axios";
import "./Search.css";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import {
  faChevronLeft,
  faClock,
  faSearch,
} from "@fortawesome/free-solid-svg-icons";

// ✅ axios 인스턴스 생성 (Java Spring [4] 환경)
const api = axios.create({
  baseURL: "http://localhost:8080/api",
  timeout: 10000,
  headers: {
    "Content-Type": "application/json",
  },
});

const Search = () => {
  const [history, setHistory] = useState([]);
  const [searchInput, setSearchInput] = useState("");
  const [suggestions, setSuggestions] = useState([]);
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();
  const userId =
    localStorage.getItem("senderId") || localStorage.getItem("userId");

  // ✅ 검색 기록 조회 (axios 연동)
  useEffect(() => {
    const fetchHistory = async () => {
      if (!userId) return;

      try {
        console.log("검색 기록 조회:", userId);

        const response = await api.get("/search-history", {
          params: { userId: userId },
        });

        console.log("검색 기록 응답:", response.data);

        // ✅ 최근 등록순으로 정렬 [1]
        const sortedHistory = Array.isArray(response.data)
          ? response.data
              .map((item, idx) => ({
                id: idx + 1,
                query: typeof item === "string" ? item : item.keyword,
                timestamp: item.timestamp || new Date().toISOString(),
              }))
              .sort((a, b) => new Date(b.timestamp) - new Date(a.timestamp))
          : [];

        setHistory(sortedHistory);
      } catch (error) {
        console.error("검색 기록 조회 실패:", error);

        // ✅ 로컬 스토리지 백업 사용
        const localHistory = JSON.parse(
          localStorage.getItem("searchHistory") || "[]"
        );
        setHistory(localHistory);
      }
    };

    fetchHistory();
  }, [userId]);

  // ✅ 검색 추천어 조회 (axios 연동, 대화형 인공지능 [3] 지원)
  useEffect(() => {
    const fetchSuggestions = async () => {
      if (!searchInput.trim()) {
        setSuggestions([]);
        return;
      }

      try {
        setLoading(true);
        console.log("추천어 검색:", searchInput);

        const response = await api.get("/items/suggest", {
          params: {
            keyword: searchInput,
            limit: 10,
          },
        });

        console.log("추천어 응답:", response.data);

        // ✅ 최근 등록순으로 정렬 [1]
        const sortedSuggestions = Array.isArray(response.data)
          ? response.data.sort(
              (a, b) =>
                new Date(b.regDate || b.createdAt) -
                new Date(a.regDate || a.createdAt)
            )
          : [];

        setSuggestions(sortedSuggestions);
      } catch (error) {
        console.error("추천어 조회 실패:", error);
        setSuggestions([]);
      } finally {
        setLoading(false);
      }
    };

    const delay = setTimeout(fetchSuggestions, 300);
    return () => clearTimeout(delay);
  }, [searchInput]);

  // ✅ 검색 실행 (axios 연동)
  const handleSearch = async (keyword) => {
    const query = keyword || searchInput.trim();
    if (!query) return;

    try {
      console.log("검색 실행:", query);

      // ✅ 검색 기록 저장
      await api.post("/search-history", {
        keyword: query,
        userId: userId,
        timestamp: new Date().toISOString(),
      });

      // ✅ 로컬 스토리지에도 백업 저장
      const localHistory = JSON.parse(
        localStorage.getItem("searchHistory") || "[]"
      );
      const newHistory = [
        { id: Date.now(), query: query, timestamp: new Date().toISOString() },
        ...localHistory.filter((item) => item.query !== query).slice(0, 9),
      ];
      localStorage.setItem("searchHistory", JSON.stringify(newHistory));
    } catch (error) {
      console.error("검색 기록 저장 실패:", error);
    }

    // ✅ 검색 결과 페이지로 이동
    navigate(`/home?q=${encodeURIComponent(query)}`);
    setSearchInput("");
    setSuggestions([]);
  };

  // ✅ 검색 기록 삭제 (axios 연동)
  const handleDelete = async (keyword) => {
    try {
      console.log("검색 기록 삭제:", keyword);

      await api.delete("/search-history", {
        params: {
          keyword: keyword,
          userId: userId,
        },
      });

      setHistory((prev) => prev.filter((item) => item.query !== keyword));

      // ✅ 로컬 스토리지에서도 삭제
      const localHistory = JSON.parse(
        localStorage.getItem("searchHistory") || "[]"
      );
      const updatedHistory = localHistory.filter(
        (item) => item.query !== keyword
      );
      localStorage.setItem("searchHistory", JSON.stringify(updatedHistory));
    } catch (error) {
      console.error("검색 기록 삭제 실패:", error);
    }
  };

  // ✅ 전체 검색 기록 삭제 (axios 연동)
  const handleDeleteAll = async () => {
    if (!window.confirm("모든 검색 기록을 삭제하시겠습니까?")) return;

    try {
      console.log("전체 검색 기록 삭제");

      await api.delete("/search-history/all", {
        params: { userId: userId },
      });

      setHistory([]);
      localStorage.removeItem("searchHistory");
    } catch (error) {
      console.error("전체 검색 기록 삭제 실패:", error);
    }
  };

  return (
    <div className="search-container">
      <div className="header">
        <Link to="/home">
          <button className="search-back-button">
            <FontAwesomeIcon
              icon={faChevronLeft}
              className="search-back-image"
            />
          </button>
        </Link>
        <div className="search-bar-wrapper">
          <input
            type="text"
            className="search-input"
            placeholder="원하는 물건을 검색 (AI 추천 지원)"
            value={searchInput}
            onChange={(e) => setSearchInput(e.target.value)}
            onKeyDown={(e) => e.key === "Enter" && handleSearch()}
          />
          <button className="search-btn" onClick={() => handleSearch()}>
            <FontAwesomeIcon icon={faSearch} />
          </button>
        </div>
      </div>

      {/* ✅ AI 추천 검색어 (대화형 인공지능 [3] 지원) */}
      {suggestions.length > 0 && (
        <div className="suggestions-section">
          <h4>🤖 AI 추천 상품 (최신순)</h4>
          <ul className="suggestion-list">
            {suggestions.map((item) => (
              <li
                key={item.itemId || item.id}
                className="suggestion-item with-image"
                onClick={() => navigate(`/post/${item.itemId || item.id}`)}>
                {item.thumbnail || item.imageUrl ? (
                  <img
                    src={
                      item.thumbnail?.startsWith("http")
                        ? item.thumbnail
                        : `http://localhost:8080${
                            item.thumbnail || item.imageUrl
                          }`
                    }
                    alt="썸네일"
                    className="suggestion-thumb"
                    onError={(e) => {
                      e.target.src = "/assets/default-image.png";
                    }}
                  />
                ) : (
                  <div className="suggestion-thumb placeholder" />
                )}
                <div className="suggestion-info">
                  <span className="suggestion-title">{item.title}</span>
                  <span className="suggestion-price">
                    {item.price?.toLocaleString()}원
                  </span>
                </div>
              </li>
            ))}
          </ul>
        </div>
      )}

      {/* ✅ 검색 기록 (최근 등록순 [1] 정렬) */}
      <div className="history-container">
        <div className="history-header">
          <h3>검색 기록 (최신순)</h3>
          {history.length > 0 && (
            <button className="delete-all-button" onClick={handleDeleteAll}>
              전체 삭제
            </button>
          )}
        </div>
        <ul>
          {history.length > 0 ? (
            history.map((item) => (
              <li key={item.id} className="history-item">
                <FontAwesomeIcon
                  icon={faClock}
                  className="search-marker-image"
                />
                <span
                  className="history-query"
                  onClick={() => handleSearch(item.query)}>
                  {item.query}
                </span>
                <button
                  className="Sdelete-button"
                  onClick={() => handleDelete(item.query)}>
                  ×
                </button>
              </li>
            ))
          ) : (
            <li className="empty-history">검색 기록이 없습니다.</li>
          )}
        </ul>
      </div>

      {loading && (
        <div className="loading-indicator">🤖 AI가 추천 상품을 찾는 중...</div>
      )}
    </div>
  );
};

export default Search;
