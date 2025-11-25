import React, { useState } from "react";
import { register } from "../api/authApi";

function Register({ onRegisterSuccess, onBackToLogin }) {
  const [form, setForm] = useState({ userId: "", password: "", name: "", email: "" });
  const [error, setError] = useState("");

  const handleChange = (e) => {
    const { name, value } = e.target;
    setForm({ ...form, [name]: value });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");

    try {
      await register(form);
      alert("회원가입이 완료되었습니다!");
      onRegisterSuccess();
    } catch (err) {
      if (err.response && err.response.status === 409) {
        setError("이미 존재하는 아이디입니다.");
      } else {
        setError("회원가입 중 오류가 발생했습니다.");
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
    backgroundColor: "#59A14F",
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
      <h2 style={{ textAlign: "center", marginBottom: "20px" }}>회원가입</h2>
      <form onSubmit={handleSubmit}>
        <input style={inputStyle} name="userId" placeholder="아이디" value={form.userId} onChange={handleChange} required />
        <input style={inputStyle} name="password" type="password" placeholder="비밀번호" value={form.password} onChange={handleChange} required />
        <input style={inputStyle} name="name" placeholder="이름" value={form.name} onChange={handleChange} required />
        <input style={inputStyle} name="email" type="email" placeholder="이메일" value={form.email} onChange={handleChange} />
        <button type="submit" style={buttonStyle}>회원가입</button>
      </form>
      <button onClick={onBackToLogin} style={secondaryButtonStyle}>로그인 화면으로</button>
      {error && <p style={errorStyle}>{error}</p>}
    </div>
  );
}

export default Register;
