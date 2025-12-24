package com.jys.smartbudget.dto;

import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ExpenseDTO {

    private Long id;          // PK
    private Long budgetId;    // FK -> budget.id
    private String category;      // 카테고리 설명
    private String categoryCode;      // 카테고리 코드
    private Integer amount;   // 지출 금액
    private String description;      // 메모
    private Integer year;
    private Integer month;
    private Integer day;
    private String userId;    // 유저

    private Integer version;         //Optimistic Lock용

}
