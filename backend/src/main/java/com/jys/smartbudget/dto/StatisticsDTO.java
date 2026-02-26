
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
public class StatisticsDTO {
    private String code;
    private Long totalAmount; // SUM 결과이므로 Long 권장
}