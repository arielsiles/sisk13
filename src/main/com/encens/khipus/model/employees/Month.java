package com.encens.khipus.model.employees;

import com.encens.khipus.util.DateUtils;

import java.util.Date;

/**
 * Month enumeration
 *
 * @author
 * @version 1.2.4
 */
public enum Month {
    JANUARY(0, "Month.january"),
    FEBRUARY(1, "Month.february"),
    MARCH(2, "Month.march"),
    APRIL(3, "Month.april"),
    MAY(4, "Month.may"),
    JUNE(5, "Month.june"),
    JULY(6, "Month.july"),
    AUGUST(7, "Month.august"),
    SEPTEMBER(8, "Month.september"),
    OCTOBER(9, "Month.october"),
    NOVEMBER(10, "Month.november"),
    DECEMBER(11, "Month.december");

    private int value;
    private String resourceKey;

    Month(int value, String resourceKey) {
        this.value = value;
        this.resourceKey = resourceKey;
    }

    public int getValue() {
        return value;
    }

    public int getValueAsPosition() {
        return value + 1;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public String getResourceKey() {
        return resourceKey;
    }

    public void setResourceKey(String resourceKey) {
        this.resourceKey = resourceKey;
    }

    public static Month getCurrentMonth() {
        return getMonth(new Date());
    }

    public static Month getMonth(Date date) {
        Integer currentMonth = DateUtils.getCurrentMonth(date);

        for (Month month : values()) {
            if (month.getValue() == (currentMonth - 1)) {
                return month;
            }
        }

        return JANUARY;
    }

    public static Month getMonth(Integer currentMonth) {
        for (Month month : values()) {
            if (month.getValue() == (currentMonth - 1)) {
                return month;
            }
        }

        return JANUARY;
    }

    public static Month getMonthByCalendarIndex(Integer calendarIndex) {
        for (Month month : values()) {
            if (month.getValue() == calendarIndex) {
                return month;
            }
        }

        return JANUARY;
    }

    public String getMonthLiteral() {
        String result = "";
        if (Month.JANUARY == this)
            result = "Enero";
        if (Month.FEBRUARY == this)
            result = "Febrero";
        if (Month.MARCH == this)
            result = "Marzo";
        if (Month.APRIL == this)
            result = "Abril";
        if (Month.MAY == this)
            result = "Mayo";
        if (Month.JUNE == this)
            result = "Junio";
        if (Month.JULY == this)
            result = "Julio";
        if (Month.AUGUST == this)
            result = "Agosto";
        if (Month.SEPTEMBER == this)
            result = "Septiembre";
        if (Month.OCTOBER == this)
            result = "Octubre";
        if (Month.NOVEMBER == this)
            result = "Noviembre";
        if (Month.DECEMBER == this)
            result = "Diciembre";

        return result;
    }
}
