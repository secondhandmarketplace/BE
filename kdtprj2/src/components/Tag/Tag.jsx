import React from "react";
import styles from "./Tag.module.css";

function TagComponent({ text }) {
    return <span className={styles.tag}>#{text}</span>;
}

export default TagComponent;
