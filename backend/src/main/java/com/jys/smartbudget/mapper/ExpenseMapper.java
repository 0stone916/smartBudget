package com.jys.smartbudget.mapper;

import com.jys.smartbudget.dto.ExpenseDTO;
import io.lettuce.core.dynamic.annotation.Param;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface ExpenseMapper {

    List<ExpenseDTO> searchExpenses(ExpenseDTO expense);

    void insertExpense(ExpenseDTO expense);

    int updateExpense(ExpenseDTO expense);

    void deleteExpenseByIdAndUserId(Long id, String userId);

    Boolean checkOverBudget(ExpenseDTO expense);

    ExpenseDTO findLatestExpense(String userId);

    int hasExpensesByBudgetId(@Param("budgetId") Long budgetId);

}
