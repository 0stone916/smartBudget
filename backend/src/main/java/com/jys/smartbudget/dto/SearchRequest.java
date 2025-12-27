package com.jys.smartbudget.dto;

import org.hibernate.validator.constraints.Range;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter @Setter
public class SearchRequest {

    @NotNull(message = "년도는 필수입니다.")
    @Min(value = 2000, message = "년도가 올바르지 않습니다.")
    private Integer year;

    @NotNull(message = "월은 필수입니다.")
    @Range(min = 1, max = 12, message = "월은 1~12 사이여야 합니다.")
    private Integer month;
}