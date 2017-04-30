package com.encens.khipus.util.employees.payroll.tributary;

import com.encens.khipus.model.employees.CategoryTributaryPayroll;
import com.encens.khipus.model.employees.SeniorityBonus;
import com.encens.khipus.model.employees.SeniorityBonusDetail;
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

    @Override
    public void execute(CategoryTributaryPayroll instance) {
        instance.setSeniorityBonus(BigDecimal.ZERO);
        if (null != instance.getSeniorityYears()) {
            SeniorityBonusDetail rule = searchSeniorityRule(instance.getSeniorityYears());
            if (null != rule) {
                instance.setSeniorityBonus(rule.getAmount());
            }
        }
    }

    private SeniorityBonusDetail searchSeniorityRule(Integer seniority) {
        if (null != seniorityBonus) {
            for (SeniorityBonusDetail detail : seniorityBonus.getDetails()) {
                if (null != detail.getEndYear()) {
                    if (detail.getStartYear() <= seniority && seniority <= detail.getEndYear()) {
                        return detail;
                    }
                } else {
                    if (detail.getStartYear() <= seniority) {
                        return detail;
                    }
                }
            }
        }

        return null;
    }
}
