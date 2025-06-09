export const handleSave = ({ password, confirmPassword, nickname, phone, address, setIsEditing }) => {
    if (password !== confirmPassword) {
        alert("비밀번호가 일치하지 않습니다.");
        return;
    }

    const updated = {
        nickname,
        phone,
        address,
    };

    const prevUser = JSON.parse(localStorage.getItem("user")) || {};

    const newUser = {
        ...prevUser,
        ...updated,
    };

    localStorage.setItem("user", JSON.stringify(newUser));
    alert("정보가 저장되었습니다.");
    setIsEditing(false);
};
