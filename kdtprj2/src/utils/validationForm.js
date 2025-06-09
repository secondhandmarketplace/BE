export const isSignupFormValid = (form) => {
    const { email, password, passwordConfirm, nickname } = form;

    if (!email.includes("@")) return false;
    if (password.length < 6) return false;
    if (password !== passwordConfirm) return false;
    if (!nickname.trim()) return false;

    return true;
};