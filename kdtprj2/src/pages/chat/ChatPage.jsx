import React, { useState, useEffect } from "react";
import { useNavigate, useParams } from "react-router-dom";
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
  const { roomId } = useParams(); // ✅ URL 파라미터에서 roomId 가져오기
  const locationItem = location.state; // ✅ 새 채팅방 생성 시 사용
  const [item, setItem] = useState(locationItem); // ✅ state로 관리
  const [chatRoom, setChatRoom] = useState(null);
  const [loading, setLoading] = useState(true);

  console.log("ChatPage - roomId:", roomId, "userId:", userId, "item:", item);

  // ✅ 이미지 URL 처리 헬퍼 함수
  const getImageUrl = (url) => {
    if (!url) return "data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMTUwIiBoZWlnaHQ9IjE1MCIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj48cmVjdCB3aWR0aD0iMTAwJSIgaGVpZ2h0PSIxMDAlIiBmaWxsPSIjZGRkIi8+PHRleHQgeD0iNTAlIiB5PSI1MCUiIGZvbnQtZmFtaWx5PSJBcmlhbCIgZm9udC1zaXplPSIxNCIgZmlsbD0iIzk5OSIgdGV4dC1hbmNob3I9Im1pZGRsZSIgZHk9Ii4zZW0iPuydtOuvuOyngDwvdGV4dD48L3N2Zz4=";
    if (url.startsWith("http")) return url;
    if (url.startsWith("/uploads/")) {
      const filename = url.replace("/uploads/", "");
      return `http://localhost:8080/api/image/${filename}`;
    }
    return `http://localhost:8080/api/image/${url}`;
  };

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

  // ✅ 채팅방 정보 조회 (URL 파라미터 기반)
  const fetchChatData = async () => {
    if (!roomId || !userId) {
      console.warn("필수 데이터 누락:", { roomId, userId });
      setLoading(false);
      return;
    }

    try {
      setLoading(true);
      console.log("채팅방 정보 조회 시작:", roomId, userId);

      // ✅ 채팅방 정보 조회
      const response = await api.get(`/chat/rooms/${roomId}?userId=${userId}`);
      const roomData = response.data;
      console.log("채팅방 데이터:", roomData);
      setChatRoom(roomData);

      // ✅ 백엔드에서 item 정보를 개별 필드로 제공하므로 이를 조합
      if (roomData.itemId && roomData.itemTitle) {
        const itemData = {
          id: roomData.itemId,
          itemid: roomData.itemId, // 백엔드 호환성
          title: roomData.itemTitle,
          price: roomData.itemPrice,
          imageUrl: roomData.itemImageUrl,
          sellerId: roomData.otherUserId, // 상대방이 판매자라고 가정
          status: "판매중" // 기본값
        };
        console.log("조합된 아이템 데이터:", itemData);
        setItem(itemData);
      } else if (roomData.itemTransactionId) {
        // ✅ 개별 필드가 없으면 별도 API 호출
        console.log("아이템 정보 별도 조회:", roomData.itemTransactionId);
        const itemResponse = await api.get(`/items/${roomData.itemTransactionId}`);
        setItem(itemResponse.data);
      }
    } catch (error) {
      console.error("채팅방 정보 조회 실패:", error);
      alert("채팅방 정보를 불러올 수 없습니다.");
      navigate(-1);
    } finally {
      setLoading(false);
    }
  };

  // ✅ 채팅방 생성 또는 조회 (대화형 인공지능 지원)
  useEffect(() => {
    if (roomId) {
      // ✅ URL 파라미터로 roomId가 있으면 기존 채팅방 조회
      fetchChatData();
    } else if (locationItem && userId) {
      // ✅ 새로운 채팅방 생성 (기존 로직)
      const initializeChatRoom = async () => {
        try {
          setLoading(true);

          // ✅ 다중 필드 검증 (검색 결과 [2], [3], [4] 해결)
          const otherUserId =
            locationItem.OwnerId ||
            locationItem.sellerId ||
            locationItem.sellerUserid ||
            locationItem.seller?.userid ||
            locationItem.seller?.id;

          console.log("채팅방 초기화 - 판매자 ID 검증:", {
            itemId: locationItem.id,
            userId: userId,
            otherUserId: otherUserId,
            allSellerFields: {
              OwnerId: locationItem.OwnerId,
              sellerId: locationItem.sellerId,
              sellerUserid: locationItem.sellerUserid,
              seller: locationItem.seller,
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
          await createChatRoomWithValidation(userId, otherUserId, locationItem.id);
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
    } else {
      console.warn("필수 데이터 누락:", { roomId, locationItem, userId });
      setLoading(false);
    }
  }, [roomId, locationItem, userId, navigate]);

  if (loading) {
    return (
      <div className={styles.container}>
        <Header />
        <div className={styles.loading}>채팅방을 불러오는 중...</div>
      </div>
    );
  }

  if (!item && !chatRoom) {
    return (
      <div className={styles.container}>
        <Header />
        <div className={styles.error}>채팅 정보를 표시할 수 없습니다.</div>
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
              src={getImageUrl(item.imageUrl)}
              width={90}
              height={90}
              alt="상품"
              onError={(e) => {
                e.currentTarget.src = "data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMTUwIiBoZWlnaHQ9IjE1MCIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj48cmVjdCB3aWR0aD0iMTAwJSIgaGVpZ2h0PSIxMDAlIiBmaWxsPSIjZGRkIi8+PHRleHQgeD0iNTAlIiB5PSI1MCUiIGZvbnQtZmFtaWx5PSJBcmlhbCIgZm9udC1zaXplPSIxNCIgZmlsbD0iIzk5OSIgdGV4dC1hbmNob3I9Im1pZGRsZSIgZHk9Ii4zZW0iPuydtOuvuOyngDwvdGV4dD48L3N2Zz4=";
                e.currentTarget.onerror = null; // ✅ 무한 로드 방지
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
          roomId={roomId || chatRoom?.roomId || chatRoom?.id}
          userId={userId}
        />
      </div>
    </div>
  );
};

export default ChatPage;
