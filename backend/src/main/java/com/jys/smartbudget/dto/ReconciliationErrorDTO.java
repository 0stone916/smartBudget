package com.jys.smartbudget.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ReconciliationErrorDTO {
    private String approvalNo;
    private String errorType;  // MISSING, MISMATCH
    private Long bankAmount;
    private Long cmsAmount;
}