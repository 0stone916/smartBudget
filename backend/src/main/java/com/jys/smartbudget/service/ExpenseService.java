package com.jys.smartbudget.service;

import com.jys.smartbudget.dto.ExpenseDTO;
import com.jys.smartbudget.dto.SearchRequest;
import com.jys.smartbudget.dto.StatisticsDTO;
import com.jys.smartbudget.mapper.ExpenseMapper;
import jakarta.persistence.OptimisticLockException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final ExpenseMapper expenseMapper;

    private final SqlSessionFactory sqlSessionFactory;

    @Transactional
    public void insertDummyData() {
        // 배치 모드로 SqlSession 오픈
        try (SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH, false)) {
            // 배치용 매퍼 가져오기
            ExpenseMapper batchMapper = sqlSession.getMapper(ExpenseMapper.class);

            for (int i = 0; i <= 1000000; i++) {
                ExpenseDTO dto = new ExpenseDTO();
                dto.setBudgetId(120L);
                dto.setAmount(3000);
                dto.setYear(2026);
                dto.setMonth(2);
                dto.setDay((int) (Math.random() * 28) + 1);
                dto.setDescription("더미 지출 ");
                dto.setUserId("user3");

                batchMapper.insertExpense(dto);

                // 1000건마다 flush (메모리 관리)
                if (i % 1000 == 0) {
                    sqlSession.flushStatements();
                }
            }
            sqlSession.commit(); // 루프 종료 후 최종 커밋
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void insertDummyData2() {
        for (int i = 0; i <= 1000000; i++) {
            try {
                ExpenseDTO dto = new ExpenseDTO();
                dto.setBudgetId(120L);
                dto.setAmount(3000);
                dto.setYear(2026);
                dto.setMonth(2);
                dto.setDay((int) (Math.random() * 28) + 1);
                dto.setDescription("더미 지출 " + i);
                dto.setUserId("user3");
                
                expenseMapper.insertExpense(dto);
            } catch (Exception e) {
                System.err.println(">>> 데이터 삽입 중 에러 발생: " + e.getMessage());
                e.printStackTrace(); // 여기서 에러 스택트레이스를 강제로 찍습니다.
                break; // 에러 나면 멈춤
            }
            
            // 1만 건마다 로그 출력
            if (i % 1000000 == 0) System.out.println(i + "건 삽입 완료...");
        }
    }

    public List<ExpenseDTO> searchExpenses(SearchRequest searchRequest) {
        return expenseMapper.searchExpenses(searchRequest);
    }

    public List<StatisticsDTO> getExpenseStatistics(SearchRequest searchRequest) {
        return expenseMapper.getExpenseStatistics(searchRequest);
    }

    public void insertExpense(ExpenseDTO expense) {
        expenseMapper.insertExpense(expense);
    }

    @Transactional
    public void updateExpense(ExpenseDTO expense) {
       int updatedCount =  expenseMapper.updateExpense(expense);

        if (updatedCount == 0) {
            throw new OptimisticLockException("이미 수정된 데이터입니다.");
        }
    }

    public void deleteExpense(Long id, String userId) {
        expenseMapper.deleteExpenseByIdAndUserId(id, userId);
    }

    public Boolean checkOverBudget(ExpenseDTO expense) {
        return expenseMapper.checkOverBudget(expense);
    }

    public boolean hasExpensesByBudgetId(Long budgetId) {
        return expenseMapper.hasExpensesByBudgetId(budgetId) > 0;
    }



}
