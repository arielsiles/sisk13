package com.encens.khipus.util.employees.payroll.tributary;

import com.encens.khipus.model.employees.CategoryTributaryPayroll;
import com.encens.khipus.model.employees.SeniorityBonus;
import com.encens.khipus.util.employees.payroll.Seniority;
import com.encens.khipus.util.employees.payroll.structure.Calculator;

import java.math.BigDecimal;

/**
 * @author
 * @version 3.4
 */
public class SeniorityBonusCalculator extends Calculator<CategoryTributaryPayroll> {
    private SeniorityBonus seniorityBonus;

    public SeniorityBonusCalculator(SeniorityBonus seniorityBonus) {
        this.seniorityBonus = seniorityBonus;
    }

    /**
     * Aca no hace falta preguntar nada: esta cadena corre <b>solo</b> para quien esta activo para
     * generacion de planilla fiscal, y eso ya da derecho al bono. Quien tiene modalidad LABORAL
     * pero no genera planilla fiscal no pasa por aca, y se le paga en la planilla de sueldos.
     */
    @Override
    public void execute(CategoryTributaryPayroll instance) {
        instance.setSeniorityBonus(Seniority.bonusOf(seniorityBonus, instance.getSeniorityYears()));
    }
}
