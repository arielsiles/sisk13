package com.encens.khipus.util.employees.payroll.fiscal;

import com.encens.khipus.model.employees.*;
import com.encens.khipus.model.finances.JobContract;
import com.encens.khipus.util.employees.payroll.structure.Calculator;

import java.math.BigDecimal;

/**
 * @author
 * @version 2.26
 */
public class FiscalPayrollGeneralCalculator extends Calculator<CategoryFiscalPayroll> {
    private BigDecimal defaultHourDayPayment;

    private Employee employee;

    private JobContract jobContract;
    private GestionPayroll gestionPayroll;
    private FiscalInternalGeneralPayroll fiscalInternalGeneralPayroll;
    private CategoryTributaryPayroll categoryTributaryPayroll;

    public FiscalPayrollGeneralCalculator(Employee employee, JobContract jobContract,
                                          GestionPayroll gestionPayroll, BigDecimal defaultHourDayPayment,
                                          FiscalInternalGeneralPayroll fiscalInternalGeneralPayroll, CategoryTributaryPayroll categoryTributaryPayroll) {
        this.employee = employee;
        this.jobContract = jobContract;
        this.gestionPayroll = gestionPayroll;
        this.defaultHourDayPayment = defaultHourDayPayment;
        this.fiscalInternalGeneralPayroll = fiscalInternalGeneralPayroll;
        this.categoryTributaryPayroll = categoryTributaryPayroll;
    }

    @Override
    public void execute(CategoryFiscalPayroll instance) {
        instance.setPersonalIdentifier(employee.getIdNumber());
        instance.setName(formatName(employee));
        instance.setNationality("");
        if (null != employee.getCountry()) {
            instance.setNationality(employee.getCountry().getName());
        }

        if (null != employee.getBirthDay()) {
            instance.setBirthday(employee.getBirthDay());
        }

        instance.setGender(employee.getGender());
        instance.setJobContract(jobContract);
        instance.setBusinessUnit(gestionPayroll.getBusinessUnit());
        instance.setEmployee(employee);
        instance.setEntranceDate(jobContract.getContract().getInitDate());

        instance.setHourDayPayment(defaultHourDayPayment);

        instance.setOtherDiscount(BigDecimal.ZERO);
        instance.setAbsenceMinutesDiscount(fiscalInternalGeneralPayroll.getAbsenceMinutesDiscount());
        BigDecimal tardinessMinutesDiscount = fiscalInternalGeneralPayroll.getTardinessMinutesDiscount();
        if (fiscalInternalGeneralPayroll.getPayrollGenerationType().equals(PayrollGenerationType.GENERATION_BY_PERIODSALARY)) {
            instance.setAbsenceMinutesDiscount(BigDecimal.ZERO);
        }
        instance.setTardinessMinutesDiscount(tardinessMinutesDiscount);
        instance.setLoanDiscount(fiscalInternalGeneralPayroll.getLoanDiscount());
        instance.setAdvanceDiscount(fiscalInternalGeneralPayroll.getAdvanceDiscount());
        instance.setWinDiscount(fiscalInternalGeneralPayroll.getWinDiscount());
        instance.setOtherSalaryMovementDiscount(fiscalInternalGeneralPayroll.getOtherDiscounts());
        instance.setSeniorityYears(categoryTributaryPayroll.getSeniorityYears());
        instance.setSeniorityBonus(categoryTributaryPayroll.getSeniorityBonus());
        instance.setExtraHourCost(categoryTributaryPayroll.getExtraHourCost());
        instance.setExtraHour(categoryTributaryPayroll.getExtraHour());
        instance.setSundayBonus(categoryTributaryPayroll.getSundayBonus());
        instance.setProductionBonus(categoryTributaryPayroll.getProductionBonus());
        instance.setOtherBonus(categoryTributaryPayroll.getOtherBonus());
        instance.setOtherIncomes(categoryTributaryPayroll.getOtherIncomes());
    }

    private String formatName(Employee employee) {
        String name = employee.getLastName() + " " + employee.getMaidenName();

        if (!"".equals(name.trim())) {
            name += ", ";
        }

        name += employee.getFirstName();

        return name;
    }
}
