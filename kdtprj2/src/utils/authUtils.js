// authUtils.js
/**
 * ✅ 사용자 ID 조회 (Java Spring [1] 환경 반영)
 */
export const getUserId = () => {
  try {
    const userid =
      localStorage.getItem("userid") ||
      localStorage.getItem("senderId") ||
      sessionStorage.getItem("userid");

    // ✅ 하드코딩된 값 완전 차단
    if (
      !userid ||
      userid === "null" ||
      userid === "undefined" ||
      userid === "1" || // ✅ 하드코딩된 "1" 차단
      userid === "guest"
    ) {
      console.warn("유효하지 않은 사용자 ID 감지:", userid);
      return null;
    }

    console.log("유효한 사용자 ID:", userid);
    return userid;
  } catch (error) {
    console.error("사용자 ID 조회 중 오류:", error);
    return null;
  }
};

/**
 * ✅ 사용자 ID 설정
 */
export const setUserId = (userid) => {
  try {
    if (!userid || userid === "undefined" || userid === "null") {
      console.error("유효하지 않은 사용자 ID:", userid);
      return false;
    }

    localStorage.setItem("userId", userid);
    localStorage.setItem("senderId", userid);
    sessionStorage.setItem("userId", userid);

    console.log("사용자 ID 설정 완료:", userid);
    return true;
  } catch (error) {
    console.error("사용자 ID 설정 중 오류:", error);
    return false;
  }
};

/**
 * ✅ 사용자 정보 전체 설정 (누락된 함수 추가)
 */
export const setUserInfo = (userInfo) => {
  try {
    console.log("사용자 정보 저장 시작:", userInfo);

    if (!userInfo || !userInfo.userid) {
      console.error("유효하지 않은 사용자 정보:", userInfo);
      return false;
    }

    // ✅ 사용자 ID 저장 (최근 등록순 [2] 반영)
    if (userInfo.userid) {
      setUserId(userInfo.userid);
    }

    // ✅ 토큰 저장
    if (userInfo.token) {
      localStorage.setItem("token", userInfo.token);
      sessionStorage.setItem("token", userInfo.token);
    }

    // ✅ 사용자 이름 저장
    if (userInfo.name) {
      localStorage.setItem("userName", userInfo.name);
      sessionStorage.setItem("userName", userInfo.name);
    }

    // ✅ 로그인 시간 저장 (최근 등록순 [2] 정렬용)
    if (userInfo.loginTime) {
      localStorage.setItem("loginTime", userInfo.loginTime);
    } else {
      localStorage.setItem("loginTime", new Date().toISOString());
    }

    // ✅ 실시간 메시징 상태 초기화 (메모리 엔트리 [3] 반영)
    localStorage.setItem("messagingEnabled", "true");

    // ✅ 대화형 AI 설정 (메모리 엔트리 [4] 반영)
    localStorage.setItem("aiEnabled", "true");

    console.log("사용자 정보 저장 완료:", {
      userId: userInfo.userid,
      hasToken: !!userInfo.token,
      hasName: !!userInfo.name,
      loginTime: userInfo.loginTime,
    });

    return true;
  } catch (error) {
    console.error("사용자 정보 저장 중 오류:", error);
    return false;
  }
};

/**
 * ✅ 사용자 정보 조회
 */
export const getUserInfo = () => {
  try {
    const userid = getUserId();
    const token = localStorage.getItem("token");
    const userName = localStorage.getItem("userName");
    const loginTime = localStorage.getItem("loginTime");

    if (!userid) {
      return null;
    }

    return {
      userId: userid,
      token: token,
      name: userName,
      loginTime: loginTime,
      messagingEnabled: localStorage.getItem("messagingEnabled") === "true",
      aiEnabled: localStorage.getItem("aiEnabled") === "true",
    };
  } catch (error) {
    console.error("사용자 정보 조회 중 오류:", error);
    return null;
  }
};

/**
 * ✅ 토큰 관리
 */
export const setToken = (token) => {
  if (token) {
    localStorage.setItem("token", token);
    sessionStorage.setItem("token", token);
    console.log("토큰 저장 완료");
  }
};

export const getToken = () => {
  return localStorage.getItem("token") || sessionStorage.getItem("token");
};

/**
 * ✅ 로그인 상태 확인 (Java Spring [1] 환경)
 */
export const isAuthenticated = () => {
  const userid = getUserId();
  const token = getToken();

  const isAuth = userid && userid !== "null" && userid !== "undefined" && token;
  console.log(
    "로그인 상태 확인:",
    isAuth,
    "userId:",
    userid,
    "hasToken:",
    !!token
  );
  return isAuth;
};

/**
 * ✅ 인증 정보 삭제 (로그아웃)
 */
export const clearAuth = () => {
  try {
    // ✅ localStorage 정리
    localStorage.removeItem("userid");
    localStorage.removeItem("senderId");
    localStorage.removeItem("token");
    localStorage.removeItem("userName");
    localStorage.removeItem("loginTime");
    localStorage.removeItem("messagingEnabled");
    localStorage.removeItem("aiEnabled");

    // ✅ sessionStorage 정리
    sessionStorage.removeItem("userid");
    sessionStorage.removeItem("token");
    sessionStorage.removeItem("userName");

    console.log("인증 정보 삭제 완료");
    return true;
  } catch (error) {
    console.error("인증 정보 삭제 중 오류:", error);
    return false;
  }
};

/**
 * ✅ 사용자별 설정 관리 (대화형 AI [4] 지원)
 */
export const updateUserPreferences = (preferences) => {
  try {
    const userid = getUserId();
    if (!userid) return false;

    const userPrefs = {
      ...preferences,
      userId: userid,
      updatedAt: new Date().toISOString(),
    };

    localStorage.setItem(`userPrefs_${userid}`, JSON.stringify(userPrefs));
    console.log("사용자 설정 업데이트 완료:", userPrefs);
    return true;
  } catch (error) {
    console.error("사용자 설정 업데이트 실패:", error);
    return false;
  }
};

export const getUserPreferences = () => {
  try {
    const userid = getUserId();
    if (!userid) return null;

    const saved = localStorage.getItem(`userPrefs_${userid}`);
    return saved ? JSON.parse(saved) : null;
  } catch (error) {
    console.error("사용자 설정 조회 실패:", error);
    return null;
  }
};
