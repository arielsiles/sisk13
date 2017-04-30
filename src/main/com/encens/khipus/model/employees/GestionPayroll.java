package com.encens.khipus.model.employees;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyListener;
import com.encens.khipus.model.UpperCaseStringListener;
import com.encens.khipus.model.admin.BusinessUnit;
import com.encens.khipus.model.admin.Company;
import com.encens.khipus.model.finances.ExchangeRate;
import org.hibernate.annotations.Filter;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Entity for GESTION PAYROLL
 *
 * @author
 * @version 2.26
 */

@TableGenerator(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "GestionPayroll.tableGenerator",
        table = com.encens.khipus.util.Constants.SEQUENCE_TABLE_NAME,
        pkColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_PK_COLUMN_NAME,
        valueColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_VALUE_COLUMN_NAME,
        pkColumnValue = "gestionplanilla",
        allocationSize = com.encens.khipus.util.Constants.SEQUENCE_ALLOCATION_SIZE)

@NamedQueries(
        {
                @NamedQuery(name = "GestionPayroll.findAll", query = "select o from GestionPayroll o order by o.id asc"),
                @NamedQuery(name = "GestionPayroll.findById", query = "select o from GestionPayroll o where o.id=:id"),
                @NamedQuery(name = "GestionPayroll.findByYearByMonth", query = "select o from GestionPayroll o " +
                        "where o.gestion.year=:year and o.month=:month order by o.id asc"),
                @NamedQuery(name = "GestionPayroll.findByInitEndRangeSector", query = "select o from GestionPayroll o where o.initDate>=:initDate and " +
                        "o.endDate<=:endDate and o.jobCategory.sector=:sector"),
                @NamedQuery(name = "GestionPayroll.findByInitEndRange", query = "select o from GestionPayroll o where o.initDate>=:initDate and " +
                        "o.endDate<=:endDate"),

                @NamedQuery(name = "GestionPayroll.findByInitRangeSector", query = "select o from GestionPayroll o where o.initDate>=:initDate and " +
                        " o.jobCategory.sector=:sector"),
                @NamedQuery(name = "GestionPayroll.findByEndRangeSector", query = "select o from GestionPayroll o where o.endDate<=:endDate and " +
                        " o.jobCategory.sector=:sector"),
                @NamedQuery(name = "GestionPayroll.findByInitRange", query = "select o from GestionPayroll o where o.initDate>=:initDate "),
                @NamedQuery(name = "GestionPayroll.findByEndRange", query = "select o from GestionPayroll o where o.endDate<=:endDate "),
                @NamedQuery(name = "GestionPayroll.findBySector", query = "select o from GestionPayroll o where " +
                        " o.jobCategory.sector=:sector"),
                @NamedQuery(name = "GestionPayroll.findGestionPayrollById", query = "select o from GestionPayroll" +
                        " o where o.id =:id"),
                @NamedQuery(name = "GestionPayroll.findValidGestionPayrolls", query = "select distinct o.gestionPayroll from GeneratedPayroll o" +
                        " where o.gestionPayroll.gestion=:gestion and o.gestionPayroll.month=:month" +
                        " and o.generatedPayrollType=:generatedPayrollType"),
                @NamedQuery(name = "GestionPayroll.countValidGestionPayrolls", query = "select count(distinct o.gestionPayroll) from GeneratedPayroll o" +
                        " where o.gestionPayroll.gestion=:gestion and o.gestionPayroll.month=:month" +
                        " and o.generatedPayrollType=:generatedPayrollType"),
                @NamedQuery(name = "GestionPayroll.findGestionPayrolls", query = "select o from GestionPayroll o where o.gestion=:gestion and o.month=:month"),
                @NamedQuery(name = "GestionPayroll.findGestionPayrollByGestionAndBusinessUnitAndMonth",
                        query = "select o from GestionPayroll o where o.gestion=:gestion and o.month=:month and o.businessUnit=:businessUnit"),
                @NamedQuery(name = "GestionPayroll.findGestionPayrollByGestionAndBusinessUnitAndMonthAndJobCategory",
                        query = "select o from GestionPayroll o where o.gestion=:gestion and o.month=:month " +
                                " and o.businessUnit=:businessUnit and o.jobCategory=:jobCategory"),
                @NamedQuery(name = "GestionPayroll.countByGestionAndBusinessUnitAndJobCategoryAndType",
                        query = "select count(gestionPayroll.id) from GestionPayroll gestionPayroll " +
                                "where gestionPayroll.gestion=:gestion and gestionPayroll.businessUnit=:businessUnit " +
                                "and gestionPayroll.jobCategory=:jobCategory and gestionPayroll.gestionPayrollType=:gestionPayrollType "),
                @NamedQuery(name = "GestionPayroll.countByGestionAndBusinessUnitAndJobCategoryAndTypeNotInIdList",
                        query = "select count(gestionPayroll.id) from GestionPayroll gestionPayroll " +
                                "where gestionPayroll.gestion=:gestion and gestionPayroll.businessUnit=:businessUnit " +
                                "and gestionPayroll.jobCategory=:jobCategory and gestionPayroll.gestionPayrollType=:gestionPayrollType " +
                                "and gestionPayroll.id not in (:idList) "),
                @NamedQuery(name = "GestionPayroll.countByBusinessUnitAndGestionAndMonthAndJobCategoryAndTypeAndNotInList",
                        query = "select count(gestionPayroll.id) from GestionPayroll gestionPayroll " +
                                "where gestionPayroll.gestion=:gestion and gestionPayroll.businessUnit=:businessUnit " +
                                "and gestionPayroll.month=:month and gestionPayroll.jobCategory=:jobCategory " +
                                "and gestionPayroll.gestionPayrollType=:gestionPayrollType and gestionPayroll.id not in (:idList)"),
                @NamedQuery(name = "GestionPayroll.countByBusinessUnitAndGestionAndMonthAndJobCategoryAndType",
                        query = "select count(gestionPayroll.id) from GestionPayroll gestionPayroll " +
                                "where gestionPayroll.gestion=:gestion and gestionPayroll.businessUnit=:businessUnit " +
                                "and gestionPayroll.month=:month and gestionPayroll.jobCategory=:jobCategory " +
                                "and gestionPayroll.gestionPayrollType=:gestionPayrollType "),
                @NamedQuery(name = "GestionPayroll.countGestionPayrolls", query = "select count(o) from GestionPayroll o where o.gestion=:gestion and o.month=:month"),
                @NamedQuery(name = "GestionPayroll.countByGeneratedPayrollType", query = "select count(o) from GeneratedPayroll o" +
                        " where o.gestionPayroll=:gestionPayroll and o.generatedPayrollType=:generatedPayrollType"),
                @NamedQuery(name = "GestionPayroll.findExchangeRateFromGestionPayroll", query = "select distinct o.exchangeRate from GestionPayroll o where o.gestion=:gestion and o.month=:month"),
                @NamedQuery(name = "GestionPayroll.sumManagersLiquidForBankAccount", query = "select sum(mp.liquid)" +
                        " from ManagersPayroll mp " +
                        " join mp.employee employee" +
                        " join employee.bankAccountList  bankAccount" +
                        " where mp.generatedPayroll.gestionPayroll.gestion=:gestion and mp.generatedPayroll.gestionPayroll.month=:month" +
                        " and mp.generatedPayroll.generatedPayrollType=:generatedPayrollType and employee.paymentType=:paymentType" +
                        " and bankAccount is not null and bankAccount.defaultAccount=:defaultAccount" +
                        " and bankAccount.currency.id=:currencyId"),
                @NamedQuery(name = "GestionPayroll.sumManagersLiquidForCheck", query = "select sum(mp.liquid)" +
                        " from ManagersPayroll mp " +
                        " join mp.employee employee" +
                        " left join employee.bankAccountList  bankAccount" +
                        " where mp.generatedPayroll.gestionPayroll.gestion=:gestion and mp.generatedPayroll.gestionPayroll.month=:month" +
                        " and mp.generatedPayroll.generatedPayrollType=:generatedPayrollType" +
                        " and (employee.paymentType=:paymentType or (employee.paymentType<>:paymentType and (bankAccount is null or bankAccount.defaultAccount<>:defaultAccount)))"),
                @NamedQuery(name = "GestionPayroll.findManagersLiquidForCheck", query = "select distinct mp.liquid,employee" +
                        " from ManagersPayroll mp " +
                        " join mp.employee employee" +
                        " left join employee.bankAccountList  bankAccount, ControlReport cr" +
                        " where mp.generatedPayroll.gestionPayroll.gestion=:gestion and mp.generatedPayroll.gestionPayroll.month=:month" +
                        " and mp.generatedPayroll.generatedPayrollType=:generatedPayrollType" +
                        " and (employee.paymentType=:paymentType or (employee.paymentType<>:paymentType and (bankAccount is null or bankAccount.defaultAccount<>:defaultAccount)))" +
                        " and cr.generatedPayroll = cr.generatedPayroll and cr.horaryBandContract.jobContract.contract.employee=employee" +
                        " and cr.horaryBandContract.jobContract.job.salary.currency.id=:currencyId"),
                @NamedQuery(name = "GestionPayroll.sumProffesorsLiquidForBankAccount", query = "select sum(mp.liquid)" +
                        " from GeneralPayroll mp " +
                        " join mp.employee employee" +
                        " join employee.bankAccountList  bankAccount" +
                        " where mp.generatedPayroll.gestionPayroll.gestion=:gestion and mp.generatedPayroll.gestionPayroll.month=:month" +
                        " and mp.generatedPayroll.generatedPayrollType=:generatedPayrollType and employee.paymentType=:paymentType" +
                        " and bankAccount is not null and bankAccount.defaultAccount=:defaultAccount" +
                        " and bankAccount.currency.id=:currencyId"),
                @NamedQuery(name = "GestionPayroll.sumProffesorsLiquidForCheck", query = "select sum(mp.liquid)" +
                        " from GeneralPayroll mp " +
                        " join mp.employee employee" +
                        " left join employee.bankAccountList  bankAccount" +
                        " where mp.generatedPayroll.gestionPayroll.gestion=:gestion and mp.generatedPayroll.gestionPayroll.month=:month" +
                        " and mp.generatedPayroll.generatedPayrollType=:generatedPayrollType" +
                        " and (employee.paymentType=:paymentType or (employee.paymentType<>:paymentType and (bankAccount is null or bankAccount.defaultAccount<>:defaultAccount)))"),
                @NamedQuery(name = "GestionPayroll.findProffesorsLiquidForCheck", query = "select distinct mp.liquid,employee" +
                        " from GeneralPayroll mp " +
                        " join mp.employee employee" +
                        " left join employee.bankAccountList  bankAccount, ControlReport cr" +
                        " where mp.generatedPayroll.gestionPayroll.gestion=:gestion and mp.generatedPayroll.gestionPayroll.month=:month" +
                        " and mp.generatedPayroll.generatedPayrollType=:generatedPayrollType" +
                        " and (employee.paymentType=:paymentType or (employee.paymentType<>:paymentType and (bankAccount is null or bankAccount.defaultAccount<>:defaultAccount)))" +
                        " and cr.generatedPayroll=mp.generatedPayroll and cr.horaryBandContract.jobContract.contract.employee=employee" +
                        " and cr.horaryBandContract.jobContract.job.salary.currency.id=:currencyId"),
                @NamedQuery(name = "GestionPayroll.findAvailableGestionPayroll", query = "select o from GestionPayroll o where o.generationBeginning>=:date and o.officialPayrollDeadline<=:date "),
                @NamedQuery(name = "GestionPayroll.countGeneratedPayrolls",
                        query = "select count(o) from GeneratedPayroll o where o.gestionPayroll=:gestionPayroll"),
                @NamedQuery(name = "GestionPayroll.countByPayrollGenerationCycle",
                        query = "select count(element) from GestionPayroll element where element.payrollGenerationCycle=:payrollGenerationCycle"),
                @NamedQuery(name = "GestionPayroll.countGeneratedPayrollByPayrollGenerationCycle",
                        query = "select count(element) from GeneratedPayroll element" +
                                " left join element.gestionPayroll gestionPayroll" +
                                " left join gestionPayroll.payrollGenerationCycle payrollGenerationCycle" +
                                " where payrollGenerationCycle=:payrollGenerationCycle"),
                @NamedQuery(name = "GestionPayroll.countGeneratedPayrollByTypeAndPayrollGenerationCycle",
                        query = "select count(element) from GeneratedPayroll element" +
                                " left join element.gestionPayroll gestionPayroll" +
                                " left join gestionPayroll.payrollGenerationCycle payrollGenerationCycle" +
                                " where payrollGenerationCycle=:payrollGenerationCycle" +
                                " and element.generatedPayrollType=:generatedPayrollType"),
                @NamedQuery(name = "GestionPayroll.countGeneratedPayrollByTypeBusinessUnitAndJobCategory",
                        query = "select count(element) from GeneratedPayroll element" +
                                " left join element.gestionPayroll gestionPayroll" +
                                " left join gestionPayroll.payrollGenerationCycle payrollGenerationCycle" +
                                " left join gestionPayroll.businessUnit businessUnit" +
                                " left join gestionPayroll.jobCategory jobCategory" +
                                " where payrollGenerationCycle=:payrollGenerationCycle" +
                                " and businessUnit=:businessUnit and jobCategory=:jobCategory" +
                                " and element.generatedPayrollType=:generatedPayrollType"),
                @NamedQuery(name = "GestionPayroll.countByPayrollGenerationCycleByGenerationType",
                        query = "select count(element) from GestionPayroll element" +
                                " left join element.jobCategory jobCategory" +
                                " where element.payrollGenerationCycle=:payrollGenerationCycle" +
                                " and jobCategory.payrollGenerationType in (:payrollGenerationTypeList)"),
                @NamedQuery(name = "GestionPayroll.countGeneratedPayrollByGenerationType",
                        query = "select count(element) from GeneratedPayroll element" +
                                " left join element.gestionPayroll gestionPayroll" +
                                " left join gestionPayroll.payrollGenerationCycle payrollGenerationCycle" +
                                " left join gestionPayroll.jobCategory jobCategory" +
                                " where element.generatedPayrollType=:generatedPayrollType" +
                                " and payrollGenerationCycle=:payrollGenerationCycle" +
                                " and jobCategory.payrollGenerationType in (:payrollGenerationTypeList)")
        }
)

@Entity
@Filter(name = com.encens.khipus.util.Constants.COMPANY_FILTER_NAME)
@EntityListeners({CompanyListener.class, UpperCaseStringListener.class})
@Table(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "gestionplanilla", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"idcompania", "nombregestion"}),
        @UniqueConstraint(columnNames = {"idcompania", "idunidadnegocio", "idgestion", "mes", "idcategoriapuesto", "fechainicio", "fechafin"})
})

public class GestionPayroll implements BaseModel {

    @Id
    @javax.persistence.Column(name = "idgestionplanilla", nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "GestionPayroll.tableGenerator")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH}, optional = false)
    @JoinColumn(name = "idtipocambio", nullable = false)
    private ExchangeRate exchangeRate;

    @OneToMany(mappedBy = "gestionPayroll", fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
    @Filter(name = com.encens.khipus.util.Constants.COMPANY_FILTER_NAME)
    private List<GeneratedPayroll> generatedPayrollList = new ArrayList<GeneratedPayroll>(0);

    @Column(name = "nombregestion", nullable = false, length = 200)
    @Length(max = 200)
    private String gestionName;

    @Column(name = "mes", length = 20)
    @Enumerated(EnumType.STRING)
    private Month month;

    @Column(name = "fechainicio")
    @Temporal(TemporalType.DATE)
    private Date initDate;

    @Column(name = "fechafin")
    @Temporal(TemporalType.DATE)
    private Date endDate;

    @Column(name = "fechaaperturagen")
    @Temporal(TemporalType.DATE)
    private Date generationBeginning;

    @Column(name = "fechalimitegen")
    @Temporal(TemporalType.DATE)
    private Date generationDeadline;

    @Column(name = "fechaplanoficial")
    @Temporal(TemporalType.DATE)
    private Date officialPayrollDeadline;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idgestion", nullable = false, updatable = false, insertable = true)
    private Gestion gestion;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idunidadnegocio", nullable = false, updatable = false, insertable = true)
    private BusinessUnit businessUnit;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idcategoriapuesto", nullable = false, updatable = false, insertable = true)
    private JobCategory jobCategory;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IDCICLOGENERACIONPLANILLA", referencedColumnName = "IDCICLOGENERACIONPLANILLA")
    private PayrollGenerationCycle payrollGenerationCycle;

    @Column(name = "tipo", nullable = false, length = 20)
    @NotNull
    @Enumerated(EnumType.STRING)
    private GestionPayrollType gestionPayrollType;

    @Version
    @Column(name = "version", nullable = false)
    private long version;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idcompania", nullable = false, updatable = false, insertable = true)
    private Company company;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Month getMonth() {
        return month;
    }

    public void setMonth(Month month) {
        this.month = month;
    }

    public Date getInitDate() {
        return initDate;
    }

    public void setInitDate(Date initDate) {
        this.initDate = initDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public Date getGenerationBeginning() {
        return generationBeginning;
    }

    public void setGenerationBeginning(Date generationBeginning) {
        this.generationBeginning = generationBeginning;
    }

    public Date getGenerationDeadline() {
        return generationDeadline;
    }

    public void setGenerationDeadline(Date generationDeadline) {
        this.generationDeadline = generationDeadline;
    }

    public Date getOfficialPayrollDeadline() {
        return officialPayrollDeadline;
    }

    public void setOfficialPayrollDeadline(Date officialPayrollDeadline) {
        this.officialPayrollDeadline = officialPayrollDeadline;
    }

    public List<GeneratedPayroll> getGeneratedPayrollList() {
        return generatedPayrollList;
    }

    public void setGeneratedPayrollList(List<GeneratedPayroll> generatedPayrollList) {
        this.generatedPayrollList = generatedPayrollList;
    }

    public String getGestionName() {
        return gestionName;
    }

    public void setGestionName(String gestionName) {
        this.gestionName = gestionName;
    }

    public JobCategory getJobCategory() {
        return jobCategory;
    }

    public void setJobCategory(JobCategory jobCategory) {
        this.jobCategory = jobCategory;
    }

    public ExchangeRate getExchangeRate() {
        return exchangeRate;
    }

    public void setExchangeRate(ExchangeRate exchangeRate) {
        this.exchangeRate = exchangeRate;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public String getGestionPayroll() {
        return month + "-" + gestion.getYear();
    }

    public BusinessUnit getBusinessUnit() {
        return businessUnit;
    }

    public void setBusinessUnit(BusinessUnit businessUnit) {
        this.businessUnit = businessUnit;
    }

    public Gestion getGestion() {
        return gestion;
    }

    public void setGestion(Gestion gestion) {
        this.gestion = gestion;
    }

    public GestionPayrollType getGestionPayrollType() {
        return gestionPayrollType;
    }

    public void setGestionPayrollType(GestionPayrollType gestionPayrollType) {
        this.gestionPayrollType = gestionPayrollType;
    }

    public PayrollGenerationCycle getPayrollGenerationCycle() {
        return payrollGenerationCycle;
    }

    public void setPayrollGenerationCycle(PayrollGenerationCycle payrollGenerationCycle) {
        this.payrollGenerationCycle = payrollGenerationCycle;
    }

    public boolean isSalaryType() {
        return null != getGestionPayrollType() &&
                getGestionPayrollType().equals(GestionPayrollType.SALARY);
    }

    public boolean isChristmasBonusType() {
        return null != getGestionPayrollType() &&
                getGestionPayrollType().equals(GestionPayrollType.CHRISTMAS_BONUS);
    }

    @Override
    public String toString() {
        return "GestionPayroll{" +
                "id=" + id +
                ", exchangeRate=" + exchangeRate +
                ", generatedPayrollList=" + generatedPayrollList +
                ", gestionName='" + gestionName + '\'' +
                ", month=" + month +
                ", initDate=" + initDate +
                ", endDate=" + endDate +
                ", generationBeginning=" + generationBeginning +
                ", generationDeadline=" + generationDeadline +
                ", officialPayrollDeadline=" + officialPayrollDeadline +
                ", gestion=" + gestion +
                ", businessUnit=" + businessUnit +
                ", jobCategory=" + jobCategory +
                ", version=" + version +
                ", company=" + company +
                '}';
    }
}