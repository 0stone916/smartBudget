// src/api/budgetApi.js
import api from "./api";

export const getBudgets = (year, month) => api.get(`/budgets/search?year=${year}&month=${month}`);
export const createBudget = (budget) => api.post("/budgets", budget);
export const updateBudget = (budget) => api.put("/budgets", budget);
export const deleteBudget = (id) => api.delete(`/budgets/${id}`);
