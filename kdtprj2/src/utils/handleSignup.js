export const handleSignup = async ({ form, setUserId, setMessage, navigate }) => {
    const { email, password, passwordConfirm, nickname, phone, address, school } = form;

    // 1. 유효성 검사
    if (!email.includes("@")) {
        return setMessage("올바른 이메일 형식을 입력하세요.");
    }
    if (password.length < 6) {
        return setMessage("비밀번호는 6자 이상이어야 합니다.");
    }
    if (password !== passwordConfirm) {
        return setMessage("비밀번호가 일치하지 않습니다.");
    }

    // 2. 서버 요청
    try {
        const response = await fetch("/api/user/signup", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ email, password, nickname, phone, address, school }),
        });

        const data = await response.json();

        if (!response.ok) {
            return setMessage(data.message || "회원가입 실패");
        }

        if (!data.userId) {
            return setMessage("서버 응답 오류: userId 없음");
        }

        // 3. 로컬스토리지 저장
        const userData = { userId: data.userId, nickname, email, phone, address, school };
        localStorage.setItem("user", JSON.stringify(userData));

        setUserId(data.userId);
        setMessage("회원가입이 완료되었습니다.");
        navigate("/login");

    } catch (error) {
        console.error("Signup Error:", error);
        setMessage("서버 에러");
    }
};
