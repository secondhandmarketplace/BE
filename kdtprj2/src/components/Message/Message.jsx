import React from 'react';
import styles from './message.module.css';

function Message({ text, isMine, time }) {
    return (
        <div className={isMine ? styles.myRow : styles.otherRow}>
            {isMine && <span className={styles.time}>{time}</span>}
            <div className={`${styles.messageBubble} ${isMine ? styles.myMessage : styles.otherMessage}`}>
                {text}
            </div>
            {!isMine && <span className={styles.time}>{time}</span>}
        </div>
    );
}

export default Message;
