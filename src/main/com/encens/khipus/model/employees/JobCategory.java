package com.encens.khipus.model.employees;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyListener;
import com.encens.khipus.model.UpperCaseStringListener;
import com.encens.khipus.model.admin.Company;
import com.encens.khipus.model.finances.CashAccount;
import com.encens.khipus.model.finances.Job;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.Type;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity for JobCategory
 *
 * @author
 */

@NamedQueries(
        {
                @NamedQuery(name = "JobCategory.findJobCategory", query = "select o from JobCategory o where o.id=:id"),
                @NamedQuery(name = "JobCategory.findActiveJobCategory", query = "select o from JobCategory o where o.active=:active"),
                @NamedQuery(name = "JobCategory.countActiveJobCategory", query = "select count(o) from JobCategory o where o.active=:active"),
                @NamedQuery(name = "JobCategory.findBySector", query = "select o from JobCategory o where o.sector=:sector"),
                @NamedQuery(name = "JobCategory.countBySector", query = "select count(o) from JobCategory o where o.sector=:sector")
        }
)

@TableGenerator(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "JobCategory.tableGenerator",
        table = com.encens.khipus.util.Constants.SEQUENCE_TABLE_NAME,
        pkColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_PK_COLUMN_NAME,
        valueColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_VALUE_COLUMN_NAME,
        pkColumnValue = "categoriapuesto",
        allocationSize = com.encens.khipus.util.Constants.SEQUENCE_ALLOCATION_SIZE)

@Entity
@Filter(name = com.encens.khipus.util.Constants.COMPANY_FILTER_NAME)
@EntityListeners({CompanyListener.class, UpperCaseStringListener.class})
@Table(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "categoriapuesto", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"idcompania", "nombre"}),
        @UniqueConstraint(columnNames = {"idcompania", "sigla"})
})
public class JobCategory implements BaseModel {

    @Id
    @Column(name = "idcategoriapuesto", nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "JobCategory.tableGenerator")
    private Long id;

    @OneToMany(mappedBy = "jobCategory", fetch = FetchType.LAZY)
    @Filter(name = com.encens.khipus.util.Constants.COMPANY_FILTER_NAME)
    private List<Job> jobList = new ArrayList<Job>(0);

    @OneToMany(mappedBy = "jobCategory", fetch = FetchType.LAZY)
    @Filter(name = com.encens.khipus.util.Constants.COMPANY_FILTER_NAME)
    private List<GestionPayroll> gestionPayrollList = new ArrayList<GestionPayroll>(0);

    @Column(name = "nombre", nullable = false, length = 200)
    @Length(max = 200)
    private String name;

    @Column(name = "sigla", nullable = false, length = 200)
    @Length(max = 200)
    private String acronym;

    @Column(name = "descripcion", nullable = true, length = 200)
    @Length(max = 200)
    private String description;

    @Column(name = "idsector", updatable = false, insertable = false)
    private Long sectorId;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idsector", nullable = false)
    private Sector sector;

    @Column(name = "codctactbdebe", nullable = false, length = 50)
    @Length(max = 50)
    private String nationalCurrencyDebitAccountCode;

    @Column(name = "codctactbhaber", nullable = false, length = 50)
    @Length(max = 50)
    private String nationalCurrencyCreditAccountCode;

    @Column(name = "codctactbdebeme", nullable = false, length = 50)
    @Length(max = 50)
    private String foreignCurrencyDebitAccountCode;

    @Column(name = "codctactbhaberme", nullable = false, length = 50)
    @Length(max = 50)
    private String foreignCurrencyCreditAccountCode;

    @Column(name = "codctagastoaguimn", nullable = false, length = 50)
    @Length(max = 50)
    private String nationalCurrencyChristmasExpendAccountCode;

    @Column(name = "codctaprovaguimn", nullable = false, length = 50)
    @Length(max = 50)
    private String nationalCurrencyChristmasProvisionAccountCode;

    @Column(name = "codctaprovaguime", nullable = false, length = 50)
    @Length(max = 50)
    private String foreignCurrencyChristmasProvisionAccountCode;

    @Column(name = "codctagastoindemmn", nullable = false, length = 50)
    @Length(max = 50)
    private String nationalCurrencyCompensationExpendAccountCode;

    @Column(name = "codctaprevindemmn", nullable = false, length = 50)
    @Length(max = 50)
    private String nationalCurrencyCompensationPrevisionAccountCode;

    @Column(name = "codctaprevindemme", nullable = false, length = 50)
    @Length(max = 50)
    private String foreignCurrencyCompensationPrevisionAccountCode;

    @Column(name = "fonpenctapatronal", nullable = false, length = 50)
    @Length(max = 50)
    private String pensionFundPatronalAccountCode;

    @Column(name = "segsocctapatronal", nullable = false, length = 50)
    @Length(max = 50)
    private String socialSecurityPatronalAccountCode;

    @Column(name = "numerocompania", updatable = false)
    @Length(max = 2)
    private String companyNumber;

    @ManyToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "numerocompania", referencedColumnName = "no_cia", nullable = false, updatable = false, insertable = false),
            @JoinColumn(name = "codctactbdebe", referencedColumnName = "cuenta", nullable = false, updatable = false, insertable = false)
    })
    private CashAccount nationalCurrencyDebitAccount;

    @ManyToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "numerocompania", referencedColumnName = "no_cia", nullable = false, updatable = false, insertable = false),
            @JoinColumn(name = "codctactbhaber", referencedColumnName = "cuenta", nullable = false, updatable = false, insertable = false)
    })
    private CashAccount nationalCurrencyCreditAccount;

    @ManyToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "numerocompania", referencedColumnName = "no_cia", nullable = false, updatable = false, insertable = false),
            @JoinColumn(name = "codctactbdebeme", referencedColumnName = "cuenta", nullable = false, updatable = false, insertable = false)
    })
    private CashAccount foreignCurrencyDebitAccount;

    @ManyToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "numerocompania", referencedColumnName = "no_cia", nullable = false, updatable = false, insertable = false),
            @JoinColumn(name = "codctactbhaberme", referencedColumnName = "cuenta", nullable = false, updatable = false, insertable = false)
    })
    private CashAccount foreignCurrencyCreditAccount;

    @ManyToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "numerocompania", referencedColumnName = "no_cia", nullable = false, updatable = false, insertable = false),
            @JoinColumn(name = "codctagastoaguimn", referencedColumnName = "cuenta", nullable = false, updatable = false, insertable = false)
    })
    private CashAccount nationalCurrencyChristmasExpendAccount;

    @ManyToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "numerocompania", referencedColumnName = "no_cia", nullable = false, updatable = false, insertable = false),
            @JoinColumn(name = "codctaprovaguimn", referencedColumnName = "cuenta", nullable = false, updatable = false, insertable = false)
    })
    private CashAccount nationalCurrencyChristmasProvisionAccount;

    @ManyToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "numerocompania", referencedColumnName = "no_cia", nullable = false, updatable = false, insertable = false),
            @JoinColumn(name = "codctaprovaguime", referencedColumnName = "cuenta", nullable = false, updatable = false, insertable = false)
    })
    private CashAccount foreignCurrencyChristmasProvisionAccount;

    @ManyToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "numerocompania", referencedColumnName = "no_cia", nullable = false, updatable = false, insertable = false),
            @JoinColumn(name = "codctagastoindemmn", referencedColumnName = "cuenta", nullable = false, updatable = false, insertable = false)
    })
    private CashAccount nationalCurrencyCompensationExpendAccount;

    @ManyToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "numerocompania", referencedColumnName = "no_cia", nullable = false, updatable = false, insertable = false),
            @JoinColumn(name = "codctaprevindemmn", referencedColumnName = "cuenta", nullable = false, updatable = false, insertable = false)
    })
    private CashAccount nationalCurrencyCompensationPrevisionAccount;

    @ManyToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "numerocompania", referencedColumnName = "no_cia", nullable = false, updatable = false, insertable = false),
            @JoinColumn(name = "codctaprevindemme", referencedColumnName = "cuenta", nullable = false, updatable = false, insertable = false)
    })
    private CashAccount foreignCurrencyCompensationPrevisionAccount;

    @ManyToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "numerocompania", referencedColumnName = "no_cia", nullable = false, updatable = false, insertable = false),
            @JoinColumn(name = "fonpenctapatronal", referencedColumnName = "cuenta", nullable = false, updatable = false, insertable = false)
    })
    private CashAccount pensionFundPatronalAccount;

    @ManyToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "numerocompania", referencedColumnName = "no_cia", nullable = false, updatable = false, insertable = false),
            @JoinColumn(name = "segsocctapatronal", referencedColumnName = "cuenta", nullable = false, updatable = false, insertable = false)
    })
    private CashAccount socialSecurityPatronalAccount;

    @Column(name = "activo", nullable = false)
    @Type(type = com.encens.khipus.model.usertype.IntegerBooleanUserType.NAME)
    private Boolean active;

    @Column(name = "POSICION")
    private Integer position;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idtolerancia", nullable = false, updatable = false, insertable = true)
    private Tolerance tolerance;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idlimite", nullable = false, updatable = false, insertable = true)
    private Limit limit;

    @Column(name = "tipogeneracion", length = 30, nullable = false)
    @Enumerated(EnumType.STRING)
    private PayrollGenerationType payrollGenerationType;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idcompania", nullable = false, updatable = false, insertable = true)
    @NotNull
    private Company company;

    @Version
    @Column(name = "version", nullable = false)
    private long version;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }


    public List<Job> getJobList() {
        return jobList;
    }

    public void setJobList(List<Job> jobList) {
        this.jobList = jobList;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAcronym() {
        return acronym;
    }

    public void setAcronym(String acronym) {
        this.acronym = acronym;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public List<GestionPayroll> getGestionPayrollList() {
        return gestionPayrollList;
    }

    public void setGestionPayrollList(List<GestionPayroll> gestionPayrollList) {
        this.gestionPayrollList = gestionPayrollList;
    }

    public Sector getSector() {
        return sector;
    }

    public void setSector(Sector sector) {
        this.sector = sector;
    }

    public String getFullName() {
        return getAcronym() + " " + getName();
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public String getNationalCurrencyDebitAccountCode() {
        return nationalCurrencyDebitAccountCode;
    }

    public void setNationalCurrencyDebitAccountCode(String nationalCurrencyDebitAccountCode) {
        this.nationalCurrencyDebitAccountCode = nationalCurrencyDebitAccountCode;
    }

    public String getNationalCurrencyCreditAccountCode() {
        return nationalCurrencyCreditAccountCode;
    }

    public void setNationalCurrencyCreditAccountCode(String nationalCurrencyCreditAccountCode) {
        this.nationalCurrencyCreditAccountCode = nationalCurrencyCreditAccountCode;
    }

    public String getForeignCurrencyDebitAccountCode() {
        return foreignCurrencyDebitAccountCode;
    }

    public void setForeignCurrencyDebitAccountCode(String foreignCurrencyDebitAccountCode) {
        this.foreignCurrencyDebitAccountCode = foreignCurrencyDebitAccountCode;
    }

    public String getForeignCurrencyCreditAccountCode() {
        return foreignCurrencyCreditAccountCode;
    }

    public void setForeignCurrencyCreditAccountCode(String foreignCurrencyCreditAccountCode) {
        this.foreignCurrencyCreditAccountCode = foreignCurrencyCreditAccountCode;
    }

    public String getNationalCurrencyChristmasExpendAccountCode() {
        return nationalCurrencyChristmasExpendAccountCode;
    }

    public void setNationalCurrencyChristmasExpendAccountCode(String nationalCurrencyChristmasExpendAccountCode) {
        this.nationalCurrencyChristmasExpendAccountCode = nationalCurrencyChristmasExpendAccountCode;
    }

    public String getNationalCurrencyChristmasProvisionAccountCode() {
        return nationalCurrencyChristmasProvisionAccountCode;
    }

    public void setNationalCurrencyChristmasProvisionAccountCode(String nationalCurrencyChristmasProvisionAccountCode) {
        this.nationalCurrencyChristmasProvisionAccountCode = nationalCurrencyChristmasProvisionAccountCode;
    }

    public String getForeignCurrencyChristmasProvisionAccountCode() {
        return foreignCurrencyChristmasProvisionAccountCode;
    }

    public void setForeignCurrencyChristmasProvisionAccountCode(String foreignCurrencyChristmasProvisionAccountCode) {
        this.foreignCurrencyChristmasProvisionAccountCode = foreignCurrencyChristmasProvisionAccountCode;
    }

    public String getNationalCurrencyCompensationExpendAccountCode() {
        return nationalCurrencyCompensationExpendAccountCode;
    }

    public void setNationalCurrencyCompensationExpendAccountCode(String nationalCurrencyCompensationExpendAccountCode) {
        this.nationalCurrencyCompensationExpendAccountCode = nationalCurrencyCompensationExpendAccountCode;
    }

    public String getNationalCurrencyCompensationPrevisionAccountCode() {
        return nationalCurrencyCompensationPrevisionAccountCode;
    }

    public void setNationalCurrencyCompensationPrevisionAccountCode(String nationalCurrencyCompensationPrevisionAccountCode) {
        this.nationalCurrencyCompensationPrevisionAccountCode = nationalCurrencyCompensationPrevisionAccountCode;
    }

    public String getForeignCurrencyCompensationPrevisionAccountCode() {
        return foreignCurrencyCompensationPrevisionAccountCode;
    }

    public void setForeignCurrencyCompensationPrevisionAccountCode(String foreignCurrencyCompensationPrevisionAccountCode) {
        this.foreignCurrencyCompensationPrevisionAccountCode = foreignCurrencyCompensationPrevisionAccountCode;
    }

    public String getPensionFundPatronalAccountCode() {
        return pensionFundPatronalAccountCode;
    }

    public void setPensionFundPatronalAccountCode(String pensionFundPatronalAccountCode) {
        this.pensionFundPatronalAccountCode = pensionFundPatronalAccountCode;
    }

    public String getSocialSecurityPatronalAccountCode() {
        return socialSecurityPatronalAccountCode;
    }

    public void setSocialSecurityPatronalAccountCode(String socialSecurityPatronalAccountCode) {
        this.socialSecurityPatronalAccountCode = socialSecurityPatronalAccountCode;
    }

    public String getCompanyNumber() {
        return companyNumber;
    }

    public void setCompanyNumber(String companyNumber) {
        this.companyNumber = companyNumber;
    }

    public CashAccount getNationalCurrencyDebitAccount() {
        return nationalCurrencyDebitAccount;
    }

    public void setNationalCurrencyDebitAccount(CashAccount nationalCurrencyDebitAccount) {
        this.nationalCurrencyDebitAccount = nationalCurrencyDebitAccount;
        setCompanyNumber(nationalCurrencyDebitAccount != null ? nationalCurrencyDebitAccount.getCompanyNumber() : null);
        setNationalCurrencyDebitAccountCode(nationalCurrencyDebitAccount != null ? nationalCurrencyDebitAccount.getAccountCode() : null);
    }

    public CashAccount getNationalCurrencyCreditAccount() {
        return nationalCurrencyCreditAccount;
    }

    public void setNationalCurrencyCreditAccount(CashAccount nationalCurrencyCreditAccount) {
        this.nationalCurrencyCreditAccount = nationalCurrencyCreditAccount;
        setCompanyNumber(nationalCurrencyCreditAccount != null ? nationalCurrencyCreditAccount.getCompanyNumber() : null);
        setNationalCurrencyCreditAccountCode(nationalCurrencyCreditAccount != null ? nationalCurrencyCreditAccount.getAccountCode() : null);
    }

    public CashAccount getForeignCurrencyDebitAccount() {
        return foreignCurrencyDebitAccount;
    }

    public void setForeignCurrencyDebitAccount(CashAccount foreignCurrencyDebitAccount) {
        this.foreignCurrencyDebitAccount = foreignCurrencyDebitAccount;
        setCompanyNumber(foreignCurrencyDebitAccount != null ? foreignCurrencyDebitAccount.getCompanyNumber() : null);
        setForeignCurrencyDebitAccountCode(foreignCurrencyDebitAccount != null ? foreignCurrencyDebitAccount.getAccountCode() : null);
    }

    public CashAccount getForeignCurrencyCreditAccount() {
        return foreignCurrencyCreditAccount;
    }

    public void setForeignCurrencyCreditAccount(CashAccount foreignCurrencyCreditAccount) {
        this.foreignCurrencyCreditAccount = foreignCurrencyCreditAccount;
        setCompanyNumber(foreignCurrencyCreditAccount != null ? foreignCurrencyCreditAccount.getCompanyNumber() : null);
        setForeignCurrencyCreditAccountCode(foreignCurrencyCreditAccount != null ? foreignCurrencyCreditAccount.getAccountCode() : null);
    }

    public CashAccount getNationalCurrencyChristmasExpendAccount() {
        return nationalCurrencyChristmasExpendAccount;
    }

    public void setNationalCurrencyChristmasExpendAccount(CashAccount nationalCurrencyChristmasExpendAccount) {
        this.nationalCurrencyChristmasExpendAccount = nationalCurrencyChristmasExpendAccount;
        setCompanyNumber(this.nationalCurrencyChristmasExpendAccount != null ? this.nationalCurrencyChristmasExpendAccount.getCompanyNumber() : null);
        setNationalCurrencyChristmasExpendAccountCode(this.nationalCurrencyChristmasExpendAccount != null ? this.nationalCurrencyChristmasExpendAccount.getAccountCode() : null);
    }

    public CashAccount getNationalCurrencyChristmasProvisionAccount() {
        return nationalCurrencyChristmasProvisionAccount;
    }

    public void setNationalCurrencyChristmasProvisionAccount(CashAccount nationalCurrencyChristmasProvisionAccount) {
        this.nationalCurrencyChristmasProvisionAccount = nationalCurrencyChristmasProvisionAccount;
        setCompanyNumber(this.nationalCurrencyChristmasProvisionAccount != null ? this.nationalCurrencyChristmasProvisionAccount.getCompanyNumber() : null);
        setNationalCurrencyChristmasProvisionAccountCode(this.nationalCurrencyChristmasProvisionAccount != null ? this.nationalCurrencyChristmasProvisionAccount.getAccountCode() : null);
    }

    public CashAccount getForeignCurrencyChristmasProvisionAccount() {
        return foreignCurrencyChristmasProvisionAccount;
    }

    public void setForeignCurrencyChristmasProvisionAccount(CashAccount foreignCurrencyChristmasProvisionAccount) {
        this.foreignCurrencyChristmasProvisionAccount = foreignCurrencyChristmasProvisionAccount;
        setCompanyNumber(this.foreignCurrencyChristmasProvisionAccount != null ? this.foreignCurrencyChristmasProvisionAccount.getCompanyNumber() : null);
        setForeignCurrencyChristmasProvisionAccountCode(this.foreignCurrencyChristmasProvisionAccount != null ? this.foreignCurrencyChristmasProvisionAccount.getAccountCode() : null);
    }

    public CashAccount getNationalCurrencyCompensationExpendAccount() {
        return nationalCurrencyCompensationExpendAccount;
    }

    public void setNationalCurrencyCompensationExpendAccount(CashAccount nationalCurrencyCompensationExpendAccount) {
        this.nationalCurrencyCompensationExpendAccount = nationalCurrencyCompensationExpendAccount;
        setCompanyNumber(this.nationalCurrencyCompensationExpendAccount != null ? this.nationalCurrencyCompensationExpendAccount.getCompanyNumber() : null);
        setNationalCurrencyCompensationExpendAccountCode(this.nationalCurrencyCompensationExpendAccount != null ? this.nationalCurrencyCompensationExpendAccount.getAccountCode() : null);
    }

    public CashAccount getNationalCurrencyCompensationPrevisionAccount() {
        return nationalCurrencyCompensationPrevisionAccount;
    }

    public void setNationalCurrencyCompensationPrevisionAccount(CashAccount nationalCurrencyCompensationPrevisionAccount) {
        this.nationalCurrencyCompensationPrevisionAccount = nationalCurrencyCompensationPrevisionAccount;
        setCompanyNumber(this.nationalCurrencyCompensationPrevisionAccount != null ? this.nationalCurrencyCompensationPrevisionAccount.getCompanyNumber() : null);
        setNationalCurrencyCompensationPrevisionAccountCode(this.nationalCurrencyCompensationPrevisionAccount != null ? this.nationalCurrencyCompensationPrevisionAccount.getAccountCode() : null);
    }

    public CashAccount getForeignCurrencyCompensationPrevisionAccount() {
        return foreignCurrencyCompensationPrevisionAccount;
    }

    public void setForeignCurrencyCompensationPrevisionAccount(CashAccount foreignCurrencyCompensationPrevisionAccount) {
        this.foreignCurrencyCompensationPrevisionAccount = foreignCurrencyCompensationPrevisionAccount;
        setCompanyNumber(this.foreignCurrencyCompensationPrevisionAccount != null ? this.foreignCurrencyCompensationPrevisionAccount.getCompanyNumber() : null);
        setForeignCurrencyCompensationPrevisionAccountCode(this.foreignCurrencyCompensationPrevisionAccount != null ? this.foreignCurrencyCompensationPrevisionAccount.getAccountCode() : null);
    }

    public CashAccount getPensionFundPatronalAccount() {
        return pensionFundPatronalAccount;
    }

    public void setPensionFundPatronalAccount(CashAccount pensionFundPatronalAccount) {
        this.pensionFundPatronalAccount = pensionFundPatronalAccount;
        setCompanyNumber(this.pensionFundPatronalAccount != null ? this.pensionFundPatronalAccount.getCompanyNumber() : null);
        setPensionFundPatronalAccountCode(this.pensionFundPatronalAccount != null ? this.pensionFundPatronalAccount.getAccountCode() : null);
    }

    public CashAccount getSocialSecurityPatronalAccount() {
        return socialSecurityPatronalAccount;
    }

    public void setSocialSecurityPatronalAccount(CashAccount socialSecurityPatronalAccount) {
        this.socialSecurityPatronalAccount = socialSecurityPatronalAccount;
        setCompanyNumber(this.socialSecurityPatronalAccount != null ? this.socialSecurityPatronalAccount.getCompanyNumber() : null);
        setSocialSecurityPatronalAccountCode(this.socialSecurityPatronalAccount != null ? this.socialSecurityPatronalAccount.getAccountCode() : null);
    }

    public Integer getPosition() {
        return position;
    }

    public void setPosition(Integer position) {
        this.position = position;
    }

    public Tolerance getTolerance() {
        return tolerance;
    }

    public void setTolerance(Tolerance tolerance) {
        this.tolerance = tolerance;
    }

    public Limit getLimit() {
        return limit;
    }

    public void setLimit(Limit limit) {
        this.limit = limit;
    }

    public Long getSectorId() {
        return sectorId;
    }

    public void setSectorId(Long sectorId) {
        this.sectorId = sectorId;
    }

    public PayrollGenerationType getPayrollGenerationType() {
        return payrollGenerationType;
    }

    public void setPayrollGenerationType(PayrollGenerationType payrollGenerationType) {
        this.payrollGenerationType = payrollGenerationType;
    }

    @Override
    public String toString() {
        return "JobCategory{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", acronym='" + acronym + '\'' +
                ", description='" + description + '\'' +
                ", sector=" + sector +
                ", company=" + company +
                ", version=" + version +
                '}';
    }
}