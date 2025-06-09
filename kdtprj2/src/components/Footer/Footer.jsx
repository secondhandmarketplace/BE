import React from "react";
import { useLocation, Link } from "react-router-dom";
import styles from "./footer.module.css";

function Footer() {
  const location = useLocation();

  if (location.pathname === "/login" || location.pathname === "/signup")
    return null;

  if (
    location.pathname === "/home" ||
    location.pathname.startsWith("/item/") ||
    location.pathname === "/chatList" ||
    location.pathname === "/history/liked" ||
    location.pathname === "/history/viewed" ||
    location.pathname === "/history/purchased" ||
    location.pathname === "/history/sales"
  ) {
    return (
      <footer className={styles.homeFooter}>
        <Link to="/home" className={styles.item}>
          <img src="/home.svg" alt="홈" />
          <span>홈</span>
        </Link>
        <Link to="/ai" className={styles.item}>
          <img src="/aiChat.svg" alt="AI서비스" />
          <span>AI 서비스</span>
        </Link>

        <Link to="/register" className={styles.centerButton}>
          <img src="/register.svg" alt="글추가" />
        </Link>

        <Link to="/chatList" className={styles.item}>
          <img src="/chat.svg" alt="채팅" />
          <span>채팅</span>
        </Link>
        <Link to="/mypage" className={styles.item}>
          <img src="/mypage.svg" alt="마이페이지" />
          <span>프로필</span>
        </Link>
      </footer>
    );
  }

  return <footer className={styles.footer}></footer>;
}

export default Footer;
