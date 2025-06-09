import React, { useEffect, useState } from "react";
import axios from "axios";
import styles from "./RegisterForm.module.css";
import { getUserId } from "../../utils/authUtils.js";

// axios 인스턴스 생성
const api = axios.create({
  baseURL: "http://localhost:8080/api",
  timeout: 30000, // 파일 업로드를 위해 타임아웃 증가
  headers: {
    "Content-Type": "application/json",
  },
});

function RegisterForm({ onSubmit, initialItem, onSave }) {
  const [images, setImages] = useState([]);
  const [title, setTitle] = useState("");
  const [price, setPrice] = useState("");
  const [tag, setTag] = useState("");
  const [description, setDescription] = useState("");
  const [condition, setCondition] = useState("");
  const [category, setCategory] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const isTitleOver = title.length > 20;

  // 이미지 업로드 함수 수정
  const uploadImages = async (imageFiles) => {
    try {
      const uploadPromises = imageFiles.map(async (imageData) => {
        if (!imageData.file) {
          return imageData.url;
        }

        const formData = new FormData();
        formData.append("file", imageData.file);

        // ✅ 올바른 엔드포인트로 수정
        const response = await api.post("/upload/image", formData, {
          headers: {
            "Content-Type": "multipart/form-data",
          },
        });

        return response.data.imageUrl;
      });

      return await Promise.all(uploadPromises);
    } catch (err) {
      console.error("이미지 업로드 실패:", err);

      if (err.response?.status === 405) {
        throw new Error("서버에서 이미지 업로드를 지원하지 않습니다.");
      } else if (err.code === "ERR_NETWORK") {
        throw new Error("네트워크 연결을 확인해주세요.");
      } else {
        throw new Error("이미지 업로드에 실패했습니다.");
      }
    }
  };

  // 상품 등록 함수 수정
  const registerItem = async (itemData) => {
    try {
      const response = await api.post("/items", itemData, {
        headers: {
          "Content-Type": "application/json", // ✅ 명시적으로 설정
        },
      });
      return response.data;
    } catch (err) {
      console.error("상품 등록 실패:", err);
      throw new Error(
        err.response?.data?.message || "상품 등록에 실패했습니다."
      );
    }
  };

  // 상품 수정
  const updateItem = async (itemId, itemData) => {
    try {
      const response = await api.put(`/items/${itemId}`, itemData);
      return response.data;
    } catch (err) {
      console.error("상품 수정 실패:", err);
      throw new Error(
        err.response?.data?.message || "상품 수정에 실패했습니다."
      );
    }
  };

  // 이미지 변경
  const handleImageChange = (e) => {
    const files = Array.from(e.target.files);

    // 파일 크기 검증 (5MB 제한)
    const maxSize = 5 * 1024 * 1024; // 5MB
    const oversizedFiles = files.filter((file) => file.size > maxSize);

    if (oversizedFiles.length > 0) {
      alert("파일 크기는 5MB 이하여야 합니다.");
      return;
    }

    const previews = files.map((file) => ({
      file,
      url: URL.createObjectURL(file),
    }));

    setImages((prev) => [...prev, ...previews].slice(0, 10));
    e.target.value = "";
  };

  // 이미지 삭제
  const handleRemoveImage = (index) => {
    setImages((prev) => {
      // 미리보기 URL 해제
      if (prev[index].url.startsWith("blob:")) {
        URL.revokeObjectURL(prev[index].url);
      }
      const updated = [...prev];
      updated.splice(index, 1);
      return updated;
    });
  };

  // 등록/수정 제출
  const handleSubmit = async (e) => {
    e.preventDefault();

    if (!title || !price || !tag || !condition || !description) {
      alert("모든 필수 항목을 입력해주세요.");
      return;
    }

    if (isTitleOver) {
      alert("제목은 20자 이하로 입력해주세요.");
      return;
    }

    if (images.length === 0) {
      alert("최소 1개의 이미지를 업로드해주세요.");
      return;
    }

    setLoading(true);
    setError(null);

    try {
      // 1. 이미지 업로드
      const imageUrls = await uploadImages(images);

      // 2. 태그 배열 생성
      const tagArray = tag
        .split(" ")
        .map((t) => t.trim())
        .filter((t) => t);

      // 3. 상품 데이터 구성 (thumbnail, imageUrl, imageUrls 모두 포함)
      const itemData = {
        title,
        price: price === "" ? 0 : Number(price),
        tags: tagArray,
        value: condition,
        description,
        category: category || "기타",
        imageUrl: imageUrls[0], // ✅ 대표 이미지 (첫 번째 이미지)
        thumbnail: imageUrls[0], // ✅ 썸네일 (대표 이미지와 동일)
        imageUrls: imageUrls, // ✅ 모든 이미지 배열
        status: "판매중",
        sellerId: getUserId(),
        meetLocation: "미정",
      };

      console.log("최종 전송 데이터:", itemData);

      const result = await registerItem(itemData);

      if (initialItem) {
        onSave?.(result);
        alert("상품이 성공적으로 수정되었습니다.");
      } else {
        onSubmit?.(result);
        alert("상품이 성공적으로 등록되었습니다.");
        resetForm();
      }
    } catch (err) {
      console.error("제출 실패:", err);
      setError(err.message);
      alert(err.message);
    } finally {
      setLoading(false);
    }
  };
  // 폼 초기화
  const resetForm = () => {
    setTitle("");
    setPrice("");
    setTag("");
    setDescription("");
    setCondition("");
    setCategory("");
    setImages([]);
    setError(null);
  };

  // 수정모드일 때 값 초기화
  useEffect(() => {
    if (initialItem) {
      setTitle(initialItem.title || "");
      setPrice(initialItem.price || "");
      setTag((initialItem.tags || []).join(" ") || "");
      setDescription(initialItem.description || "");
      setCondition(initialItem.value || "");
      setCategory(initialItem.category || "");

      // 기존 이미지 설정
      const existingImages = [];
      if (initialItem.itemImages && initialItem.itemImages.length > 0) {
        existingImages.push(
          ...initialItem.itemImages.map((url) => ({ url, file: null }))
        );
      } else if (initialItem.imageUrl) {
        existingImages.push({ url: initialItem.imageUrl, file: null });
      }
      setImages(existingImages);
    }
  }, [initialItem]);

  // 컴포넌트 언마운트 시 URL 해제
  useEffect(() => {
    return () => {
      images.forEach((img) => {
        if (img.url.startsWith("blob:")) {
          URL.revokeObjectURL(img.url);
        }
      });
    };
  }, []);

  return (
    <form className={styles.registerContainer} onSubmit={handleSubmit}>
      {error && <div className={styles.error}>오류: {error}</div>}

      <div className={styles.imageContainer}>
        <input
          type="file"
          accept="image/*"
          multiple
          onChange={handleImageChange}
          hidden
          id="imageUploadInput"
          disabled={loading}
        />
        <label htmlFor="imageUploadInput" className={styles.imageGrid}>
          {images.map((img, idx) => (
            <div key={idx} className={styles.imageBox}>
              <img
                src={img.url.startsWith("http") ? img.url : img.url}
                alt={`upload-${idx}`}
                onError={(e) => {
                  e.target.src = "/assets/default-image.png";
                }}
              />
              <button
                type="button"
                className={styles.removeBtn}
                onClick={(e) => {
                  e.preventDefault();
                  handleRemoveImage(idx);
                }}
                disabled={loading}>
                ×
              </button>
            </div>
          ))}
          {images.length < 10 && (
            <div className={styles.imageBox}>
              <span className={styles.addIcon}>＋</span>
            </div>
          )}
        </label>
        <div className={styles.imageCount}>{images.length}/10</div>
      </div>

      <label className={styles.label}>
        <span className={styles.required}>*</span>글 제목
      </label>
      <input
        value={title}
        onChange={(e) => setTitle(e.target.value)}
        maxLength={30}
        disabled={loading}
        placeholder="상품 제목을 입력하세요"
      />
      {isTitleOver && (
        <p className={styles.warning}>20자 이하로 입력해주세요 :)</p>
      )}

      <label className={styles.label}>
        <span className={styles.required}>*</span>카테고리
      </label>
      <select
        value={category}
        onChange={(e) => setCategory(e.target.value)}
        className={styles.select}
        disabled={loading}>
        <option value="">카테고리 선택</option>
        <option value="전자제품">전자제품</option>
        <option value="가구">가구</option>
        <option value="의류">의류</option>
        <option value="도서">도서</option>
        <option value="생활용품">생활용품</option>
        <option value="스포츠/레저">스포츠/레저</option>
        <option value="기타">기타</option>
      </select>

      <label className={styles.label}>
        <span className={styles.required}>*</span>가격
      </label>
      <input
        type="number"
        value={price}
        min={0}
        onChange={(e) => setPrice(e.target.value)}
        disabled={loading}
        placeholder="가격을 입력하세요 (0원은 무료나눔)"
      />

      <label className={styles.label}>
        <span className={styles.required}>*</span>태그
      </label>
      <input
        value={tag}
        onChange={(e) => setTag(e.target.value)}
        placeholder="띄어쓰기로 여러 태그 입력"
        disabled={loading}
      />

      <label className={styles.label}>
        <span className={styles.required}>*</span>사용감
      </label>
      <div className={styles["condition-box"]}>
        {["S", "A", "B", "C", "D"].map((val) => (
          <label key={val} className={styles["radio-btn"]}>
            <input
              type="radio"
              name="condition"
              value={val}
              checked={condition === val}
              onChange={(e) => setCondition(e.target.value)}
              disabled={loading}
            />
            <span>
              {val === "S"
                ? "S-미개봉/새상품"
                : val === "A"
                ? "A-거의 새상품"
                : val === "B"
                ? "B-사용감 적음"
                : val === "C"
                ? "C-사용감 있음"
                : "D-사용감 많음/하자 있음"}
            </span>
          </label>
        ))}
      </div>

      <label className={styles.label}>
        <span className={styles.required}>*</span>설명
      </label>
      <textarea
        className={styles.textarea}
        value={description}
        onChange={(e) => setDescription(e.target.value)}
        disabled={loading}
        placeholder="상품에 대한 자세한 설명을 입력하세요"
        rows={5}
      />

      <button
        className={styles.registerButton}
        type="submit"
        disabled={loading}>
        {loading
          ? initialItem
            ? "수정 중..."
            : "등록 중..."
          : initialItem
          ? "수정하기"
          : "등록하기"}
      </button>
    </form>
  );
}

export default RegisterForm;
