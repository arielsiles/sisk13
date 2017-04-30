package com.encens.khipus.util.employees.payroll.fiscal;

import com.encens.khipus.model.employees.CategoryFiscalPayroll;
import com.encens.khipus.model.employees.FiscalPayroll;
import com.encens.khipus.model.employees.PayrollGenerationCycle;
import com.encens.khipus.util.FormatUtils;
import com.encens.khipus.util.employees.payroll.structure.BasicMergeCalculator;
import com.encens.khipus.util.employees.payroll.structure.MergeProcessor;

/**
 * @author
 * @version 3.4
 */
public class FiscalPayrollMergeProcessor extends MergeProcessor<FiscalPayroll> {
    private PayrollGenerationCycle payrollGenerationCycle;
    private FiscalPayroll fiscalPayroll;
    private CategoryFiscalPayroll categoryFiscalPayroll;

    public FiscalPayrollMergeProcessor(PayrollGenerationCycle payrollGenerationCycle, FiscalPayroll fiscalPayroll, CategoryFiscalPayroll categoryFiscalPayroll) {
        this.payrollGenerationCycle = payrollGenerationCycle;
        this.fiscalPayroll = fiscalPayroll;
        this.categoryFiscalPayroll = categoryFiscalPayroll;
    }

    @Override
    protected void initialize() {
        if (fiscalPayroll == null) {
            fiscalPayroll = new FiscalPayroll();
            fiscalPayroll.setPayrollGenerationCycle(payrollGenerationCycle);
            fiscalPayroll.setEmployee(categoryFiscalPayroll.getEmployee());
            fiscalPayroll.setBusinessUnit(categoryFiscalPayroll.getBusinessUnit());
            fiscalPayroll.setBirthday(categoryFiscalPayroll.getBirthday());
            fiscalPayroll.setGender(categoryFiscalPayroll.getGender());
            fiscalPayroll.setName(categoryFiscalPayroll.getName());
            fiscalPayroll.setPersonalIdentifier(categoryFiscalPayroll.getPersonalIdentifier());
            fiscalPayroll.setNationality(FormatUtils.evaluateNotNullValue(categoryFiscalPayroll.getNationality()));
            fiscalPayroll.setOccupation(FormatUtils.evaluateNotNullValue(categoryFiscalPayroll.getOccupation()));
            //todo this part must be checked for correct value
            fiscalPayroll.setJobContract(categoryFiscalPayroll.getJobContract());
            //todo this part must be checked for correct value
            fiscalPayroll.setEntranceDate(categoryFiscalPayroll.getEntranceDate());
            //todo this part must be checked for correct value
            fiscalPayroll.setNewnessType(categoryFiscalPayroll.getNewnessType());
        }
        addCalculator(BasicMergeCalculator.sum("seniorityBonus", fiscalPayroll, categoryFiscalPayroll));
        addCalculator(BasicMergeCalculator.max("seniorityYears", fiscalPayroll, categoryFiscalPayroll));
        addCalculator(BasicMergeCalculator.sum("sundayBonus", fiscalPayroll, categoryFiscalPayroll));
        addCalculator(BasicMergeCalculator.sum("productionBonus", fiscalPayroll, categoryFiscalPayroll));
        addCalculator(BasicMergeCalculator.sum("extraHourCost", fiscalPayroll, categoryFiscalPayroll));
        addCalculator(BasicMergeCalculator.avg("workedDays", fiscalPayroll, categoryFiscalPayroll));
        addCalculator(BasicMergeCalculator.avg("paidDays", fiscalPayroll, categoryFiscalPayroll));
        addCalculator(BasicMergeCalculator.sum("basicAmount", fiscalPayroll, categoryFiscalPayroll));
        addCalculator(BasicMergeCalculator.avg("hourDayPayment", fiscalPayroll, categoryFiscalPayroll));
        addCalculator(BasicMergeCalculator.sum("extraHour", fiscalPayroll, categoryFiscalPayroll));
        addCalculator(BasicMergeCalculator.sum("retentionClearance", fiscalPayroll, categoryFiscalPayroll));
        addCalculator(BasicMergeCalculator.sum("liquidPayment", fiscalPayroll, categoryFiscalPayroll));
        addCalculator(BasicMergeCalculator.sum("otherBonus", fiscalPayroll, categoryFiscalPayroll));
        addCalculator(BasicMergeCalculator.sum("otherIncomes", fiscalPayroll, categoryFiscalPayroll));
        addCalculator(BasicMergeCalculator.sum("tardinessMinutesDiscount", fiscalPayroll, categoryFiscalPayroll));
        addCalculator(BasicMergeCalculator.sum("absenceMinutesDiscount", fiscalPayroll, categoryFiscalPayroll));
        addCalculator(BasicMergeCalculator.sum("loanDiscount", fiscalPayroll, categoryFiscalPayroll));
        addCalculator(BasicMergeCalculator.sum("advanceDiscount", fiscalPayroll, categoryFiscalPayroll));
        addCalculator(BasicMergeCalculator.sum("winDiscount", fiscalPayroll, categoryFiscalPayroll));
        addCalculator(BasicMergeCalculator.sum("otherSalaryMovementDiscount", fiscalPayroll, categoryFiscalPayroll));
        addCalculator(BasicMergeCalculator.sum("otherDiscount", fiscalPayroll, categoryFiscalPayroll));
        addCalculator(BasicMergeCalculator.sum("retentionAFP", fiscalPayroll, categoryFiscalPayroll));
        addCalculator(BasicMergeCalculator.sum("totalGrained", fiscalPayroll, categoryFiscalPayroll));
        addCalculator(BasicMergeCalculator.sum("totalDiscount", fiscalPayroll, categoryFiscalPayroll));
    }


    @Override
    protected FiscalPayroll getInstance() {
        return fiscalPayroll;
    }
}
