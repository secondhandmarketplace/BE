import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import axios from "axios";
import styles from "./signup.module.css";
import Header from "../../../components/Header/Header.jsx";

// axios 인스턴스 생성
const api = axios.create({
  baseURL: "http://localhost:8080/api",
  timeout: 10000,
  headers: {
    "Content-Type": "application/json",
  },
});

function SignupPage() {
  const navigate = useNavigate();

  // 폼 상태
  const [formData, setFormData] = useState({
    userid: "",
    name: "",
    email: "",
    phone: "",
    password: "",
    confirmPassword: "",
  });

  // UI 상태
  const [loading, setLoading] = useState(false);
  const [errors, setErrors] = useState({});
  const [validations, setValidations] = useState({
    userid: false,
    email: false,
    phone: false,
  });
  const [checkingDuplicate, setCheckingDuplicate] = useState({
    userid: false,
    email: false,
  });

  // 입력값 변경 핸들러
  const handleInputChange = (field, value) => {
    setFormData((prev) => ({
      ...prev,
      [field]: value,
    }));

    // 에러 메시지 초기화
    if (errors[field]) {
      setErrors((prev) => ({
        ...prev,
        [field]: "",
      }));
    }

    // 실시간 검증
    validateField(field, value);
  };

  // 필드별 실시간 검증
  const validateField = (field, value) => {
    let isValid = false;
    let errorMessage = "";

    switch (field) {
      case "userid":
        const useridRegex = /^[a-zA-Z0-9]{4,20}$/;
        isValid = useridRegex.test(value);
        if (!isValid && value) {
          errorMessage = "아이디는 4-20자의 영문, 숫자만 가능합니다.";
        }
        break;

      case "name":
        isValid = value.trim().length >= 2;
        if (!isValid && value) {
          errorMessage = "이름은 2자 이상 입력해주세요.";
        }
        break;

      case "email":
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        isValid = emailRegex.test(value);
        if (!isValid && value) {
          errorMessage = "올바른 이메일 형식을 입력해주세요.";
        }
        break;

      case "phone":
        const phoneRegex = /^01[0-9]-?[0-9]{4}-?[0-9]{4}$/;
        isValid = phoneRegex.test(value.replace(/-/g, ""));
        if (!isValid && value) {
          errorMessage = "올바른 휴대폰 번호를 입력해주세요.";
        }
        break;

      case "password":
        const passwordRegex =
          /^(?=.*[a-zA-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]{8,}$/;
        isValid = passwordRegex.test(value);
        if (!isValid && value) {
          errorMessage =
            "비밀번호는 8자 이상, 영문, 숫자, 특수문자를 포함해야 합니다.";
        }
        break;

      case "confirmPassword":
        isValid = value === formData.password;
        if (!isValid && value) {
          errorMessage = "비밀번호가 일치하지 않습니다.";
        }
        break;
    }

    setErrors((prev) => ({
      ...prev,
      [field]: errorMessage,
    }));

    if (["userid", "email", "phone"].includes(field)) {
      setValidations((prev) => ({
        ...prev,
        [field]: isValid && !errorMessage,
      }));
    }
  };

  // 아이디 중복 확인
  const checkUserIdDuplicate = async () => {
    if (!formData.userid || errors.userid) {
      setErrors((prev) => ({
        ...prev,
        userid: "올바른 아이디를 입력해주세요.",
      }));
      return;
    }

    setCheckingDuplicate((prev) => ({ ...prev, userid: true }));

    try {
      const response = await api.get(`/auth/check/userid/${formData.userid}`);

      if (!response.data.available) {
        setValidations((prev) => ({ ...prev, userid: true }));
        setErrors((prev) => ({ ...prev, userid: "" }));
      } else {
        setValidations((prev) => ({ ...prev, userid: false }));
        setErrors((prev) => ({
          ...prev,
          userid: "이미 사용중인 아이디입니다.",
        }));
      }
    } catch (err) {
      console.error("아이디 중복 확인 실패:", err);
      setErrors((prev) => ({
        ...prev,
        userid: "아이디 중복 확인 중 오류가 발생했습니다.",
      }));
    } finally {
      setCheckingDuplicate((prev) => ({ ...prev, userid: false }));
    }
  };

  // 이메일 중복 확인
  const checkEmailDuplicate = async () => {
    if (!formData.email) {
      setErrors((prev) => ({
        ...prev,
        email: "올바른 이메일을 입력해주세요.",
      }));
      return false; // 불리언 값 반환
    }

    // 중복 확인 전에 에러 메시지 초기화
    setErrors((prev) => ({
      ...prev,
      email: "",
    }));

    setCheckingDuplicate((prev) => ({ ...prev, email: true }));

    try {
      const response = await api.get(
          `/auth/check-email/${encodeURIComponent(formData.email)}`
      );

      if (response.data.exists) {
        setValidations((prev) => ({ ...prev, email: false }));
        setErrors((prev) => ({
          ...prev,
          email: "이미 사용중인 이메일입니다.",
        }));
        return false; // 중복된 이메일
      } else {
        setValidations((prev) => ({ ...prev, email: true }));
        setErrors((prev) => ({
          ...prev,
          email: "",
        }));
        return true; // 사용 가능한 이메일
      }
    } catch (err) {
      console.error("이메일 중복 확인 실패:", err);
      setErrors((prev) => ({
        ...prev,
        email: "이메일 중복 확인 중 오류가 발생했습니다.",
      }));
      return false; // 오류 발생 시 false 반환
    } finally {
      setCheckingDuplicate((prev) => ({ ...prev, email: false }));
    }
  };

  // 휴대폰 번호 포맷팅
  const formatPhoneNumber = (value) => {
    const numbers = value.replace(/[^\d]/g, "");
    if (numbers.length <= 3) return numbers;
    if (numbers.length <= 7)
      return `${numbers.slice(0, 3)}-${numbers.slice(3)}`;
    return `${numbers.slice(0, 3)}-${numbers.slice(3, 7)}-${numbers.slice(
      7,
      11
    )}`;
  };

  // 회원가입 제출
  const handleSignup = async (e) => {
    e.preventDefault();

    // 필수 필드 확인
    const requiredFields = [
      "userid",
      "name",
      "email",
      "phone",
      "password",
      "confirmPassword",
    ];
    const emptyFields = requiredFields.filter(
        (field) => !formData[field].trim()
    );

    if (emptyFields.length > 0) {
      setErrors((prev) => ({
        ...prev,
        form: "모든 필드를 입력해주세요.",
      }));
      return;
    }

    // 에러 확인
    const hasErrors = Object.values(errors).some((error) => error !== "");
    if (hasErrors) {
      setErrors((prev) => ({
        ...prev,
        form: "입력 정보를 다시 확인해주세요.",
      }));
      return;
    }

    // 중복 확인
    const isEmailValid = await checkEmailDuplicate();
    if (!isEmailValid) {
      setErrors((prev) => ({
        ...prev,
        form: "이메일 중복 확인을 해주세요.",
      }));
      return;
    }

    const isUserIdValid = validations.userid;
    if (!isUserIdValid) {
      setErrors((prev) => ({
        ...prev,
        form: "아이디 중복 확인을 해주세요.",
      }));
      return;
    }

    setLoading(true);

    try {
      const signupData = {
        userid: formData.userid,
        name: formData.name,
        email: formData.email,
        phone: formData.phone.replace(/-/g, ""), // 하이픈 제거
        password: formData.password,
        status: "active",
        mannerScore: 5.0, // 기본 매너점수
      };

      const response = await api.post("/auth/signup", signupData);

      if (response.data && response.data.success) {
        setErrors((prev) => ({
          ...prev,
          form: "회원가입이 완료되었습니다!",
        }));
        navigate("/login");
      } else {
        setErrors((prev) => ({
          ...prev,
          form: response.data.message || "회원가입에 실패했습니다.",
        }));
      }
    } catch (err) {
      console.error("회원가입 실패:", err);

      if (err.response) {
        const status = err.response.status;
        const message = err.response.data?.message || err.response.data?.error;

        if (status === 409) {
          setErrors((prev) => ({
            ...prev,
            form: "이미 존재하는 사용자입니다.",
          }));
        } else if (status === 400) {
          setErrors((prev) => ({
            ...prev,
            form: message || "입력 정보를 확인해주세요.",
          }));
        } else {
          setErrors((prev) => ({
            ...prev,
            form: message || "회원가입 중 오류가 발생했습니다.",
          }));
        }
      } else {
        setErrors((prev) => ({
          ...prev,
          form: "네트워크 오류가 발생했습니다.",
        }));
      }
    } finally {
      setLoading(false);
    }
  };

  // 회원가입 버튼 활성화 조건
  const isSignupDisabled = () => {
    return (
      loading ||
      !formData.name.trim() ||
      !formData.phone.trim() ||
      !formData.password.trim() ||
      !formData.confirmPassword.trim() ||
      Object.values(errors).some((error) => error !== "")
    );
  };

  return (
    <div className={styles.container}>
      <Header />
      <div className={styles.signupContainer}>
        <form onSubmit={handleSignup}>
          {/* 아이디 */}
          <div className={styles.labeledBox}>
            <div className={styles.boxLabel}>아이디</div>
            <div className={styles["info-input-box"]}>
              <div className={styles["input-row"]}>
                <input
                  type="text"
                  placeholder="아이디를 입력하세요"
                  value={formData.userid}
                  onChange={(e) => handleInputChange("userid", e.target.value)}
                  disabled={loading}
                />
                <button
                  type="button"
                  onClick={checkUserIdDuplicate}
                  disabled={
                    loading || checkingDuplicate.userid || !formData.userid
                  }>
                  {checkingDuplicate.userid ? "확인중..." : "중복확인"}
                </button>
              </div>
              {errors.userid && (
                <div className={styles.message}>{errors.userid}</div>
              )}
              {validations.userid && (
                <div className={styles.userId}>사용 가능한 아이디입니다.</div>
              )}
            </div>
          </div>

          {/* 이름 */}
          <div className={styles.labeledBox}>
            <div className={styles.boxLabel}>이름</div>
            <div className={styles["info-input-box"]}>
              <input
                type="text"
                placeholder="이름을 입력하세요"
                value={formData.name}
                onChange={(e) => handleInputChange("name", e.target.value)}
                disabled={loading}
              />
              {errors.name && (
                <div className={styles.message}>{errors.name}</div>
              )}
            </div>
          </div>

          {/* 이메일 */}
          <div className={styles.labeledBox}>
            <div className={styles.boxLabel}>이메일</div>
            <div className={styles["info-input-box"]}>
              <div className={styles["input-row"]}>
                <input
                  type="email"
                  placeholder="이메일을 입력하세요"
                  value={formData.email}
                  onChange={(e) => handleInputChange("email", e.target.value)}
                  disabled={loading}
                />
                <button
                  type="button"
                  onClick={checkEmailDuplicate}
                  disabled={
                    loading || checkingDuplicate.email || !formData.email
                  }>
                  {checkingDuplicate.email ? "확인중..." : "중복확인"}
                </button>
              </div>
              {errors.email && (
                <div className={styles.message}>{errors.email}</div>
              )}
              {validations.email && (
                <div className={styles.userId}>사용 가능한 이메일입니다.</div>
              )}
            </div>
          </div>

          {/* 휴대폰 번호 */}
          <div className={styles.labeledBox}>
            <div className={styles.boxLabel}>휴대폰 번호</div>
            <div className={styles["info-input-box"]}>
              <input
                type="tel"
                placeholder="휴대폰 번호를 입력하세요"
                value={formData.phone}
                onChange={(e) => {
                  const formatted = formatPhoneNumber(e.target.value);
                  handleInputChange("phone", formatted);
                }}
                disabled={loading}
                maxLength={13}
              />
              {errors.phone && (
                <div className={styles.message}>{errors.phone}</div>
              )}
            </div>
          </div>

          {/* 비밀번호 */}
          <div className={styles.labeledBox}>
            <div className={styles.boxLabel}>비밀번호</div>
            <div className={styles["info-input-box"]}>
              <input
                type="password"
                placeholder="비밀번호를 입력하세요"
                value={formData.password}
                onChange={(e) => handleInputChange("password", e.target.value)}
                disabled={loading}
              />
              {errors.password && (
                <div className={styles.message}>{errors.password}</div>
              )}
            </div>
          </div>

          {/* 비밀번호 확인 */}
          <div className={styles.labeledBox}>
            <div className={styles.boxLabel}>비밀번호 확인</div>
            <div className={styles["info-input-box"]}>
              <input
                type="password"
                placeholder="비밀번호를 다시 입력하세요"
                value={formData.confirmPassword}
                onChange={(e) =>
                  handleInputChange("confirmPassword", e.target.value)
                }
                disabled={loading}
              />
              {errors.confirmPassword && (
                <div className={styles.message}>{errors.confirmPassword}</div>
              )}
            </div>
          </div>
        </form>
      </div>

      <div className={styles.signupFooter}>
        <button
          type="submit"
          className={`${styles.signupBtn} ${
            isSignupDisabled() ? styles.disabled : ""
          }`}
          onClick={handleSignup}
          disabled={isSignupDisabled()}>
          {loading ? "회원가입 중..." : "회원가입"}
        </button>
      </div>
    </div>
  );
}

export default SignupPage;
