package com.encens.khipus.util.employees.payroll.tributary;

import com.encens.khipus.model.admin.BusinessUnit;
import com.encens.khipus.model.employees.CategoryTributaryPayroll;
import com.encens.khipus.model.employees.Employee;
import com.encens.khipus.model.finances.ExchangeRate;
import com.encens.khipus.model.finances.JobContract;
import com.encens.khipus.model.finances.Salary;
import com.encens.khipus.util.BigDecimalUtil;
import com.encens.khipus.util.employees.payroll.structure.Calculator;

import java.math.BigDecimal;

/**
 * @author
 * @version 3.4
 */
public class GeneralTributaryCalculator extends Calculator<CategoryTributaryPayroll> {

    private JobContract jobContract;
    private BusinessUnit businessUnit;
    private Employee employee;
    private static final int SCALE = 6;
    private ExchangeRate exchangeRate;

    public GeneralTributaryCalculator(JobContract jobContract, BusinessUnit businessUnit,
                                      Employee employee, ExchangeRate exchangeRate) {
        this.jobContract = jobContract;
        this.businessUnit = businessUnit;
        this.employee = employee;
        this.exchangeRate = exchangeRate;
    }

    @Override
    public void execute(CategoryTributaryPayroll instance) {
        BigDecimal value = BigDecimal.ZERO;
        if (!jobContract.getJob().getJobCategory().getSector().getName().equalsIgnoreCase("ACADEMICO")) {
            Salary salary = jobContract.getJob().getSalary();
            if (null != salary.getAmount()) {
                value = salary.getAmount();
            }
            if (salary.getCurrency().getSymbol().equalsIgnoreCase("$US")) {
                value = BigDecimalUtil.multiply(value, exchangeRate.getSale(), SCALE);
            }
        } else {
            value = null == jobContract.getContract().getOccupationalBasicAmount() ? BigDecimal.ZERO : jobContract.getContract().getOccupationalBasicAmount();
        }
        instance.setEntranceDate(jobContract.getContract().getInitDate());
        instance.setBasicAmount(value);
        instance.setJobContract(jobContract);
        instance.setBusinessUnit(businessUnit);
        instance.setEmployee(employee);
    }


}
