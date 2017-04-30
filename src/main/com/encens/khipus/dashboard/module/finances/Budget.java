package com.encens.khipus.dashboard.module.finances;

import com.encens.khipus.dashboard.component.factory.DashboardObject;
import com.encens.khipus.dashboard.component.totalizer.Sum;

import java.math.BigDecimal;

/**
 * @author
 * @version 2.7
 */
public class Budget implements DashboardObject {
    private String monthName;
    private Integer year;

    private BigDecimal yearlyBudget;
    @Sum(fieldResultName = "totalMonthlyBudget")
    private BigDecimal monthlyBudget;

    private BigDecimal accruedExecution;

    @Sum(fieldResultName = "totalMonthlyExecution")
    private BigDecimal monthlyExecution;

    public Object getIdentifier() {
        return monthName;
    }

    public String getMonthName() {
        return monthName;
    }

    public void setMonthName(String monthName) {
        this.monthName = monthName;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public BigDecimal getYearlyBudget() {
        return yearlyBudget;
    }

    public void setYearlyBudget(BigDecimal yearlyBudget) {
        this.yearlyBudget = yearlyBudget;
    }

    public BigDecimal getMonthlyBudget() {
        return monthlyBudget;
    }

    public void setMonthlyBudget(BigDecimal monthlyBudget) {
        this.monthlyBudget = monthlyBudget;
    }

    public BigDecimal getAccruedExecution() {
        return accruedExecution;
    }

    public void setAccruedExecution(BigDecimal accruedExecution) {
        this.accruedExecution = accruedExecution;
    }

    public BigDecimal getMonthlyExecution() {
        return monthlyExecution;
    }

    public void setMonthlyExecution(BigDecimal monthlyExecution) {
        this.monthlyExecution = monthlyExecution;
    }
}
