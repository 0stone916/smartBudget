package com.jys.smartbudget.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import org.hibernate.validator.constraints.Range;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class BudgetDTO extends PageRequestDTO {
    private Long id;          // 고유번호

    @NotNull(message = "예산 금액은 필수 항목입니다.")
    @Min(value = 0, message = "예산 금액은 0원 이상이어야 합니다.")
    private Integer amount;   // 금액

    @NotNull(message = "년도는 필수입니다.")
    @Min(value = 2000, message = "년도가 올바르지 않습니다.")
    private Integer year;

    @NotNull(message = "월은 필수입니다.")
    @Range(min = 1, max = 12, message = "월은 1~12 사이여야 합니다.")
    private Integer month;

    private String description; // 상세설명

    private String userId;          // 유저아이디
    
    private Integer version = 0; //Optimistic Lock용 // 선언과 동시에 0으로 초기화(Optimistic Lock 테스트 시 version가져오기 위해)
    
    @NotNull(message = "카테고리 정보는 필수입니다.")
    private CategoryDTO category;
}
