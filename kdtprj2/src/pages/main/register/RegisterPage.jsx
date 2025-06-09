import React from "react";
import { useNavigate } from "react-router-dom";
import styles from "./register.module.css";
import Header from '../../../components/Header/Header.jsx';
import RegisterForm from "../../../components/RegisterForm/RegisterForm.jsx";


function RegisterPage() {
    const navigate = useNavigate();

    const handleRegister = (newItem) => {
        const saved = JSON.parse(localStorage.getItem("items") || "[]");
        localStorage.setItem("items", JSON.stringify([...saved, newItem]));
        navigate("/home");
    };

    return (
        <div className={styles.container}>
            <Header />
            <div className={styles.Wrapper}>
                <RegisterForm onSubmit={handleRegister} />
            </div>
        </div>
    );
}

export default RegisterPage;