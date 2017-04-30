package com.encens.khipus.util.employees.payroll.fiscal;

import com.encens.khipus.model.employees.CategoryFiscalPayroll;
import com.encens.khipus.model.employees.GestionPayroll;
import com.encens.khipus.model.employees.NewnessType;
import com.encens.khipus.model.employees.PayrollGenerationCycle;
import com.encens.khipus.model.finances.JobContract;
import com.encens.khipus.util.employees.payroll.structure.Calculator;

import java.util.Date;

/**
 * @author
 * @version 3.4
 */
public class NewnessCalculator extends Calculator<CategoryFiscalPayroll> {
    private JobContract jobContract;
    private GestionPayroll gestionPayroll;

    public NewnessCalculator(JobContract jobContract, GestionPayroll gestionPayroll) {
        this.jobContract = jobContract;
        this.gestionPayroll = gestionPayroll;
    }

    @Override
    public void execute(CategoryFiscalPayroll instance) {
        PayrollGenerationCycle payrollGenerationCycle = gestionPayroll.getPayrollGenerationCycle();
        if (instance.getEntranceDate().compareTo(payrollGenerationCycle.getStartDate()) > 0) {
            instance.setNewnessType(NewnessType.I);
        }
        Date contractEndDate = jobContract.getContract().getEndDate();
        if (null != contractEndDate && contractEndDate.compareTo(payrollGenerationCycle.getEndDate()) < 0) {
            instance.setNewnessType(NewnessType.R);
        }
    }
}
