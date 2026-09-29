package com.encens.khipus.util.employees.payroll.tributary;

import com.encens.khipus.model.employees.*;
import com.encens.khipus.util.BigDecimalUtil;
import com.encens.khipus.util.Constants;
import com.encens.khipus.util.employees.payroll.structure.Calculator;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Aportes del asegurado al Sistema Integral de Pensiones, sobre el total ganado.
 * <p/>
 * Que componentes se cobran lo decide el {@link SIPContributionRegime} del contrato. Un
 * regimen nulo -catalogo vacio o contrato sin regimen asignado- cobra todos los aportes,
 * que es el comportamiento historico del sistema.
 *
 * @author
 * @version 3.4
 */
public class RetentionAFPCalculator extends Calculator<CategoryTributaryPayroll> {
    private static final int TWO_DECIMAL_SCALE = 2;
    private AFPRate laborIndividualAFP;
    private AFPRate laborCommonRiskAFP;
    private AFPRate laborSolidaryContributionAFP;
    private AFPRate laborComissionAFP;
    private DiscountRule nationalSolidaryAFPDiscountRule;
    private SIPContributionRegime contributionRegime;

    public RetentionAFPCalculator(AFPRate laborIndividualAFP,
                                  AFPRate laborCommonRiskAFP,
                                  AFPRate laborSolidaryContributionAFP,
                                  AFPRate laborComissionAFP,
                                  DiscountRule nationalSolidaryAFPDiscountRule,
                                  SIPContributionRegime contributionRegime) {
        this.laborIndividualAFP = laborIndividualAFP;
        this.laborCommonRiskAFP = laborCommonRiskAFP;
        this.laborSolidaryContributionAFP = laborSolidaryContributionAFP;
        this.laborComissionAFP = laborComissionAFP;
        this.nationalSolidaryAFPDiscountRule = nationalSolidaryAFPDiscountRule;
        this.contributionRegime = contributionRegime;
    }

    @Override
    public void execute(CategoryTributaryPayroll instance) {
        BigDecimal totalGrained = instance.getTotalGrained();

        instance.setLaborIndividualAFP(percentageIf(contributes(SIPContributionConcept.INDIVIDUAL_ACCOUNT),
                totalGrained, laborIndividualAFP));
        instance.setLaborCommonRiskAFP(percentageIf(contributes(SIPContributionConcept.COMMON_RISK),
                totalGrained, laborCommonRiskAFP));
        instance.setLaborSolidaryContributionAFP(percentageIf(contributes(SIPContributionConcept.SOLIDARY),
                totalGrained, laborSolidaryContributionAFP));
        instance.setLaborComissionAFP(percentageIf(contributes(SIPContributionConcept.COMISSION),
                totalGrained, laborComissionAFP));
        instance.setSolidaryAFP(contributes(SIPContributionConcept.NATIONAL_SOLIDARY)
                ? calculateNationalSolidary(instance, totalGrained) : BigDecimal.ZERO);

        instance.setRetentionAFP(BigDecimalUtil.sum(
                instance.getLaborIndividualAFP(),
                instance.getLaborCommonRiskAFP(),
                instance.getLaborSolidaryContributionAFP(),
                instance.getLaborComissionAFP(),
                instance.getSolidaryAFP()));
    }

    private boolean contributes(SIPContributionConcept concept) {
        return SIPContributionConcept.contributes(contributionRegime, concept);
    }

    private BigDecimal percentageIf(boolean applies, BigDecimal totalGrained, AFPRate afpRate) {
        if (!applies || null == afpRate) {
            return BigDecimal.ZERO;
        }
        return BigDecimalUtil.getPercentage(totalGrained, afpRate.getRate(), TWO_DECIMAL_SCALE);
    }

    /**
     * Aporte Nacional Solidario: escalonado sobre el excedente de cada tramo de la regla de
     * descuento. Todos los tramos que cubren el total ganado suman, de modo que la escala
     * de la Ley 065 se configura agregando filas a la regla, sin tocar codigo.
     */
    private BigDecimal calculateNationalSolidary(CategoryTributaryPayroll instance, BigDecimal totalGrained) {
        BigDecimal result = BigDecimal.ZERO;
        if (null == nationalSolidaryAFPDiscountRule) {
            return result;
        }
        for (DiscountRuleRange discountRuleRange : findDiscountRuleRangeListInList(totalGrained, nationalSolidaryAFPDiscountRule)) {
            BigDecimal amount;
            if (discountRuleRange.getDiscountRule().getDiscountUnitType().equals(DiscountUnitType.CURRENCY)) {
                amount = discountRuleRange.getDiscountRule().getCurrency().getSymbol().equalsIgnoreCase("$US") ?
                        BigDecimalUtil.multiply(discountRuleRange.getAmount(), instance.getGeneratedPayroll().getExchangeRate().getSale()) :
                        discountRuleRange.getAmount();
            } else {
                //percentage case
                amount = BigDecimalUtil.divide(
                        BigDecimalUtil.multiply(
                                BigDecimalUtil.subtract(totalGrained, BigDecimalUtil.toBigDecimal(discountRuleRange.getInitRange())),
                                discountRuleRange.getAmount(), Constants.BIG_DECIMAL_DEFAULT_SCALE),
                        BigDecimalUtil.ONE_HUNDRED, Constants.BIG_DECIMAL_DEFAULT_SCALE);
            }
            result = BigDecimalUtil.sum(result, amount);
        }
        return result;
    }

    public List<DiscountRuleRange> findDiscountRuleRangeListInList(BigDecimal amount, DiscountRule discountRule) {
        List<DiscountRuleRange> discountRuleRanges = new ArrayList<DiscountRuleRange>();
        for (DiscountRuleRange discountRuleRange : discountRule.getDiscountRuleRangeList()) {
            if ((null == discountRuleRange.getInitRange() || discountRuleRange.getInitRange().doubleValue() <= (amount.doubleValue()))
                    && (null == discountRuleRange.getEndRange() || discountRuleRange.getEndRange().doubleValue() >= (amount.doubleValue()))) {
                discountRuleRanges.add(discountRuleRange);
            }
        }
        return discountRuleRanges;
    }
}
