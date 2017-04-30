package com.encens.khipus.util.employees.payroll.tributary;

import com.encens.khipus.model.admin.BusinessUnit;
import com.encens.khipus.model.employees.*;
import com.encens.khipus.model.finances.JobContract;
import com.encens.khipus.util.BigDecimalUtil;
import com.encens.khipus.util.employees.payroll.structure.PayrollColumn;
import com.encens.khipus.util.employees.payroll.structure.PayrollGenerator;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * @author
 * @version 2.26
 */
public class TributaryPayrollGenerator extends PayrollGenerator<CategoryTributaryPayroll> {
    private Employee employee;
    private JobContract jobContract;
    private ExtraHoursWorked extraHoursWorked;
    private List<GrantedBonus> grantedBonus;
    private SeniorityBonus seniorityBonus;
    private BigDecimal otherIncomes;
    private Integer workedDays;
    private Date endDate;
    private BusinessUnit businessUnit;
    private AFPRate afpRate;
    private BigDecimal patronalRetentionAFPRate;
    private AFPRate patronalProffesionalRiskRetentionAFP;
    private AFPRate patronalProHomeRetentionAFP;
    private AFPRate patronalSolidaryRetentionAFP;
    private CNSRate cnsRate;
    private SMNRate smnRate;
    private IVARate ivaRate;
    private BigDecimal initialUfvExchangeRate;
    private BigDecimal finalUfvExchangeRate;
    private InvoicesForm invoicesForm;
    private BigDecimal lastMonthBalance;
    private GestionPayroll gestionPayroll;
    private DiscountRule nationalSolidaryAFPDiscountRule;


    public TributaryPayrollGenerator(Employee employee,
                                     JobContract jobContract,
                                     ExtraHoursWorked extraHoursWorked,
                                     List<GrantedBonus> grantedBonus,
                                     SeniorityBonus seniorityBonus,
                                     BigDecimal otherIncomes,
                                     Integer workedDays,
                                     Date endDate, PayrollGenerationCycle payrollGenerationCycle,
                                     InvoicesForm invoicesForm,
                                     BigDecimal lastMonthBalance,
                                     GestionPayroll gestionPayroll) {
        this.employee = employee;
        this.jobContract = jobContract;
        this.extraHoursWorked = extraHoursWorked;
        this.grantedBonus = grantedBonus;
        this.seniorityBonus = seniorityBonus;
        this.otherIncomes = otherIncomes;
        this.workedDays = workedDays;
        this.endDate = endDate;
        this.patronalProffesionalRiskRetentionAFP = payrollGenerationCycle.getProfessionalRiskAfpRate();
        this.patronalProHomeRetentionAFP = payrollGenerationCycle.getProHousingAfpRate();
        this.patronalSolidaryRetentionAFP = payrollGenerationCycle.getSolidaryAfpRate();
        this.patronalRetentionAFPRate = BigDecimalUtil.sum(this.patronalProffesionalRiskRetentionAFP.getRate(),
                this.patronalProHomeRetentionAFP.getRate(), this.patronalSolidaryRetentionAFP.getRate());
        this.cnsRate = payrollGenerationCycle.getCnsRate();
        this.businessUnit = payrollGenerationCycle.getBusinessUnit();
        this.afpRate = payrollGenerationCycle.getAfpRate();
        this.nationalSolidaryAFPDiscountRule = payrollGenerationCycle.getNationalSolidaryAfpDiscountRule();
        this.smnRate = payrollGenerationCycle.getSmnRate();
        this.ivaRate = payrollGenerationCycle.getIvaRate();
        this.initialUfvExchangeRate = payrollGenerationCycle.getInitialUfvExchangeRate();
        this.finalUfvExchangeRate = payrollGenerationCycle.getFinalUfvExchangeRate();
        this.invoicesForm = invoicesForm;
        this.lastMonthBalance = lastMonthBalance;
        this.gestionPayroll = gestionPayroll;
    }

    @Override
    protected void initializeColumns() {
        addColumn(PayrollColumn.getInstance(new CodeCalculator(employee)));
        addColumn(PayrollColumn.getInstance(new NameCalculator(employee)));
        addColumn(PayrollColumn.getInstance(new GeneralTributaryCalculator(jobContract, businessUnit, employee, gestionPayroll.getExchangeRate())));
        addColumn(PayrollColumn.getInstance(new SeniorityYearsCalculator(gestionPayroll)));
        addColumn(PayrollColumn.getInstance(new SeniorityBonusCalculator(seniorityBonus)));
        addColumn(PayrollColumn.getInstance(new ExtraHourCalculator(extraHoursWorked)));
        addColumn(PayrollColumn.getInstance(new GeneralBonusCalculator(grantedBonus)));
        addColumn(PayrollColumn.getInstance(new OtherIncomesCalculator(otherIncomes)));
        addColumn(PayrollColumn.getInstance(new TotalGrainedCalculator(workedDays)));
        addColumn(PayrollColumn.getInstance(new RetentionAFPCalculator(afpRate, nationalSolidaryAFPDiscountRule, endDate)));
        addColumn(PayrollColumn.getInstance(new PatronalAFPRetentionCalculator(patronalRetentionAFPRate,
                patronalProffesionalRiskRetentionAFP,
                patronalProHomeRetentionAFP,
                patronalSolidaryRetentionAFP)));
        addColumn(PayrollColumn.getInstance(new PatronalOtherRetentionCalculator(cnsRate)));
        addColumn(PayrollColumn.getInstance(new NetSalaryCalculator()));
        addColumn(PayrollColumn.getInstance(new SalaryNotTaxableTwoSMNCalculator(smnRate)));
        addColumn(PayrollColumn.getInstance(new UnlikeTaxableCalculator(smnRate)));
        addColumn(PayrollColumn.getInstance(new TaxCalculator(ivaRate)));
        addColumn(PayrollColumn.getInstance(new FiscalCreditCalculator(invoicesForm)));
        addColumn(PayrollColumn.getInstance(new TaxForTwoSMNCalculator(ivaRate)));
        addColumn(PayrollColumn.getInstance(new PhysicalBalanceCalculator()));
        addColumn(PayrollColumn.getInstance(new DependentBalanceCalculator()));
        addColumn(PayrollColumn.getInstance(new LastMonthBalanceCalculator(lastMonthBalance)));
        addColumn(PayrollColumn.getInstance(new MaintenanceOfValueCalculator(initialUfvExchangeRate, finalUfvExchangeRate)));
        addColumn(PayrollColumn.getInstance(new LastBalanceUpdatedCalculator()));
        addColumn(PayrollColumn.getInstance(new DependentTotalBalanceCalculator()));
        addColumn(PayrollColumn.getInstance(new UsedBalanceCalculator()));
        addColumn(PayrollColumn.getInstance(new RetentionClearanceCalculator()));
        addColumn(PayrollColumn.getInstance(new DependentBalanceToNextMonthCalculator()));
    }

    @Override
    protected CategoryTributaryPayroll getInstance() {
        return new CategoryTributaryPayroll();
    }
}
