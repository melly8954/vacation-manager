package com.melly.vacationmanager.global.common.utils;

import java.time.LocalDate;

public class DateParseUtils {
    public static int parseYear(String year, LocalDate today) {
        if (year == null || year.isBlank()) {
            return today.getYear();
        }
        try {
            return Integer.parseInt(year);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Year must be a valid number");
        }
    }

    public static int parseMonth(String month, LocalDate today) {
        if (month == null || month.isBlank()) {
            return today.getMonthValue();
        }
        try {
            int m = Integer.parseInt(month);
            if (m < 1 || m > 12) throw new IllegalArgumentException("Month must be between 1 and 12");
            return m;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Month must be a valid number");
        }
    }
}