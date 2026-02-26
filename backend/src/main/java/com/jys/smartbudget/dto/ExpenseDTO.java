package com.jys.smartbudget.dto;

import org.hibernate.validator.constraints.Range;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ExpenseDTO {

    private Long id;          // PK

    private Long budgetId;    // FK -> budget.id

    @NotNull(message = "예산 금액은 필수 항목입니다.")
    @Min(value = 0, message = "예산 금액은 0원 이상이어야 합니다.")
    private Integer amount;   // 지출 금액

    @NotNull(message = "년도는 필수입니다.")
    @Min(value = 2000, message = "년도가 올바르지 않습니다.")
    private Integer year;

    @NotNull(message = "월은 필수입니다.")
    @Range(min = 1, max = 12, message = "월은 1~12 사이여야 합니다.")
    private Integer month;

    private String description;      // 메모

    @NotNull(message = "일자는 필수 항목입니다.")
    @Range(min = 1, max = 31, message = "일자는 1~31일 사이여야 합니다.")
    private Integer day;
    
    private String userId;    // 유저

    private Integer version;         //Optimistic Lock용

    private CategoryDTO category;

    private Long totalAmount;

}
