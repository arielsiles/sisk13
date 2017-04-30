package com.encens.khipus.util.employees.payroll.tributary;

import com.encens.khipus.model.employees.CategoryTributaryPayroll;
import com.encens.khipus.model.employees.GestionPayroll;
import com.encens.khipus.util.DateUtils;
import com.encens.khipus.util.employees.payroll.structure.Calculator;

import java.util.Date;

/**
 * @author
 * @version 3.4
 */
public class SeniorityYearsCalculator extends Calculator<CategoryTributaryPayroll> {
    private static final Integer DAYS_OF_YEAR = 365;
    private GestionPayroll gestionPayroll;

    public SeniorityYearsCalculator(GestionPayroll gestionPayroll) {
        this.gestionPayroll = gestionPayroll;
    }

    @Override
    public void execute(CategoryTributaryPayroll instance) {
        instance.setSeniorityYears(0);
        Date hireDate = instance.getEntranceDate();
        if (null != hireDate) {
            Integer seniorityYears = calculateSeniorityYears(hireDate);
            instance.setSeniorityYears(seniorityYears);
        }
    }


    private Integer calculateSeniorityYears(Date hireDate) {
        Long seniorityDays = DateUtils.daysBetween(hireDate, gestionPayroll.getEndDate());
        Integer counter = 0;
        while (seniorityDays >= DAYS_OF_YEAR) {
            counter++;
            seniorityDays = seniorityDays - DAYS_OF_YEAR;
        }
        return counter;
    }
}
