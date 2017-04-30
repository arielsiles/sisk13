package com.encens.khipus.util.employees.payroll.tributary;

import com.encens.khipus.model.employees.CategoryTributaryPayroll;
import com.encens.khipus.model.employees.InvoicesForm;
import com.encens.khipus.util.employees.payroll.structure.Calculator;

import java.math.BigDecimal;

/**
 * @author
 * @version 2.26
 */
public class FiscalCreditCalculator extends Calculator<CategoryTributaryPayroll> {
    private InvoicesForm invoicesForm;

    public FiscalCreditCalculator(InvoicesForm invoicesForm) {
        this.invoicesForm = invoicesForm;
    }

    @Override
    public void execute(CategoryTributaryPayroll instance) {
        BigDecimal value = BigDecimal.ZERO;
        if (null != invoicesForm) {
            value = new BigDecimal(invoicesForm.getFiscalCredit());
        }
        instance.setFiscalCredit(value);
    }
}
