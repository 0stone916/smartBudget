// src/api/api.js
import axios from "axios";

const api = axios.create({
  baseURL: "http://localhost:8080", // 서버 주소
});

// 요청 인터셉터: JWT 자동 첨부
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem("jwtToken");
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// 응답 인터셉터: 401 처리
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response && error.response.status === 401) {
      alert("인증 실패. 다시 로그인해주세요.");
      localStorage.removeItem("jwtToken");
      window.location.reload(); // 로그인 화면으로 이동
    }
    return Promise.reject(error);
  }
);

export default api;
