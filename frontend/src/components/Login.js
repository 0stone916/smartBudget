import React, { useState } from "react";
import { login } from "../api/authApi";
import api from "../api/api";

function Login({ onLoginSuccess, onShowRegister }) {
  const [userId, setUserId] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");

    try {
      const response = await login(userId, password);
      console.log(response);

      const accessToken = response.data.data.accessToken;
      const refreshToken = response.data.data.refreshToken;

      // 세션 저장
      sessionStorage.setItem("accessToken", accessToken);
      sessionStorage.setItem("refreshToken", refreshToken);

      // axios 기본 헤더 등록
      api.defaults.headers.common["Authorization"] = `Bearer ${accessToken}`;

      onLoginSuccess();
    } catch (err) {
      if (err.response?.data?.message) {
        setError(err.response.data.message);
      } else {
        setError("로그인 중 오류가 발생했습니다.");
      }
    }
  };


  const containerStyle = {
    width: "400px",
    margin: "50px auto",
    padding: "30px",
    border: "1px solid #ccc",
    borderRadius: "10px",
    boxShadow: "0 4px 8px rgba(0,0,0,0.1)",
    backgroundColor: "#f9f9f9",
  };

  const inputStyle = {
    width: "100%",
    padding: "10px",
    margin: "10px 0",
    borderRadius: "5px",
    border: "1px solid #ccc",
    fontSize: "16px",
  };

  const buttonStyle = {
    width: "100%",
    padding: "10px",
    marginTop: "10px",
    border: "none",
    borderRadius: "5px",
    backgroundColor: "#4E79A7",
    color: "white",
    fontSize: "16px",
    cursor: "pointer",
  };

  const secondaryButtonStyle = {
    ...buttonStyle,
    backgroundColor: "#F28E2B",
    marginTop: "10px",
  };

  const errorStyle = { color: "red", marginTop: "10px", textAlign: "center" };

  return (
    <div style={containerStyle}>
      <h2 style={{ textAlign: "center", marginBottom: "20px" }}>로그인</h2>
      <form onSubmit={handleSubmit}>
        <input
          style={inputStyle}
          type="text"
          placeholder="아이디"
          value={userId}
          onChange={(e) => setUserId(e.target.value)}
          required
        />
        <input
          style={inputStyle}
          type="password"
          placeholder="비밀번호"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          required
        />
        <button type="submit" style={buttonStyle}>로그인</button>
      </form>
      <button onClick={onShowRegister} style={secondaryButtonStyle}>
        회원가입 하러가기
      </button>
      {error && <p style={errorStyle}>{error}</p>}
    </div>
  );
}

export default Login;
