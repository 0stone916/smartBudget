import React, { useState, useEffect } from "react";
import { createExpense, updateExpense } from "../api/expenseApi";

const formStyle = {
  display: "flex",
  flexDirection: "column",
  gap: "10px",
};

const inputStyle = {
  padding: "8px 12px",
  borderRadius: "6px",
  border: "1px solid #ccc",
  fontSize: "14px",
};

const submitButtonStyle = {
  padding: "10px 16px",
  borderRadius: "6px",
  border: "none",
  cursor: "pointer",
  backgroundColor: "#4CAF50",
  color: "#fff",
  fontWeight: "bold",
};

const selectStyle = {
  padding: "8px 12px",
  borderRadius: "6px",
  border: "1px solid #ccc",
  fontSize: "14px",
  backgroundColor: "#fff",
  appearance: "none", // 기본 화살표 제거
  cursor: "pointer",
};

export default function ExpenseForm({ selectedExpense, budgets, year, month, onSave }) {
  const [form, setForm] = useState({
    budgetId: "",
    day: "",
    amount: "",
    description: "",
  });

  // selectedExpense가 바뀌면 폼 초기화
  useEffect(() => {
    if (selectedExpense) {
      setForm({
        budgetId: selectedExpense.budgetId || "",
        day: selectedExpense.day || "",
        amount: selectedExpense.amount || "",
        description: selectedExpense.description || "",
        id: selectedExpense.id || null,
      });
    } else {
      setForm({ budgetId: "", day: "", amount: "", description: "" });
    }
  }, [selectedExpense]);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setForm({ ...form, [name]: value });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    const payload = { ...form, year, month };

    try {
      let response;
      if (form.id) {
        response = await updateExpense(payload);
      } else {
        response = await createExpense(payload);
      }

      // 서버에서 반환한 메시지 alert로 표시
      if (response && response.data && response.data.message) {
        alert(response.data.message);
      }
      onSave();
      setForm({ budgetId: "", day: "", amount: "", description: "" });
    } catch (err) {
      console.error("지출 저장 실패", err);
    }
  };

  return (
    <div>
      <h2>{form.id ? "지출 수정" : "지출 등록"}</h2>
      <form onSubmit={handleSubmit} style={formStyle}>
        <select name="budgetId" value={form.budgetId} onChange={handleChange} style={selectStyle} required>
          <option value="">카테고리 선택</option>
          {budgets.map((b) => (
            <option key={b.id} value={b.id}>
              {b.categoryDescription}
            </option>
          ))}
        </select>

        <input
          name="day"
          type="number"
          placeholder="일자"
          value={form.day}
          onChange={handleChange}
          style={inputStyle}
          required
        />

        <input
          name="amount"
          type="number"
          placeholder="금액"
          value={form.amount}
          onChange={handleChange}
          style={inputStyle}
          required
        />

        <input
          name="description"
          placeholder="설명"
          value={form.description}
          style={inputStyle}
          onChange={handleChange}
        />

        <button type="submit" style={submitButtonStyle}>{form.id ? "수정" : "등록"}</button>
      </form>
    </div>
  );
}
