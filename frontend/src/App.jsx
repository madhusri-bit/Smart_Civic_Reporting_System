import React from "react";
import { Navigate, Route, Routes } from "react-router-dom";
import Layout from "./components/Layout";
import DashboardPage from "./pages/DashboardPage";
import LoginPage from "./pages/LoginPage";
import RegisterPage from "./pages/RegisterPage";
import IssueImagePage from "./pages/IssueImagePage";
import IssueReportPage from "./pages/IssueReportPage";
import LocationPage from "./pages/LocationPage";
import CommunityPage from "./pages/CommunityPage";
import EscalationPage from "./pages/EscalationPage";

export default function App() {
  return (
    <Layout>
      <Routes>
        <Route path="/" element={<DashboardPage />} />
        <Route path="/auth/login" element={<LoginPage />} />
        <Route path="/auth/register" element={<RegisterPage />} />
        <Route path="/issues/image" element={<IssueImagePage />} />
        <Route path="/issues/report" element={<IssueReportPage />} />
        <Route path="/location" element={<LocationPage />} />
        <Route path="/community" element={<CommunityPage />} />
        <Route path="/escalation" element={<EscalationPage />} />
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </Layout>
  );
}
