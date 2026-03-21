package com.jys.smartbudget.batch.writer;

import com.jys.smartbudget.dto.ExpenseDTO;
import com.jys.smartbudget.dto.ReconciliationErrorDTO;
import com.jys.smartbudget.mapper.ExpenseMapper;
import com.jys.smartbudget.mapper.ReconcileLogMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReconciliationWriter implements ItemWriter<Object> {

    private final ExpenseMapper expenseMapper;
    private final ReconcileLogMapper reconcileLogMapper;

    @Override
    public void write(Chunk<? extends Object> chunk) throws Exception {
        for (Object item : chunk) {
            if (item instanceof ExpenseDTO expense) {
                // Case A: 정상 건 - 상태를 'VERIFIED'로 업데이트
                expenseMapper.updateStatusVerified(expense.getApprovalNo());
            } else if (item instanceof ReconciliationErrorDTO error) {
                // Case B, C: 오류 건 - 로그 테이블에 기록
                reconcileLogMapper.insertErrorLog(error);
            }
        }
    }
}