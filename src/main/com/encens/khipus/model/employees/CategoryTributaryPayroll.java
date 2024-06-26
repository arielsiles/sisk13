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
 * @version 3.4
 */

@TableGenerator(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "CategoryTributaryPayroll.tableGenerator",
        table = com.encens.khipus.util.Constants.SEQUENCE_TABLE_NAME,
        pkColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_PK_COLUMN_NAME,
        valueColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_VALUE_COLUMN_NAME,
        pkColumnValue = "planillatributariaporcategoria",
        allocationSize = com.encens.khipus.util.Constants.SEQUENCE_ALLOCATION_SIZE)
@NamedQueries({
        @NamedQuery(name = "CategoryTributaryPayroll.loadByGeneratedPayroll",
                query = "select element from CategoryTributaryPayroll element" +
                        " left join fetch element.generatedPayroll generatedPayrollTP" +
                        " left join fetch element.jobContract jobContractTP" +
                        " left join fetch element.employee employeeTP" +
                        " left join fetch element.businessUnit businessUnitTP" +
                        " left join fetch element.categoryFiscalPayroll categoryFiscalPayroll" +
                        " left join fetch categoryFiscalPayroll.generatedPayroll generatedPayrollFP" +
                        " left join fetch categoryFiscalPayroll.jobContract jobContractFP" +
                        " left join fetch categoryFiscalPayroll.employee employeeFP" +
                        " left join fetch categoryFiscalPayroll.businessUnit businessUnitFP" +
                        " where generatedPayrollTP=:generatedPayroll"),
        @NamedQuery(name = "CategoryTributaryPayroll.findByJobContract",
                query = "select element from CategoryTributaryPayroll element where element.generatedPayroll =:generatedPayroll and element.jobContract in(:jobContracts)"),
        @NamedQuery(name = "CategoryTributaryPayroll.findByGeneratedPayroll",
                query = "select element from CategoryTributaryPayroll element where element.generatedPayroll =:generatedPayroll order by element.number"),
        @NamedQuery(name = "CategoryTributaryPayroll.findJobContractIdsByGeneratedPayroll",
                query = "select element.jobContractId from CategoryTributaryPayroll element where element.generatedPayroll =:generatedPayroll order by element.number")
})
@Entity
@EntityListeners({CompanyListener.class, UpperCaseStringListener.class})
@Filter(name = com.encens.khipus.util.Constants.COMPANY_FILTER_NAME)
@Table(name = "planillatributariaporcategoria", schema = Constants.KHIPUS_SCHEMA)
public class CategoryTributaryPayroll implements BaseModel {
    @Id
    @Column(name = "idplanillatributariaporcat", nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "CategoryTributaryPayroll.tableGenerator")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "idcompania", nullable = false, updatable = false, insertable = true)
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "idplanillagenerada", nullable = false, updatable = false)
    private GeneratedPayroll generatedPayroll;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "idcontratopuesto", insertable = true, updatable = false, nullable = false)
    private JobContract jobContract;

    @Column(name = "idcontratopuesto", insertable = false, updatable = false, nullable = false)
    private Long jobContractId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "idempleado", insertable = true, updatable = false, nullable = false)
    private Employee employee;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "idunidadnegocio", updatable = true, insertable = true)
    private BusinessUnit businessUnit;

    @Column(name = "codigo", nullable = false)
    private String code;

    @Column(name = "haberbasico", precision = 13, scale = 2, nullable = false)
    @NotNull
    private BigDecimal basicAmount;

    @Column(name = "aniosantiguedad")
    private Integer seniorityYears;

    @Column(name = "bonoantiguedad", precision = 13, scale = 2, nullable = false)
    @NotNull
    private BigDecimal seniorityBonus;

    @Column(name = "costohorasextra", precision = 13, scale = 2, nullable = false)
    private BigDecimal extraHourCost;

    @Column(name = "horasextra", precision = 13, scale = 2, nullable = false)
    private BigDecimal extraHour;

    @Column(name = "bonodominical", precision = 13, scale = 2, nullable = false)
    private BigDecimal sundayBonus;

    @Column(name = "bonoproduccion", precision = 13, scale = 2, nullable = false)
    private BigDecimal productionBonus;

    @Column(name = "bononocturno", precision = 13, scale = 2, nullable = false)
    private BigDecimal nightWorkBonus;

    @Column(name = "bonopasaje", precision = 13, scale = 2, nullable = false)
    private BigDecimal transReturnBonus;

    @Column(name = "bonorefrigerio", precision = 13, scale = 2, nullable = false)
    private BigDecimal refreshmentBonus;

    @Column(name = "otrosbonos", precision = 13, scale = 2, nullable = false)
    private BigDecimal otherBonus;

    @Column(name = "otrosingresos", precision = 13, scale = 2, nullable = false)
    private BigDecimal otherIncomes;

    @Column(name = "fechaingreso")
    @Temporal(TemporalType.DATE)
    private Date entranceDate;

    @Column(name = "totalotrosingresos", precision = 13, scale = 2, nullable = false)
    @NotNull
    private BigDecimal totalOtherIncomes;

    @Column(name = "creditofiscal", precision = 13, scale = 2, nullable = false)
    private BigDecimal fiscalCredit;

    @Column(name = "difsujetaimpuesto", precision = 13, scale = 2, nullable = false)
    private BigDecimal unlikeTaxable;

    @Column(name = "impuesto", precision = 13, scale = 2, nullable = false)
    private BigDecimal tax;

    @Column(name = "impsobredossmn", precision = 13, scale = 2, nullable = false)
    private BigDecimal taxForTwoSMN;

    @Column(name = "liquidacionretencion", precision = 13, scale = 2, nullable = false)
    private BigDecimal retentionClearance;

    @Column(name = "mantenimientovalor", precision = 13, scale = 2, nullable = false)
    private BigDecimal maintenanceOfValue;

    @Column(name = "numero", nullable = false)
    private Long number;

    @Column(name = "nombre", nullable = false)
    private String name;

    @Column(name = "retencionafp", precision = 13, scale = 2, nullable = false)
    private BigDecimal retentionAFP;

    @Column(name = "afplab_individual", precision = 13, scale = 2, nullable = false)
    private BigDecimal laborIndividualAFP;

    @Column(name = "afplab_riesgocomun", precision = 13, scale = 2, nullable = false)
    @NotNull
    private BigDecimal laborCommonRiskAFP;

    @Column(name = "afplab_solidario", precision = 13, scale = 2, nullable = false)
    @NotNull
    private BigDecimal laborSolidaryContributionAFP;

    @Column(name = "afplab_comision", precision = 13, scale = 2, nullable = false)
    @NotNull
    private BigDecimal laborComissionAFP;

    @Column(name = "afpsolidario", precision = 13, scale = 2, nullable = false)
    @NotNull
    private BigDecimal solidaryAFP;

    @Column(name = "retencionpatronalafp", precision = 13, scale = 2, nullable = false)
    @NotNull
    private BigDecimal patronalRetentionAFP;

    @Column(name = "retpatrriesgoprofafp", precision = 13, scale = 2, nullable = false)
    @NotNull
    private BigDecimal patronalProffesionalRiskRetentionAFP;

    @Column(name = "retpatrproviviendaafp", precision = 13, scale = 2, nullable = false)
    @NotNull
    private BigDecimal patronalProHomeRetentionAFP;

    @Column(name = "retpatrsolidarioafp", precision = 13, scale = 2, nullable = false)
    @NotNull
    private BigDecimal patronalSolidaryRetentionAFP;

    @Column(name = "cns", precision = 13, scale = 2, nullable = false)
    @NotNull
    private BigDecimal cns;

    @Column(name = "sueldoneto", precision = 13, scale = 2, nullable = false)
    private BigDecimal netSalary;

    @Column(name = "sueldonoimpdossmn", precision = 13, scale = 2, nullable = false)
    private BigDecimal salaryNotTaxableTwoSMN;

    @Column(name = "saldofisco", precision = 13, scale = 2, nullable = false)
    private BigDecimal physicalBalance;

    @Column(name = "saldodependiente", precision = 13, scale = 2, nullable = false)
    private BigDecimal dependentBalance;

    @Column(name = "saldomesanterior", precision = 13, scale = 2, nullable = false)
    private BigDecimal lastMonthBalance;

    @Column(name = "saldoanterioractualizado", precision = 13, scale = 2, nullable = false)
    private BigDecimal lastBalanceUpdated;

    @Column(name = "saldototaldependiente", precision = 13, scale = 2, nullable = false)
    private BigDecimal dependentTotalBalance;

    @Column(name = "saldoutilizado", precision = 13, scale = 2, nullable = false)
    private BigDecimal usedBalance;

    @Column(name = "saldodependientemessgute", precision = 13, scale = 2, nullable = false)
    private BigDecimal dependentBalanceToNextMonth;

    @Column(name = "totalganado", precision = 13, scale = 2, nullable = false)
    private BigDecimal totalGrained;

    @OneToOne(mappedBy = "categoryTributaryPayroll", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private CategoryFiscalPayroll categoryFiscalPayroll;


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

    public GeneratedPayroll getGeneratedPayroll() {
        return generatedPayroll;
    }

    public void setGeneratedPayroll(GeneratedPayroll generatedPayroll) {
        this.generatedPayroll = generatedPayroll;
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

    public CategoryFiscalPayroll getCategoryFiscalPayroll() {
        return categoryFiscalPayroll;
    }

    public void setCategoryFiscalPayroll(CategoryFiscalPayroll categoryFiscalPayroll) {
        this.categoryFiscalPayroll = categoryFiscalPayroll;
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

    public BigDecimal getLaborIndividualAFP() {
        return laborIndividualAFP;
    }

    public void setLaborIndividualAFP(BigDecimal laborIndividualAFP) {
        this.laborIndividualAFP = laborIndividualAFP;
    }

    public BigDecimal getLaborCommonRiskAFP() {
        return laborCommonRiskAFP;
    }

    public void setLaborCommonRiskAFP(BigDecimal laborCommonRiskAFP) {
        this.laborCommonRiskAFP = laborCommonRiskAFP;
    }

    public BigDecimal getLaborSolidaryContributionAFP() {
        return laborSolidaryContributionAFP;
    }

    public void setLaborSolidaryContributionAFP(BigDecimal laborSolidaryContributionAFP) {
        this.laborSolidaryContributionAFP = laborSolidaryContributionAFP;
    }

    public BigDecimal getLaborComissionAFP() {
        return laborComissionAFP;
    }

    public void setLaborComissionAFP(BigDecimal laborComissionAFP) {
        this.laborComissionAFP = laborComissionAFP;
    }

    public BigDecimal getNightWorkBonus() {
        return nightWorkBonus;
    }

    public void setNightWorkBonus(BigDecimal nightWorkBonus) {
        this.nightWorkBonus = nightWorkBonus;
    }

    public BigDecimal getTransReturnBonus() {
        return transReturnBonus;
    }

    public void setTransReturnBonus(BigDecimal transReturnBonus) {
        this.transReturnBonus = transReturnBonus;
    }

    public BigDecimal getRefreshmentBonus() {
        return refreshmentBonus;
    }

    public void setRefreshmentBonus(BigDecimal refreshmentBonus) {
        this.refreshmentBonus = refreshmentBonus;
    }
}
