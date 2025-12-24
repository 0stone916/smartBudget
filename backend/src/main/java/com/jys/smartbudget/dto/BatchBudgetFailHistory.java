package com.jys.smartbudget.dto;

import lombok.*;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchBudgetFailHistory {
    
    private Long id;
    private String jobName;
    private String userId;
    private Integer year;
    private Integer month;
    private String category;
    private String reason;
}
