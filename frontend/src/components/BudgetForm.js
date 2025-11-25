import React, { useState, useEffect } from "react";
import { createBudget, updateBudget } from "../api/budgetApi";

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

export default function BudgetForm({ selectedBudget, onSave, year, month, budgets }) {
  const [form, setForm] = useState({
    category: "",
    amount: "",
    budgetDescription: "",
  });

  // 카테고리 코드 목록
  const categories = [
    { category: "FOOD", name: "식비" },
    { category: "TRANSPORT", name: "교통비" },
    { category: "HOUSING", name: "주거비" },
    { category: "ENTERTAIN", name: "여가/취미" },
    { category: "OTHER", name: "기타" },
  ];

  // 선택된 예산이 바뀌면 폼 초기화
  useEffect(() => {
    if (selectedBudget) {
      setForm(selectedBudget);
    } else {
      setForm({ category: "", amount: "", budgetDescription: "" });
    }
  }, [selectedBudget]);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setForm({ ...form, [name]: value });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    const payload = { ...form, year, month };
    try {
      if (form.id) {
        await updateBudget(payload);
      } else {
        await createBudget(payload);
      }
      onSave();
      setForm({ category: "", amount: "", budgetDescription: "" });
    } catch (error) {
      console.error("저장 실패", error);
    }
  };

  // 현재 화면 년/월에 이미 등록된 카테고리 제외
  const availableCategories = categories.filter((c) => {
    return !budgets.some(
      (b) => b.category === c.category && b.id !== form.id
    );
  });


  return (
    <div>
      <h2>{form.id ? "예산 수정" : "예산 등록"}</h2>
      <form onSubmit={handleSubmit} style={formStyle}>
        <select name="category" value={form.category} onChange={handleChange} style={selectStyle} required>
          <option value="">카테고리 선택</option>
          {availableCategories.map((c) => (
            <option key={c.category} value={c.category}>
              {c.name}
            </option>
          ))}
        </select>

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
          name="budgetDescription"
          placeholder="설명"
          value={form.budgetDescription}
          onChange={handleChange}
          style={inputStyle}
        />
        <button type="submit" style={submitButtonStyle}>{form.id ? "수정" : "등록"}</button>
      </form>
    </div>
  );
}
