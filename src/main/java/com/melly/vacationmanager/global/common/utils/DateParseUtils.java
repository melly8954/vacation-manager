package com.melly.vacationmanager.global.common.utils;

import com.melly.vacationmanager.global.common.enums.ErrorCode;
import com.melly.vacationmanager.global.common.exception.CustomException;

import java.time.LocalDate;

public class DateParseUtils {
    public static int parseYear(String year, LocalDate today) {
        if (year == null || year.isBlank()) {
            // 입력값 없으면 현재 연도 리턴
            return today.getYear();
        }
        try {
            return Integer.parseInt(year);
        } catch (NumberFormatException e) {
            throw new CustomException(ErrorCode.INVALID_YEAR_FORMAT);
        }
    }

    public static int parseMonth(String month, LocalDate today) {
        if (month == null || month.isBlank()) {
            // 입력값 없으면 현재 월 리턴
            return today.getMonthValue();
        }
        try {
            int m = Integer.parseInt(month);
            if (m < 1 || m > 12) {
                throw new CustomException(ErrorCode.MONTH_OUT_OF_RANGE);
            }
            return m;
        } catch (NumberFormatException e) {
            throw new CustomException(ErrorCode.INVALID_MONTH_FORMAT);
        }
    }
}