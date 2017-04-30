package com.encens.khipus.service.employees;

import com.encens.khipus.framework.service.GenericServiceBean;
import com.encens.khipus.model.employees.*;
import com.encens.khipus.model.finances.BankAccount;
import com.encens.khipus.util.BigDecimalUtil;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @author
 * @version 3.2
 */
@Stateless
@Name("christmasPayrollService")
@AutoCreate
public class ChristmasPayrollServiceBean extends GenericServiceBean implements ChristmasPayrollService {

    public ChristmasPayroll buildChristmasPayroll(GeneratedPayroll generatedPayroll, GestionPayroll gestionPayroll,
                                                  Employee employee, Date initContractDate, BigDecimal salary,
                                                  int workedDays, ManagersPayroll novemberManagersPayroll,
                                                  BigDecimal septemberTotalIncome, BigDecimal octoberTotalIncome,
                                                  BigDecimal novemberTotalIncome, BigDecimal averageSalary,
                                                  BigDecimal contributableSalary, BankAccount bankAccount) {
        ChristmasPayroll christmasPayroll = new ChristmasPayroll();
        christmasPayroll.setEmployee(employee);
        christmasPayroll.setContractInitDate(initContractDate);
        christmasPayroll.setWorkedDays(BigDecimalUtil.toBigDecimal(workedDays));
        christmasPayroll.setSalary(salary);
        christmasPayroll.setSeptemberTotalIncome(septemberTotalIncome);
        christmasPayroll.setOctoberTotalIncome(octoberTotalIncome);
        christmasPayroll.setNovemberTotalIncome(novemberTotalIncome);
        christmasPayroll.setAverageSalary(averageSalary);
        christmasPayroll.setContributableSalary(contributableSalary);
        christmasPayroll.setLiquid(contributableSalary);
        christmasPayroll.setGeneratedPayroll(generatedPayroll);
        christmasPayroll.setBusinessUnit(novemberManagersPayroll.getBusinessUnit());
        christmasPayroll.setCostCenter(novemberManagersPayroll.getCostCenter());
        christmasPayroll.setArea(novemberManagersPayroll.getArea());
        christmasPayroll.setCharge(novemberManagersPayroll.getCharge());
        christmasPayroll.setJobCategory(gestionPayroll.getJobCategory());
        if (null != bankAccount) {
            christmasPayroll.setBankAccount(bankAccount.getAccountNumber());
            christmasPayroll.setBankAccountCurrency(bankAccount.getCurrency().getSymbol());
            christmasPayroll.setClientCode(bankAccount.getClientCod());
        }
        return christmasPayroll;
    }
}
