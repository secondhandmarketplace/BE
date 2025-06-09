import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import axios from "axios";
import styles from "./chatList.module.css";
import Header from "../../../components/Header/Header.jsx";
import Footer from "../../../components/Footer/Footer.jsx";

const api = axios.create({
  baseURL: "http://localhost:8080/api",
  timeout: 10000,
  headers: {
    "Content-Type": "application/json",
  },
});

function ChatListPage() {
  const [chatRooms, setChatRooms] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const navigate = useNavigate();

  // ✅ 채팅방 목록 가져오기 (Spring Boot 연동)
  // ✅ 채팅방 목록 가져오기 (에러 처리 강화)
  const fetchChatRooms = async () => {
    try {
      setLoading(true);
      setError(null);

      const userId =
        localStorage.getItem("userId") ||
        localStorage.getItem("senderId") ||
        "guest";

      console.log("채팅방 목록 요청:", userId);

      // ✅ 사용자 ID 유효성 검사
      if (!userId || userId === "guest" || userId === "null") {
        console.warn("유효하지 않은 사용자 ID:", userId);
        setChatRooms([]);
        return;
      }

      // ✅ Spring Boot API 호출 (타임아웃 증가)
      const response = await api.get("/chat/rooms", {
        params: { userId: userId },
        timeout: 15000, // 15초로 증가
      });

      console.log("채팅방 목록 응답:", response.data);

      // ✅ 응답 데이터 유효성 검사
      if (!Array.isArray(response.data)) {
        console.warn("잘못된 응답 형식:", response.data);
        setChatRooms([]);
        return;
      }

      // ✅ 백엔드 데이터를 프론트엔드 형식에 맞게 변환
      const transformedChatRooms = response.data
        .filter((room) => room && room.id) // null 제거
        .map((room) => ({
          id: room.id || room.roomId,
          roomId: room.roomId || room.id,
          nickname: room.otherUserName || room.nickname || "상대방",
          lastMessage: room.lastMessage || "메시지가 없습니다.",
          lastTimestamp:
            room.lastTimestamp || room.updatedAt || new Date().toISOString(),
          imageUrl:
            room.itemImageUrl || room.imageUrl || "/assets/default-image.png",
          itemId: room.itemId,
          itemTitle: room.itemTitle || "상품명 없음",
          itemPrice: room.itemPrice || 0,
          unreadCount: room.unreadCount || 0,
          otherUserId: room.otherUserId || "unknown",
          status: room.status || "active",
        }));

      // ✅ 최근 등록순으로 정렬 [3]
      const sortedRooms = transformedChatRooms.sort(
        (a, b) => new Date(b.lastTimestamp) - new Date(a.lastTimestamp)
      );

      setChatRooms(sortedRooms);

      // 로컬 스토리지에 백업 저장
      localStorage.setItem("chatList", JSON.stringify(sortedRooms));

      console.log("채팅방 목록 설정 완료:", sortedRooms.length, "개");
    } catch (err) {
      console.error("채팅방 목록 조회 실패:", err);

      // ✅ 상세한 에러 메시지
      let errorMessage = "채팅 목록을 불러오는데 실패했습니다.";

      if (err.response?.status === 500) {
        errorMessage = "서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요.";
      } else if (err.response?.status === 404) {
        errorMessage = "채팅 서비스를 찾을 수 없습니다.";
      } else if (err.code === "ECONNREFUSED") {
        errorMessage =
          "서버에 연결할 수 없습니다. 서버가 실행 중인지 확인해주세요.";
      }

      setError(errorMessage);

      // ✅ 에러 시 localStorage 백업 데이터 사용
      try {
        const localData = JSON.parse(localStorage.getItem("chatList") || "[]");
        if (Array.isArray(localData) && localData.length > 0) {
          setChatRooms(localData);
          console.log("로컬 백업 데이터 사용:", localData.length, "개");
        } else {
          setChatRooms([]);
        }
      } catch (parseError) {
        console.error("로컬 데이터 파싱 실패:", parseError);
        setChatRooms([]);
      }
    } finally {
      setLoading(false);
    }
  };

  // ✅ 채팅방 삭제 (Spring Boot 연동)
  const deleteChatRoom = async (roomId, event) => {
    event.stopPropagation();

    if (!window.confirm("채팅방을 삭제하시겠습니까?")) {
      return;
    }

    try {
      const userId =
        localStorage.getItem("userId") || localStorage.getItem("senderId");

      // ✅ Spring Boot API 호출
      const response = await api.delete(`/chat/rooms/${roomId}`, {
        params: { userId: userId },
      });

      if (response.data.success) {
        // 성공 시 로컬 상태에서도 제거
        setChatRooms((prev) => prev.filter((room) => room.roomId !== roomId));
        alert("채팅방이 삭제되었습니다.");
      } else {
        alert(response.data.message || "채팅방 삭제에 실패했습니다.");
      }
    } catch (err) {
      console.error("채팅방 삭제 실패:", err);
      alert("채팅방 삭제에 실패했습니다.");
    }
  };

  // ✅ 읽지 않은 메시지 수 업데이트 (Spring Boot 연동)
  const markAsRead = async (roomId) => {
    try {
      const userId =
        localStorage.getItem("userId") || localStorage.getItem("senderId");

      // ✅ Spring Boot API 호출
      await api.post(`/chat/rooms/${roomId}/read`, null, {
        params: { userId: userId },
      });

      // 로컬 상태에서 읽지 않은 메시지 수 초기화
      setChatRooms((prev) =>
        prev.map((room) =>
          room.roomId === roomId ? { ...room, unreadCount: 0 } : room
        )
      );
    } catch (err) {
      console.error("읽음 처리 실패:", err);
    }
  };

  // ✅ 채팅방 클릭 핸들러
  const handleChatRoomClick = (room) => {
    // 읽지 않은 메시지 읽음 처리
    if (room.unreadCount > 0) {
      markAsRead(room.roomId);
    }

    // 채팅 페이지로 이동
    navigate("/chat", {
      state: {
        ...room,
        roomId: room.roomId,
        itemId: room.itemId,
        otherUserId: room.otherUserId,
      },
    });
  };

  useEffect(() => {
    fetchChatRooms();

    // ✅ 10초마다 채팅방 목록 새로고침 (실시간 업데이트)
    const interval = setInterval(fetchChatRooms, 10000);

    return () => clearInterval(interval);
  }, []);

  if (loading) {
    return (
      <div className={styles.container}>
        <Header />
        <div className={styles.loading}>로딩 중...</div>
        <Footer />
      </div>
    );
  }

  return (
    <div className={styles.container}>
      <Header />
      <div className={styles.chatList}>
        <div className={styles.chatListHeader}>
          <span className={styles.chatListTitle}>채팅 목록 (최신순)</span>
          {error && (
            <div className={styles.error}>
              오류: {error}
              <button onClick={fetchChatRooms} className={styles.retryButton}>
                새로고침
              </button>
            </div>
          )}
        </div>

        {chatRooms.length === 0 ? (
          <div className={styles.emptyMessage}>채팅 목록이 비어있습니다.</div>
        ) : (
          <>
            {chatRooms.map((room) => (
              <div
                key={room.roomId || room.id}
                className={styles.chatRoom}
                onClick={() => handleChatRoomClick(room)}>
                <img
                  src={
                    room.imageUrl?.startsWith("http")
                      ? room.imageUrl
                      : `http://localhost:8080${room.imageUrl}`
                  }
                  alt="상품 이미지"
                  className={styles.mainImage}
                  onError={(e) => {
                    e.target.src = "/assets/default-image.png";
                  }}
                />
                <div className={styles.chatBox}>
                  <div className={styles.chatRoomInfo}>
                    <span className={styles.userName}>{room.nickname}</span>
                    {room.itemTitle && (
                      <span className={styles.itemTitle}>
                        [{room.itemTitle}]
                      </span>
                    )}
                    {room.unreadCount > 0 && (
                      <span className={styles.unreadBadge}>
                        {room.unreadCount}
                      </span>
                    )}
                  </div>
                  <span className={styles.lastMessage}>{room.lastMessage}</span>
                  {room.itemPrice && (
                    <span className={styles.itemPrice}>
                      {room.itemPrice.toLocaleString()}원
                    </span>
                  )}
                </div>
                <div className={styles.chatRoomMeta}>
                  <span className={styles.time}>
                    {new Date(room.lastTimestamp).toLocaleTimeString("ko-KR", {
                      hour: "2-digit",
                      minute: "2-digit",
                    })}
                  </span>
                  <button
                    className={styles.deleteButton}
                    onClick={(e) => deleteChatRoom(room.roomId, e)}
                    title="채팅방 삭제">
                    나가기
                  </button>
                </div>
              </div>
            ))}
          </>
        )}
      </div>
      <Footer />
    </div>
  );
}

export default ChatListPage;
