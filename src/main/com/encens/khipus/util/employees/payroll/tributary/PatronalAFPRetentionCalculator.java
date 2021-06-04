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
public class PatronalAFPRetentionCalculator extends Calculator<CategoryTributaryPayroll> { // /** Ojo Patronal and Laboral **/
    private static final int TWO_DECIMAL_SCALE = 2;
    private BigDecimal patronalAFPRate;
    private AFPRate patronalProffesionalRiskRetentionAFP;
    private AFPRate patronalProHomeRetentionAFP;
    private AFPRate patronalSolidaryRetentionAFP;

    /*private AFPRate laborIndividualAFP;
    private AFPRate laborCommonRiskAFP;
    private AFPRate laborSolidaryContributionAFP;
    private AFPRate laborComissionAFP;*/

    public PatronalAFPRetentionCalculator(BigDecimal patronalAFPRate,
                                          AFPRate patronalProffesionalRiskRetentionAFP,
                                          AFPRate patronalProHomeRetentionAFP,
                                          AFPRate patronalSolidaryRetentionAFP
                                          /*AFPRate laborIndividualAFP,
                                          AFPRate laborCommonRiskAFP,
                                          AFPRate laborSolidaryContributionAFP,
                                          AFPRate laborComissionAFP*/
                                                                                        ) {
        this.patronalAFPRate = patronalAFPRate;
        this.patronalProffesionalRiskRetentionAFP = patronalProffesionalRiskRetentionAFP;
        this.patronalProHomeRetentionAFP = patronalProHomeRetentionAFP;
        this.patronalSolidaryRetentionAFP = patronalSolidaryRetentionAFP;

        /*this.laborIndividualAFP = laborIndividualAFP;
        this.laborCommonRiskAFP = laborCommonRiskAFP;
        this.laborSolidaryContributionAFP = laborSolidaryContributionAFP;
        this.laborComissionAFP = laborComissionAFP;*/
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

        /** todo AFP **/
        if(instance.getJobContract().getContract().getEmployee().getIdNumber().equals("815059")){
            instance.setLaborCommonRiskAFP(BigDecimal.ZERO);
        }
        if(instance.getJobContract().getContract().getEmployee().getIdNumber().equals("2862262")){
            instance.setLaborIndividualAFP(BigDecimal.ZERO);
        }
        if(instance.getJobContract().getContract().getEmployee().getIdNumber().equals("2868139")){
            instance.setLaborIndividualAFP(BigDecimal.ZERO);
            instance.setLaborCommonRiskAFP(BigDecimal.ZERO);
        }
        if(instance.getJobContract().getContract().getEmployee().getIdNumber().equals("921886")){
            instance.setLaborCommonRiskAFP(BigDecimal.ZERO);
        }
        /** **/

        /*instance.setLaborIndividualAFP(BigDecimalUtil.getPercentage(instance.getTotalGrained(), laborIndividualAFP.getRate(), TWO_DECIMAL_SCALE));
        instance.setLaborCommonRiskAFP(BigDecimalUtil.getPercentage(instance.getTotalGrained(), laborCommonRiskAFP.getRate(), TWO_DECIMAL_SCALE));
        instance.setLaborSolidaryContributionAFP(BigDecimalUtil.getPercentage(instance.getTotalGrained(), laborSolidaryContributionAFP.getRate(), TWO_DECIMAL_SCALE));
        instance.setLaborComissionAFP(BigDecimalUtil.getPercentage(instance.getTotalGrained(), laborComissionAFP.getRate(), TWO_DECIMAL_SCALE));*/

    }
}
