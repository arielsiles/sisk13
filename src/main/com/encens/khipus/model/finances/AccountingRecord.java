package com.encens.khipus.model.finances;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyListener;
import com.encens.khipus.model.UpperCaseStringListener;
import com.encens.khipus.model.admin.BusinessUnit;
import com.encens.khipus.model.admin.Company;
import com.encens.khipus.model.admin.User;
import com.encens.khipus.model.common.Text;
import com.encens.khipus.model.employees.*;
import org.hibernate.annotations.Filter;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * AccountingRecord
 *
 * @author
 * @version 1.4
 */
@NamedQueries({
        @NamedQuery(name = "AccountingRecord.findByGestionAndMonth", query = "select a from AccountingRecord a" +
                " where a.gestion=:gestion and a.month=:month"),
        @NamedQuery(name = "AccountingRecord.countByGestionAndMonth", query = "select count(a) from AccountingRecord a" +
                " where a.gestion=:gestion and a.month=:month")
})

@TableGenerator(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "AccountingRecord.tableGenerator",
        table = com.encens.khipus.util.Constants.SEQUENCE_TABLE_NAME,
        pkColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_PK_COLUMN_NAME,
        valueColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_VALUE_COLUMN_NAME,
        pkColumnValue = "registrocontable",
        allocationSize = com.encens.khipus.util.Constants.SEQUENCE_ALLOCATION_SIZE)

@Entity
@Filter(name = com.encens.khipus.util.Constants.COMPANY_FILTER_NAME)
@EntityListeners({CompanyListener.class, UpperCaseStringListener.class})
@Table(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "registrocontable")
public class AccountingRecord implements BaseModel {
    @Id
    @Column(name = "idregistrocontable", nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "AccountingRecord.tableGenerator")
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idunidadnegocio", nullable = false, updatable = false)
    private BusinessUnit businessUnit;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idcategoriapuesto", nullable = false, updatable = false)
    private JobCategory jobCategory;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idgestion", nullable = false, updatable = false)
    @NotNull
    private Gestion gestion;

    @Column(name = "tipo", nullable = false, length = 20)
    @NotNull
    @Enumerated(EnumType.STRING)
    private GestionPayrollType gestionPayrollType;

    @Column(name = "mes", nullable = false, updatable = false, length = 20)
    @Enumerated(EnumType.STRING)
    @NotNull
    private Month month;

    @Column(name = "fecharegistro", nullable = false, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    @NotNull
    private Date recordDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idplanillagenerada", nullable = false)
    private GeneratedPayroll generatedPayroll;

    @ManyToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "numerocompania", referencedColumnName = "no_cia", updatable = false, insertable = false),
            @JoinColumn(name = "codigocencos", referencedColumnName = "cod_cc", updatable = false, insertable = false)
    })
    private CostCenter costCenter;

    @Column(name = "numerocompania", updatable = false)
    @Length(max = 2)
    private String companyNumber;

    @Column(name = "codigocencos", length = 6)
    @Length(max = 6)
    private String costCenterCode;

    @Column(name = "noidentificacion", length = 100)
    private String idNumber;

    @Column(name = "montominimo", precision = 13, scale = 2)
    private BigDecimal lesserAmount;

    @Column(name = "montomaxino", precision = 13, scale = 2)
    private BigDecimal higherAmount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idusuario", nullable = false, updatable = false)
    @NotNull
    private User recorderUser;

    @Column(name = "impmonnacbanco", precision = 13, scale = 2)
    private BigDecimal nationalAmountForBank;

    @Column(name = "impmonextbanco", precision = 13, scale = 2)
    private BigDecimal foreignAmountForBank;

    @Column(name = "impmonnaccheque", precision = 13, scale = 2)
    private BigDecimal nationalAmountForCheck;

    @Column(name = "impmonextcheque", precision = 13, scale = 2)
    private BigDecimal foreignAmountForCheck;

    @OneToOne(optional = true, fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "iddescripcion", nullable = true)
    private Text description;

    @Column(name = "codigoproveedor")
    private String providerCode;

    @Column(name = "tipodocumentocxp")
    private String documentTypeCode;

    @Column(name = "cuentaxpagar", length = 31)
    @Length(max = 31)
    private String payableAccountCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "numerocompania", referencedColumnName = "NO_CIA", nullable = false, insertable = false, updatable = false),
            @JoinColumn(name = "codigoproveedor", referencedColumnName = "COD_PROV", nullable = false, insertable = false, updatable = false)
    })
    private Provider provider;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "numerocompania", referencedColumnName = "NO_CIA", nullable = false, insertable = false, updatable = false),
            @JoinColumn(name = "tipodocumentocxp", referencedColumnName = "TIPO_DOC", nullable = false, insertable = false, updatable = false)
    })
    private PayableDocumentType documentType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "numerocompania", referencedColumnName = "NO_CIA", nullable = false, updatable = false, insertable = false),
            @JoinColumn(name = "cuentaxpagar", referencedColumnName = "CUENTA", nullable = false, insertable = false, updatable = false)
    })
    private CashAccount payableAccount;

    @OneToMany(mappedBy = "accountingRecord", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<AccountingRecordRelatedTransaction> relatedTransactionList = new ArrayList<AccountingRecordRelatedTransaction>(0);

    @Column(name = "notrans")
    private String transactionNumber;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idcompania", nullable = false, updatable = false, insertable = true)
    @NotNull
    private Company company;

    @Version
    @Column(name = "version", nullable = false)
    @NotNull
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

    public JobCategory getJobCategory() {
        return jobCategory;
    }

    public void setJobCategory(JobCategory jobCategory) {
        this.jobCategory = jobCategory;
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

    public GestionPayrollType getGestionPayrollType() {
        return gestionPayrollType;
    }

    public void setGestionPayrollType(GestionPayrollType gestionPayrollType) {
        this.gestionPayrollType = gestionPayrollType;
    }

    public Date getRecordDate() {
        return recordDate;
    }

    public void setRecordDate(Date recordDate) {
        this.recordDate = recordDate;
    }

    public User getRecorderUser() {
        return recorderUser;
    }

    public void setRecorderUser(User recorderUser) {
        this.recorderUser = recorderUser;
    }

    public BigDecimal getNationalAmountForBank() {
        return nationalAmountForBank;
    }

    public void setNationalAmountForBank(BigDecimal nationalAmountForBank) {
        this.nationalAmountForBank = nationalAmountForBank;
    }

    public BigDecimal getForeignAmountForBank() {
        return foreignAmountForBank;
    }

    public void setForeignAmountForBank(BigDecimal foreignAmountForBank) {
        this.foreignAmountForBank = foreignAmountForBank;
    }

    public BigDecimal getNationalAmountForCheck() {
        return nationalAmountForCheck;
    }

    public void setNationalAmountForCheck(BigDecimal nationalAmountForCheck) {
        this.nationalAmountForCheck = nationalAmountForCheck;
    }

    public BigDecimal getForeignAmountForCheck() {
        return foreignAmountForCheck;
    }

    public void setForeignAmountForCheck(BigDecimal foreignAmountForCheck) {
        this.foreignAmountForCheck = foreignAmountForCheck;
    }

    public Text getDescription() {
        return description;
    }

    public void setDescription(Text description) {
        this.description = description;
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

    public GeneratedPayroll getGeneratedPayroll() {
        return generatedPayroll;
    }

    public void setGeneratedPayroll(GeneratedPayroll generatedPayroll) {
        this.generatedPayroll = generatedPayroll;
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

    public String getIdNumber() {
        return idNumber;
    }

    public void setIdNumber(String idNumber) {
        this.idNumber = idNumber;
    }

    public BigDecimal getLesserAmount() {
        return lesserAmount;
    }

    public void setLesserAmount(BigDecimal lesserAmount) {
        this.lesserAmount = lesserAmount;
    }

    public BigDecimal getHigherAmount() {
        return higherAmount;
    }

    public void setHigherAmount(BigDecimal higherAmount) {
        this.higherAmount = higherAmount;
    }

    public Boolean getGestionPayrollSalaryType() {
        return GestionPayrollType.SALARY.equals(getGestionPayrollType());
    }

    public Boolean getGestionPayrollChristmasBonusType() {
        return GestionPayrollType.CHRISTMAS_BONUS.equals(getGestionPayrollType());
    }


    public String getProviderCode() {
        return providerCode;
    }

    public void setProviderCode(String providerCode) {
        this.providerCode = providerCode;
    }

    public String getDocumentTypeCode() {
        return documentTypeCode;
    }

    public void setDocumentTypeCode(String documentTypeCode) {
        this.documentTypeCode = documentTypeCode;
    }

    public String getPayableAccountCode() {
        return payableAccountCode;
    }

    public void setPayableAccountCode(String payableAccountCode) {
        this.payableAccountCode = payableAccountCode;
    }

    public Provider getProvider() {
        return provider;
    }

    public void setProvider(Provider provider) {
        this.provider = provider;
        setProviderCode(provider != null ? provider.getProviderCode() : null);
    }

    public PayableDocumentType getDocumentType() {
        return documentType;
    }

    public void setDocumentType(PayableDocumentType documentType) {
        this.documentType = documentType;
        setDocumentTypeCode(documentType != null ? documentType.getDocumentType() : null);
    }

    public CashAccount getPayableAccount() {
        return payableAccount;
    }

    public void setPayableAccount(CashAccount payableAccount) {
        this.payableAccount = payableAccount;
        setPayableAccountCode(payableAccount != null ? payableAccount.getAccountCode() : null);
    }

    public List<AccountingRecordRelatedTransaction> getRelatedTransactionList() {
        return relatedTransactionList;
    }

    public void setRelatedTransactionList(List<AccountingRecordRelatedTransaction> relatedTransactionList) {
        this.relatedTransactionList = relatedTransactionList;
    }

    public String getTransactionNumber() {
        return transactionNumber;
    }

    public void setTransactionNumber(String transactionNumber) {
        this.transactionNumber = transactionNumber;
    }

    @Override
    public String toString() {
        return "AccountingRecord{" +
                "id=" + id +
                ", businessUnit=" + businessUnit +
                ", jobCategory=" + jobCategory +
                ", gestion=" + gestion +
                ", month=" + month +
                ", recordDate=" + recordDate +
                ", generatedPayroll=" + generatedPayroll +
                ", idNumber='" + idNumber + '\'' +
                ", lesserAmount=" + lesserAmount +
                ", higherAmount=" + higherAmount +
                ", recorderUser=" + recorderUser +
                ", nationalAmountForBank=" + nationalAmountForBank +
                ", foreignAmountForBank=" + foreignAmountForBank +
                ", nationalAmountForCheck=" + nationalAmountForCheck +
                ", foreignAmountForCheck=" + foreignAmountForCheck +
                ", description=" + description +
                ", company=" + company +
                ", version=" + version +
                '}';
    }
}
