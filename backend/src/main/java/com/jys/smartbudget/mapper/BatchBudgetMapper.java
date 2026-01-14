package com.jys.smartbudget.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.jys.smartbudget.dto.BatchBudgetFailHistory;
import com.jys.smartbudget.dto.BatchJobSummary;

@Mapper
public interface  BatchBudgetMapper {
    void insertBatchBudgetFailHistory(BatchBudgetFailHistory history);

    void insertBatchJobSummary(BatchJobSummary summary);

}
