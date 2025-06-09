import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import Header from "../../../components/Header/Header.jsx";
import Footer from "../../../components/Footer/Footer.jsx";
import styles from "./SalesItems.module.css";
import { getUserId } from "../../../utils/authUtils.js";
import ItemCard from "../../../components/ItemCard/ItemCard.jsx";
import RegisterForm from "../../../components/RegisterForm/RegisterForm.jsx";

function SalesItemsPage() {
  const navigate = useNavigate();
  const [myItems, setMyItems] = useState([]);
  const [filter, setFilter] = useState("판매중");
  const [isEdit, setIsEdit] = useState(false);
  const [selectedItem, setSelectedItem] = useState(null);

  const handleSave = (updatedItem) => {
    const allItems = JSON.parse(localStorage.getItem("items") || "[]");

    const newItems = allItems.map((item) =>
      item.id === updatedItem.id ? updatedItem : item
    );

    localStorage.setItem("items", JSON.stringify(newItems));

    const userId = getUserId();
    setMyItems(newItems.filter((item) => item.OwnerId === userId));
    setIsEdit(false);
    setSelectedItem(null);
    navigate(-1);
  };

  useEffect(() => {
    const userId = getUserId();
    const allItems = JSON.parse(localStorage.getItem("items") || "");

    //확인용 로그
    console.log(
      "로컬스토리지 아이템 상태:",
      allItems.map((i) => i.status)
    );

    const filtered = allItems.filter((item) => item.OwnerId === userId);
    setMyItems(filtered);
  }, []);

  const filteredItems = myItems.filter((item) => item.status === filter);

  return (
    <div className={styles.container}>
      <Header />
      <div className={styles.statusBox}>
        <button
          className={`${styles.onSale} ${
            filter === "판매중" ? styles.active : ""
          }`}
          onClick={() => setFilter("판매중")}>
          판매중
        </button>
        <button
          className={`${styles.complete} ${
            filter === "거래완료" ? styles.active : ""
          }`}
          onClick={() => setFilter("거래완료")}>
          거래완료
        </button>
      </div>
      <div className={styles.itemsBox}>
        {isEdit ? (
          <>
            <RegisterForm initialItem={selectedItem} onSave={handleSave} />
          </>
        ) : (
          <>
            {filteredItems.length === 0 ? (
              <div className={styles.empty}>등록한 상품이 없습니다.</div>
            ) : (
              filteredItems.map((item) => (
                <div
                  className={`${styles.item} ${
                    item.status === "거래완료" ? styles.disabled : ""
                  }`}
                  onClick={() => {
                    if (item.status !== "거래완료") {
                      navigate(`/item/${item.id}`, { state: item });
                    }
                  }}>
                  <ItemCard item={item} hideCompleted={false} />
                  {item.status !== "거래완료" && (
                    <div className={styles.btnRow}>
                      <button
                        onClick={(e) => {
                          e.stopPropagation(); // 부모 div 클릭 방지
                          setIsEdit(true);
                          setSelectedItem(item);
                        }}>
                        수정
                      </button>
                      <button>삭제</button>
                    </div>
                  )}
                </div>
              ))
            )}
          </>
        )}
      </div>
      <Footer />
    </div>
  );
}

export default SalesItemsPage;
