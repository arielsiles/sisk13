package com.encens.khipus.util.employees.payroll.tributary;

import com.encens.khipus.model.employees.CategoryTributaryPayroll;
import com.encens.khipus.model.employees.GestionPayroll;
import com.encens.khipus.util.employees.payroll.Seniority;
import com.encens.khipus.util.employees.payroll.structure.Calculator;

import java.util.Date;

/**
 * @author
 * @version 3.4
 */
public class SeniorityYearsCalculator extends Calculator<CategoryTributaryPayroll> {
    private GestionPayroll gestionPayroll;

    public SeniorityYearsCalculator(GestionPayroll gestionPayroll) {
        this.gestionPayroll = gestionPayroll;
    }

    /**
     * La regla vive en {@link Seniority}, no aca: la planilla de sueldos muestra la misma
     * antiguedad y con dos copias algun dia dirian anios distintos para la misma persona.
     */
    @Override
    public void execute(CategoryTributaryPayroll instance) {
        instance.setSeniorityYears(
                Seniority.yearsOf(instance.getEntranceDate(), gestionPayroll.getEndDate()));
    }
}
