package com.jys.smartbudget.mapper;

import com.jys.smartbudget.dto.ExpenseDTO;
import com.jys.smartbudget.dto.NotiRequestDto;
import com.jys.smartbudget.dto.SearchRequest;
import org.apache.ibatis.annotations.Mapper;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface ExpenseMapper {

    // 지출내역 조회
    List<ExpenseDTO> getExpenses(SearchRequest searchRequest);

    // bank에서 알림받은 지출정보 저장
    void insertExpense(NotiRequestDto expense);

    // approval_no로 CMS 데이터 조회 (검증 배치Processor에서 사용)
    ExpenseDTO findByApprovalNo(String approvalNo);

    // 검증 배치 시 어제 내역 조회
    List<ExpenseDTO> findExpensesByCondition(String accountNumber, LocalDateTime startDateTime,
            LocalDateTime endDateTime);

    // 검증배치 시 완료 상태 업데이트 (Writer에서 사용)
    void updateStatusVerified(String approvalNo);

}
