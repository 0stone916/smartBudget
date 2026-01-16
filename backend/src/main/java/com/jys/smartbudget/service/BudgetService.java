package com.jys.smartbudget.service;

import com.jys.smartbudget.dto.BudgetDTO;
import com.jys.smartbudget.exception.BusinessException;
import com.jys.smartbudget.exception.ErrorCode;
import com.jys.smartbudget.mapper.BudgetMapper;
import com.jys.smartbudget.mapper.ExpenseMapper;
import jakarta.persistence.OptimisticLockException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.util.List;

@RequiredArgsConstructor
@Service
public class BudgetService {

    private final BudgetMapper budgetMapper;
    private final ExpenseMapper expenseMapper;
    private static final Logger auditLog = LoggerFactory.getLogger("AUDIT");

    public void insertBudget(BudgetDTO budget) {
        budgetMapper.insertBudget(budget);
    }

    public List<BudgetDTO> selectBudgetsByConditionWithPaging(BudgetDTO condition) {
        return budgetMapper.selectBudgetsByConditionWithPaging(condition);
    }

    @Transactional
    public void updateBudget(BudgetDTO budget) {

        BudgetDTO originBudget = budgetMapper.selectById(budget.getId());

        if(!budget.getCategory().equals(originBudget.getCategory().getCode())) {
                if (expenseMapper.hasExpensesByBudgetId(budget.getId()) > 0) {
                        throw new BusinessException(ErrorCode.CANNOT_CHANGE_CATEGORY_WITH_EXPENSES);
                }
        }

        int updatedCount = budgetMapper.updateBudget(budget);

        if (updatedCount == 0) {
            throw new OptimisticLockException("이미 수정된 데이터입니다.");
        }
    }

    public void deleteBudgetByIdAndUserId(Long id, String userId) {
        budgetMapper.deleteBudgetByIdAndUserId(id, userId);
    }

    public Boolean existsByYearMonthCategory(BudgetDTO budget) {
        return budgetMapper.existsByYearMonthCategory(budget);
    }

    public BudgetDTO selectById(Long id) {
        return budgetMapper.selectById(id);
    }




}
