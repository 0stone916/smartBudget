package com.jys.smartbudget.dto;

import lombok.Data;

@Data
public class BudgetUsageDTO {
    private String category;
    private double budgetAmount;
    private double spentAmount;
    private double usageRate; // (spent / budget) * 100
}
