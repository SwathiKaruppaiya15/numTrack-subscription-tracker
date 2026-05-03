import { configureStore, createSlice } from "@reduxjs/toolkit";

// ── Auth slice ───────────────────────────────────────────────
const authSlice = createSlice({
  name: "auth",
  initialState: {
    token:    localStorage.getItem("token") || null,
    user:     JSON.parse(localStorage.getItem("user") || "null"),
    loading:  false,
    error:    null,
  },
  reducers: {
    setCredentials(state, { payload }) {
      state.token = payload.token;
      state.user  = { userId: payload.userId, email: payload.email, fullName: payload.fullName };
      localStorage.setItem("token", payload.token);
      localStorage.setItem("user", JSON.stringify(state.user));
    },
    logout(state) {
      state.token = null;
      state.user  = null;
      localStorage.removeItem("token");
      localStorage.removeItem("user");
    },
    setAuthLoading(state, { payload }) { state.loading = payload; },
    setAuthError(state, { payload })   { state.error   = payload; },
  },
});

// ── UI slice (global notifications) ─────────────────────────
const uiSlice = createSlice({
  name: "ui",
  initialState: { toast: null },
  reducers: {
    showToast(state, { payload }) { state.toast = payload; },
    clearToast(state)             { state.toast = null; },
  },
});

export const { setCredentials, logout, setAuthLoading, setAuthError } = authSlice.actions;
export const { showToast, clearToast } = uiSlice.actions;

export const store = configureStore({
  reducer: {
    auth: authSlice.reducer,
    ui:   uiSlice.reducer,
  },
});
