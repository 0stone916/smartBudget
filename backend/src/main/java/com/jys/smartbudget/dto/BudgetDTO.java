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
    private Integer amount;   // 금액
    private Integer year;     // 년
    private Integer month;    // 월
    private String description; // 상세설명
    private String userId;          // 유저아이디
    private Integer version = 0; //Optimistic Lock용 // 선언과 동시에 0으로 초기화(Optimistic Lock 테스트 시 version가져오기 위해)
    
    private CategoryDTO category;
}
