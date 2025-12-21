package com.jys.smartbudget;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import com.jys.smartbudget.dto.BudgetDTO;
import com.jys.smartbudget.mapper.BudgetMapper;
import lombok.extern.slf4j.Slf4j;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Slf4j
class BudgetDuplicateInsertTest {

    @Autowired
    private BudgetMapper budgetMapper;

    private final String userId = "testuser";

    private Long firstId;

    @Test
    @DisplayName("같은 연월/카테고리 예산은 중복 등록할 수 없다")
    void duplicateBudgetInsert_shouldFail() {
        // Given
        BudgetDTO first = new BudgetDTO();
        first.setUserId(userId);
        first.setYear(2025);
        first.setMonth(12);
        first.setCategory("FOOD");
        first.setAmount(500_000);
        first.setBudgetDescription("첫 번째 예산");

        budgetMapper.insertBudget(first);
        firstId = first.getId();

        log.info("테스트 데이터 생성: id={}, version={}", firstId, first.getVersion());

        // When & Then : 해당 의도한 에러가 발생해야 테스트 성공
        assertThatThrownBy(() -> budgetMapper.insertBudget(first))
                .isInstanceOf(org.springframework.dao.DuplicateKeyException.class);
    }

    @AfterEach
    void tearDown() {
        // 테스트에서 생성한 id만 삭제하여 DB를 깨끗하게 유지
        if (firstId != null) {
            budgetMapper.deleteBudget(firstId, userId);
            log.info("테스트 데이터 삭제: id={}", firstId);
        }
    }
}