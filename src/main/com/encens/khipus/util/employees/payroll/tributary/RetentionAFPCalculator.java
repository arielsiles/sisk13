package com.encens.khipus.util.employees.payroll.tributary;

import com.encens.khipus.model.employees.*;
import com.encens.khipus.util.BigDecimalUtil;
import com.encens.khipus.util.Constants;
import com.encens.khipus.util.employees.payroll.structure.Calculator;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author
 * @version 3.4
 */
public class RetentionAFPCalculator extends Calculator<CategoryTributaryPayroll> {
    private static final int TWO_DECIMAL_SCALE = 2;
    private static final Double AGE_COMPLIANT_PERCENTAGE = 1.0;
    private static final Double NOT_AGE_COMPLIANT_PERCENTAGE = 2.71;
    private AFPRate afpRate;
    private AFPRate laborIndividualAFP;
    private AFPRate laborCommonRiskAFP;
    private AFPRate laborSolidaryContributionAFP;
    private AFPRate laborComissionAFP;
    private Date endDate;
    private DiscountRule nationalSolidaryAFPDiscountRule;

    public RetentionAFPCalculator(AFPRate afpRate,
                                  AFPRate laborIndividualAFP,
                                  AFPRate laborCommonRiskAFP,
                                  AFPRate laborSolidaryContributionAFP,
                                  AFPRate laborComissionAFP,
                                  DiscountRule nationalSolidaryAFPDiscountRule, Date endDate) {
        this.afpRate = afpRate;
        this.laborIndividualAFP = laborIndividualAFP;
        this.laborCommonRiskAFP = laborCommonRiskAFP;
        this.laborSolidaryContributionAFP = laborSolidaryContributionAFP;
        this.laborComissionAFP = laborComissionAFP;
        this.nationalSolidaryAFPDiscountRule = nationalSolidaryAFPDiscountRule;
        this.endDate = endDate;
    }

    @Override
    public void execute(CategoryTributaryPayroll instance) {
        BigDecimal retentionAFP;
        BigDecimal solydaryAFPRetention = BigDecimal.ZERO;
        BigDecimal afpRatePercentage;
        if (!instance.getEmployee().getJubilateFlag()) {
            afpRatePercentage = afpRate.getRate();
        } else {
            Integer ageInDays = instance.getEmployee().computeAgeInDaysAtDate(endDate);
            Double ageInYears = ageInDays.doubleValue() / Constants.YEAR_DAYS.doubleValue();
            afpRatePercentage = BigDecimalUtil.toBigDecimal(ageInYears.compareTo(Constants.JUBILATION_AGE) >= 0 ? AGE_COMPLIANT_PERCENTAGE : NOT_AGE_COMPLIANT_PERCENTAGE);
        }
        BigDecimal totalGrained = instance.getTotalGrained();
        List<DiscountRuleRange> discountRuleRangeList = findDiscountRuleRangeListInList(totalGrained, nationalSolidaryAFPDiscountRule);
        for (DiscountRuleRange discountRuleRange : discountRuleRangeList) {
            BigDecimal amount;
            if (discountRuleRange.getDiscountRule().getDiscountUnitType().equals(DiscountUnitType.CURRENCY)) {
                amount = discountRuleRange.getDiscountRule().getCurrency().getSymbol().equalsIgnoreCase("$US") ?
                        BigDecimalUtil.multiply(discountRuleRange.getAmount(), instance.getGeneratedPayroll().getExchangeRate().getSale()) :
                        discountRuleRange.getAmount();
            } else {
                //percentage case
                amount = BigDecimalUtil.divide(BigDecimalUtil.multiply(BigDecimalUtil.subtract(totalGrained, BigDecimalUtil.toBigDecimal(discountRuleRange.getInitRange())), discountRuleRange.getAmount(), Constants.BIG_DECIMAL_DEFAULT_SCALE), BigDecimalUtil.ONE_HUNDRED, Constants.BIG_DECIMAL_DEFAULT_SCALE);
            }
            solydaryAFPRetention = BigDecimalUtil.sum(solydaryAFPRetention, amount);
        }

        //retentionAFP = BigDecimalUtil.getPercentage(totalGrained, afpRatePercentage, TWO_DECIMAL_SCALE);
        //retentionAFP = BigDecimalUtil.sum(retentionAFP, solydaryAFPRetention);
        instance.setSolidaryAFP(solydaryAFPRetention);
        //instance.setRetentionAFP(retentionAFP);

        /** revision **/

        instance.setLaborIndividualAFP(BigDecimalUtil.getPercentage(instance.getTotalGrained(),  laborIndividualAFP.getRate(), TWO_DECIMAL_SCALE));
        instance.setLaborCommonRiskAFP(BigDecimalUtil.getPercentage(instance.getTotalGrained(), laborCommonRiskAFP.getRate(), TWO_DECIMAL_SCALE));
        instance.setLaborSolidaryContributionAFP(BigDecimalUtil.getPercentage(instance.getTotalGrained(), laborSolidaryContributionAFP.getRate(), TWO_DECIMAL_SCALE));
        instance.setLaborComissionAFP(BigDecimalUtil.getPercentage(instance.getTotalGrained(), laborComissionAFP.getRate(), TWO_DECIMAL_SCALE));

        /** todo AFP **/
        if(instance.getJobContract().getContract().getEmployee().getIdNumber().equals("815059")){
            instance.setLaborCommonRiskAFP(BigDecimal.ZERO);
        }
        if(instance.getJobContract().getContract().getEmployee().getIdNumber().equals("2862262")){ // Juana Pozo
            instance.setLaborIndividualAFP(BigDecimal.ZERO);
            instance.setLaborCommonRiskAFP(BigDecimal.ZERO);
        }
        if(instance.getJobContract().getContract().getEmployee().getIdNumber().equals("2868139")){ // Eliseo Camacho
            instance.setLaborIndividualAFP(BigDecimal.ZERO);
            instance.setLaborCommonRiskAFP(BigDecimal.ZERO);
        }
        if(instance.getJobContract().getContract().getEmployee().getIdNumber().equals("921886")){
            instance.setLaborCommonRiskAFP(BigDecimal.ZERO);
        }
        /** **/

        retentionAFP = BigDecimalUtil.sum(
                instance.getLaborIndividualAFP(),
                instance.getLaborCommonRiskAFP(),
                instance.getLaborSolidaryContributionAFP(),
                instance.getLaborComissionAFP(),
                instance.getSolidaryAFP());
        instance.setRetentionAFP(retentionAFP);
        /** End revision **/
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
