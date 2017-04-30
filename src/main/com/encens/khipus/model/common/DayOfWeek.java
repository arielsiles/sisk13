package com.encens.khipus.model.common;

import java.util.Arrays;
import java.util.List;

/**
 * @author
 * @version 3.1
 */
public enum DayOfWeek {
    MONDAY(1, 2, "DayOfWeek.MONDAY"),
    TUESDAY(2, 3, "DayOfWeek.TUESDAY"),
    WEDNESDAY(3, 4, "DayOfWeek.WEDNESDAY"),
    THURSDAY(4, 5, "DayOfWeek.THURSDAY"),
    FRIDAY(5, 6, "DayOfWeek.FRIDAY"),
    SATURDAY(6, 7, "DayOfWeek.SATURDAY"),
    SUNDAY(7, 1, "DayOfWeek.SUNDAY");

    // this value is reference to Joda DateTime
    private int dateTimeValue;
    // this value is reference to DayMap.java
    private int calendarValue;
    private String resourceKey;

    DayOfWeek(int dateTimeValue, int calendarValue, String resourceKey) {
        this.dateTimeValue = dateTimeValue;
        this.calendarValue = calendarValue;
        this.resourceKey = resourceKey;
    }

    public String getResourceKey() {
        return resourceKey;
    }

    public int getDateTimeValue() {
        return dateTimeValue;
    }

    public int getCalendarValue() {
        return calendarValue;
    }

    public static List<DayOfWeek> find(int start, int length) {
        DayOfWeek[] result = new DayOfWeek[length];
        System.arraycopy(values(), start, result, 0, length);
        return Arrays.asList(result);
    }

    public static DayOfWeek getDayOfWeek(Integer calendarDayOfWeek) {
        for (DayOfWeek dayOfWeek : values()) {
            if (dayOfWeek.getCalendarValue() == calendarDayOfWeek) {
                return dayOfWeek;
            }
        }
        return MONDAY;
    }
}