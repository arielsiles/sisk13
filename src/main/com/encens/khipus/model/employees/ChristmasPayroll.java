package com.encens.khipus.model.employees;

import com.encens.khipus.model.CompanyListener;
import com.encens.khipus.model.CompanyNumberListener;
import com.encens.khipus.model.UpperCaseStringListener;
import com.encens.khipus.model.admin.BusinessUnit;
import com.encens.khipus.model.admin.Company;
import com.encens.khipus.model.finances.CostCenter;
import com.encens.khipus.model.usertype.IntegerBooleanUserType;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.Type;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @author
 * @version 3.2
 */
@NamedQueries({
        @NamedQuery(name = "ChristmasPayroll.sumLiquidByPaymentType", query = "SELECT SUM(christmasPayroll.liquid) FROM ChristmasPayroll christmasPayroll WHERE " +
                "christmasPayroll.generatedPayroll.id =:generatedPayrollId AND " +
                "christmasPayroll.employee.paymentType =:paymentType"),
        @NamedQuery(name = "ChristmasPayroll.sumAndDivideLiquidByGestionPayrollListAndPaymentType", query = "SELECT SUM(christmasPayroll.liquid/christmasPayroll.generatedPayroll.gestionPayroll.exchangeRate.rate) FROM ChristmasPayroll christmasPayroll WHERE " +
                "christmasPayroll.generatedPayroll.gestionPayroll in (:gestionPayrollList) AND " +
                "christmasPayroll.generatedPayroll.generatedPayrollType =:generatedPayrollType AND " +
                "christmasPayroll.employee.paymentType =:paymentType"),
        @NamedQuery(name = "ChristmasPayroll.countByPaymentType", query = "SELECT COUNT(christmasPayroll) FROM ChristmasPayroll christmasPayroll WHERE " +
                "christmasPayroll.generatedPayroll.id =:generatedPayrollId AND " +
                "christmasPayroll.employee.paymentType =:paymentType"),
        @NamedQuery(name = "ChristmasPayroll.sumLiquidByPaymentTypeAndCurrency", query = "SELECT SUM(christmasPayroll.liquid) FROM ChristmasPayroll christmasPayroll " +
                " LEFT JOIN christmasPayroll.employee.bankAccountList bankAccount WHERE " +
                "christmasPayroll.generatedPayroll.id =:generatedPayrollId AND " +
                "christmasPayroll.employee.paymentType =:paymentType AND " +
                "bankAccount.defaultAccount =:defaultAccount AND " +
                "bankAccount.currency.id =:currencyId"),
        @NamedQuery(name = "ChristmasPayroll.countByPaymentTypeAndCurrency", query = "SELECT COUNT(christmasPayroll) FROM ChristmasPayroll christmasPayroll " +
                " LEFT JOIN christmasPayroll.employee.bankAccountList bankAccount WHERE " +
                "christmasPayroll.generatedPayroll.id =:generatedPayrollId AND " +
                "christmasPayroll.employee.paymentType =:paymentType AND " +
                "bankAccount.defaultAccount =:defaultAccount AND " +
                "bankAccount.currency.id =:currencyId"),
        @NamedQuery(name = "ChristmasPayroll.sumLiquidByCostCenterAndPaymentType", query = "SELECT SUM(christmasPayroll.liquid) FROM ChristmasPayroll christmasPayroll WHERE " +
                "christmasPayroll.generatedPayroll.id =:generatedPayrollId AND " +
                "christmasPayroll.costCenterCode=:costCenterCode AND " +
                "christmasPayroll.employee.paymentType =:paymentType"),
        @NamedQuery(name = "ChristmasPayroll.countByCostCenterAndPaymentType", query = "SELECT COUNT(christmasPayroll) FROM ChristmasPayroll christmasPayroll WHERE " +
                "christmasPayroll.generatedPayroll.id =:generatedPayrollId AND " +
                "christmasPayroll.costCenterCode=:costCenterCode AND " +
                "christmasPayroll.employee.paymentType =:paymentType"),
        @NamedQuery(name = "ChristmasPayroll.sumLiquidByCostCenterAndPaymentTypeAndCurrency", query = "SELECT SUM(christmasPayroll.liquid) FROM ChristmasPayroll christmasPayroll " +
                " LEFT JOIN christmasPayroll.employee.bankAccountList bankAccount WHERE " +
                "christmasPayroll.generatedPayroll.id =:generatedPayrollId AND " +
                "christmasPayroll.costCenterCode=:costCenterCode AND " +
                "christmasPayroll.employee.paymentType =:paymentType AND " +
                "bankAccount.defaultAccount =:defaultAccount AND " +
                "bankAccount.currency.id =:currencyId"),
        @NamedQuery(name = "ChristmasPayroll.countByCostCenterAndPaymentTypeAndCurrency", query = "SELECT COUNT(christmasPayroll) FROM ChristmasPayroll christmasPayroll " +
                " LEFT JOIN christmasPayroll.employee.bankAccountList bankAccount WHERE " +
                "christmasPayroll.generatedPayroll.id =:generatedPayrollId AND " +
                "christmasPayroll.costCenterCode=:costCenterCode AND " +
                "christmasPayroll.employee.paymentType =:paymentType AND " +
                "bankAccount.defaultAccount =:defaultAccount AND " +
                "bankAccount.currency.id =:currencyId"),
        @NamedQuery(name = "ChristmasPayroll.findByGeneratedPayroll", query = "select cp from ChristmasPayroll cp where cp.generatedPayroll=:generatedPayroll"),
        @NamedQuery(name = "ChristmasPayroll.countPayrollWithNegativeAmount", query = "select count(cp) from ChristmasPayroll cp where cp.generatedPayroll=:generatedPayroll and cp.liquid<0"),
        @NamedQuery(name = "ChristmasPayroll.findOfficialGeneratedPayroll", query = "SELECT generatedPayroll FROM ChristmasPayroll christmasPayroll " +
                " LEFT JOIN christmasPayroll.generatedPayroll generatedPayroll" +
                " LEFT JOIN generatedPayroll.gestionPayroll gestionPayroll" +
                " LEFT JOIN gestionPayroll.businessUnit businessUnit " +
                " LEFT JOIN gestionPayroll.jobCategory jobCategory WHERE " +
                " businessUnit=:businessUnit AND " +
                " jobCategory=:jobCategory AND " +
                " gestionPayroll.gestion=:gestion AND " +
                " generatedPayroll.generatedPayrollType=:generatedPayrollType"),
        @NamedQuery(name = "ChristmasPayroll.findByPayrollGenerationParameters", query = "SELECT christmasPayroll FROM ChristmasPayroll christmasPayroll " +
                " LEFT JOIN FETCH christmasPayroll.employee employee" +
                " LEFT JOIN FETCH christmasPayroll.generatedPayroll generatedPayroll" +
                " LEFT JOIN FETCH generatedPayroll.gestionPayroll gestionPayroll" +
                " LEFT JOIN FETCH gestionPayroll.businessUnit businessUnit " +
                " LEFT JOIN FETCH gestionPayroll.jobCategory jobCategory " +
                " LEFT JOIN FETCH jobCategory.sector sector WHERE " +
                " businessUnit=:businessUnit AND " +
                " jobCategory=:jobCategory AND " +
                " gestionPayroll.gestion=:gestion AND " +
                " generatedPayroll.generatedPayrollType=:generatedPayrollType"),
        @NamedQuery(name = "ChristmasPayroll.employeeWithoutBankAccount", query = "SELECT count(christmasPayroll) FROM ChristmasPayroll christmasPayroll " +
                " LEFT JOIN christmasPayroll.employee employee" +
                " LEFT JOIN christmasPayroll.generatedPayroll generatedPayroll" +
                " LEFT JOIN christmasPayroll.employee.bankAccountList bankAccount WHERE " +
                "christmasPayroll.generatedPayroll =:generatedPayroll AND " +
                "christmasPayroll.employee.paymentType =:paymentType AND " +
                "bankAccount is null"),
        @NamedQuery(name = "ChristmasPayroll.countByAccountingRecord",
                query = "SELECT count(christmasPayroll) " +
                        " FROM ChristmasPayroll christmasPayroll " +
                        " WHERE christmasPayroll.hasAccountingRecord=:BOOLEAN_TRUE" +
                        " AND christmasPayroll.generatedPayrollId IN (SELECT generatedPayroll.id FROM GeneratedPayroll generatedPayroll WHERE generatedPayroll=:generatedPayroll AND generatedPayroll.generatedPayrollType=:generatedPayrollType)" +
                        " AND christmasPayroll.id IN (:selectIdList)"),
        @NamedQuery(name = "ChristmasPayroll.countByAccountingRecordOrInactivePayment",
                query = "SELECT count(christmasPayroll) " +
                        " FROM ChristmasPayroll christmasPayroll " +
                        " WHERE (christmasPayroll.hasAccountingRecord=:BOOLEAN_TRUE" +
                        " OR christmasPayroll.hasActivePayment=:BOOLEAN_FALSE) " +
                        " AND christmasPayroll.generatedPayrollId IN (SELECT generatedPayroll.id FROM GeneratedPayroll generatedPayroll WHERE generatedPayroll=:generatedPayroll AND generatedPayroll.generatedPayrollType=:generatedPayrollType)" +
                        " AND christmasPayroll.id IN (:selectIdList)"),
        @NamedQuery(name = "ChristmasPayroll.updateActivePaymentToSelectItems",
                query = "UPDATE ChristmasPayroll christmasPayroll " +
                        " SET christmasPayroll.hasActivePayment=:BOOLEAN_TRUE" +
                        " WHERE christmasPayroll.hasAccountingRecord=:BOOLEAN_FALSE" +
                        " AND christmasPayroll.hasActivePayment=:BOOLEAN_FALSE" +
                        " AND christmasPayroll.generatedPayrollId IN (SELECT generatedPayroll.id FROM GeneratedPayroll generatedPayroll WHERE generatedPayroll=:generatedPayroll AND generatedPayroll.generatedPayrollType=:generatedPayrollType)" +
                        " AND christmasPayroll.id IN (:selectIdList)"),
        @NamedQuery(name = "ChristmasPayroll.updateInactivePaymentToUnselectItems",
                query = "UPDATE ChristmasPayroll christmasPayroll " +
                        " SET christmasPayroll.hasActivePayment=:BOOLEAN_FALSE" +
                        " WHERE christmasPayroll.hasAccountingRecord=:BOOLEAN_FALSE" +
                        " AND christmasPayroll.hasActivePayment=:BOOLEAN_TRUE" +
                        " AND christmasPayroll.generatedPayrollId IN (SELECT generatedPayroll.id FROM GeneratedPayroll generatedPayroll WHERE generatedPayroll=:generatedPayroll AND generatedPayroll.generatedPayrollType=:generatedPayrollType)" +
                        " AND christmasPayroll.id NOT IN (:selectIdList)"),
        @NamedQuery(name = "ChristmasPayroll.findSelectIdList",
                query = "SELECT christmasPayroll.id " +
                        " FROM ChristmasPayroll christmasPayroll " +
                        " WHERE christmasPayroll.hasAccountingRecord=:BOOLEAN_FALSE" +
                        " AND christmasPayroll.hasActivePayment=:BOOLEAN_TRUE " +
                        " AND christmasPayroll.generatedPayrollId IN (SELECT generatedPayroll.id FROM GeneratedPayroll generatedPayroll WHERE generatedPayroll=:generatedPayroll AND generatedPayroll.generatedPayrollType=:generatedPayrollType)"),
        @NamedQuery(name = "ChristmasPayroll.findSelectItemList",
                query = "SELECT christmasPayroll " +
                        " FROM ChristmasPayroll christmasPayroll " +
                        " WHERE christmasPayroll.hasAccountingRecord=:BOOLEAN_FALSE" +
                        " AND christmasPayroll.hasActivePayment=:BOOLEAN_TRUE " +
                        " AND christmasPayroll.generatedPayrollId IN (SELECT generatedPayroll.id FROM GeneratedPayroll generatedPayroll WHERE generatedPayroll=:generatedPayroll AND generatedPayroll.generatedPayrollType=:generatedPayrollType)"),
        @NamedQuery(name = "ChristmasPayroll.findEmployeesByIdList",
                query = "SELECT employee " +
                        " FROM ChristmasPayroll christmasPayroll " +
                        " left join christmasPayroll.employee employee" +
                        " WHERE christmasPayroll.id IN (:idList)"),
        @NamedQuery(name = "ChristmasPayroll.findEmployeeIdList",
                query = "SELECT employee.id " +
                        " FROM ChristmasPayroll christmasPayroll " +
                        " left join christmasPayroll.employee employee" +
                        " WHERE christmasPayroll.id IN (:idList)"),
        @NamedQuery(name = "ChristmasPayroll.findEmployeeIdListByGeneratedPayroll",
                query = " SELECT christmasPayroll.employee.id FROM ChristmasPayroll christmasPayroll " +
                        " WHERE christmasPayroll.generatedPayroll=:generatedPayroll "),
        @NamedQuery(name = "ChristmasPayroll.findEmployeeIdListByGeneratedPayrollInEmployeeIdList",
                query = " SELECT christmasPayroll.employee.id FROM ChristmasPayroll christmasPayroll " +
                        " WHERE christmasPayroll.generatedPayroll=:generatedPayroll " +
                        " AND christmasPayroll.employee.id in(:employeeIdList) "),
        @NamedQuery(name = "ChristmasPayroll.findByGeneratedPayrollAndEmployeeIdList",
                query = " SELECT employee.id, christmasPayroll FROM ChristmasPayroll christmasPayroll " +
                        " WHERE christmasPayroll.generatedPayroll=:generatedPayroll " +
                        " AND christmasPayroll.employee.id in(:employeeIdList) "),
        @NamedQuery(name = "ChristmasPayroll.findByGeneratedPayrollAndEmployee",
                query = "SELECT christmasPayroll " +
                        " FROM ChristmasPayroll christmasPayroll " +
                        " WHERE christmasPayroll.generatedPayroll=:generatedPayroll and christmasPayroll.employee=:employee"),
        @NamedQuery(name = "ChristmasPayroll.findByGeneratedPayrollList",
                query = "select christmasPayroll from ChristmasPayroll christmasPayroll where christmasPayroll.generatedPayroll in(:generatedPayrolls)"),

        //report summary queries
        @NamedQuery(name = "ChristmasPayroll.countByCostCenter", query = "SELECT COUNT(christmasPayroll) FROM ChristmasPayroll christmasPayroll " +
                " WHERE christmasPayroll.generatedPayroll.id =:generatedPayrollId AND " +
                "christmasPayroll.costCenterCode=:costCenterCode"),
        @NamedQuery(name = "ChristmasPayroll.sumLiquidByCostCenter", query = "SELECT SUM(christmasPayroll.liquid) FROM ChristmasPayroll christmasPayroll " +
                " WHERE christmasPayroll.generatedPayroll.id =:generatedPayrollId AND " +
                "christmasPayroll.costCenterCode=:costCenterCode "),
        @NamedQuery(name = "ChristmasPayroll.loadByGeneratedPayrollList",
                query = "select element from ChristmasPayroll element" +
                        " left join fetch element.charge charge" +
                        " left join fetch element.employee employee" +
                        " left join fetch element.costCenter costCenter" +
                        " left join fetch element.businessUnit businessUnit" +
                        " left join fetch element.jobCategory jobCategory" +
                        " left join fetch businessUnit.organization organization" +
                        " left join fetch element.generatedPayroll generatedPayroll" +
                        " left join fetch generatedPayroll.gestionPayroll gestionPayroll" +
                        " left join fetch gestionPayroll.exchangeRate exchangeRate " +
                        " where element.id in(:idList)" +
                        " order by businessUnit.executorUnitCode,costCenter.id")

})
@TableGenerator(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "ChristmasPayroll.tableGenerator",
        table = com.encens.khipus.util.Constants.SEQUENCE_TABLE_NAME,
        pkColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_PK_COLUMN_NAME,
        valueColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_VALUE_COLUMN_NAME,
        pkColumnValue = "PLANILLAAGUINALDO",
        allocationSize = com.encens.khipus.util.Constants.SEQUENCE_ALLOCATION_SIZE)
@Entity
@Filter(name = com.encens.khipus.util.Constants.COMPANY_FILTER_NAME)
@EntityListeners({CompanyListener.class, CompanyNumberListener.class, UpperCaseStringListener.class})
@Table(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "PLANILLAAGUINALDO")
public class ChristmasPayroll implements GenericPayroll {

    @Id
    @Column(name = "IDPLANILLAAGUINALDO", nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "ChristmasPayroll.tableGenerator")
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @NotNull
    @JoinColumn(name = "IDEMPLEADO", referencedColumnName = "idempleado", nullable = false, updatable = false)
    private Employee employee;

    @Temporal(TemporalType.DATE)
    @NotNull
    @Column(name = "FECHAINICIOCONTRATO", nullable = false)
    private Date contractInitDate;

    @Temporal(TemporalType.DATE)
    @Column(name = "FECHAFINCONTRATO")
    private Date contractEndDate;

    @Column(name = "DIASTRABAJADOS", nullable = false, precision = 13, scale = 2)
    @NotNull
    private BigDecimal workedDays;

    @Column(name = "TOTALGANADOSEPTIEMBRE", precision = 13, scale = 2)
    private BigDecimal septemberTotalIncome;

    @Column(name = "TOTALGANADOOCTUBRE", precision = 13, scale = 2)
    private BigDecimal octoberTotalIncome;

    @Column(name = "TOTALGANADONOVIEMBRE", precision = 13, scale = 2)
    private BigDecimal novemberTotalIncome;

    @Column(name = "SUELDO", nullable = false, precision = 13, scale = 2)
    @NotNull
    private BigDecimal salary;

    @Column(name = "SUELDOPROMEDIO", nullable = false, precision = 13, scale = 2)
    @NotNull
    private BigDecimal averageSalary;

    @Column(name = "SUELDOCOTIZABLE", nullable = false, precision = 13, scale = 2)
    @NotNull
    private BigDecimal contributableSalary;

    @Column(name = "LIQUIDOPAGABLE", nullable = false, precision = 13, scale = 2)
    @NotNull
    private BigDecimal liquid;

    @Column(name = "CUENTABANCARIA", length = 255)
    @Length(max = 255)
    private String bankAccount;

    @Column(name = "MONEDACTABANCARIA", length = 100)
    @Length(max = 100)
    private String bankAccountCurrency;

    @Column(name = "CODIGOCLIENTE", length = 150)
    @Length(max = 150)
    private String clientCode;

    @Column(name = "AREA")
    private String area;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "IDPLANILLAGENERADA", referencedColumnName = "idplanillagenerada", nullable = false, updatable = false)
    @NotNull
    private GeneratedPayroll generatedPayroll;

    @Column(name = "IDPLANILLAGENERADA", updatable = false, insertable = false)
    private Long generatedPayrollId;

    @Column(name = "REGISTROCONTABLE")
    @Type(type = IntegerBooleanUserType.NAME)
    private Boolean hasAccountingRecord = Boolean.FALSE;

    @Column(name = "PAGOACTIVO")
    @Type(type = IntegerBooleanUserType.NAME)
    private Boolean hasActivePayment = Boolean.FALSE;

    @Column(name = "ACTIVOGENPLANFIS")
    @Type(type = com.encens.khipus.model.usertype.IntegerBooleanUserType.NAME)
    private Boolean activeForTaxPayrollGeneration = Boolean.FALSE;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "IDUNIDADNEGOCIO", referencedColumnName = "idunidadnegocio", nullable = false)
    private BusinessUnit businessUnit;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @NotNull
    @JoinColumns({
            @JoinColumn(name = "NUMEROCOMPANIA", referencedColumnName = "NO_CIA", updatable = false, insertable = false, nullable = false),
            @JoinColumn(name = "CODIGOCENCOS", referencedColumnName = "COD_CC", updatable = false, insertable = false, nullable = false)
    })
    private CostCenter costCenter;

    @Column(name = "CODIGOCENCOS", length = 6, nullable = false)
    @NotNull
    @Length(max = 6)
    private String costCenterCode;

    @Column(name = "NUMEROCOMPANIA", updatable = false, nullable = false, length = 2)
    @Length(max = 2)
    @NotNull
    private String companyNumber;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @NotNull
    @JoinColumn(name = "IDCARGO", referencedColumnName = "idcargo", nullable = false)
    private Charge charge;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @NotNull
    @JoinColumn(name = "IDCATEGORIAPUESTO", referencedColumnName = "idcategoriapuesto", nullable = false)
    private JobCategory jobCategory;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @NotNull
    @JoinColumn(name = "IDCOMPANIA", referencedColumnName = "idcompania", nullable = false, updatable = false)
    private Company company;

    @Version
    @Column(name = "VERSION", nullable = false)
    private long version;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getContractInitDate() {
        return contractInitDate;
    }

    public void setContractInitDate(Date contractInitDate) {
        this.contractInitDate = contractInitDate;
    }

    public Date getContractEndDate() {
        return contractEndDate;
    }

    public void setContractEndDate(Date contractEndDate) {
        this.contractEndDate = contractEndDate;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public GeneratedPayroll getGeneratedPayroll() {
        return generatedPayroll;
    }

    public void setGeneratedPayroll(GeneratedPayroll generatedPayroll) {
        this.generatedPayroll = generatedPayroll;
    }

    public Long getGeneratedPayrollId() {
        return generatedPayrollId;
    }

    public void setGeneratedPayrollId(Long generatedPayrollId) {
        this.generatedPayrollId = generatedPayrollId;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public BigDecimal getWorkedDays() {
        return workedDays;
    }

    public void setWorkedDays(BigDecimal workedDays) {
        this.workedDays = workedDays;
    }

    public BigDecimal getSalary() {
        return salary;
    }

    public void setSalary(BigDecimal salary) {
        this.salary = salary;
    }

    public BigDecimal getLiquid() {
        return liquid;
    }

    public void setLiquid(BigDecimal liquid) {
        this.liquid = liquid;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public Boolean getHasAccountingRecord() {
        return hasAccountingRecord;
    }

    public void setHasAccountingRecord(Boolean hasAccountingRecord) {
        this.hasAccountingRecord = hasAccountingRecord;
    }

    public Boolean getHasActivePayment() {
        return hasActivePayment;
    }

    public void setHasActivePayment(Boolean hasActivePayment) {
        this.hasActivePayment = hasActivePayment;
    }

    public Boolean getActiveForTaxPayrollGeneration() {
        return activeForTaxPayrollGeneration;
    }

    public void setActiveForTaxPayrollGeneration(Boolean activeForTaxPayrollGeneration) {
        this.activeForTaxPayrollGeneration = activeForTaxPayrollGeneration;
    }

    public BusinessUnit getBusinessUnit() {
        return businessUnit;
    }

    public void setBusinessUnit(BusinessUnit businessUnit) {
        this.businessUnit = businessUnit;
    }

    public CostCenter getCostCenter() {
        return costCenter;
    }

    public void setCostCenter(CostCenter costCenter) {
        this.costCenter = costCenter;
        setCompanyNumber(costCenter != null ? costCenter.getCompanyNumber() : null);
        setCostCenterCode(costCenter != null ? costCenter.getCode() : null);
    }

    public String getCompanyNumber() {
        return companyNumber;
    }

    public void setCompanyNumber(String companyNumber) {
        this.companyNumber = companyNumber;
    }

    public String getCostCenterCode() {
        return costCenterCode;
    }

    public void setCostCenterCode(String costCenterCode) {
        this.costCenterCode = costCenterCode;
    }

    public Charge getCharge() {
        return charge;
    }

    public void setCharge(Charge charge) {
        this.charge = charge;
    }

    public JobCategory getJobCategory() {
        return jobCategory;
    }

    public BigDecimal getTardinessMinutesDiscount() {
        return null;
    }

    public BigDecimal getLoanDiscount() {
        return null;
    }

    public BigDecimal getAdvanceDiscount() {
        return null;
    }

    public BigDecimal getAfp() {
        return null;
    }

    public BigDecimal getRciva() {
        return null;
    }

    public BigDecimal getWinDiscount() {
        return null;
    }

    public BigDecimal getOtherDiscounts() {
        return null;
    }

    public BigDecimal getDiscountsOutOfRetention() {
        return null;
    }

    public void setJobCategory(JobCategory jobCategory) {
        this.jobCategory = jobCategory;
    }

    public BigDecimal getOctoberTotalIncome() {
        return octoberTotalIncome;
    }

    public void setOctoberTotalIncome(BigDecimal octoberTotalIncome) {
        this.octoberTotalIncome = octoberTotalIncome;
    }

    public BigDecimal getNovemberTotalIncome() {
        return novemberTotalIncome;
    }

    public void setNovemberTotalIncome(BigDecimal novemberTotalIncome) {
        this.novemberTotalIncome = novemberTotalIncome;
    }

    public BigDecimal getSeptemberTotalIncome() {
        return septemberTotalIncome;
    }

    public void setSeptemberTotalIncome(BigDecimal septemberTotalIncome) {
        this.septemberTotalIncome = septemberTotalIncome;
    }

    public BigDecimal getAverageSalary() {
        return averageSalary;
    }

    public void setAverageSalary(BigDecimal averageSalary) {
        this.averageSalary = averageSalary;
    }

    public BigDecimal getContributableSalary() {
        return contributableSalary;
    }

    public void setContributableSalary(BigDecimal contributableSalary) {
        this.contributableSalary = contributableSalary;
    }

    public String getBankAccount() {
        return bankAccount;
    }

    public void setBankAccount(String bankAccount) {
        this.bankAccount = bankAccount;
    }

    public String getBankAccountCurrency() {
        return bankAccountCurrency;
    }

    public void setBankAccountCurrency(String bankAccountCurrency) {
        this.bankAccountCurrency = bankAccountCurrency;
    }

    public String getClientCode() {
        return clientCode;
    }

    public void setClientCode(String clientCode) {
        this.clientCode = clientCode;
    }
}