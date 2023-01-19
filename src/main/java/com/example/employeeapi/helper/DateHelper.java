package com.example.employeeapi.helper;

import com.example.employeeapi.exception.InvalidDateException;

import java.time.DayOfWeek;
import java.time.LocalDate;

public class DateHelper {

    public static boolean isWeekend(LocalDate date) {
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        return dayOfWeek != DayOfWeek.SATURDAY && dayOfWeek != DayOfWeek.SUNDAY;
    }

    public static int calculateLeaveDays(LocalDate startDate, LocalDate endDate) {
        int leaveDays = 0;
        for (LocalDate date = startDate; date.isBefore(endDate); date = date.plusDays(1)) {
            if (!isWeekend(date)) {
                leaveDays++;
            }
        }
        return leaveDays;
    }

    public static int calculateRemainingDaysForNewRecord(LocalDate hireDate) {
        LocalDate today = LocalDate.now();
        int seniority = today.getYear() - hireDate.getYear();

        if (seniority < 0) {
            throw new InvalidDateException("Hire date can not be later than this year");
        }
        if (seniority > 1 && seniority <= 5) {
            return 15;
        }

        if (seniority > 5 && seniority <= 10) {
            return 18;
        }

        if (seniority > 10) {
            return 24;
        }

        return 5;
    }
}
