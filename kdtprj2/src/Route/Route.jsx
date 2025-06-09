import React from "react";
import { Routes, Route } from "react-router-dom";

// Main/History/Chat
import HomePage from "../pages/main/home/HomePage.jsx";
import RegisterPage from "../pages/main/register/RegisterPage.jsx";
import MyPage from "../pages/main/myPage/MyPage.jsx";
import ItemDetailPage from "../pages/main/itemDetail/ItemDetailPage.jsx";
import ChatPage from "../pages/chat/ChatPage.jsx";
import ChatListPage from "../pages/main/chatList/ChatListPage.jsx";

// History
import ViewedItemsPage from "../pages/history/viewedItems/ViewedItemsPage.jsx";
import LikedItemsPage from "../pages/history/likedItems/LikedItemsPage.jsx";
import SalesItemsPage from "../pages/history/salesItems/SalesItemsPage.jsx";
import PurchaseItemsPage from "../pages/history/purchaseItems/PurchaseItemsPage.jsx";

// Auth
import LoginPage from "../pages/lending/login/loginPage.jsx";
import SignupPage from "../pages/lending/signup/SignupPage.jsx";
import ForgotPassword from "../pages/lending/forgot/ForgotPassword.jsx";

// Search
import Search from "../components/Serach/Search.jsx";

// Ai
import AiPage from "../components/Ai/Ai.jsx";

const AllRoutes = () => (
  <Routes>
    {/* Main/Home */}
    <Route path="/home" element={<HomePage />} />
    <Route path="/register" element={<RegisterPage />} />
    <Route path="/mypage" element={<MyPage />} />

    {/* Item/Chat */}
    <Route path="/item/:id" element={<ItemDetailPage />} />
    <Route path="/chat" element={<ChatPage />} />
    <Route path="/chatList" element={<ChatListPage />} />

    {/* History */}
    <Route path="/history/sales" element={<SalesItemsPage />} />
    <Route path="/history/viewed" element={<ViewedItemsPage />} />
    <Route path="/history/liked" element={<LikedItemsPage />} />
    <Route path="/history/purchased" element={<PurchaseItemsPage />} />

    {/* Auth */}
    <Route path="/" element={<LoginPage />} />
    <Route path="/signup" element={<SignupPage />} />
    <Route path="/forgot" element={<ForgotPassword />} />

    {/* Search */}
    <Route path="/search" element={<Search />} />

    {/* Ai */}
    <Route path="/ai" element={<AiPage />} />
  </Routes>
);

export default AllRoutes;
