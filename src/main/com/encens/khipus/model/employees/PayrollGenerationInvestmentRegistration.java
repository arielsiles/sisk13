package com.encens.khipus.model.employees;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyListener;
import com.encens.khipus.model.CompanyNumberListener;
import com.encens.khipus.model.UpperCaseStringListener;
import com.encens.khipus.model.admin.Company;
import com.encens.khipus.model.common.Text;
import com.encens.khipus.model.finances.PayableDocument;
import com.encens.khipus.model.finances.PayableDocumentType;
import org.hibernate.annotations.Filter;
import org.hibernate.validator.Digits;
import org.hibernate.validator.NotNull;

import javax.persistence.*;
import java.math.BigDecimal;

/**
 * @author
 * @version 3.5
 */
@NamedQueries({
        @NamedQuery(name = "PayrollGenerationInvestmentRegistration.countByPayrollGenerationCycle",
                query = "select count(entity) from PayrollGenerationInvestmentRegistration entity where entity.payrollGenerationCycle=:payrollGenerationCycle")
})
@TableGenerator(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "PayrollGenerationInvestmentRegistration.tableGenerator",
        table = com.encens.khipus.util.Constants.SEQUENCE_TABLE_NAME,
        pkColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_PK_COLUMN_NAME,
        valueColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_VALUE_COLUMN_NAME,
        pkColumnValue = "regaporgenplan",
        allocationSize = com.encens.khipus.util.Constants.SEQUENCE_ALLOCATION_SIZE)
@Entity
@Filter(name = com.encens.khipus.util.Constants.COMPANY_FILTER_NAME)
@EntityListeners({CompanyListener.class, CompanyNumberListener.class, UpperCaseStringListener.class})
@Table(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "regaporgenplan")
public class PayrollGenerationInvestmentRegistration implements BaseModel {
    @Id
    @Column(name = "idregaporgenplan", nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "PayrollGenerationInvestmentRegistration.tableGenerator")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idciclogeneracionplanilla", nullable = false)
    @NotNull
    private PayrollGenerationCycle payrollGenerationCycle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "identidadbensocial", nullable = false)
    @NotNull
    private SocialWelfareEntity socialWelfareEntity;

    @Column(name = "nocia")
    private String companyNumber;

    @Column(name = "tipodoc")
    private String documentTypeCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "nocia", referencedColumnName = "no_cia", nullable = false, insertable = false, updatable = false),
            @JoinColumn(name = "tipodoc", referencedColumnName = "tipo_doc", nullable = false, insertable = false, updatable = false)
    })
    @NotNull
    private PayableDocumentType documentType;

    @Column(name = "notrans")
    private String transactionNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notrans", referencedColumnName = "no_trans", insertable = false, updatable = false)
    private PayableDocument payableDocument;

    @Column(name = "monto", precision = 16, scale = 2)
    @Digits(integerDigits = 16, fractionalDigits = 2)
    private BigDecimal amount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idmoneda")
    @NotNull
    private Currency currency;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "iddescripcion", nullable = true)
    private Text description;

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

    public PayrollGenerationCycle getPayrollGenerationCycle() {
        return payrollGenerationCycle;
    }

    public void setPayrollGenerationCycle(PayrollGenerationCycle payrollGenerationCycle) {
        this.payrollGenerationCycle = payrollGenerationCycle;
    }

    public SocialWelfareEntity getSocialWelfareEntity() {
        return socialWelfareEntity;
    }

    public void setSocialWelfareEntity(SocialWelfareEntity socialWelfareEntity) {
        this.socialWelfareEntity = socialWelfareEntity;
    }

    public String getCompanyNumber() {
        return companyNumber;
    }

    public void setCompanyNumber(String companyNumber) {
        this.companyNumber = companyNumber;
    }

    public String getDocumentTypeCode() {
        return documentTypeCode;
    }

    public void setDocumentTypeCode(String documentTypeCode) {
        this.documentTypeCode = documentTypeCode;
    }

    public PayableDocumentType getDocumentType() {
        return documentType;
    }

    public void setDocumentType(PayableDocumentType documentType) {
        this.documentType = documentType;
        setDocumentTypeCode(documentType != null ? documentType.getDocumentType() : null);
    }

    public String getTransactionNumber() {
        return transactionNumber;
    }

    public void setTransactionNumber(String transactionNumber) {
        this.transactionNumber = transactionNumber;
    }

    public PayableDocument getPayableDocument() {
        return payableDocument;
    }

    public void setPayableDocument(PayableDocument payableDocument) {
        this.payableDocument = payableDocument;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Currency getCurrency() {
        return currency;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
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
}
