package com.jys.smartbudget.batch;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import com.jys.smartbudget.dto.BudgetDTO;
import com.jys.smartbudget.dto.CategoryDTO;
import com.jys.smartbudget.dto.ExpenseDTO;
import com.jys.smartbudget.dto.UserDTO;
import com.jys.smartbudget.mapper.BudgetMapper;
import com.jys.smartbudget.mapper.ExpenseMapper;
import com.jys.smartbudget.mapper.UserMapper;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;

@SpringBatchTest
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class MonthlyBudgetIntegrationTest {

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    private Job monthlyBudgetJob;

    @Autowired
    private Job failedBudgetReprocessJob;

    @Autowired
    private BudgetMapper budgetMapper;

    @Autowired
    private ExpenseMapper expenseMapper;

    @Autowired
    private UserMapper userMapper;

    private final String userId = "testuser";

    // 🔑 테스트 중 생성된 PK 보관
    private final List<Long> budgetIds = new ArrayList<>();
    private final List<Long> expenseIds = new ArrayList<>();

    private final YearMonth baseYm = YearMonth.now().minusMonths(1);
    private final YearMonth targetYm = YearMonth.now();

    @BeforeEach
    void setUp() {
        budgetMapper.deleteBudgetsByUserId(userId);
        expenseMapper.deleteExpensesByUserId(userId);
        userMapper.deleteUser(userId);
        // 1️⃣ User 생성
        userMapper.insertUser(
            UserDTO.builder()
                .userId(userId)
                .password("pw")
                .name("테스트유저")
                .email("test@test.com")
                .build()
        );

        userMapper.changeAutoBudgetPolicy(true, userId, userId);
        

        // 2️⃣ 기준월 Budget 2건 생성
        Long foodBudgetId = insertBaseBudget("FOOD");
        Long transportBudgetId = insertBaseBudget("TRANSPORT");

        // 3️⃣ 각 Budget에 정상 Expense 생성
        insertExpense(foodBudgetId, 10_000);
        insertExpense(transportBudgetId, 20_000);
    }

    private Long insertBaseBudget(String categoryCode) {
        BudgetDTO budget = new BudgetDTO();
        budget.setUserId(userId);
        budget.setYear(baseYm.getYear());
        budget.setMonth(baseYm.getMonthValue());
        budget.setAmount(100_000);

        CategoryDTO category = new CategoryDTO();
        category.setCode(categoryCode);
        budget.setCategory(category);

        budgetMapper.insertBudget(budget);
        budgetIds.add(budget.getId());
        return budget.getId();
    }

    private void insertExpense(Long budgetId, int amount) {
        ExpenseDTO expense = new ExpenseDTO();
        expense.setBudgetId(budgetId);
        expense.setUserId(userId);
        expense.setYear(baseYm.getYear());
        expense.setMonth(baseYm.getMonthValue());
        expense.setDay(1);
        expense.setAmount(amount);
        expense.setDescription("테스트 지출");

        expenseMapper.insertExpense(expense);
        expenseIds.add(expense.getId());
    }

    @Test
    void 기존배치_실패1건_재배치로_정상복구() throws Exception {

        // ===== 1️⃣ 기존 배치 =====
        jobLauncherTestUtils.setJob(monthlyBudgetJob);
        JobExecution monthlyExecution =
            jobLauncherTestUtils.launchJob();

        Assertions.assertEquals(
            ExitStatus.COMPLETED,
            monthlyExecution.getExitStatus()
        );

        // FOOD 실패 / TRANSPORT 성공 → 1건
        int afterMonthly =
            budgetMapper.countByYearMonth(
                targetYm.getYear(),
                targetYm.getMonthValue()
            );
        Assertions.assertEquals(1, afterMonthly);

        // ===== 2️⃣ 재배치 =====
        jobLauncherTestUtils.setJob(failedBudgetReprocessJob);
        JobExecution reprocessExecution =
            jobLauncherTestUtils.launchJob();

        Assertions.assertEquals(
            ExitStatus.COMPLETED,
            reprocessExecution.getExitStatus()
        );

        // ===== 3️⃣ 최종 검증 =====
        int afterReprocess =
            budgetMapper.countByYearMonth(
                targetYm.getYear(),
                targetYm.getMonthValue()
            );

        Assertions.assertEquals(2, afterReprocess);
    }

    @AfterEach
    void tearDown() {
        // 🔥 삭제 순서 중요: Expense → Budget → User
        for (Long expenseId : expenseIds) {
            expenseMapper.deleteExpenseByIdAndUserId(expenseId, userId);
        }

        for (Long budgetId : budgetIds) {
            budgetMapper.deleteBudgetByIdAndUserId(budgetId, userId);
        }

        userMapper.deleteUser(userId);
    }
}
