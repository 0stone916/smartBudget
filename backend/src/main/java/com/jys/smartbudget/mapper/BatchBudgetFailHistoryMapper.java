package com.jys.smartbudget.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.jys.smartbudget.dto.BatchBudgetFailHistory;

@Mapper
public interface  BatchBudgetFailHistoryMapper {
    void insert(BatchBudgetFailHistory history);
}
