package com.jys.smartbudget.dto;

import java.time.LocalDateTime;

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
    private String processedYn;
    private LocalDateTime processedAt;
}
