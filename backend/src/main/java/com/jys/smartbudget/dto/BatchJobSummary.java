package com.jys.smartbudget.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BatchJobSummary {

    private Long id;

    private String jobName;

    private int baseYear;
    private int baseMonth;

    private int targetYear;
    private int targetMonth;

    private long successCount;
    private long skipCount;
    private long failCount;
}
