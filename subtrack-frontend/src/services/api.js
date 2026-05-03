import axios from "axios";
import { store } from "../store/store";
import { logout } from "../store/store";

const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || "http://localhost:8080",
  timeout: 15000,
  headers: { "Content-Type": "application/json" },
});

// ── Request interceptor: attach JWT ─────────────────────────
api.interceptors.request.use(
  (config) => {
    const token = store.getState().auth.token;
    if (token) config.headers.Authorization = `Bearer ${token}`;
    return config;
  },
  (error) => Promise.reject(error)
);

// ── Response interceptor: handle 401 ────────────────────────
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      store.dispatch(logout());
      window.location.href = "/";
    }
    return Promise.reject(error);
  }
);

export const authAPI = {
  register: (data) => api.post("/api/auth/register", data),
  login:    (data) => api.post("/api/auth/login", data),
};

export const subscriptionAPI = {
  getAll:  ()         => api.get("/api/subscriptions"),
  getById: (id)       => api.get(`/api/subscriptions/${id}`),
  create:  (data)     => api.post("/api/subscriptions", data),
  update:  (id, data) => api.put(`/api/subscriptions/${id}`, data),
  cancel:  (id)       => api.patch(`/api/subscriptions/${id}/cancel`),
  delete:  (id)       => api.delete(`/api/subscriptions/${id}`),
};

export const paymentAPI = {
  getAll:            ()     => api.get("/api/payments"),
  getBySubscription: (id)   => api.get(`/api/payments/subscription/${id}`),
  record:            (data) => api.post("/api/payments", data),
};

export const usageAPI = {
  log:               (data) => api.post("/api/usage", data),
  getBySubscription: (id)   => api.get(`/api/usage/subscription/${id}`),
};

export const analyticsAPI = {
  get:         () => api.get("/api/analytics"),
  getInsights: () => api.get("/api/analytics/insights"),
};

export const reminderAPI = {
  getAll:      ()   => api.get("/api/reminders"),
  triggerTest: (id) => api.post(`/api/reminders/test/${id}`),
};

export default api;
