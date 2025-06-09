import React, { useEffect, useState } from "react";
import styles from "./likedItems.module.css";
import Header from "../../../components/Header/Header.jsx";
import Footer from "../../../components/Footer/Footer.jsx";
import ItemCard from "../../../components/ItemCard/ItemCard.jsx";

function LikedItemsPage() {
    const [likedItems, setLikedItems] = useState([]);

    useEffect(() => {
        const stored = JSON.parse(localStorage.getItem("likedItems") || "[]");
        setLikedItems(stored);
    }, []);

    const handleUnlike = (id) => {
        const updated = likedItems.filter(item => item.id !== id);
        setLikedItems(updated);
        localStorage.setItem("likedItems", JSON.stringify(updated));
    }

    return (
        <div className={styles.container}>
            <Header />
                <div className={styles.likedBox}>
                    {likedItems.length === 0 ? (
                        <div className={styles.empty}>찜한 상품이 없습니다.</div>
                    ) : (
                        <div className={styles.likedItem}>
                            {likedItems.map((item) => (
                                <div key={item.id} className={styles.item}>
                                    <ItemCard item={item} />
                                    <button className={styles.unlikeBtn} onClick={() =>handleUnlike(item.id)}>찜해제</button>
                                </div>
                            ))}
                        </div>

                    )}
                </div>
            <Footer />
        </div>
    );
}

export default LikedItemsPage;
