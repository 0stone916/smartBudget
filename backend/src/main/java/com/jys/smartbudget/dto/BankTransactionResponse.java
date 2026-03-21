package com.jys.smartbudget.dto;

public record BankTransactionResponse(
        String approval_no,
        Long amount) {
}