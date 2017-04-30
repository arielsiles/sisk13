package com.encens.khipus.util.employees.payroll.tributary;

import com.encens.khipus.model.employees.AFPRate;
import com.encens.khipus.model.employees.CategoryTributaryPayroll;
import com.encens.khipus.util.BigDecimalUtil;
import com.encens.khipus.util.employees.payroll.structure.Calculator;

import java.math.BigDecimal;

/**
 * @author
 * @version 3.4
 */
public class PatronalAFPRetentionCalculator extends Calculator<CategoryTributaryPayroll> {
    private static final int TWO_DECIMAL_SCALE = 2;
    private BigDecimal patronalAFPRate;
    private AFPRate patronalProffesionalRiskRetentionAFP;
    private AFPRate patronalProHomeRetentionAFP;
    private AFPRate patronalSolidaryRetentionAFP;

    public PatronalAFPRetentionCalculator(BigDecimal patronalAFPRate,
                                          AFPRate patronalProffesionalRiskRetentionAFP,
                                          AFPRate patronalProHomeRetentionAFP,
                                          AFPRate patronalSolidaryRetentionAFP) {
        this.patronalAFPRate = patronalAFPRate;
        this.patronalProffesionalRiskRetentionAFP = patronalProffesionalRiskRetentionAFP;
        this.patronalProHomeRetentionAFP = patronalProHomeRetentionAFP;
        this.patronalSolidaryRetentionAFP = patronalSolidaryRetentionAFP;
    }

    @Override
    public void execute(CategoryTributaryPayroll instance) {
        instance.setPatronalRetentionAFP(BigDecimalUtil.getPercentage(instance.getTotalGrained(),
                patronalAFPRate, TWO_DECIMAL_SCALE));
        instance.setPatronalProffesionalRiskRetentionAFP(BigDecimalUtil.getPercentage(instance.getTotalGrained(),
                patronalProffesionalRiskRetentionAFP.getRate(), TWO_DECIMAL_SCALE));
        instance.setPatronalProHomeRetentionAFP(BigDecimalUtil.getPercentage(instance.getTotalGrained(),
                patronalProHomeRetentionAFP.getRate(), TWO_DECIMAL_SCALE));
        instance.setPatronalSolidaryRetentionAFP(BigDecimalUtil.getPercentage(instance.getTotalGrained(),
                patronalSolidaryRetentionAFP.getRate(), TWO_DECIMAL_SCALE));
    }
}
