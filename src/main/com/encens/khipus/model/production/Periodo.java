package com.encens.khipus.model.production;

import java.util.Calendar;
import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: Diego
 * Date: 31/08/13
 * Time: 12:07
 * To change this template use File | Settings | File Templates.
 */
public enum Periodo {
    FIRSTPERIODO("Periodo.first", 1, 15),
    SECONDPERIODO("Periodo.second", 16, 30);

    private int initDay;
    private int endDay;
    private Date initDate;
    private Date endDate;
    private String resourceKey;

    private Periodo(String resourceKey, int initDay, int endDay) {
        this.initDay = initDay;
        this.endDay = endDay;
        this.resourceKey = resourceKey;
    }

   /* public static Periodo getPeriodo(int initDay, int endDay) {
        this.initDay = initDay;
        this.endDay = endDay;
        if(endDay > 15)
            this.resourceKey = "Periodo.second";
        else
            this.resourceKey = "Periodo.first";
        return this;
    }*/

    public String getResourceKey() {
        return resourceKey;
    }

    public void setResourceKey(String resourceKey) {
        this.resourceKey = resourceKey;
    }

    public int getInitDay() {
        return initDay;
    }

    public void setInitDay(int initDay) {
        this.initDay = initDay;
    }

    public int getEndDay(int mount, int year) {
        if ((mount == 1 || mount == 3 || mount == 5 || mount == 7 || mount == 8 || mount == 10 || mount == 12) && (resourceKey == "Periodo.second"))
            return endDay + 1;

        if (mount == 2 && (resourceKey == "Periodo.second")) {
            if (isBisiesto(year)) {
                return 29;
            }
            return 28;
        }
        return endDay;
    }

    private boolean isBisiesto(int year) {
        if (year % 4 == 0 && (year % 100 != 0 || year % 400 == 0)) {
            return true;
        }

        return false;
    }

    public int getEndDay() {

        return endDay;
    }

    public void setEndDay(int endDay) {
        this.endDay = endDay;
    }

    public String getPeriodoLiteral() {
        return (resourceKey == "Periodo.first") ? " Primera Quincena " : " Segunda Quincena ";
    }
}