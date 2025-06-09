import React, { useState } from "react";
import styles from "./myPage.module.css";
import { Link, useNavigate } from "react-router-dom";
import Modal from "../../../components/Modal/Modal.jsx";
import { getUserId } from "../../../utils/authUtils.js";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import {
  faChevronLeft,
  faChevronRight,
  faUser,
} from "@fortawesome/free-solid-svg-icons";
import Footer from "../../../components/Footer/Footer.jsx";

function MyPage() {
  const navigate = useNavigate();
  const [isEditing, setIsEditing] = useState(false);
  const [isOpen, setIsOpen] = useState(false);
  const [nickname, setNickname] = useState("");
  const [phone, setPhone] = useState("");
  const [address, setAddress] = useState("");
  const userId = getUserId();

  return (
    <div className={styles.container}>
      <div className={styles["profile-topbar"]}>
        <button className={styles["back-button"]} onClick={() => navigate(-1)}>
          <FontAwesomeIcon
            icon={faChevronLeft}
            className={styles["back-icon"]}
          />
        </button>
        <span className={styles["topbar-title"]}>마이페이지</span>
      </div>

      {!isEditing ? (
        <>
          <div className={styles["profile-info"]}>
            <div className={styles["profile-avatar"]}>
              <FontAwesomeIcon
                icon={faUser}
                className={styles["avatar-icon"]}
              />
            </div>
            <div className={styles["profile-name"]}>{user.nickname}</div>
            <div>{user.school}</div>
            <button
              className={styles["edit-profile-button"]}
              onClick={() => setIsEditing(true)}>
              프로필 수정
            </button>
          </div>

          <div className={styles["profile-menu"]}>
            <div className={styles["menu-item"]}>
              <Link to="/history/viewed" className={styles["menu-link"]}>
                상품 조회 내역
              </Link>
              <FontAwesomeIcon
                icon={faChevronRight}
                className={styles["menu-arrow"]}
              />
            </div>
            <div className={styles["menu-item"]}>
              <Link to="/history/liked" className={styles["menu-link"]}>
                찜 목록
              </Link>
              <FontAwesomeIcon
                icon={faChevronRight}
                className={styles["menu-arrow"]}
              />
            </div>
            <div className={styles["menu-item"]}>
              <Link to="/history/sales" className={styles["menu-link"]}>
                판매 내역
              </Link>
              <FontAwesomeIcon
                icon={faChevronRight}
                className={styles["menu-arrow"]}
              />
            </div>
            <div className={styles["menu-item"]}>
              <Link to="/history/purchased" className={styles["menu-link"]}>
                구매 내역
              </Link>
              <FontAwesomeIcon
                icon={faChevronRight}
                className={styles["menu-arrow"]}
              />
            </div>
          </div>

          <div className={styles["profile-actions"]}>
            <button
              className={styles["logout-btn"]}
              onClick={() => navigate("/")}>
              로그아웃
            </button>
            <button
              className={styles["withdraw-btn"]}
              onClick={() => setIsOpen(true)}>
              회원탈퇴
            </button>
          </div>
        </>
      ) : (
        <form
          className={styles["edit-form"]}
          onSubmit={(e) => {
            e.preventDefault();
            setIsEditing(false);
          }}>
          <div className={styles["edit-row"]}>
            <label className={styles["edit-label"]}>닉네임</label>
            <input
              className={styles["edit-input"]}
              value={nickname}
              onChange={(e) => setNickname(e.target.value)}
            />
          </div>
          <div className={styles["edit-row"]}>
            <label className={styles["edit-label"]}>연락처</label>
            <input
              className={styles["edit-input"]}
              value={phone}
              onChange={(e) => setPhone(e.target.value)}
            />
          </div>
          <div className={styles["edit-row"]}>
            <label className={styles["edit-label"]}>주소</label>
            <input
              className={styles["edit-input"]}
              value={address}
              onChange={(e) => setAddress(e.target.value)}
            />
          </div>
          <div className={styles["edit-btn-row"]}>
            <button type="submit" className={styles["edit-btn"]}>
              저장
            </button>
            <button
              type="button"
              className={`${styles["edit-btn"]} ${styles["cancel"]}`}
              onClick={() => setIsEditing(false)}>
              취소
            </button>
          </div>
        </form>
      )}

      {isOpen && (
        <Modal
          onConfirm={() => {
            localStorage.removeItem("userId");
            // 기타 데이터 정리
            navigate("/login");
          }}
          onCancel={() => setIsOpen(false)}
        />
      )}
    </div>
  );
}

export default MyPage;
