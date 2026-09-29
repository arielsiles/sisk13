package com.encens.khipus.util.employees.payroll.tributary;

import com.encens.khipus.model.employees.AFPRate;
import com.encens.khipus.model.employees.CategoryTributaryPayroll;
import com.encens.khipus.model.employees.SIPContributionConcept;
import com.encens.khipus.model.employees.SIPContributionRegime;
import com.encens.khipus.util.BigDecimalUtil;
import com.encens.khipus.util.employees.payroll.structure.Calculator;

import java.math.BigDecimal;

/**
 * Aportes patronales al Sistema Integral de Pensiones, sobre el total ganado: riesgo
 * profesional, pro vivienda y solidario patronal. Los paga el empleador, no el trabajador.
 * <p/>
 * Se eximen solo si el {@link SIPContributionRegime} del contrato lo indica; por defecto se
 * cobran siempre, incluso a un jubilado que continua trabajando -el riesgo profesional lo
 * sigue cubriendo y los otros dos financian vivienda y el fondo solidario-.
 *
 * @author
 * @version 3.4
 */
public class PatronalAFPRetentionCalculator extends Calculator<CategoryTributaryPayroll> {
    private static final int TWO_DECIMAL_SCALE = 2;
    private BigDecimal patronalAFPRate;
    private AFPRate patronalProffesionalRiskRetentionAFP;
    private AFPRate patronalProHomeRetentionAFP;
    private AFPRate patronalSolidaryRetentionAFP;
    private SIPContributionRegime contributionRegime;

    public PatronalAFPRetentionCalculator(BigDecimal patronalAFPRate,
                                          AFPRate patronalProffesionalRiskRetentionAFP,
                                          AFPRate patronalProHomeRetentionAFP,
                                          AFPRate patronalSolidaryRetentionAFP,
                                          SIPContributionRegime contributionRegime) {
        this.patronalAFPRate = patronalAFPRate;
        this.patronalProffesionalRiskRetentionAFP = patronalProffesionalRiskRetentionAFP;
        this.patronalProHomeRetentionAFP = patronalProHomeRetentionAFP;
        this.patronalSolidaryRetentionAFP = patronalSolidaryRetentionAFP;
        this.contributionRegime = contributionRegime;
    }

    @Override
    public void execute(CategoryTributaryPayroll instance) {
        boolean applies = SIPContributionConcept.contributes(contributionRegime, SIPContributionConcept.PATRONAL);
        BigDecimal totalGrained = instance.getTotalGrained();

        instance.setPatronalRetentionAFP(percentageIf(applies, totalGrained, patronalAFPRate));
        instance.setPatronalProffesionalRiskRetentionAFP(percentageIf(applies, totalGrained,
                null == patronalProffesionalRiskRetentionAFP ? null : patronalProffesionalRiskRetentionAFP.getRate()));
        instance.setPatronalProHomeRetentionAFP(percentageIf(applies, totalGrained,
                null == patronalProHomeRetentionAFP ? null : patronalProHomeRetentionAFP.getRate()));
        instance.setPatronalSolidaryRetentionAFP(percentageIf(applies, totalGrained,
                null == patronalSolidaryRetentionAFP ? null : patronalSolidaryRetentionAFP.getRate()));
    }

    private BigDecimal percentageIf(boolean applies, BigDecimal totalGrained, BigDecimal rate) {
        if (!applies || null == rate) {
            return BigDecimal.ZERO;
        }
        return BigDecimalUtil.getPercentage(totalGrained, rate, TWO_DECIMAL_SCALE);
    }
}
