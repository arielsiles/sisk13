package com.encens.khipus.exception.employees;

import javax.ejb.ApplicationException;

import java.math.BigDecimal;

/**
 * @author
 * @version 3.4
 */
@ApplicationException(rollback = true)
public class VacationPlanningExceedVacationDaysException extends Exception {
    private BigDecimal vacationDays;
    private BigDecimal daysUsed;
    private Integer advanceDaysLimit = 0;

    public VacationPlanningExceedVacationDaysException(BigDecimal vacationDays, BigDecimal daysUsed) {
        this.vacationDays = vacationDays;
        this.daysUsed = daysUsed;
    }

    public VacationPlanningExceedVacationDaysException(String message, BigDecimal vacationDays, BigDecimal daysUsed) {
        super(message);
        this.vacationDays = vacationDays;
        this.daysUsed = daysUsed;
    }

    public VacationPlanningExceedVacationDaysException(String message, Throwable cause, BigDecimal vacationDays, BigDecimal daysUsed) {
        super(message, cause);
        this.vacationDays = vacationDays;
        this.daysUsed = daysUsed;
    }

    public VacationPlanningExceedVacationDaysException(Throwable cause, BigDecimal vacationDays, BigDecimal daysUsed) {
        super(cause);
        this.vacationDays = vacationDays;
        this.daysUsed = daysUsed;
    }

    public BigDecimal getVacationDays() {
        return vacationDays;
    }

    public BigDecimal getDaysUsed() {
        return daysUsed;
    }

    public Integer getAdvanceDaysLimit() {
        return advanceDaysLimit;
    }

    public void setAdvanceDaysLimit(Integer advanceDaysLimit) {
        this.advanceDaysLimit = advanceDaysLimit;
    }
}
