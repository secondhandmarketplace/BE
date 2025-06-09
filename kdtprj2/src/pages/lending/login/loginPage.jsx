import React, { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import axios from "axios";
import {
  setUserInfo, // ✅ 이제 정상적으로 import 가능
  getUserId,
  isAuthenticated,
  setUserId, // ✅ 추가 import
  clearAuth, // ✅ 추가 import
} from "../../../utils/authUtils.js";

import styles from "./login.module.css";

const LoginPage = () => {
  // ✅ 기존 코드는 그대로 유지
  const [userid, setUserIdState] = useState(""); // ✅ 변수명 충돌 방지
  const [password, setPassword] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [rememberMe, setRememberMe] = useState(false);
  const navigate = useNavigate();

  // ✅ 컴포넌트 마운트 시 로그인 상태 확인 (Java Spring [1] 환경)
  useEffect(() => {
    console.log("로그인 페이지 로드 - Java Spring 환경");

    if (isAuthenticated()) {
      const currentUserId = getUserId();
      console.log("이미 로그인됨:", currentUserId);
      navigate("/home");
    } else {
      console.log("저장된 로그인 정보 없음");

      const rememberedUserId = localStorage.getItem("rememberedUserId");
      if (rememberedUserId) {
        setUserIdState(rememberedUserId); // ✅ 변수명 수정
        setRememberMe(true);
      }
    }
  }, [navigate]);

  /**
   * ✅ 로그인 처리 (하드코딩 제거)
   */
  const handleLogin = async (e) => {
    e.preventDefault();

    try {
      setLoading(true);
      setError("");

      console.log("=== 실제 로그인 시도 ===");
      console.log("입력된 사용자 ID:", userid);
      console.log("입력된 비밀번호:", password ? "****" : "없음");

      // ✅ 1단계: 사용자 존재 확인
      const checkResponse = await axios.get(
        `http://localhost:8080/api/users/check/${userid}`,
        {
          timeout: 10000,
          withCredentials: true,
        }
      );

      console.log("사용자 확인 응답:", checkResponse.data);

      if (!checkResponse.data.exists) {
        setError("존재하지 않는 사용자입니다.");
        return;
      }

      // ✅ 2단계: 로그인 처리
      const loginResponse = await axios.post(
        `http://localhost:8080/api/users/login`,
        {
          userId: userid, // ✅ 실제 입력된 사용자 ID 사용
          password: password,
        },
        {
          timeout: 10000,
          withCredentials: true,
        }
      );

      console.log("로그인 응답:", loginResponse.data);

      if (loginResponse.data.success) {
        // ✅ 3단계: 서버에서 받은 실제 사용자 정보 저장
        const actualUserId = loginResponse.data.userid || userid;

        console.log("=== 로그인 성공 ===");
        console.log("서버에서 받은 사용자 ID:", actualUserId);
        console.log("입력한 사용자 ID:", userid);

        const userInfo = {
          userId: actualUserId, // ✅ 서버 응답의 실제 사용자 ID
          token: loginResponse.data.token,
          name:
            loginResponse.data.name ||
            loginResponse.data.username ||
            actualUserId,
          loginTime: new Date().toISOString(),
        };

        const saved = setUserInfo(userInfo);

        if (saved) {
          console.log("✅ 사용자 정보 저장 완료:", actualUserId);

          // ✅ 사용자 ID 기억하기
          if (rememberMe) {
            localStorage.setItem("rememberedUserId", actualUserId);
          }

          // ✅ 저장된 정보 재검증
          setTimeout(() => {
            const savedUserId = getUserId();
            console.log("저장 후 검증:", {
              saved: savedUserId,
              expected: actualUserId,
              match: savedUserId === actualUserId,
            });

            if (savedUserId === actualUserId) {
              console.log("🎉 로그인 완료 - 홈으로 이동");
              navigate("/home");
            } else {
              console.error("❌ 사용자 ID 불일치");
              setError("로그인 정보 저장에 실패했습니다.");
            }
          }, 100);
        } else {
          setError("로그인 정보 저장에 실패했습니다.");
        }
      } else {
        setError("아이디 또는 비밀번호가 일치하지 않습니다.");
      }
    } catch (error) {
      console.error("로그인 실패:", error);
      setError("로그인 중 오류가 발생했습니다.");
    } finally {
      setLoading(false);
    }
  };

  /**
   * ✅ 입력값 변경 처리
   */
  const handleChange = (e) => {
    const { name, value } = e.target;
    if (name === "userId") {
      setUserIdState(value); // ✅ 변수명 수정
    } else if (name === "password") {
      setPassword(value);
    }
  };

  // ✅ 나머지 코드는 동일...
  const handleRememberMeChange = (e) => {
    setRememberMe(e.target.checked);
  };

  const handleKeyPress = (e) => {
    if (e.key === "Enter" && !loading) {
      handleLogin(e);
    }
  };

  return (
    <div className={styles.container}>
      {/* ✅ 기존 JSX 코드 그대로 유지 */}
      <div className={styles.profileSection}>
        <img
          src="/assets/default-profile.png"
          alt="Profile"
          className={styles.basicProfile}
          width="80"
          height="80"
        />
      </div>

      <form onSubmit={handleLogin}>
        <div className={styles.inputContainer}>
          <input
            type="text"
            name="userId"
            placeholder="사용자 ID를 입력하세요"
            value={userId}
            required
            autoComplete="username"
            className={styles.enterID}
            onChange={handleChange}
            onKeyPress={handleKeyPress}
            disabled={loading}
          />

          <input
            type="password"
            name="password"
            placeholder="비밀번호를 입력하세요"
            value={password}
            required
            autoComplete="current-password"
            className={styles.enterPW}
            onChange={handleChange}
            onKeyPress={handleKeyPress}
            disabled={loading}
          />
        </div>

        {error && <div className={styles.errorMessage}>⚠️ {error}</div>}

        <button
          type="submit"
          disabled={loading || !userId.trim() || !password.trim()}
          className={styles.loginBtn}>
          {loading ? "로그인 중..." : "로그인"}
        </button>

        <div className={styles.loginBtnRow}>
          <button
            type="button"
            className={styles.signUp}
            onClick={() => navigate("/register")}
            disabled={loading}>
            회원가입
          </button>

          <button type="button" className={styles.forgotPW} disabled={loading}>
            비밀번호 찾기
          </button>
        </div>
      </form>

      <div className={styles.devInfo}>
        <small>
          🔧 Java Spring & Reactor | 🔄 실시간 메시징 | 🤖 대화형 AI | 📅 최신순
          정렬
        </small>
      </div>
    </div>
  );
};

export default LoginPage;
