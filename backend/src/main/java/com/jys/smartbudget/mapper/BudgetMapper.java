package com.jys.smartbudget.mapper;

import com.jys.smartbudget.dto.BudgetDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface BudgetMapper {
    void insertBudget(BudgetDTO budget);
    List<BudgetDTO> selectBudgetsByConditionWithPaging(BudgetDTO condition);
    int updateBudget(BudgetDTO budget);
    void deleteBudgetByIdAndUserId(Long id, String userId);
    Boolean existsByYearMonthCategory(BudgetDTO budget);

    BudgetDTO selectById(Long id);      //Optimistic Lock 테스트 시 사용


    int countByYearMonth(int year, int month);

    void deleteBudgetsByUserId(@Param("userId") String userId);



}
