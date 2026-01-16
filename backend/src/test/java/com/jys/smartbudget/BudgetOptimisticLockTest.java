package com.jys.smartbudget;

import com.jys.smartbudget.dto.BudgetDTO;
import com.jys.smartbudget.dto.CategoryDTO;
import com.jys.smartbudget.mapper.BudgetMapper;
import com.jys.smartbudget.service.BudgetService;
import jakarta.persistence.OptimisticLockException;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest // 스프링 통합 테스트 환경 구축
// @Transactional테스트 완료 후 데이터 롤백하려고했지만 
// 메인 스레드가 커넥션을 잡고 안 놔주는 상태에서 다른 스레드들이 대기하다가 Connection is not available 에러발생해서 AfterEach로 대체
@Slf4j

//JUnit은 기본적으로 독립성 원칙이므로 테스트 메서드 간 순서가 없음
//순서를 지정해주고 싶으면 사용: @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class BudgetOptimisticLockTest {

    @Autowired
    private BudgetService budgetService;

    private Long budgetId;
    private final String userId = "testuser";

    @BeforeEach // 각 테스트 시작 전 공통 데이터 준비
    void setUp() {
        BudgetDTO budget = new BudgetDTO();
        budget.setUserId(userId);
        budget.setYear(2026);
        budget.setMonth(12);
        budget.setAmount(500_000);
        budget.setDescription("테스트 예산");

        CategoryDTO category = new CategoryDTO();
        category.setCode("FOOD");
        budget.setCategory(category);

        budgetService.insertBudget(budget);
        budgetId = budget.getId();      //쿼리의 useGeneratedKeys="true" keyProperty="id"기능으로 insert된 id 가져옴

        //useGeneratedKeys="true" keyProperty="id"는 id만 가져오고 version은 가져오지못함 DTO의 version을 0으로 선언과 동시에 초기화로 해결
        log.info("테스트 데이터 생성: id={}, version={}", budgetId, budget.getVersion());
    }

    @Test
    @DisplayName("낙관적 락 기본 동작: version 불일치 시 수정 실패")
    void optimisticLock_basic() {
        // [1] 동일한 시점의 데이터를 두 번 조회 (둘 다 version 0)
        BudgetDTO first = budgetService.selectById(budgetId);
        BudgetDTO second = budgetService.selectById(budgetId);

        // [2] 첫 번째 수정 요청: 성공 (DB version 0 -> 1로 변경)
        first.setAmount(600_000);
        budgetService.updateBudget(first);

        // [3] 두 번째 수정 요청: 실패 (가지고 있는 version은 0인데 DB는 이미 1임)
        second.setAmount(700_000); 
        assertThatThrownBy(() -> budgetService.updateBudget(second))
                .isInstanceOf(OptimisticLockException.class);
    }

    @Test
    @DisplayName("동시 수정 시 하나만 성공하고 나머지는 실패")
    void optimisticLock_concurrency() throws InterruptedException {
        // 스레드 여러개 생성
        int threadCount = 4;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        // 모든 스레드 작업이 끝날 때까지 대기하기 위한 장치
        CountDownLatch latch = new CountDownLatch(threadCount);

        // 멀티스레드 환경에서 안전하게 숫자를 세기 위한 변수
        AtomicInteger success = new AtomicInteger();
        AtomicInteger fail = new AtomicInteger();

        // [1] 기준 데이터 조회 (모두가 이 version으로 시작하게 됨)
        BudgetDTO base = budgetService.selectById(budgetId);

        for (int i = 0; i < threadCount; i++) {
            executor.execute(() -> {
                try {
                    // [2] 각 스레드는 독립적인 객체를 가져야 함 (BeanUtils로 필드 복사)
                    BudgetDTO budget = new BudgetDTO();
                    BeanUtils.copyProperties(base, budget);
                    
                    budget.setAmount(base.getAmount() + 10_000);

                    // [3] 수정 시도: 가장 먼저 도착한 요청만 성공
                    budgetService.updateBudget(budget);
                    success.incrementAndGet();
                } catch (OptimisticLockException e) {
                    // [4] 이미 version이 변경된 경우 예외 발생
                    fail.incrementAndGet();
                } finally {
                    latch.countDown(); // 스레드 작업 완료 알림
                }
            });
        }

        latch.await(); // 모든 스레드 작업이 끝날 때까지 대기
        executor.shutdown();

        // [5] 결과 검증: 1개만 성공하고 나머지는 전부 실패했는지 확인
        assertThat(success.get()).isEqualTo(1);
        assertThat(fail.get()).isEqualTo(threadCount - 1);

        // [6] 최종 DB 상태 확인: 버전이 1번만 상승했는지 검증
        BudgetDTO finalBudget = budgetService.selectById(budgetId);
        assertThat(finalBudget.getVersion()).isEqualTo(1);

        //세가지검증에 하나라도 통과못하면 빌드 fail
    }

    @Test
    @DisplayName("잘못된 version으로 수정 시 무조건 실패")
    void optimisticLock_invalidVersion() {
        BudgetDTO budget = budgetService.selectById(budgetId);

        // [1] DB에 없는 억지 버전으로 세팅
        budget.setVersion(999); 
        budget.setAmount(999_999);

        // [2] version 조건이 일치하지 않아 업데이트 행이 0이 되므로 예외 발생
        assertThatThrownBy(() -> budgetService.updateBudget(budget))
                .isInstanceOf(OptimisticLockException.class);
    }

    @AfterEach
    void tearDown() {
        // 테스트에서 생성한 id만 삭제하여 DB를 깨끗하게 유지
        if (budgetId != null) {
            budgetService.deleteBudgetByIdAndUserId(budgetId, userId);
            log.info("테스트 데이터 삭제: id={}", budgetId);
        }
    }
}