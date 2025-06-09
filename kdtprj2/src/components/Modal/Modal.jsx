import React from "react";
import styles from "./Modal.module.css";

function Modal({ onConfirm, onCancel }) {
    return (
        <div className={styles.overlay}>
            <div className={styles.modal}>
                <div className={styles.inputBox}>
                    <p>탈퇴하려면 비밀번호를 입력해주세요.</p>
                    <input
                        type="password"
                        name="passwordConfirm"
                        placeholder="비밀번호를 입력하세요"
                        required
                    />
                </div>
                <div className={styles.btnRow}>
                    <button onClick={onConfirm}>확인</button>
                    <button onClick={onCancel}>취소</button>
                </div>
            </div>
        </div>
    );
}

export default Modal;