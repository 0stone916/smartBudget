import api from "./api";

// 예산별 지출 조회
// 인자를 객체 { } 로 받도록 수정
export const getExpenses = ({ year, month, lastDay, lastId, size = 10 }) => {
  return api.get('/expenses/search', {
    params: { year, month, lastDay, lastId, size }
  });
};

export const getExpenseStatistics = (year, month) => api.get(`/expenses/getExpenseStatistics?year=${year}&month=${month}`);
export const createExpense = (expense) => api.post("/expenses", expense);
export const updateExpense = (expense) => api.put("/expenses", expense);
export const deleteExpense = (id) => api.delete(`/expenses/${id}`);
