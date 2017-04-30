package com.encens.khipus.dashboard.module.finances;

import com.encens.khipus.dashboard.component.factory.DashboardObject;
import com.encens.khipus.dashboard.component.totalizer.Sum;

import java.math.BigDecimal;

/**
 * @author
 * @version 2.7
 */
public class BudgetExtended implements DashboardObject {
    private String code;
    private String name;

    @Sum(fieldResultName = "yearBudget")
    private BigDecimal yearBudget;
    @Sum(fieldResultName = "accruedExecution")
    private BigDecimal accruedExecution;
    @Sum(fieldResultName = "monthBudget")
    private BigDecimal monthBudget;
    @Sum(fieldResultName = "monthExecution")
    private BigDecimal monthExecution;
    @Sum(fieldResultName = "monthVarianceExecution")
    private BigDecimal monthVarianceExecution;
    @Sum(fieldResultName = "currentExecution")
    private BigDecimal currentExecution;
    @Sum(fieldResultName = "yearVarianceExecution")
    private BigDecimal yearVarianceExecution;
    @Sum(fieldResultName = "yearPercentageExecution")
    private BigDecimal yearPercentageExecution;


    public Object getIdentifier() {
        return code;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getYearBudget() {
        return yearBudget;
    }

    public void setYearBudget(BigDecimal yearBudget) {
        this.yearBudget = yearBudget;
    }

    public BigDecimal getAccruedExecution() {
        return accruedExecution;
    }

    public void setAccruedExecution(BigDecimal accruedExecution) {
        this.accruedExecution = accruedExecution;
    }

    public BigDecimal getMonthBudget() {
        return monthBudget;
    }

    public void setMonthBudget(BigDecimal monthBudget) {
        this.monthBudget = monthBudget;
    }

    public BigDecimal getMonthVarianceExecution() {
        return monthVarianceExecution;
    }

    public void setMonthVarianceExecution(BigDecimal monthVarianceExecution) {
        this.monthVarianceExecution = monthVarianceExecution;
    }

    public BigDecimal getCurrentExecution() {
        return currentExecution;
    }

    public void setCurrentExecution(BigDecimal currentExecution) {
        this.currentExecution = currentExecution;
    }

    public BigDecimal getYearVarianceExecution() {
        return yearVarianceExecution;
    }

    public void setYearVarianceExecution(BigDecimal yearVarianceExecution) {
        this.yearVarianceExecution = yearVarianceExecution;
    }

    public BigDecimal getYearPercentageExecution() {
        return yearPercentageExecution;
    }

    public void setYearPercentageExecution(BigDecimal yearPercentageExecution) {
        this.yearPercentageExecution = yearPercentageExecution;
    }

    public BigDecimal getMonthExecution() {
        return monthExecution;
    }

    public void setMonthExecution(BigDecimal monthExecution) {
        this.monthExecution = monthExecution;
    }
}
