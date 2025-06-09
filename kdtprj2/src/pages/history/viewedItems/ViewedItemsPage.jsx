import React, {useEffect, useState} from "react";
import styles from "./viewedItems.module.css";
import Header from "../../../components/Header/Header.jsx";
import Footer from "../../../components/Footer/Footer.jsx";
import ItemCard from "../../../components/ItemCard/ItemCard.jsx";

function ViewedItemsPage() {
    const [viewItems, setViewItems] = useState([]);

    useEffect(() => {
        const storedViewed = JSON.parse(localStorage.getItem("viewedItems") || "[]");
        setViewItems(storedViewed);
    },[]);

    return (
        <div className={styles.container}>
            <Header />

            <div className={styles.viewedBox}>
                {viewItems.length === 0 ? (
                    <div className={styles.empty}>조회한 상품이 없습니다.</div>
                ) : (
                    viewItems.map(item => (
                        <div className={styles.item} key={item.id}>
                            <ItemCard item={item} />
                        </div>
                    ))
                )}
            </div>
            <Footer />
        </div>
    )
}

export default ViewedItemsPage;
