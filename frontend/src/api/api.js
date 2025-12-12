import axios from "axios";

const api = axios.create({
  baseURL: "http://localhost:8080",
});

// 요청 인터셉터
api.interceptors.request.use((config) => {
  const token = sessionStorage.getItem("accessToken");
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// 401 발생했을 때 Refresh 시도
let isRefreshing = false;
let refreshSubscribers = [];

// 재요청 등록
function subscribeTokenRefresh(cb) {
  refreshSubscribers.push(cb);
}

// 새 토큰 받은 후 재요청 실행
function onRefreshed(token) {
  refreshSubscribers.map((cb) => cb(token));
  refreshSubscribers = [];
}

api.interceptors.response.use(
  (response) => response,
  async (error) => {
    const { config, response } = error;

    if (response && response.status === 401) {
      // 이미 한 번 재요청한 경우 무한루프 방지
      if (config._retry) {
        alert("인증 만료. 다시 로그인해주세요.");
        sessionStorage.clear();
        window.location.reload();
        return Promise.reject(error);
      }
      config._retry = true;

      // 이미 refresh 진행 중인 경우 → refresh가 끝나기를 기다렸다가 재요청
      if (isRefreshing) {
        return new Promise((resolve) => {
          subscribeTokenRefresh((token) => {
            config.headers.Authorization = `Bearer ${token}`;
            resolve(api(config));
          });
        });
      }

      // Refresh Token 요청 시작
      isRefreshing = true;
      const refreshToken = sessionStorage.getItem("refreshToken");

      try {
        const refreshRes = await axios.post(
          "http://localhost:8080/auth/refresh",
          { refreshToken }
        );

        const newAccessToken = refreshRes.data.accessToken;

        // 저장
        sessionStorage.setItem("accessToken", newAccessToken);

        // 모든 대기 요청 처리
        onRefreshed(newAccessToken);

        // 원래 요청 재실행
        config.headers.Authorization = `Bearer ${newAccessToken}`;
        return api(config);
      } catch (e) {
        alert("세션 만료. 다시 로그인해주세요.");
        sessionStorage.clear();
        window.location.reload();
        return Promise.reject(e);
      } finally {
        isRefreshing = false;
      }
    }

    return Promise.reject(error);
  }
);

export default api;
