// src/api/authApi.js
import api from "./api";

export const login = (userId, password) =>
  api.post("/auth/login", { userId, password });

export const register = (userData) =>
  api.post("/auth/register", userData);
