package com.encens.khipus.model.employees;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyListener;
import com.encens.khipus.model.UpperCaseStringListener;
import com.encens.khipus.model.admin.BusinessUnit;
import com.encens.khipus.model.admin.Company;
import com.encens.khipus.model.finances.JobContract;
import com.encens.khipus.util.Constants;
import org.hibernate.annotations.Filter;
import org.hibernate.validator.NotNull;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @author
 * @version 2.26
 */

@TableGenerator(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "TributaryPayroll.tableGenerator",
        table = com.encens.khipus.util.Constants.SEQUENCE_TABLE_NAME,
        pkColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_PK_COLUMN_NAME,
        valueColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_VALUE_COLUMN_NAME,
        pkColumnValue = "planillatributaria",
        allocationSize = com.encens.khipus.util.Constants.SEQUENCE_ALLOCATION_SIZE)
@NamedQueries({
        @NamedQuery(name = "TributaryPayroll.findByPayrollGenerationCycleAndEmployeeList",
                query = "select tributaryPayroll from TributaryPayroll tributaryPayroll " +
                        "where tributaryPayroll.payrollGenerationCycle =:payrollGenerationCycle and tributaryPayroll.employee.id in(:employeeIdList)"),
        @NamedQuery(name = "TributaryPayroll.findByPayrollGenerationCycle",
                query = "select element from TributaryPayroll element where element.payrollGenerationCycle =:payrollGenerationCycle order by element.number"),
        @NamedQuery(name = "TributaryPayroll.countByPayrollGenerationCycle",
                query = "select count(*) from TributaryPayroll element where element.payrollGenerationCycle =:payrollGenerationCycle order by element.number"),
        @NamedQuery(name = "TributaryPayroll.findJobContractIdsByPayrollGenerationCycle",
                query = "select element.jobContractId from TributaryPayroll element where element.payrollGenerationCycle =:payrollGenerationCycle order by element.number"),
        @NamedQuery(name = "TributaryPayroll.loadByPayrollGenerationCycle",
                query = "select element from TributaryPayroll element " +
                        " left join fetch element.payrollGenerationCycle payrollGenerationCycleTP" +
                        " left join fetch element.jobContract jobContractTP" +
                        " left join fetch element.employee employeeTP" +
                        " left join fetch element.businessUnit businessUnitTP" +
                        " left join fetch element.fiscalPayroll fiscalPayroll" +
                        " left join fetch fiscalPayroll.payrollGenerationCycle payrollGenerationCycleFP" +
                        " left join fetch fiscalPayroll.jobContract jobContractFP" +
                        " left join fetch fiscalPayroll.employee employeeFP" +
                        " left join fetch fiscalPayroll.businessUnit businessUnitFP" +
                        " where payrollGenerationCycleTP =:payrollGenerationCycle order by element.number"),
        @NamedQuery(name = "TributaryPayroll.maxNumberByPayrollGenerationCycle",
                query = "select max(element.number) from TributaryPayroll element " +
                        " where element.payrollGenerationCycle=:payrollGenerationCycle"),
        @NamedQuery(name = "TributaryPayroll.sumLaboralRetentionAFP",
                query = "select new com.encens.khipus.util.employees.TributaryPayrollCalculateResult(businessUnit,costCenter,jobCategory,sum(element.retentionAFP))" +
                        " from TributaryPayroll element" +
                        " left join element.businessUnit businessUnit" +
                        " left join element.jobContract jobContract" +
                        " left join element.payrollGenerationCycle payrollGenerationCycle" +
                        " left join jobContract.contract contract" +
                        " left join contract.pensionFundOrganization pensionFundOrganization" +
                        " left join jobContract.job job" +
                        " left join job.jobCategory jobCategory" +
                        " left join job.organizationalUnit organizationalUnit" +
                        " left join organizationalUnit.costCenter costCenter" +
                        " where payrollGenerationCycle=:payrollGenerationCycle and pensionFundOrganization=:pensionFundOrganization" +
                        " group by businessUnit.id,costCenter.id,jobCategory.id" +
                        " order by businessUnit.id,costCenter.id,jobCategory.id"),
        @NamedQuery(name = "TributaryPayroll.sumPatronalRetentionAFP",
                query = "select new com.encens.khipus.util.employees.TributaryPayrollCalculateResult(businessUnit,costCenter,jobCategory,sum(element.patronalProffesionalRiskRetentionAFP),sum(element.patronalProHomeRetentionAFP),sum(element.patronalSolidaryRetentionAFP))" +
                        " from TributaryPayroll element" +
                        " left join element.businessUnit businessUnit" +
                        " left join element.jobContract jobContract" +
                        " left join element.payrollGenerationCycle payrollGenerationCycle" +
                        " left join jobContract.contract contract" +
                        " left join contract.pensionFundOrganization pensionFundOrganization" +
                        " left join jobContract.job job" +
                        " left join job.jobCategory jobCategory" +
                        " left join job.organizationalUnit organizationalUnit" +
                        " left join organizationalUnit.costCenter costCenter" +
                        " where payrollGenerationCycle=:payrollGenerationCycle and pensionFundOrganization=:pensionFundOrganization" +
                        " group by businessUnit.id,costCenter.id,jobCategory.id" +
                        " order by businessUnit.id,costCenter.id,jobCategory.id"),
        @NamedQuery(name = "TributaryPayroll.sumCNS",
                query = "select new com.encens.khipus.util.employees.TributaryPayrollCalculateResult(businessUnit,costCenter,jobCategory,sum(element.cns))" +
                        " from TributaryPayroll element " +
                        " left join element.businessUnit businessUnit" +
                        " left join element.jobContract jobContract" +
                        " left join element.payrollGenerationCycle payrollGenerationCycle" +
                        " left join jobContract.contract contract" +
                        " left join contract.socialSecurityOrganization socialSecurityOrganization" +
                        " left join jobContract.job job" +
                        " left join job.jobCategory jobCategory" +
                        " left join job.organizationalUnit organizationalUnit" +
                        " left join organizationalUnit.costCenter costCenter" +
                        " where payrollGenerationCycle=:payrollGenerationCycle and socialSecurityOrganization=:socialSecurityOrganization" +
                        " group by businessUnit.id,costCenter.id,jobCategory.id" +
                        " order by businessUnit.id,costCenter.id,jobCategory.id"),
        @NamedQuery(name = "TributaryPayroll.sumGlobalRetentionAFP",
                query = "select new com.encens.khipus.util.employees.TributaryPayrollCalculateResult(pensionFundOrganization.id,sum(element.retentionAFP),sum(element.patronalProffesionalRiskRetentionAFP),sum(element.patronalProHomeRetentionAFP),sum(element.patronalSolidaryRetentionAFP))" +
                        " from TributaryPayroll element" +
                        " left join element.payrollGenerationCycle payrollGenerationCycle" +
                        " left join element.jobContract jobContract" +
                        " left join jobContract.contract contract" +
                        " left join contract.pensionFundOrganization pensionFundOrganization" +
                        " where pensionFundOrganization is not null and payrollGenerationCycle=:payrollGenerationCycle" +
                        " group by pensionFundOrganization.id"),
        @NamedQuery(name = "TributaryPayroll.sumGlobalRetentionCNS",
                query = "select new com.encens.khipus.util.employees.TributaryPayrollCalculateResult(socialSecurityOrganization.id,sum(element.cns))" +
                        " from TributaryPayroll element " +
                        " left join element.payrollGenerationCycle payrollGenerationCycle" +
                        " left join element.jobContract jobContract" +
                        " left join jobContract.contract contract" +
                        " left join contract.socialSecurityOrganization socialSecurityOrganization" +
                        " where socialSecurityOrganization is not null and payrollGenerationCycle=:payrollGenerationCycle" +
                        " group by socialSecurityOrganization.id"),
        @NamedQuery(name = "TributaryPayroll.countUnregisteredPensionFundOrganization",
                query = "select count(element.id)" +
                        " from TributaryPayroll element" +
                        " left join element.payrollGenerationCycle payrollGenerationCycle" +
                        " left join element.jobContract jobContract" +
                        " left join jobContract.contract contract" +
                        " left join contract.pensionFundOrganization pensionFundOrganization" +
                        " where pensionFundOrganization is null and payrollGenerationCycle=:payrollGenerationCycle"),
        @NamedQuery(name = "TributaryPayroll.countUnregisteredSocialSecurityOrganization",
                query = "select count(element.id)" +
                        " from TributaryPayroll element " +
                        " left join element.payrollGenerationCycle payrollGenerationCycle" +
                        " left join element.jobContract jobContract" +
                        " left join jobContract.contract contract" +
                        " left join contract.socialSecurityOrganization socialSecurityOrganization" +
                        " where socialSecurityOrganization is null and payrollGenerationCycle=:payrollGenerationCycle")
})
@Entity
@EntityListeners({CompanyListener.class, UpperCaseStringListener.class})
@Filter(name = com.encens.khipus.util.Constants.COMPANY_FILTER_NAME)
@Table(name = "PLANILLATRIBUTARIA", schema = Constants.KHIPUS_SCHEMA)
public class TributaryPayroll implements BaseModel {
    @Id
    @Column(name = "IDPLANILLATRIBUTARIA", nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "TributaryPayroll.tableGenerator")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "IDCOMPANIA", nullable = false, updatable = false, insertable = true)
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "IDCICLOGENERACIONPLANILLA", nullable = false, updatable = false)
    private PayrollGenerationCycle payrollGenerationCycle;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "IDCONTRATOPUESTO", insertable = true, updatable = false, nullable = false)
    private JobContract jobContract;

    @Column(name = "IDCONTRATOPUESTO", insertable = false, updatable = false, nullable = false)
    private Long jobContractId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "IDEMPLEADO", insertable = true, updatable = false, nullable = false)
    private Employee employee;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "IDUNIDADNEGOCIO", updatable = true, insertable = true)
    private BusinessUnit businessUnit;

    @Column(name = "CODIGO", nullable = false)
    private String code;

    @Column(name = "HABERBASICO", precision = 13, scale = 2, nullable = false)
    @NotNull
    private BigDecimal basicAmount;

    @Column(name = "ANIOSANTIGUEDAD")
    private Integer seniorityYears;

    @Column(name = "BONOANTIGUEDAD", precision = 13, scale = 2, nullable = false)
    @NotNull
    private BigDecimal seniorityBonus;

    @Column(name = "COSTOHORASEXTRA", precision = 13, scale = 2, nullable = false)
    private BigDecimal extraHourCost;

    @Column(name = "HORASEXTRA", precision = 13, scale = 2, nullable = false)
    private BigDecimal extraHour;

    @Column(name = "BONODOMINICAL", precision = 13, scale = 2, nullable = false)
    private BigDecimal sundayBonus;

    @Column(name = "BONOPRODUCCION", precision = 13, scale = 2, nullable = false)
    private BigDecimal productionBonus;

    @Column(name = "OTROSBONOS", precision = 13, scale = 2, nullable = false)
    private BigDecimal otherBonus;

    @Column(name = "OTROSINGRESOS", precision = 13, scale = 2, nullable = false)
    private BigDecimal otherIncomes;

    @Column(name = "FECHAINGRESO")
    @Temporal(TemporalType.DATE)
    private Date entranceDate;

    @Column(name = "TOTALOTROSINGRESOS", precision = 13, scale = 2, nullable = false)
    @NotNull
    private BigDecimal totalOtherIncomes;

    @Column(name = "CREDITOFISCAL", precision = 13, scale = 2, nullable = false)
    private BigDecimal fiscalCredit;

    @Column(name = "DIFSUJETAIMPUESTO", precision = 13, scale = 2, nullable = false)
    private BigDecimal unlikeTaxable;

    @Column(name = "IMPUESTO", precision = 13, scale = 2, nullable = false)
    private BigDecimal tax;

    @Column(name = "IMPSOBREDOSSMN", precision = 13, scale = 2, nullable = false)
    private BigDecimal taxForTwoSMN;

    @Column(name = "LIQUIDACIONRETENCION", precision = 13, scale = 2, nullable = false)
    private BigDecimal retentionClearance;

    @Column(name = "MANTENIMIENTOVALOR", precision = 13, scale = 2, nullable = false)
    private BigDecimal maintenanceOfValue;

    @Column(name = "NUMERO", nullable = false)
    private Long number;

    @Column(name = "NOMBRE", nullable = false)
    private String name;

    // PREVISION LABORAL
    @Column(name = "RETENCIONAFP", precision = 13, scale = 2, nullable = false)
    private BigDecimal retentionAFP;

    @Column(name = "AFPSOLIDARIO", precision = 13, scale = 2, nullable = false)
    @NotNull
    private BigDecimal solidaryAFP;

    // SUMATORIA => PREVISION PATRONAL	PREVISION VIVIENDA	PREVISION SOLIDARIO
    @Column(name = "RETENCIONPATRONALAFP", precision = 13, scale = 2, nullable = false)
    @NotNull
    private BigDecimal patronalRetentionAFP;

    // PREVISION PATRONAL
    @Column(name = "RETPATRRIESGOPROFAFP", precision = 13, scale = 2, nullable = false)
    @NotNull
    private BigDecimal patronalProffesionalRiskRetentionAFP;

    //  PREVISION VIVIENDA
    @Column(name = "RETPATRPROVIVIENDAAFP", precision = 13, scale = 2, nullable = false)
    @NotNull
    private BigDecimal patronalProHomeRetentionAFP;

    // PREVISION SOLIDARIO
    @Column(name = "RETPATRSOLIDARIOAFP", precision = 13, scale = 2, nullable = false)
    @NotNull
    private BigDecimal patronalSolidaryRetentionAFP;

    // CNS 10%
    @Column(name = "CNS", precision = 13, scale = 2, nullable = false)
    @NotNull
    private BigDecimal cns;

    @Column(name = "SUELDONETO", precision = 13, scale = 2, nullable = false)
    private BigDecimal netSalary;

    @Column(name = "SUELDONOIMPDOSSMN", precision = 13, scale = 2, nullable = false)
    private BigDecimal salaryNotTaxableTwoSMN;

    @Column(name = "SALDOFISCO", precision = 13, scale = 2, nullable = false)
    private BigDecimal physicalBalance;

    @Column(name = "SALDODEPENDIENTE", precision = 13, scale = 2, nullable = false)
    private BigDecimal dependentBalance;

    @Column(name = "SALDOMESANTERIOR", precision = 13, scale = 2, nullable = false)
    private BigDecimal lastMonthBalance;

    @Column(name = "SALDOANTERIORACTUALIZADO", precision = 13, scale = 2, nullable = false)
    private BigDecimal lastBalanceUpdated;

    @Column(name = "SALDOTOTALDEPENDIENTE", precision = 13, scale = 2, nullable = false)
    private BigDecimal dependentTotalBalance;

    @Column(name = "SALDOUTILIZADO", precision = 13, scale = 2, nullable = false)
    private BigDecimal usedBalance;

    @Column(name = "SALDODEPENDIENTEMESSGUTE", precision = 13, scale = 2, nullable = false)
    private BigDecimal dependentBalanceToNextMonth;

    @Column(name = "TOTALGANADO", precision = 13, scale = 2, nullable = false)
    private BigDecimal totalGrained;

    @OneToOne(mappedBy = "tributaryPayroll", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private FiscalPayroll fiscalPayroll;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public PayrollGenerationCycle getPayrollGenerationCycle() {
        return payrollGenerationCycle;
    }

    public void setPayrollGenerationCycle(PayrollGenerationCycle payrollGenerationCycle) {
        this.payrollGenerationCycle = payrollGenerationCycle;
    }

    public JobContract getJobContract() {
        return jobContract;
    }

    public void setJobContract(JobContract jobContract) {
        this.jobContract = jobContract;
    }

    public Long getJobContractId() {
        return jobContractId;
    }

    public void setJobContractId(Long jobContractId) {
        this.jobContractId = jobContractId;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public BusinessUnit getBusinessUnit() {
        return businessUnit;
    }

    public void setBusinessUnit(BusinessUnit businessUnit) {
        this.businessUnit = businessUnit;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public BigDecimal getBasicAmount() {
        return basicAmount;
    }

    public void setBasicAmount(BigDecimal basicAmount) {
        this.basicAmount = basicAmount;
    }

    public Integer getSeniorityYears() {
        return seniorityYears;
    }

    public void setSeniorityYears(Integer seniorityYears) {
        this.seniorityYears = seniorityYears;
    }

    public BigDecimal getSeniorityBonus() {
        return seniorityBonus;
    }

    public void setSeniorityBonus(BigDecimal seniorityBonus) {
        this.seniorityBonus = seniorityBonus;
    }

    public BigDecimal getExtraHourCost() {
        return extraHourCost;
    }

    public void setExtraHourCost(BigDecimal extraHourCost) {
        this.extraHourCost = extraHourCost;
    }

    public BigDecimal getExtraHour() {
        return extraHour;
    }

    public void setExtraHour(BigDecimal extraHour) {
        this.extraHour = extraHour;
    }

    public BigDecimal getSundayBonus() {
        return sundayBonus;
    }

    public void setSundayBonus(BigDecimal sundayBonus) {
        this.sundayBonus = sundayBonus;
    }

    public BigDecimal getProductionBonus() {
        return productionBonus;
    }

    public void setProductionBonus(BigDecimal productionBonus) {
        this.productionBonus = productionBonus;
    }

    public BigDecimal getOtherBonus() {
        return otherBonus;
    }

    public void setOtherBonus(BigDecimal otherBonus) {
        this.otherBonus = otherBonus;
    }

    public BigDecimal getOtherIncomes() {
        return otherIncomes;
    }

    public void setOtherIncomes(BigDecimal otherIncomes) {
        this.otherIncomes = otherIncomes;
    }

    public Date getEntranceDate() {
        return entranceDate;
    }

    public void setEntranceDate(Date entranceDate) {
        this.entranceDate = entranceDate;
    }

    public BigDecimal getTotalOtherIncomes() {
        return totalOtherIncomes;
    }

    public void setTotalOtherIncomes(BigDecimal totalOtherIncomes) {
        this.totalOtherIncomes = totalOtherIncomes;
    }

    public BigDecimal getFiscalCredit() {
        return fiscalCredit;
    }

    public void setFiscalCredit(BigDecimal fiscalCredit) {
        this.fiscalCredit = fiscalCredit;
    }

    public BigDecimal getUnlikeTaxable() {
        return unlikeTaxable;
    }

    public void setUnlikeTaxable(BigDecimal unlikeTaxable) {
        this.unlikeTaxable = unlikeTaxable;
    }

    public BigDecimal getTax() {
        return tax;
    }

    public void setTax(BigDecimal tax) {
        this.tax = tax;
    }

    public BigDecimal getTaxForTwoSMN() {
        return taxForTwoSMN;
    }

    public void setTaxForTwoSMN(BigDecimal taxForTwoSMN) {
        this.taxForTwoSMN = taxForTwoSMN;
    }

    public BigDecimal getRetentionClearance() {
        return retentionClearance;
    }

    public void setRetentionClearance(BigDecimal retentionClearance) {
        this.retentionClearance = retentionClearance;
    }

    public BigDecimal getMaintenanceOfValue() {
        return maintenanceOfValue;
    }

    public void setMaintenanceOfValue(BigDecimal maintenanceOfValue) {
        this.maintenanceOfValue = maintenanceOfValue;
    }

    public Long getNumber() {
        return number;
    }

    public void setNumber(Long number) {
        this.number = number;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getRetentionAFP() {
        return retentionAFP;
    }

    public void setRetentionAFP(BigDecimal retentionAFP) {
        this.retentionAFP = retentionAFP;
    }

    public BigDecimal getNetSalary() {
        return netSalary;
    }

    public void setNetSalary(BigDecimal netSalary) {
        this.netSalary = netSalary;
    }

    public BigDecimal getSalaryNotTaxableTwoSMN() {
        return salaryNotTaxableTwoSMN;
    }

    public void setSalaryNotTaxableTwoSMN(BigDecimal salaryNotTaxableTwoSMN) {
        this.salaryNotTaxableTwoSMN = salaryNotTaxableTwoSMN;
    }

    public BigDecimal getPhysicalBalance() {
        return physicalBalance;
    }

    public void setPhysicalBalance(BigDecimal physicalBalance) {
        this.physicalBalance = physicalBalance;
    }

    public BigDecimal getDependentBalance() {
        return dependentBalance;
    }

    public void setDependentBalance(BigDecimal dependentBalance) {
        this.dependentBalance = dependentBalance;
    }

    public BigDecimal getLastMonthBalance() {
        return lastMonthBalance;
    }

    public void setLastMonthBalance(BigDecimal lastMonthBalance) {
        this.lastMonthBalance = lastMonthBalance;
    }

    public BigDecimal getLastBalanceUpdated() {
        return lastBalanceUpdated;
    }

    public void setLastBalanceUpdated(BigDecimal lastBalanceUpdated) {
        this.lastBalanceUpdated = lastBalanceUpdated;
    }

    public BigDecimal getDependentTotalBalance() {
        return dependentTotalBalance;
    }

    public void setDependentTotalBalance(BigDecimal dependentTotalBalance) {
        this.dependentTotalBalance = dependentTotalBalance;
    }

    public BigDecimal getUsedBalance() {
        return usedBalance;
    }

    public void setUsedBalance(BigDecimal usedBalance) {
        this.usedBalance = usedBalance;
    }

    public BigDecimal getDependentBalanceToNextMonth() {
        return dependentBalanceToNextMonth;
    }

    public void setDependentBalanceToNextMonth(BigDecimal dependentBalanceToNextMonth) {
        this.dependentBalanceToNextMonth = dependentBalanceToNextMonth;
    }

    public BigDecimal getTotalGrained() {
        return totalGrained;
    }

    public void setTotalGrained(BigDecimal totalGrained) {
        this.totalGrained = totalGrained;
    }

    public FiscalPayroll getFiscalPayroll() {
        return fiscalPayroll;
    }

    public void setFiscalPayroll(FiscalPayroll fiscalPayroll) {
        this.fiscalPayroll = fiscalPayroll;
    }

    public BigDecimal getPatronalRetentionAFP() {
        return patronalRetentionAFP;
    }

    public void setPatronalRetentionAFP(BigDecimal patronalRetentionAFP) {
        this.patronalRetentionAFP = patronalRetentionAFP;
    }

    public BigDecimal getPatronalProffesionalRiskRetentionAFP() {
        return patronalProffesionalRiskRetentionAFP;
    }

    public void setPatronalProffesionalRiskRetentionAFP(BigDecimal patronalProffesionalRiskRetentionAFP) {
        this.patronalProffesionalRiskRetentionAFP = patronalProffesionalRiskRetentionAFP;
    }

    public BigDecimal getPatronalProHomeRetentionAFP() {
        return patronalProHomeRetentionAFP;
    }

    public void setPatronalProHomeRetentionAFP(BigDecimal patronalProHomeRetentionAFP) {
        this.patronalProHomeRetentionAFP = patronalProHomeRetentionAFP;
    }

    public BigDecimal getPatronalSolidaryRetentionAFP() {
        return patronalSolidaryRetentionAFP;
    }

    public void setPatronalSolidaryRetentionAFP(BigDecimal patronalSolidaryRetentionAFP) {
        this.patronalSolidaryRetentionAFP = patronalSolidaryRetentionAFP;
    }

    public BigDecimal getSolidaryAFP() {
        return solidaryAFP;
    }

    public void setSolidaryAFP(BigDecimal solidaryAFP) {
        this.solidaryAFP = solidaryAFP;
    }

    public BigDecimal getCns() {
        return cns;
    }

    public void setCns(BigDecimal cns) {
        this.cns = cns;
    }
}
