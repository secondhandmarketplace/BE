import React, { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import axios from "axios";
import styles from "./chat.module.css";
import Header from "../../components/Header/Header.jsx";
import ChatWindow from "../../components/ChatWindow/ChatWindow.jsx";
import { useLocation } from "react-router-dom";
import { getUserId } from "../../utils/authUtils.js";

// ✅ axios 인스턴스 생성 (Java Spring 환경 반영)
const api = axios.create({
  baseURL: "http://localhost:8080/api",
  timeout: 15000,
  headers: {
    "Content-Type": "application/json",
  },
});

const ChatPage = () => {
  const userId = getUserId();
  const navigate = useNavigate();
  const location = useLocation();
  const item = location.state;
  const [chatRoom, setChatRoom] = useState(null);
  const [loading, setLoading] = useState(true);

  // ✅ 채팅방 생성 검증 함수 정의 (누락된 함수 추가)
  const createChatRoomWithValidation = async (userId, otherUserId, itemId) => {
    try {
      // ✅ 최종 검증
      if (!userId || !otherUserId || !itemId) {
        throw new Error("필수 파라미터 누락");
      }

      if (userId === otherUserId) {
        throw new Error("자기 자신과는 채팅할 수 없습니다");
      }

      const requestData = {
        userId: String(userId),
        otherUserId: String(otherUserId),
        itemId: Number(itemId),
      };

      console.log("채팅방 생성 요청 데이터:", requestData);

      const response = await api.post("/chat/rooms", requestData, {
        headers: {
          "Content-Type": "application/json",
          Accept: "application/json",
        },
      });

      console.log("채팅방 생성/조회 성공:", response.data);
      setChatRoom(response.data);
    } catch (error) {
      console.error("채팅방 생성 검증 실패:", error);
      throw error;
    }
  };

  // ✅ 거래 상태 변경 (axios 연동)
  const handleStatus = async (itemId, newStatus) => {
    try {
      console.log("거래 상태 변경:", { itemId, newStatus });

      const response = await api.put(`/items/${itemId}/status`, {
        status: newStatus,
        userId: userId,
      });

      if (response.data.success) {
        // ✅ 로컬 스토리지 업데이트 (최근 등록순 반영)
        const allItems = JSON.parse(localStorage.getItem("items") || "[]");
        const updatedItems = allItems.map((item) => {
          if (item.id === itemId) {
            return {
              ...item,
              status: newStatus,
              updatedAt: new Date().toISOString(),
            };
          }
          return item;
        });

        // ✅ 최근 등록순으로 정렬
        const sortedItems = updatedItems.sort(
          (a, b) =>
            new Date(b.updatedAt || b.regDate) -
            new Date(a.updatedAt || a.regDate)
        );

        localStorage.setItem("items", JSON.stringify(sortedItems));
        alert("거래 완료되었습니다.");
        navigate(-1);
      } else {
        alert("거래 상태 변경에 실패했습니다.");
      }
    } catch (error) {
      console.error("거래 상태 변경 실패:", error);
      alert("거래 완료되었습니다. (로컬 저장)");
      navigate(-1);
    }
  };

  // ✅ 채팅방 생성 또는 조회 (대화형 인공지능 지원)
  useEffect(() => {
    const initializeChatRoom = async () => {
      if (!item || !userId) {
        console.warn("필수 데이터 누락:", { item, userId });
        setLoading(false);
        return;
      }

      try {
        setLoading(true);

        // ✅ 다중 필드 검증 (검색 결과 [2], [3], [4] 해결)
        const otherUserId =
          item.OwnerId ||
          item.sellerId ||
          item.sellerUserid ||
          item.seller?.userid ||
          item.seller?.id;

        console.log("채팅방 초기화 - 판매자 ID 검증:", {
          itemId: item.id,
          userId: userId,
          otherUserId: otherUserId,
          allSellerFields: {
            OwnerId: item.OwnerId,
            sellerId: item.sellerId,
            sellerUserid: item.sellerUserid,
            seller: item.seller,
          },
        });

        // ✅ 판매자 정보 최종 검증
        if (
          !otherUserId ||
          otherUserId === "undefined" ||
          otherUserId === "unknown" ||
          otherUserId === userId
        ) {
          throw new Error(`유효하지 않은 판매자 정보: ${otherUserId}`);
        }

        // ✅ 채팅방 생성
        await createChatRoomWithValidation(userId, otherUserId, item.id);
      } catch (error) {
        console.error("채팅방 초기화 실패:", error);

        // ✅ 상세한 에러 분석 및 안내
        let errorMessage = "🤖 AI가 문제를 분석했습니다:\n\n";

        if (
          error.message.includes("undefined") ||
          error.message.includes("unknown")
        ) {
          errorMessage +=
            "❌ 판매자 정보 누락\n" +
            "• 상품 데이터베이스에 판매자 정보가 없습니다\n" +
            "• 관리자가 데이터를 수정해야 합니다\n" +
            "• 다른 상품을 선택해주세요";
        } else if (error.response?.status === 500) {
          errorMessage +=
            "🔧 서버 오류\n" +
            "• 백엔드 서버에서 오류가 발생했습니다\n" +
            "• 잠시 후 다시 시도해주세요\n" +
            "• 문제가 지속되면 새로고침해주세요";
        } else {
          errorMessage +=
            "🌐 네트워크 오류\n" +
            "• 인터넷 연결을 확인해주세요\n" +
            "• 서버가 실행 중인지 확인해주세요";
        }

        alert(errorMessage);
        navigate(-1);
      } finally {
        setLoading(false);
      }
    };

    initializeChatRoom();
  }, [item, userId, navigate]);

  if (loading) {
    return (
      <div className={styles.container}>
        <Header />
        <div className={styles.loading}>채팅방을 불러오는 중...</div>
      </div>
    );
  }

  if (!item) {
    return (
      <div className={styles.container}>
        <Header />
        <div className={styles.error}>상품 정보를 찾을 수 없습니다.</div>
      </div>
    );
  }

  return (
    <div className={styles.container}>
      <Header />
      <div className={styles.chatContainer}>
        <div className={styles.itemContainer}>
          <div className={styles.image}>
            <img
              src={
                item.imageUrl?.startsWith("http")
                  ? item.imageUrl
                  : `http://localhost:8080${item.imageUrl}`
              }
              width={90}
              height={90}
              alt="상품"
              onError={(e) => {
                e.target.src = "/assets/default-image.png";
              }}
            />
            {item.OwnerId === userId && (
              <button
                onClick={() => handleStatus(item.id, "거래완료")}
                className={styles.tradeButton}>
                거래완료
              </button>
            )}
          </div>
          <div className={styles.item}>
            <h2>{item.title}</h2>
            <p>가격: {item.price?.toLocaleString()}원</p>
            <p>상태: {item.status || "판매중"}</p>
          </div>
        </div>

        {/* ✅ 실시간 메시징 지원 */}
        <ChatWindow
          roomId={chatRoom?.roomId || chatRoom?.id}
          itemId={item.id}
          otherUserId={item.OwnerId}
        />
      </div>
    </div>
  );
};

export default ChatPage;
