import React, { useState, useEffect, useRef } from "react";
import ExpenseList from "./components/ExpenseList";
import Login from "./components/Login";
import Register from "./components/Register";
import { logout } from "./api/authApi";
import { getExpenses, deleteExpense } from "./api/expenseApi";
import SockJS from "sockjs-client";
import { Stomp } from "@stomp/stompjs";
import { Toaster, toast } from 'react-hot-toast'; // 토스트 알림 추가

const cardStyle = {
  padding: "25px",
  marginBottom: "20px",
  boxShadow: "0 4px 20px rgba(0,0,0,0.08)",
  borderRadius: "16px",
  backgroundColor: "#fff",
};

const buttonStyle = {
  padding: "10px 18px",
  borderRadius: "8px",
  border: "none",
  cursor: "pointer",
  fontWeight: "bold",
  transition: "0.2s",
};

export default function App() {
  const [reload, setReload] = useState(false);
  const [isLoggedIn, setIsLoggedIn] = useState(false);
  const [showRegister, setShowRegister] = useState(false);

  const [budgets, setBudgets] = useState(0);
  const [expenses, setExpenses] = useState([]);

  const [yearMonth, setYearMonth] = useState(() => {
    const now = new Date();
    return `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, "0")}`;
  });
  const [year, month] = yearMonth.split("-");

  const stompClient = useRef(null);

  useEffect(() => {
    if (!isLoggedIn) return;

    const currentUserId = sessionStorage.getItem("userId"); 

    if (currentUserId == null) {
      console.log("currentUserId is null");
      return;
    }

    const socket = new SockJS("http://localhost:8080/ws-connect");
    stompClient.current = Stomp.over(socket);

    stompClient.current.connect({}, (frame) => {
      console.log("STOMP 연결 성공!", frame);

      const myTopic = `/topic/payment/${currentUserId}`;
      
      stompClient.current.subscribe(myTopic, (message) => {
        console.log("★★★ 웹소켓 원본 메시지 도착 ★★★", message.body);
        const newPayment = JSON.parse(message.body);
        console.log("파싱된 데이터:", newPayment);
        
        setExpenses((prev) => [newPayment, ...prev]);
        setBudgets((prev) => Number(prev) - Number(newPayment.amount));

        toast.custom((t) => (
<div
      onClick={() => toast.dismiss(t.id)} 
      style={{
        background: "#333",
        color: "#fff",
        padding: "20px",
        borderRadius: "14px",
        boxShadow: "0 12px 30px rgba(0,0,0,0.25)",
        display: "flex",
        flexDirection: "column",
        gap: "6px",
        minWidth: "280px",
        cursor: "pointer", // 클릭 가능하다는 것을 사용자에게 인지시킴
        border: "1px solid rgba(255,255,255,0.1)",
        transition: "transform 0.1s active",
        animation: t.visible ? "enter 0.3s ease-out" : "leave 0.3s ease-in"
      }}
    >
            <div style={{ fontWeight: "bold", fontSize: "0.95em", display: "flex", alignItems: "center", gap: "8px" }}>
              <span style={{ color: "#4ade80" }}>●</span> 실시간 결제 승인
            </div>
            <div style={{ fontSize: "0.9em", opacity: 0.9 }}>
              {newPayment.merchantName} : {Number(newPayment.amount).toLocaleString()}원
            </div>
          </div>
        ), { duration: Infinity, position: "top-right" });
      });
    });

    return () => {
      if (stompClient.current) stompClient.current.disconnect();
    };
  }, [isLoggedIn]);

  // 초기 로그인 체크
  useEffect(() => {
    const token = sessionStorage.getItem("accessToken");
    if (token) setIsLoggedIn(true);
  }, []);

  // 실시간 지출 내역 조회 (초기 로딩)
  useEffect(() => {
    if (!isLoggedIn) return;
    async function fetchInitialExpenses() {
      try {
        // year, month만 보내면 서버에서 해당 월의 startTime/endTime을 계산함
        const response = await getExpenses({ year, month, accountNumber: '110-123-456789', size: 10 });
        setExpenses(response.data.data.expenses); 
        setBudgets(response.data.data.accountInfo.balance);
      } catch (e) { console.error(e); }
    }
    fetchInitialExpenses();
  }, [isLoggedIn, reload, year, month]);

  // 더보기 (No-Offset 페이징)
  const loadMoreExpenses = async () => {
    const lastExpense = expenses[expenses.length - 1];
    if (!lastExpense) return;

    const response = await getExpenses({ 
      year, 
      month, 
      lastTimestamp: lastExpense.transactedAt, 
      lastId: lastExpense.id,
      accountNumber: '110-123-456789',
      size: 10
    });
    
    setExpenses(prev => [...prev, ...response.data.data.expenses]);
  };

  const handleLoginSuccess = () => setIsLoggedIn(true);
  
  const handleLogout = async () => {
    try { await logout(); } catch (e) { console.error(e); }
    sessionStorage.clear();
    setIsLoggedIn(false);
  };

  const handleDeleteExpense = async (id) => {
    if(window.confirm("내역을 삭제하시겠습니까?")) {
        try {
            await deleteExpense(id);
            setReload((prev) => !prev);
        } catch (err) { console.error(err); }
    }
  };

  const moveMonth = (diff) => {
    const date = new Date(yearMonth + "-01");
    date.setMonth(date.getMonth() + diff);
    setYearMonth(`${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, "0")}`);
  };

  if (!isLoggedIn) {
    if (showRegister) {
      return <Register onRegisterSuccess={() => setShowRegister(false)} onBackToLogin={() => setShowRegister(false)} />;
    }
    return <Login onLoginSuccess={handleLoginSuccess} onShowRegister={() => setShowRegister(true)} />;
  }

  return (
    <div style={{ maxWidth: "850px", margin: "0 auto", padding: "30px", backgroundColor: "#f8f9fa", minHeight: "100vh" }}>
      {/* 토스트 컨테이너 설정 */}
      <Toaster />
      
      <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: "30px" }}>
        <h1 style={{ margin: 0, color: "#1a1a1a", letterSpacing: "-1px" }}>Financial CMS<span style={{ color: "#0046ff", fontSize: "0.5em" }}>v2.0</span></h1>
        <button style={{ ...buttonStyle, backgroundColor: "#ffeded", color: "#ff4d4d" }} onClick={handleLogout}>로그아웃</button>
      </div>

      <div style={{ ...cardStyle, background: "linear-gradient(135deg, #0046ff 0%, #0031b3 100%)", color: "white" }}>
        <div style={{ display: "flex", justifyContent: "space-between", opacity: 0.8, fontSize: "0.9em", marginBottom: "10px" }}>
          <span>연동 계좌: 110-123-456789</span>
          <div style={{ fontSize: "0.85em", backgroundColor: "rgba(255,255,255,0.1)", padding: "8px 15px", borderRadius: "8px" }}>
            ● WebSocket 실시간 모니터링 중
          </div>
        </div>
        <h3 style={{ margin: 0, fontWeight: "normal" }}>현재 결제 가능 예산</h3>
        <h2 style={{ fontSize: "2.8em", margin: "10px 0" }}>
          {Number(budgets).toLocaleString()} <small style={{fontSize: '0.5em'}}>원</small>
        </h2>
      </div>

      <div style={{ display: "flex", justifyContent: "center", alignItems: "center", gap: "20px", marginBottom: "25px" }}>
        <button style={{ ...buttonStyle, backgroundColor: "#fff", border: "1px solid #ddd" }} onClick={() => moveMonth(-1)}>◀</button>
        <strong style={{ fontSize: "20px", color: "#333" }}>{year}년 {month}월</strong>
        <button style={{ ...buttonStyle, backgroundColor: "#fff", border: "1px solid #ddd" }} onClick={() => moveMonth(1)}>▶</button>
      </div>

      <div style={cardStyle}>
        <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: "20px" }}>
          <h2 style={{ margin: 0, fontSize: "1.2em" }}>📊 실시간 지출 피드</h2>
          <button style={{ ...buttonStyle, backgroundColor: "#f0f2f5", color: "#666", fontSize: "0.85em" }} onClick={() => setReload(!reload)}>새로고침</button>
        </div>
        
        <ExpenseList 
          expenses={expenses} 
          onEdit={() => {}} 
          onDelete={handleDeleteExpense} 
        />
        
        {expenses.length > 4 && (
          <button 
            onClick={loadMoreExpenses}
            style={{ width: "100%", marginTop: "20px", padding: "12px", border: "1px solid #eee", borderRadius: "8px", background: "#fff", cursor: "pointer", color: "#888", fontWeight: "bold" }}
          >
            과거 내역 더보기
          </button>
        )}
      </div>

      <div style={{ ...cardStyle, border: "2px dashed #e0e0e0", backgroundColor: "transparent", textAlign: "center" }}>
        <h3 style={{ color: "#888", marginTop: 0 }}>🔍 일일 장부 대조 현황 (Batch)</h3>
        <p style={{ color: "#999", fontSize: "0.9em" }}>
          은행 서버 원장과 로컬 지출 내역의 정합성을 배치가 검증하고 있습니다.
        </p>
        <div style={{ color: "#aaa", fontSize: "0.8em" }}>마지막 대조 일시: 2026-03-02 00:00:05 (정상)</div>
      </div>
    </div>
  );
}