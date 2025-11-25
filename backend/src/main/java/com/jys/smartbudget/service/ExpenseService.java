package com.jys.smartbudget.service;

import com.jys.smartbudget.dto.ExpenseDTO;
import com.jys.smartbudget.mapper.ExpenseMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ExpenseService {

    private final ExpenseMapper expenseMapper;

    public ExpenseService(ExpenseMapper expenseMapper) {
        this.expenseMapper = expenseMapper;
    }

    public List<ExpenseDTO> searchExpenses(ExpenseDTO expense) {
        return expenseMapper.searchExpenses(expense);
    }

    public void insertExpense(ExpenseDTO expense) {
        expenseMapper.insertExpense(expense);
    }

    public void updateExpense(ExpenseDTO expense) {
        expenseMapper.updateExpense(expense);
    }

    public void deleteExpense(Long id, String userId) {
        expenseMapper.deleteExpenseByIdAndUserId(id, userId);
    }

    public Boolean checkOverBudget(ExpenseDTO expense) {
        return expenseMapper.checkOverBudget(expense);
    }

}
