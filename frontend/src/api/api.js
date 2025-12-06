// src/api/api.js
import axios from "axios";

const api = axios.create({
  baseURL: "http://localhost:8080", // 서버 주소
});

// 요청 인터셉터: JWT 자동 첨부
api.interceptors.request.use(
  (config) => {
    const token = sessionStorage.getItem("jwtToken");
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
      // ★ JSON 객체에서 message 추출
      let message = "인증 실패. 다시 로그인해주세요.";
      
      if (error.response.data) {
        if (typeof error.response.data === 'string') {
          message = error.response.data;
        } else if (error.response.data.message) {
          message = error.response.data.message;  // ← 여기서 message 속성 가져오기
        }
      }
      
      alert(message);
      sessionStorage.removeItem("jwtToken");
      window.location.reload();
    }
    return Promise.reject(error);
  }
);

export default api;
