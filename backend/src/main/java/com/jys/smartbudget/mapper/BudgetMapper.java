package com.jys.smartbudget.mapper;

import com.jys.smartbudget.dto.BudgetDTO;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface BudgetMapper {
    void insertBudget(BudgetDTO budget);
    List<BudgetDTO> selectBudgetsByConditionWithPaging(BudgetDTO condition);
    int updateBudget(BudgetDTO budget);
    void deleteBudget(Long id, String userId);
    Boolean existsByYearMonthCategory(BudgetDTO budget);

    BudgetDTO selectById(Long id);
}
