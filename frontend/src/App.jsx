import React from "react";
import { Navigate, Route, Routes } from "react-router-dom";
import Layout from "./components/Layout";
import DashboardPage from "./pages/DashboardPage";
import LoginPage from "./pages/LoginPage";
import RegisterPage from "./pages/RegisterPage";
import CommunityPage from "./pages/CommunityPage";
import DepartmentDashboardPage from "./pages/DepartmentDashboardPage";
import AdminDashboardPage from "./pages/AdminDashboardPage";

export default function App() {
  return (
    <Layout>
      <Routes>
        <Route path="/" element={<DashboardPage />} />
        <Route path="/community" element={<CommunityPage />} />
        <Route path="/department" element={<DepartmentDashboardPage />} />
        <Route path="/admin" element={<AdminDashboardPage />} />
        <Route path="/auth/login" element={<LoginPage />} />
        <Route path="/auth/register" element={<RegisterPage />} />
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </Layout>
  );
}
