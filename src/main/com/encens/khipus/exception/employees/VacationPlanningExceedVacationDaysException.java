package com.encens.khipus.exception.employees;

import javax.ejb.ApplicationException;

/**
 * @author
 * @version 3.4
 */
@ApplicationException(rollback = true)
public class VacationPlanningExceedVacationDaysException extends Exception {
    private Integer vacationDays;
    private Integer daysUsed;

    public VacationPlanningExceedVacationDaysException(Integer vacationDays, Integer daysUsed) {
        this.vacationDays = vacationDays;
        this.daysUsed = daysUsed;
    }

    public VacationPlanningExceedVacationDaysException(String message, Integer vacationDays, Integer daysUsed) {
        super(message);
        this.vacationDays = vacationDays;
        this.daysUsed = daysUsed;
    }

    public VacationPlanningExceedVacationDaysException(String message, Throwable cause, Integer vacationDays, Integer daysUsed) {
        super(message, cause);
        this.vacationDays = vacationDays;
        this.daysUsed = daysUsed;
    }

    public VacationPlanningExceedVacationDaysException(Throwable cause, Integer vacationDays, Integer daysUsed) {
        super(cause);
        this.vacationDays = vacationDays;
        this.daysUsed = daysUsed;
    }

    public Integer getVacationDays() {
        return vacationDays;
    }

    public Integer getDaysUsed() {
        return daysUsed;
    }
}
