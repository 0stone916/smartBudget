package com.jys.smartbudget.mapper;

import java.time.LocalDateTime;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import com.jys.smartbudget.dto.BatchBudgetFailHistory;
import com.jys.smartbudget.dto.BatchJobSummary;

@Mapper
public interface  BatchBudgetMapper {
    void insertBatchBudgetFailHistory(BatchBudgetFailHistory history);

    void insertBatchJobSummary(BatchJobSummary summary);

    List<BatchBudgetFailHistory> selectUnprocessedFailHistories();

    void markFailHistoryProcessed(String userId,int year,int month,String category,LocalDateTime processedAt);

    int countUnprocessedFailHistories();

}
