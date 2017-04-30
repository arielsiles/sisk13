package com.encens.khipus.util.employees.payroll.fiscal;

import com.encens.khipus.model.employees.*;
import com.encens.khipus.model.finances.JobContract;
import com.encens.khipus.util.employees.payroll.structure.PayrollColumn;
import com.encens.khipus.util.employees.payroll.structure.PayrollGenerator;

import java.math.BigDecimal;

/**
 * @author
 * @version 2.26
 */
public class FiscalPayrollGenerator extends PayrollGenerator<CategoryFiscalPayroll> {
    private Employee employee;
    private JobContract jobContract;
    private CategoryTributaryPayroll categoryTributaryPayroll;
    private FiscalInternalGeneralPayroll fiscalInternalGeneralPayroll;
    private GestionPayroll gestionPayroll;
    private BigDecimal defaultHourDayPayment;
    private int workedDays;

    public FiscalPayrollGenerator(Employee employee,
                                  JobContract jobContract,
                                  CategoryTributaryPayroll categoryTributaryPayroll,
                                  FiscalInternalGeneralPayroll fiscalInternalGeneralPayroll,
                                  GestionPayroll gestionPayroll,
                                  BigDecimal defaultHourDayPayment,
                                  int workedDays) {
        this.employee = employee;
        this.jobContract = jobContract;
        this.categoryTributaryPayroll = categoryTributaryPayroll;
        this.fiscalInternalGeneralPayroll = fiscalInternalGeneralPayroll;
        this.gestionPayroll = gestionPayroll;
        this.defaultHourDayPayment = defaultHourDayPayment;
        this.workedDays = workedDays;

    }

    @Override
    protected void initializeColumns() {
        addColumn(PayrollColumn.getInstance(new FiscalPayrollGeneralCalculator(employee, jobContract, gestionPayroll,
                defaultHourDayPayment, fiscalInternalGeneralPayroll, categoryTributaryPayroll)));
        addColumn(PayrollColumn.getInstance(new OccupationCalculator(jobContract)));
        addColumn(PayrollColumn.getInstance(new WorkedDaysCalculator(fiscalInternalGeneralPayroll)));
        addColumn(PayrollColumn.getInstance(new NewnessCalculator(jobContract, gestionPayroll)));
        addColumn(PayrollColumn.getInstance(new PaidDaysCalculator(workedDays)));
        addColumn(PayrollColumn.getInstance(new BasicAmountCalculator(categoryTributaryPayroll)));
        addColumn(PayrollColumn.getInstance(new TotalGrainedCalculator(categoryTributaryPayroll)));
        addColumn(PayrollColumn.getInstance(new RetentionAFPCalculator(categoryTributaryPayroll)));
        addColumn(PayrollColumn.getInstance(new RetentionClearanceCalculator(categoryTributaryPayroll)));
        addColumn(PayrollColumn.getInstance(new OtherDiscountsCalculator()));
        addColumn(PayrollColumn.getInstance(new TotalDiscountCalculator()));
        addColumn(PayrollColumn.getInstance(new LiquidPaymentCalculator()));
    }

    @Override
    protected CategoryFiscalPayroll getInstance() {
        return new CategoryFiscalPayroll();
    }
}
