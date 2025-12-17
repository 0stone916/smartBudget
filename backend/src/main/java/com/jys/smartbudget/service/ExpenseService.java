package com.jys.smartbudget.service;

import com.jys.smartbudget.dto.ExpenseDTO;
import com.jys.smartbudget.mapper.ExpenseMapper;
import jakarta.persistence.OptimisticLockException;
import jakarta.transaction.Transactional;
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

    @Transactional
    public void updateExpense(ExpenseDTO expense) {
       int updatedCount =  expenseMapper.updateExpense(expense);

        if (updatedCount == 0) {
            throw new OptimisticLockException("이미 수정된 데이터입니다.");
        }
    }

    public void deleteExpense(Long id, String userId) {
        expenseMapper.deleteExpenseByIdAndUserId(id, userId);
    }

    public Boolean checkOverBudget(ExpenseDTO expense) {
        return expenseMapper.checkOverBudget(expense);
    }

}
