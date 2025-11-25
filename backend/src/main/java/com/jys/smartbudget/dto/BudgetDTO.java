package com.jys.smartbudget.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class BudgetDTO extends PageRequestDTO {
    private Long id;          // 고유번호
    private String category;  // 카테고리 (식비, 교통비 등)
    private Integer amount;   // 금액
    private Integer year;
    private Integer month;
    private String budgetDescription; // 상세설명
    private String userId;          // 유저아이디
    private String categoryDescription; // 카테고리 설명
    

}
