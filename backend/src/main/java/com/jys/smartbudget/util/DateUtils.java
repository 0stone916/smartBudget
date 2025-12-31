package com.jys.smartbudget.util;

import java.time.DateTimeException;
import java.time.LocalDate;
import com.jys.smartbudget.exception.BusinessException;
import com.jys.smartbudget.exception.ErrorCode;

public class DateUtils {
    public static void validateDate(Integer year, Integer month, Integer day) {
        if (year == null || month == null || day == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }
        try {
            LocalDate.of(year, month, day);
        } catch (DateTimeException e) {
            throw new BusinessException(ErrorCode.INVALID_DATE_FORMAT);
        }
    }
}