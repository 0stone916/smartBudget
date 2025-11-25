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
    private String category;      // 메모
    private Integer amount;   // 지출 금액
    private String description;      // 메모
    private Integer year;
    private Integer month;
    private Integer day;
    private String userId;    // 유저
}
