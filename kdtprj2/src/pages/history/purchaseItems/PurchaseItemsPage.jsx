import React from "react";
import { useNavigate } from "react-router-dom";
import styles from "./purchaseItems.module.css";
import Header from "../../../components/Header/Header.jsx";
import Footer from "../../../components/Footer/Footer.jsx";
import ItemCard from "../../../components/ItemCard/ItemCard.jsx";
import { getUserId } from "../../../utils/authUtils.js";

function PurchaseItemsPage() {
    const navigate = useNavigate();
    const userId = getUserId();
    const allItems = JSON.parse(localStorage.getItem("items") || "[]");

    const purchasedItems = allItems.filter(
        (item) =>
            item.status === "거래완료" && item.buyerId === userId
    );

      return (
        <div className={styles.container}>
          <Header />
          <div className={styles["purchase-box"]}>
            {purchasedItems.length === 0 ? (
              <div className={styles.empty}>구매한 상품이 없습니다.</div>
            ) : (
              <div className={styles["purchase-items"]}>
                  {purchasedItems.map((item) => (
                      <div
                          key={item.id}
                          onClick={() => navigate(`/item/${item.id}`, { state: item })}
                      >
                          <ItemCard item={item} hideCompleted={false} />
                      </div>
                  ))}
              </div>
            )}
          </div>
          <Footer />
        </div>
      );
}

export default PurchaseItemsPage;
