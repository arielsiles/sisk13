package com.encens.khipus.util.employees.payroll.fiscal;

import com.encens.khipus.model.employees.CategoryFiscalPayroll;
import com.encens.khipus.model.employees.FiscalInternalGeneralPayroll;
import com.encens.khipus.util.employees.payroll.structure.Calculator;

/**
 * @author
 * @version 2.26
 */
public class WorkedDaysCalculator extends Calculator<CategoryFiscalPayroll> {
    private int workedDays;

    private FiscalInternalGeneralPayroll fiscalInternalGeneralPayroll;

    public WorkedDaysCalculator(FiscalInternalGeneralPayroll fiscalInternalGeneralPayroll) {
        this.fiscalInternalGeneralPayroll = fiscalInternalGeneralPayroll;
    }

    @Override
    public void execute(CategoryFiscalPayroll instance) {
        instance.setWorkedDays(fiscalInternalGeneralPayroll.getWorkedDays());
    }
}

