package com.encens.khipus.model.employees;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyListener;
import com.encens.khipus.model.UpperCaseStringListener;
import com.encens.khipus.model.admin.BusinessUnit;
import com.encens.khipus.model.admin.Company;
import com.encens.khipus.model.finances.ExchangeRate;
import com.encens.khipus.util.Constants;
import org.hibernate.annotations.Filter;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * @author
 * @version 3.4
 */

@TableGenerator(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "PayrollGenerationCycle.tableGenerator",
        table = com.encens.khipus.util.Constants.SEQUENCE_TABLE_NAME,
        pkColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_PK_COLUMN_NAME,
        valueColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_VALUE_COLUMN_NAME,
        pkColumnValue = "CICLOGENERACIONPLANILLA",
        allocationSize = com.encens.khipus.util.Constants.SEQUENCE_ALLOCATION_SIZE)
@NamedQueries({
        @NamedQuery(name = "PayrollGenerationCycle.loadPayrollGenerationCycle",
                query = "select payrollGenerationCycle from PayrollGenerationCycle payrollGenerationCycle" +
                        " left join fetch payrollGenerationCycle.businessUnit businessUnit" +
                        " left join fetch businessUnit.organization organization" +
                        " left join fetch payrollGenerationCycle.afpRate afpRate" +
                        " left join fetch payrollGenerationCycle.cnsRate cnsRate" +
                        " left join fetch payrollGenerationCycle.exchangeRate exchangeRate" +
                        " left join fetch payrollGenerationCycle.gestion gestion" +
                        " left join fetch payrollGenerationCycle.ivaRate ivaRate" +
                        " where payrollGenerationCycle.id=:id"),
        @NamedQuery(name = "PayrollGenerationCycle.findByDates",
                query = "select payrollGenerationCycle from PayrollGenerationCycle payrollGenerationCycle " +
                        "where payrollGenerationCycle.startDate =:startDate and payrollGenerationCycle.endDate =:endDate " +
                        "and payrollGenerationCycle.businessUnit =:businessUnit"),
        @NamedQuery(name = "PayrollGenerationCycle.findByDatesAndMonth",
                query = "select payrollGenerationCycle from PayrollGenerationCycle payrollGenerationCycle " +
                        "where payrollGenerationCycle.startDate =:startDate and payrollGenerationCycle.endDate =:endDate " +
                        "and payrollGenerationCycle.month =:month and payrollGenerationCycle.businessUnit =:businessUnit"),
        @NamedQuery(name = "PayrollGenerationCycle.findByStartDate",
                query = "select payrollGenerationCycle from PayrollGenerationCycle payrollGenerationCycle " +
                        "where payrollGenerationCycle.businessUnit=:businessUnit and payrollGenerationCycle.startDate =:startDate "),
        @NamedQuery(name = "PayrollGenerationCycle.countByName",
                query = "select count(payrollGenerationCycle) from PayrollGenerationCycle payrollGenerationCycle " +
                        "where upper(payrollGenerationCycle.name) =upper(:name) "),
        @NamedQuery(name = "PayrollGenerationCycle.countByNameButThis",
                query = "select count(payrollGenerationCycle) from PayrollGenerationCycle payrollGenerationCycle " +
                        "where upper(payrollGenerationCycle.name) =upper(:name) and not payrollGenerationCycle.id=:id "),
        @NamedQuery(name = "PayrollGenerationCycle.countByBusinessUnitAndGestionAndMonth",
                query = "select count(payrollGenerationCycle) from PayrollGenerationCycle payrollGenerationCycle " +
                        "where payrollGenerationCycle.gestion=:gestion and payrollGenerationCycle.month=:month " +
                        "and payrollGenerationCycle.businessUnit=:businessUnit"),
        @NamedQuery(name = "PayrollGenerationCycle.countByBusinessUnitAndGestionAndMonthButThis",
                query = "select count(payrollGenerationCycle) from PayrollGenerationCycle payrollGenerationCycle " +
                        "where payrollGenerationCycle.gestion=:gestion and payrollGenerationCycle.month=:month " +
                        "and payrollGenerationCycle.businessUnit=:businessUnit and not payrollGenerationCycle.id=:id ")
})

@Entity
@EntityListeners({CompanyListener.class, UpperCaseStringListener.class})
@Filter(name = com.encens.khipus.util.Constants.COMPANY_FILTER_NAME)
@Table(name = "CICLOGENERACIONPLANILLA", schema = Constants.KHIPUS_SCHEMA, uniqueConstraints = {
        @UniqueConstraint(columnNames = {"IDUNIDADNEGOCIO", "IDGESTION", "MES", "IDCOMPANIA"}),
        @UniqueConstraint(columnNames = {"NOMBRE", "IDCOMPANIA"})
})
public class PayrollGenerationCycle implements BaseModel {
    @Id
    @Column(name = "IDCICLOGENERACIONPLANILLA", nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "PayrollGenerationCycle.tableGenerator")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "IDUNIDADNEGOCIO", nullable = false)
    @NotNull
    private BusinessUnit businessUnit;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "IDGESTION", nullable = false)
    @NotNull
    private Gestion gestion;

    @Column(name = "MES", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    @NotNull
    private Month month;

    @Column(name = "NOMBRE", nullable = false, length = 100)
    @NotNull
    @Length(max = 100)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH}, optional = false)
    @JoinColumn(name = "IDTIPOCAMBIO", nullable = false)
    @NotNull
    private ExchangeRate exchangeRate;

    @Column(name = "FECHAINICIOGEN", nullable = false)
    @NotNull
    @Temporal(TemporalType.DATE)
    private Date generationInitDate;

    @Column(name = "FECHAFINGEN", nullable = false)
    @NotNull
    @Temporal(TemporalType.DATE)
    private Date generationEndDate;

    @Column(name = "FECHAAPERTURAGEN", nullable = false)
    @NotNull
    @Temporal(TemporalType.DATE)
    private Date generationBeginning;

    @Column(name = "FECHALIMITEGEN", nullable = false)
    @NotNull
    @Temporal(TemporalType.DATE)
    private Date generationDeadline;

    @Column(name = "FECHAPLANOFICIAL", nullable = false)
    @NotNull
    @Temporal(TemporalType.DATE)
    private Date officialPayrollDeadline;

    @Column(name = "FECHAINICIOFISCAL", nullable = false)
    @Temporal(TemporalType.DATE)
    @NotNull
    private Date startDate;

    @Column(name = "FECHAFINFISCAL", nullable = false)
    @Temporal(TemporalType.DATE)
    @NotNull
    private Date endDate;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "IDTASAAFP")
    @NotNull
    private AFPRate afpRate;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "IDTASAAFPPROFRISK")
    @NotNull
    private AFPRate professionalRiskAfpRate;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "IDTASAAFPPROHOUS")
    @NotNull
    private AFPRate proHousingAfpRate;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "IDTASAAFPSOLIDARIO")
    @NotNull
    private AFPRate solidaryAfpRate;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "IDTASACNS")
    @NotNull
    private CNSRate cnsRate;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "IDTASAIVA")
    @NotNull
    private IVARate ivaRate;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "IDTASASMN")
    @NotNull
    private SMNRate smnRate;

    @Column(name = "FECHACREACION", nullable = false)
    @Temporal(TemporalType.DATE)
    @NotNull
    private Date creationDate;

    @Column(name = "TIPOCAMBIOINICIALUFV", precision = 16, scale = 6, nullable = false)
    @NotNull
    private BigDecimal initialUfvExchangeRate = BigDecimal.ZERO;

    @Column(name = "TIPOCAMBIOFINALUFV", precision = 16, scale = 6, nullable = false)
    @NotNull
    private BigDecimal finalUfvExchangeRate = BigDecimal.ZERO;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "IDREGLADESCUENTO", nullable = false)
    @NotNull
    private DiscountRule nationalSolidaryAfpDiscountRule;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "payrollGenerationCycle", cascade = {})
    private List<GestionPayroll> gestionPayrollList;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "payrollGenerationCycle")
    private List<ExtraHoursWorked> extraHoursWorkedList;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "payrollGenerationCycle")
    private List<GrantedBonus> grantedBonusList;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "payrollGenerationCycle")
    private List<InvoicesForm> invoicesFormList;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "IDCOMPANIA", nullable = false, updatable = false)
    private Company company;

    @Version
    @Column(name = "VERSION")
    private long version;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Month getMonth() {
        return month;
    }

    public void setMonth(Month month) {
        this.month = month;
    }

    public ExchangeRate getExchangeRate() {
        return exchangeRate;
    }

    public void setExchangeRate(ExchangeRate exchangeRate) {
        this.exchangeRate = exchangeRate;
    }

    public Date getGenerationInitDate() {
        return generationInitDate;
    }

    public void setGenerationInitDate(Date generationInitDate) {
        this.generationInitDate = generationInitDate;
    }

    public Date getGenerationEndDate() {
        return generationEndDate;
    }

    public void setGenerationEndDate(Date generationEndDate) {
        this.generationEndDate = generationEndDate;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public AFPRate getAfpRate() {
        return afpRate;
    }

    public void setAfpRate(AFPRate afpRate) {
        this.afpRate = afpRate;
    }

    public AFPRate getProfessionalRiskAfpRate() {
        return professionalRiskAfpRate;
    }

    public void setProfessionalRiskAfpRate(AFPRate professionalRiskAfpRate) {
        this.professionalRiskAfpRate = professionalRiskAfpRate;
    }

    public AFPRate getProHousingAfpRate() {
        return proHousingAfpRate;
    }

    public void setProHousingAfpRate(AFPRate proHousingAfpRate) {
        this.proHousingAfpRate = proHousingAfpRate;
    }

    public CNSRate getCnsRate() {
        return cnsRate;
    }

    public void setCnsRate(CNSRate cnsRate) {
        this.cnsRate = cnsRate;
    }

    public IVARate getIvaRate() {
        return ivaRate;
    }

    public void setIvaRate(IVARate ivaRate) {
        this.ivaRate = ivaRate;
    }

    public SMNRate getSmnRate() {
        return smnRate;
    }

    public void setSmnRate(SMNRate smnRate) {
        this.smnRate = smnRate;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public BigDecimal getInitialUfvExchangeRate() {
        return initialUfvExchangeRate;
    }

    public void setInitialUfvExchangeRate(BigDecimal initialUfvExchangeRate) {
        this.initialUfvExchangeRate = initialUfvExchangeRate;
    }

    public BigDecimal getFinalUfvExchangeRate() {
        return finalUfvExchangeRate;
    }

    public void setFinalUfvExchangeRate(BigDecimal finalUfvExchangeRate) {
        this.finalUfvExchangeRate = finalUfvExchangeRate;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public List<GestionPayroll> getGestionPayrollList() {
        return gestionPayrollList;
    }

    public void setGestionPayrollList(List<GestionPayroll> gestionPayrollList) {
        this.gestionPayrollList = gestionPayrollList;
    }

    public List<ExtraHoursWorked> getExtraHoursWorkedList() {
        return extraHoursWorkedList;
    }

    public void setExtraHoursWorkedList(List<ExtraHoursWorked> extraHoursWorkedList) {
        this.extraHoursWorkedList = extraHoursWorkedList;
    }

    public List<GrantedBonus> getGrantedBonusList() {
        return grantedBonusList;
    }

    public void setGrantedBonusList(List<GrantedBonus> grantedBonusList) {
        this.grantedBonusList = grantedBonusList;
    }

    public List<InvoicesForm> getInvoicesFormList() {
        return invoicesFormList;
    }

    public void setInvoicesFormList(List<InvoicesForm> invoicesFormList) {
        this.invoicesFormList = invoicesFormList;
    }

    public AFPRate getSolidaryAfpRate() {
        return solidaryAfpRate;
    }

    public void setSolidaryAfpRate(AFPRate solidaryAfpRate) {
        this.solidaryAfpRate = solidaryAfpRate;
    }

    public DiscountRule getNationalSolidaryAfpDiscountRule() {
        return nationalSolidaryAfpDiscountRule;
    }

    public void setNationalSolidaryAfpDiscountRule(DiscountRule nationalSolidaryAfpDiscountRule) {
        this.nationalSolidaryAfpDiscountRule = nationalSolidaryAfpDiscountRule;
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
}