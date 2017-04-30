package com.encens.khipus.util.employees.payroll.fiscal;

import com.encens.khipus.model.employees.CategoryFiscalPayroll;
import com.encens.khipus.model.finances.JobContract;
import com.encens.khipus.util.employees.payroll.structure.Calculator;

/**
 * @author
 * @version 2.26
 */
public class OccupationCalculator extends Calculator<CategoryFiscalPayroll> {
    private JobContract jobContract;

    public OccupationCalculator(JobContract jobContract) {
        this.jobContract = jobContract;
    }

    @Override
    public void execute(CategoryFiscalPayroll instance) {
        instance.setOccupation(formatOccupation(jobContract));
    }

    private String formatOccupation(JobContract jobContract) {
        String charge = jobContract.getJob().getCharge().getName();
        String level = jobContract.getJob().getOrganizationalUnit().getOrganizationalLevel().getName();
        String name = jobContract.getJob().getOrganizationalUnit().getName();
        return charge + " (" + level + " - " + name + ")";
    }
}
