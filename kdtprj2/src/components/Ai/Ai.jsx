import React, { useState, useRef, useEffect } from "react";
import { Link } from "react-router-dom";
import "./Ai.css";
import Header from "../Header/Header.jsx";

// 사용자 ID를 localStorage나 context 등에서 가져온다고 가정
function getCurrentUserId() {
  return localStorage.getItem("senderId") || "guest";
}

// ✅ 이미지 URL 처리 함수 (백엔드 API 엔드포인트 사용)
function getImageUrl(imageUrl) {
  if (!imageUrl) {
    return "/assets/default-image.png";
  }

  // 이미 완전한 URL인 경우
  if (imageUrl.startsWith("http")) {
    return imageUrl;
  }

  // /uploads/로 시작하는 경우 - API 엔드포인트로 변환
  if (imageUrl.startsWith("/uploads/")) {
    const filename = imageUrl.replace("/uploads/", "");
    const timestamp = new Date().getTime();
    return `http://localhost:8080/api/image/${filename}?t=${timestamp}`;
  }

  // 파일명만 있는 경우
  if (!imageUrl.startsWith("/")) {
    const timestamp = new Date().getTime();
    return `http://localhost:8080/api/image/${imageUrl}?t=${timestamp}`;
  }

  // 기본 이미지
  return "/assets/default-image.png";
}

// 답변을 예쁘게(줄바꿈, 볼드) 변환
function renderAiAnswer(answer) {
  if (!answer) return null;
  // **bold** 처리, 줄바꿈 처리
  let html = answer
    .replace(/\*\*(.+?)\*\*/g, "<strong>$1</strong>")
    .replace(/\n/g, "<br/>");
  return <div dangerouslySetInnerHTML={{ __html: html }} />;
}

const Ai = () => {
  const userId = getCurrentUserId();
  const STORAGE_KEY = `ai-chat-messages-${userId}`;

  const [prompt, setPrompt] = useState("");
  const [messages, setMessages] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const chatEndRef = useRef(null);

  // 1. 컴포넌트 마운트 시 사용자별 localStorage에서 메시지 불러오기
  useEffect(() => {
    const saved = localStorage.getItem(STORAGE_KEY);
    if (saved) {
      try {
        const parsedMessages = JSON.parse(saved);
        // ✅ 최근 등록순으로 정렬 (사용자 선호사항 반영)
        const sortedMessages = parsedMessages.sort((a, b) => {
          const timeA = a.timestamp || 0;
          const timeB = b.timestamp || 0;
          return timeA - timeB; // 시간순 정렬
        });
        setMessages(sortedMessages);
      } catch (e) {
        console.error("메시지 로드 실패:", e);
        setMessages([]);
      }
    }
  }, [STORAGE_KEY]);

  // 2. 메시지 변경될 때마다 사용자별 localStorage에 저장
  useEffect(() => {
    if (messages.length > 0) {
      localStorage.setItem(STORAGE_KEY, JSON.stringify(messages));
    }
    if (chatEndRef.current) {
      chatEndRef.current.scrollIntoView({ behavior: "smooth" });
    }
  }, [messages, STORAGE_KEY]);

  const handlePromptSubmit = async (e) => {
    e.preventDefault();
    if (!prompt.trim()) return;
    setError(null);

    const timestamp = new Date().getTime();
    const newMessages = [
      ...messages,
      {
        role: "user",
        content: prompt,
        timestamp: timestamp,
      },
    ];
    setMessages(newMessages);
    setLoading(true);

    try {
      const response = await fetch(
        `http://localhost:8080/api/recommendations/chat`,
        {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
          },
          body: JSON.stringify({
            message: prompt,
          }),
        }
      );

      if (!response.ok) {
        const errorText = await response.text();
        throw new Error(`서버 오류 (${response.status}): ${errorText}`);
      }

      const data = await response.json();
      console.log("AI 응답 데이터:", data);

      // ✅ 중복 검사 로깅
      if (data.type === "PRODUCT_RECOMMENDATION") {
        const mainItemId = data.mainItem?.id;
        const recommendedItemIds =
          data.recommendedItems?.map((item) => item.id) || [];

        console.log("메인 상품 ID:", mainItemId);
        console.log("AI 추천 상품 IDs:", recommendedItemIds);

        // 중복 검사
        const hasDuplicate = recommendedItemIds.includes(mainItemId);
        console.log(
          "중복 여부:",
          hasDuplicate ? "❌ 중복 발견!" : "✅ 중복 없음"
        );

        if (hasDuplicate) {
          console.warn("⚠️ 메인 상품과 AI 추천 상품 간 중복이 발견되었습니다!");
        }
      }

      const updatedMessages = [
        ...newMessages,
        {
          role: "ai",
          content: data.response || "답변이 없습니다.",
          timestamp: new Date().getTime(),
          type: data.type || "GENERAL",
          userIntent: data.userIntent, // ✅ 의도 정보 추가
          mainItem: data.mainItem || null,
          recommendedItems: data.recommendedItems || [],
          dataSource: data.dataSource || "INTERNAL_DB_ONLY",
        },
      ];
      setMessages(updatedMessages);
    } catch (err) {
      console.error("AI 요청 실패:", err);
      // 에러 처리...
    } finally {
      setLoading(false);
      setPrompt("");
    }
  };

  // 대화 기록 초기화
  const handleClearHistory = () => {
    setMessages([]);
    localStorage.removeItem(STORAGE_KEY);
  };

  // ✅ 이미지 에러 핸들러
  const handleImageError = (e, itemTitle) => {
    console.error(`이미지 로드 실패: ${itemTitle}`, e.target.src);
    e.target.src = "/assets/default-image.png";
  };

  return (
    <div className="Ai-Container">
      <Header />
      <div className="Ai-ChatArea">
        <div className="Ai-ChatList">
          {messages.length === 0 && (
            <div className="Ai-Empty">
              AI에게 무엇이든 물어보세요!
              <br />
              <small>최근 등록된 상품들을 우선으로 추천해드립니다.</small>
            </div>
          )}
          {messages.map((msg, idx) =>
            msg.role === "user" ? (
              <div className="Ai-ChatRow Ai-UserRow" key={idx}>
                <div className="Ai-UserBubble">{msg.content}</div>
                {msg.timestamp && (
                  <div className="Ai-Timestamp">
                    {new Date(msg.timestamp).toLocaleTimeString()}
                  </div>
                )}
              </div>
            ) : (
              <div className="Ai-ChatRow Ai-AiRow" key={idx}>
                <div className="Ai-AiBubble">
                  {renderAiAnswer(msg.content)}
                  {msg.dataSource && (
                    <div className="Ai-DataSource">
                      📊{" "}
                      {msg.dataSource === "INTERNAL_DB_ONLY"
                        ? "내부 DB 기반"
                        : "외부 데이터 포함"}
                    </div>
                  )}
                </div>

                {/* ✅ 상품 추천일 때만 메인 아이템 표시 */}
                {msg.type === "PRODUCT_RECOMMENDATION" && msg.mainItem && (
                  <div className="Ai-MainItem">
                    <div className="Ai-MainItemLabel">
                      🎯 {getIntentLabel(msg.userIntent || "추천")} 상품
                    </div>
                    <Link
                      to={`/item/${msg.mainItem.id}`}
                      className="Ai-MainItemCard">
                      <img
                        src={getImageUrl(
                          msg.mainItem.image || msg.mainItem.imageUrl
                        )}
                        alt={msg.mainItem.title}
                        className="Ai-MainItemImage"
                        onError={(e) => handleImageError(e, msg.mainItem.title)}
                        loading="lazy"
                      />
                      <div className="Ai-MainItemInfo">
                        <h3 className="Ai-MainItemTitle">
                          {msg.mainItem.title}
                        </h3>
                        <p className="Ai-MainItemPrice">
                          {msg.mainItem.price?.toLocaleString()}원
                        </p>
                        <p className="Ai-MainItemLocation">
                          📍{" "}
                          {msg.mainItem.location ||
                            msg.mainItem.meetLocation ||
                            "위치 미정"}
                        </p>
                        {msg.mainItem.regDate && (
                          <p className="Ai-MainItemDate">
                            📅{" "}
                            {new Date(
                              msg.mainItem.regDate
                            ).toLocaleDateString()}
                          </p>
                        )}
                      </div>
                    </Link>
                  </div>
                )}

                {/* ✅ 상품 추천일 때만 AI 추천 3개 표시 (최근 등록순) */}
                {msg.type === "PRODUCT_RECOMMENDATION" &&
                  msg.recommendedItems &&
                  msg.recommendedItems.length > 0 && (
                    <div className="Ai-RecommendedItems">
                      <div className="Ai-RecommendedLabel">
                        🤖 AI 추천 상품 3개 (최신순)
                      </div>
                      <div className="Ai-RecommendedList">
                        {msg.recommendedItems
                          .sort((a, b) => {
                            // ✅ 최근 등록순으로 정렬 (사용자 선호사항)
                            const dateA = new Date(a.regDate || a.regdate || 0);
                            const dateB = new Date(b.regDate || b.regdate || 0);
                            return dateB - dateA;
                          })
                          .slice(0, 3) // ✅ 정확히 3개만
                          .map((item, itemIdx) => (
                            <Link
                              to={`/item/${item.id}`}
                              key={itemIdx}
                              className="Ai-RecommendedCard">
                              <img
                                src={getImageUrl(item.image || item.imageUrl)}
                                alt={item.title}
                                className="Ai-RecommendedImage"
                                onError={(e) => handleImageError(e, item.title)}
                                loading="lazy"
                              />
                              <div className="Ai-RecommendedInfo">
                                <h4 className="Ai-RecommendedTitle">
                                  {item.title}
                                </h4>
                                <p className="Ai-RecommendedPrice">
                                  {item.price?.toLocaleString()}원
                                </p>
                                {item.regDate && (
                                  <p className="Ai-RecommendedDate">
                                    {new Date(
                                      item.regDate
                                    ).toLocaleDateString()}
                                  </p>
                                )}
                              </div>
                            </Link>
                          ))}
                      </div>
                    </div>
                  )}

                {msg.timestamp && (
                  <div className="Ai-Timestamp">
                    {new Date(msg.timestamp).toLocaleTimeString()}
                  </div>
                )}
              </div>
            )
          )}
          <div ref={chatEndRef} />
        </div>
        {error && <div className="Ai-Error">에러: {error}</div>}
      </div>

      <form className="Ai-PromptForm" onSubmit={handlePromptSubmit}>
        <input
          type="text"
          className="Ai-PromptInput"
          placeholder="상품 추천이나 가격 문의를 해보세요 (최신 상품 우선 추천)"
          value={prompt}
          onChange={(e) => setPrompt(e.target.value)}
          disabled={loading}
        />
        <button
          type="submit"
          className="Ai-PromptBtn"
          disabled={loading || !prompt.trim()}>
          {loading ? "🤖..." : "전송"}
        </button>
      </form>

      <div className="Ai-Controls">
        <button className="Ai-ClearBtn" onClick={handleClearHistory}>
          🗑️ 대화 기록 초기화
        </button>
        <div className="Ai-Info">
          💡 최근 등록된 상품을 우선으로 추천해드립니다
        </div>
      </div>
    </div>
  );
};

function getIntentLabel(intent) {
  switch (intent) {
    case "CHEAPEST":
      return "💰 최저가";
    case "MOST_EXPENSIVE":
      return "💎 최고가";
    case "LATEST":
      return "🆕 최신";
    case "OLDEST":
      return "📅 오래된";
    case "POPULAR":
      return "🔥 인기";
    default:
      return "추천";
  }
}

export default Ai;
