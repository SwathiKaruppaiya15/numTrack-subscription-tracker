import { Navigate, Outlet, Route, Routes } from "react-router-dom";
import { useSelector } from "react-redux";
import AppLayout from "./components/Layout/AppLayout";
import Toast from "./components/UI/Toast";
import AuthPage from "./pages/AuthPage";
import DashboardPage from "./pages/DashboardPage";
import SubscriptionsPage from "./pages/SubscriptionsPage";
import AnalyticsPage from "./pages/AnalyticsPage";
import RemindersPage from "./pages/RemindersPage";
import NotFoundPage from "./pages/NotFoundPage";

function ProtectedLayout() {
  const token = useSelector((s) => s.auth.token);
  if (!token) return <Navigate to="/" replace />;
  return <AppLayout><Outlet /></AppLayout>;
}

export default function App() {
  return (
    <>
      <Routes>
        <Route path="/" element={<AuthPage />} />
        <Route element={<ProtectedLayout />}>
          <Route path="/dashboard"     element={<DashboardPage />} />
          <Route path="/subscriptions" element={<SubscriptionsPage />} />
          <Route path="/analytics"     element={<AnalyticsPage />} />
          <Route path="/reminders"     element={<RemindersPage />} />
        </Route>
        <Route path="*" element={<NotFoundPage />} />
      </Routes>
      <Toast />
    </>
  );
}
