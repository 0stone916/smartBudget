import React, { useState, useEffect } from "react";
import BudgetList from "./components/BudgetList";
import BudgetForm from "./components/BudgetForm";
import ExpenseForm from "./components/ExpenseForm";
import ExpenseList from "./components/ExpenseList";
import ExpensePieChart from "./components/ExpensePieChart";
import Login from "./components/Login";
import Register from "./components/Register";
import { logout } from "./api/authApi";

import { getBudgets, deleteBudget } from "./api/budgetApi";
import { getExpenses, deleteExpense } from "./api/expenseApi";

// 간단 카드 스타일
const cardStyle = {
  padding: "20px",
  marginBottom: "20px",
  boxShadow: "0 2px 12px rgba(0,0,0,0.1)",
  borderRadius: "12px",
  backgroundColor: "#fff",
};

const buttonStyle = {
  padding: "6px 14px",
  borderRadius: "6px",
  border: "none",
  cursor: "pointer",
  marginRight: "8px",
};

export default function App() {
  const [selectedBudget, setSelectedBudget] = useState(null);
  const [selectedExpense, setSelectedExpense] = useState(null);
  const [reload, setReload] = useState(false);

  // 로그인/회원가입 상태
  const [isLoggedIn, setIsLoggedIn] = useState(false);
  const [showRegister, setShowRegister] = useState(false);

  const [budgets, setBudgets] = useState([]);
  const [expenses, setExpenses] = useState([]);

  const [yearMonth, setYearMonth] = useState(() => {
    const now = new Date();
    return `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, "0")}`;
  });
  const [year, month] = yearMonth.split("-");

  // 초기 로그인 체크
  useEffect(() => {
    const token = localStorage.getItem("jwtToken");
    if (token) setIsLoggedIn(true);
  }, []);

  // 예산 조회
  useEffect(() => {
    async function fetchBudgets() {
      try {
        const res = await getBudgets(year, month);
        setBudgets(res.data);
      } catch (error) {
        console.error("예산 조회 실패", error);
      }
    }
    fetchBudgets();
  }, [reload, year, month]);

  // 지출 조회
  useEffect(() => {
    async function fetchExpenses() {
      try {
        const res = await getExpenses(year, month);
        setExpenses(res.data);
      } catch (error) {
        console.error("지출 조회 실패", error);
      }
    }
    fetchExpenses();
  }, [reload, year, month]);

  const handleSave = () => setReload((prev) => !prev);
  const handleLoginSuccess = () => setIsLoggedIn(true);
  const handleLogout = async () => {
    try {
      await logout(); // 서버 로그아웃
    } catch (e) {
      console.error("로그아웃 요청 실패:", e);
    }

    // 클라이언트 토큰 삭제
    localStorage.removeItem("jwtToken");
    setIsLoggedIn(false);
  };


  const handleDeleteBudget = async (id) => {
    try {
      await deleteBudget(id);
      setReload((prev) => !prev);
    } catch (err) {
      console.error("예산 삭제 실패", err);
    }
  };

  const handleDeleteExpense = async (id) => {
    try {
      await deleteExpense(id);
      setReload((prev) => !prev);
    } catch (err) {
      console.error("지출 삭제 실패", err);
    }
  };

  const moveMonth = (diff) => {
    const date = new Date(yearMonth + "-01");
    date.setMonth(date.getMonth() + diff);
    const newYm = `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, "0")}`;
    setYearMonth(newYm);
  };

  // 회원가입 화면 전환
  const handleShowRegister = () => setShowRegister(true);
  const handleBackToLogin = () => setShowRegister(false);

  // 로그인/회원가입 화면 표시
  if (!isLoggedIn) {
    if (showRegister) {
      return <Register onRegisterSuccess={handleBackToLogin} onBackToLogin={handleBackToLogin} />;
    }
    return <Login onLoginSuccess={handleLoginSuccess} onShowRegister={handleShowRegister} />;
  }

  // 실제 가계부 화면
  return (
    <div style={{ maxWidth: "900px", margin: "0 auto", padding: "20px", fontFamily: "Arial, sans-serif" }}>
      <h1 style={{ textAlign: "center", marginBottom: "20px" }}>SmartBudget</h1>
      <div style={{ textAlign: "right", marginBottom: "20px" }}>
        <button
          style={{ ...buttonStyle, backgroundColor: "#f44336", color: "#fff" }}
          onClick={handleLogout}
        >
          로그아웃
        </button>
      </div>

      {/* 상단 년/월 페이징 */}
      <div style={{ display: "flex", justifyContent: "center", alignItems: "center", gap: "10px", marginBottom: "20px" }}>
        <button
          style={{ ...buttonStyle, backgroundColor: "#2196F3", color: "#fff" }}
          onClick={() => moveMonth(-1)}
        >
          ◀ 이전달
        </button>
        <strong style={{ fontSize: "18px" }}>{yearMonth}</strong>
        <button
          style={{ ...buttonStyle, backgroundColor: "#2196F3", color: "#fff" }}
          onClick={() => moveMonth(1)}
        >
          다음달 ▶
        </button>
      </div>

      {/* Pie Chart */}
      <div style={cardStyle}>
        <h2 style={{ textAlign: "center" }}>예산 대비 지출</h2>
        <ExpensePieChart budgets={budgets} expenses={expenses} />
      </div>

      {/* 지출 등록/수정 */}
      <div style={cardStyle}>
        <ExpenseForm selectedExpense={selectedExpense} budgets={budgets} year={year} month={month} onSave={handleSave} />
      </div>

      {/* 지출 리스트 */}
      <div style={cardStyle}>
        <ExpenseList expenses={expenses} onEdit={setSelectedExpense} onDelete={handleDeleteExpense} />
      </div>

      {/* 예산 등록/수정 */}
      <div style={cardStyle}>
        <BudgetForm selectedBudget={selectedBudget} onSave={handleSave} budgets={budgets} year={year} month={month} />
      </div>

      {/* 예산 리스트 */}
      <div style={cardStyle}>
        <BudgetList budgets={budgets} onEdit={setSelectedBudget} onDelete={handleDeleteBudget} />
      </div>
    </div>
  );
}
