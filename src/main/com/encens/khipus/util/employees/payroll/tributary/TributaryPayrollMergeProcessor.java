package com.encens.khipus.util.employees.payroll.tributary;

import com.encens.khipus.model.employees.CategoryTributaryPayroll;
import com.encens.khipus.model.employees.PayrollGenerationCycle;
import com.encens.khipus.model.employees.TributaryPayroll;
import com.encens.khipus.util.employees.payroll.structure.BasicMergeCalculator;
import com.encens.khipus.util.employees.payroll.structure.MergeProcessor;

/**
 * @author
 * @version 3.4
 */
public class TributaryPayrollMergeProcessor extends MergeProcessor<TributaryPayroll> {

    private PayrollGenerationCycle payrollGenerationCycle;
    private TributaryPayroll tributaryPayroll;
    private CategoryTributaryPayroll categoryTributaryPayroll;

    public TributaryPayrollMergeProcessor(PayrollGenerationCycle payrollGenerationCycle, TributaryPayroll tributaryPayroll, CategoryTributaryPayroll categoryTributaryPayroll) {
        this.payrollGenerationCycle = payrollGenerationCycle;
        this.tributaryPayroll = tributaryPayroll;
        this.categoryTributaryPayroll = categoryTributaryPayroll;
    }

    @Override
    protected void initialize() {
        if (tributaryPayroll == null) {
            tributaryPayroll = new TributaryPayroll();
            tributaryPayroll.setPayrollGenerationCycle(payrollGenerationCycle);
            tributaryPayroll.setEmployee(categoryTributaryPayroll.getEmployee());
            tributaryPayroll.setBusinessUnit(categoryTributaryPayroll.getBusinessUnit());
            tributaryPayroll.setCode(categoryTributaryPayroll.getCode());

            tributaryPayroll.setName(categoryTributaryPayroll.getName());
            //todo this part must be checked for correct value
            tributaryPayroll.setJobContract(categoryTributaryPayroll.getJobContract());
            //todo this part must be checked for correct value
            tributaryPayroll.setEntranceDate(categoryTributaryPayroll.getEntranceDate());
        }
        addCalculator(BasicMergeCalculator.sum("basicAmount", tributaryPayroll, categoryTributaryPayroll));
        addCalculator(BasicMergeCalculator.max("seniorityYears", tributaryPayroll, categoryTributaryPayroll));
        addCalculator(BasicMergeCalculator.sum("seniorityBonus", tributaryPayroll, categoryTributaryPayroll));
        addCalculator(BasicMergeCalculator.sum("extraHourCost", tributaryPayroll, categoryTributaryPayroll));
        addCalculator(BasicMergeCalculator.sum("extraHour", tributaryPayroll, categoryTributaryPayroll));
        addCalculator(BasicMergeCalculator.sum("sundayBonus", tributaryPayroll, categoryTributaryPayroll));
        addCalculator(BasicMergeCalculator.sum("productionBonus", tributaryPayroll, categoryTributaryPayroll));
        addCalculator(BasicMergeCalculator.sum("otherBonus", tributaryPayroll, categoryTributaryPayroll));
        addCalculator(BasicMergeCalculator.sum("otherIncomes", tributaryPayroll, categoryTributaryPayroll));
        addCalculator(BasicMergeCalculator.sum("totalOtherIncomes", tributaryPayroll, categoryTributaryPayroll));
        addCalculator(BasicMergeCalculator.sum("fiscalCredit", tributaryPayroll, categoryTributaryPayroll));
        addCalculator(BasicMergeCalculator.sum("unlikeTaxable", tributaryPayroll, categoryTributaryPayroll));
        addCalculator(BasicMergeCalculator.sum("tax", tributaryPayroll, categoryTributaryPayroll));
        addCalculator(BasicMergeCalculator.sum("taxForTwoSMN", tributaryPayroll, categoryTributaryPayroll));
        addCalculator(BasicMergeCalculator.sum("retentionClearance", tributaryPayroll, categoryTributaryPayroll));
        addCalculator(BasicMergeCalculator.sum("maintenanceOfValue", tributaryPayroll, categoryTributaryPayroll));
        addCalculator(BasicMergeCalculator.sum("retentionAFP", tributaryPayroll, categoryTributaryPayroll));
        addCalculator(BasicMergeCalculator.sum("solidaryAFP", tributaryPayroll, categoryTributaryPayroll));
        addCalculator(BasicMergeCalculator.sum("patronalRetentionAFP", tributaryPayroll, categoryTributaryPayroll));
        addCalculator(BasicMergeCalculator.sum("patronalProffesionalRiskRetentionAFP", tributaryPayroll, categoryTributaryPayroll));
        addCalculator(BasicMergeCalculator.sum("patronalProHomeRetentionAFP", tributaryPayroll, categoryTributaryPayroll));
        addCalculator(BasicMergeCalculator.sum("patronalSolidaryRetentionAFP", tributaryPayroll, categoryTributaryPayroll));
        addCalculator(BasicMergeCalculator.sum("cns", tributaryPayroll, categoryTributaryPayroll));
        addCalculator(BasicMergeCalculator.sum("netSalary", tributaryPayroll, categoryTributaryPayroll));
        addCalculator(BasicMergeCalculator.sum("salaryNotTaxableTwoSMN", tributaryPayroll, categoryTributaryPayroll));
        addCalculator(BasicMergeCalculator.sum("physicalBalance", tributaryPayroll, categoryTributaryPayroll));
        addCalculator(BasicMergeCalculator.sum("dependentBalance", tributaryPayroll, categoryTributaryPayroll));
        addCalculator(BasicMergeCalculator.sum("lastMonthBalance", tributaryPayroll, categoryTributaryPayroll));
        addCalculator(BasicMergeCalculator.sum("lastBalanceUpdated", tributaryPayroll, categoryTributaryPayroll));
        addCalculator(BasicMergeCalculator.sum("dependentTotalBalance", tributaryPayroll, categoryTributaryPayroll));
        addCalculator(BasicMergeCalculator.sum("usedBalance", tributaryPayroll, categoryTributaryPayroll));
        addCalculator(BasicMergeCalculator.sum("dependentBalanceToNextMonth", tributaryPayroll, categoryTributaryPayroll));
        addCalculator(BasicMergeCalculator.sum("totalGrained", tributaryPayroll, categoryTributaryPayroll));
    }

    @Override
    protected TributaryPayroll getInstance() {
        return tributaryPayroll;
    }
}
