package com.jys.smartbudget.mapper;

import com.jys.smartbudget.dto.ExpenseDTO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ExpenseMapper {

    List<ExpenseDTO> searchExpenses(ExpenseDTO expense);

    void insertExpense(ExpenseDTO expense);

    void updateExpense(ExpenseDTO expense);

    void deleteExpenseByIdAndUserId(Long id, String userId);

    Boolean checkOverBudget(ExpenseDTO expense);


}
