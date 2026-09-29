package com.encens.khipus.util.employees.payroll.tributary;

import com.encens.khipus.model.employees.CNSRate;
import com.encens.khipus.model.employees.CategoryTributaryPayroll;
import com.encens.khipus.model.employees.SIPContributionConcept;
import com.encens.khipus.model.employees.SIPContributionRegime;
import com.encens.khipus.util.BigDecimalUtil;
import com.encens.khipus.util.employees.payroll.structure.Calculator;

import java.math.BigDecimal;

/**
 * Prima patronal a la Caja Nacional de Salud sobre el total ganado. Es seguridad social de
 * corto plazo por la relacion laboral vigente, un regimen distinto al de pensiones, asi que
 * por defecto se cobra siempre; el {@link SIPContributionRegime} puede eximirla.
 *
 * @author
 * @version 3.4
 */
public class PatronalOtherRetentionCalculator extends Calculator<CategoryTributaryPayroll> {
    private static final int TWO_DECIMAL_SCALE = 2;
    private CNSRate cnsRate;
    private SIPContributionRegime contributionRegime;

    public PatronalOtherRetentionCalculator(CNSRate cnsRate, SIPContributionRegime contributionRegime) {
        this.cnsRate = cnsRate;
        this.contributionRegime = contributionRegime;
    }

    @Override
    public void execute(CategoryTributaryPayroll instance) {
        if (null == cnsRate || !SIPContributionConcept.contributes(contributionRegime, SIPContributionConcept.CNS)) {
            instance.setCns(BigDecimal.ZERO);
            return;
        }
        instance.setCns(BigDecimalUtil.getPercentage(instance.getTotalGrained(),
                cnsRate.getRate(), TWO_DECIMAL_SCALE));
    }
}
