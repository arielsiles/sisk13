package com.encens.khipus.model.finances;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyNumberListener;
import com.encens.khipus.model.UpperCaseStringListener;
import com.encens.khipus.util.Constants;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;
import org.hibernate.validator.Length;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Entity for Voucher
 *
 * @author
 * @version 3.5.2.2
 */
@NamedQueries({
        @NamedQuery(name = "Voucher2.findVoucherById", query = "select o from Voucher2 o WHERE o.id=:id ")
})
//@SequenceGenerator(name = "Voucher.sequenceGenerator", sequenceName = Constants.FINANCES_SCHEMA + ".sf_trans")
@TableGenerator(schema = Constants.KHIPUS_SCHEMA,
        name = "Voucher2.tableGenerator",
        table = com.encens.khipus.util.Constants.SEQUENCE_TABLE_NAME,
        pkColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_PK_COLUMN_NAME,
        valueColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_VALUE_COLUMN_NAME,
        pkColumnValue = "sf_tmpenc",
        initialValue = 1,
        allocationSize = com.encens.khipus.util.Constants.SEQUENCE_ALLOCATION_SIZE)

@Entity
@EntityListeners({CompanyNumberListener.class, UpperCaseStringListener.class})
@Table(name = "sf_tmpenc", schema = Constants.FINANCES_SCHEMA)
public class Voucher2 implements BaseModel{
    @Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "Voucher2.tableGenerator")
    @Column(name = "id_tmpenc", nullable = false)
    private Long id;

    @Column(name = "no_trans", nullable = true, length = 10)
    @Length(max = 10)
    private String transactionNumber;

    @Column(name = "no_cia", updatable = false, length = 20)
    @Length(max = 20)
    private String companyNumber = "01";

    @Column(name = "formulario", updatable = true, length = 30)
    @Length(max = 30)
    private String form;

    @Column(name = "tipo_doc", updatable = true, length = 3)
    @Length(max = 3)
    private String documentType = "TR";

    /*@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tipo_doc", referencedColumnName = "nombre", nullable = true)
    private DocType docType;*/

    @Column(name = "no_doc", length = 20)
    @Length(max = 20)
    private String documentNumber;

    @Column(name = "cta_bco", updatable = true, length = 20)
    @Length(max = 20)
    private String bankAccountCode;

    @Column(name = "beneficiario", updatable = true, length = 100)
    @Length(max = 100)
    private String employeeName;

    @Column(name = "procedencia", updatable = true, length = 3)
    @Length(max = 3)
    private String source;

    @Column(name = "monto", precision = 16, scale = 2, updatable = true)
    private BigDecimal amount;

    @Column(name = "moneda", updatable = true)
    @Enumerated(EnumType.STRING)
    private FinancesCurrencyType currency;

    @Column(name = "tc", precision = 10, scale = 6, updatable = true)
    private BigDecimal exchangeRateAmount;

    @Column(name = "descri", updatable = true, length = 1000)
    @Length(max = 1000)
    private String description;

    @Column(name = "fecha", updatable = true)
    @Temporal(TemporalType.DATE)
    private Date date;

    @Column(name = "estado", updatable = true, length = 3)
    @Length(max = 3)
    private String state = "PEN";

    @Column(name = "no_usr", updatable = true, length = 4)
    @Length(max = 4)
    private String userNumber;

    @Column(name = "glosa", updatable = true, length = 1000)
    @Length(max = 1000)
    private String gloss;

    /* the cash account*/
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "no_cia", referencedColumnName = "no_cia", updatable = false, insertable = false),
            @JoinColumn(name = "cuenta", referencedColumnName = "cuenta", updatable = false, insertable = false)
    })
    private CashAccount cashAccount;

    /* the cash account code*/
    @Column(name = "cuenta", length = 20)
    @Length(max = 20)
    private String cashAccountCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "no_cia", updatable = false, insertable = false),
            @JoinColumn(name = "cod_prov", updatable = false, insertable = false)
    })
    private Provider provider;

    @Column(name = "cod_prov", length = 6)
    @Length(max = 6)
    private String providerCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cod_enti", referencedColumnName = "cod_enti")
    private FinancesEntity financesEntity;

    @Column(name = "fecha_ven")
    @Temporal(TemporalType.DATE)
    private Date expirationDate;

    @Column(name = "entregado_a", length = 660)
    @Length(max = 660)
    private String receiver;

    @Column(name = "observacion", updatable = true, length = 1000)
    @Length(max = 1000)
    private String observation;

    @Column(name = "no_trans_rel", length = 10)
    @Length(max = 10)
    private String relatedTransactionNumber;

    @Column(name = "agregar_cta_prov", updatable = true)
    @Type(type = com.encens.khipus.model.usertype.StringBooleanUserType.NAME, parameters = {
            @Parameter(
                    name = com.encens.khipus.model.usertype.StringBooleanUserType.TRUE_PARAMETER,
                    value = com.encens.khipus.model.usertype.StringBooleanUserType.TRUE_VALUE
            ),
            @Parameter(
                    name = com.encens.khipus.model.usertype.StringBooleanUserType.FALSE_PARAMETER,
                    value = com.encens.khipus.model.usertype.StringBooleanUserType.FALSE_VALUE
            )
    })
    private Boolean addProviderAccount = true;

    @Column(name = "sede_pago_chq", length = 8)
    @Length(max = 8)
    private String checkDestinationExecutorUnitCode;

    @Column(name = "pendiente_registro", length = 2)
    @Length(max = 2)
    private String pendantRegistry;

    @Transient
    private List<VoucherDetail2> details = new ArrayList<VoucherDetail2>(0);

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "voucher", cascade = CascadeType.ALL)
    private List<VoucherDetail2> voucherDetailList = new ArrayList<VoucherDetail2>(0);

    @PrePersist
    private void defineCurrentDate() {
        if (date == null) {
            date = new Date();
        }
    }

    public Voucher2() {
    }

    public Voucher2(String form, String gloss) {
        this.form = form;
        this.gloss = gloss;
    }

    public String getTransactionNumber() {
        return transactionNumber;
    }

    public void setTransactionNumber(String transactionNumber) {
        this.transactionNumber = transactionNumber;
    }

    public String getCompanyNumber() {
        return companyNumber;
    }

    public void setCompanyNumber(String companyNumber) {
        this.companyNumber = companyNumber;
    }

    public String getForm() {
        return form;
    }

    public void setForm(String form) {
        this.form = form;
    }

    public String getDocumentType() {
        return documentType;
    }

    public void setDocumentType(String documentType) {
        this.documentType = documentType;
    }

    public String getDocumentNumber() {
        return documentNumber;
    }

    public void setDocumentNumber(String documentNumber) {
        this.documentNumber = documentNumber;
    }

    public String getBankAccountCode() {
        return bankAccountCode;
    }

    public void setBankAccountCode(String bankAccountCode) {
        this.bankAccountCode = bankAccountCode;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public FinancesCurrencyType getCurrency() {
        return currency;
    }

    public void setCurrency(FinancesCurrencyType currency) {
        this.currency = currency;
    }

    public BigDecimal getExchangeRateAmount() {
        return exchangeRateAmount;
    }

    public void setExchangeRateAmount(BigDecimal exchangeRateAmount) {
        this.exchangeRateAmount = exchangeRateAmount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getUserNumber() {
        return userNumber;
    }

    public void setUserNumber(String userNumber) {
        this.userNumber = userNumber;
    }

    public String getGloss() {
        return gloss;
    }

    public void setGloss(String gloss) {
        this.gloss = gloss;
    }

    public List<VoucherDetail2> getDetails() {
        return details;
    }

    public void setDetails(List<VoucherDetail2> details) {
        this.details = details;
    }

    public void addVoucherDetail(VoucherDetail2 voucherDetail) {
        getDetails().add(voucherDetail);

    }

    public CashAccount getCashAccount() {
        return cashAccount;
    }

    public void setCashAccount(CashAccount cashAccount) {
        this.cashAccount = cashAccount;
        setCashAccountCode(this.cashAccount != null ? this.cashAccount.getAccountCode() : null);
    }

    public String getCashAccountCode() {
        return cashAccountCode;
    }

    public void setCashAccountCode(String cashAccountCode) {
        this.cashAccountCode = cashAccountCode;
    }

    public Date getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(Date expirationDate) {
        this.expirationDate = expirationDate;
    }

    public Provider getProvider() {
        return provider;
    }

    public void setProvider(Provider provider) {
        this.provider = provider;
        setProviderCode(this.provider != null ? this.provider.getProviderCode() : null);
    }

    public String getProviderCode() {
        return providerCode;
    }

    public void setProviderCode(String providerCode) {
        this.providerCode = providerCode;
    }

    public FinancesEntity getFinancesEntity() {
        return financesEntity;
    }

    public void setFinancesEntity(FinancesEntity financesEntity) {
        this.financesEntity = financesEntity;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getObservation() {
        return observation;
    }

    public void setObservation(String observation) {
        this.observation = observation;
    }

    public String getRelatedTransactionNumber() {
        return relatedTransactionNumber;
    }

    public void setRelatedTransactionNumber(String relatedTransactionNumber) {
        this.relatedTransactionNumber = relatedTransactionNumber;
    }

    public Boolean getAddProviderAccount() {
        return addProviderAccount;
    }

    public void setAddProviderAccount(Boolean addProviderAccount) {
        this.addProviderAccount = addProviderAccount;
    }

    public String getCheckDestinationExecutorUnitCode() {
        return checkDestinationExecutorUnitCode;
    }

    public void setCheckDestinationExecutorUnitCode(String checkDestinationExecutorUnitCode) {
        this.checkDestinationExecutorUnitCode = checkDestinationExecutorUnitCode;
    }

    public String getPendantRegistry() {
        return pendantRegistry;
    }

    public void setPendantRegistry(String pendantRegistry) {
        this.pendantRegistry = pendantRegistry;
    }

    public boolean isApproved() {
        return null != getState() && "APR".equals(getState().trim());
    }

    public boolean isNullified() {
        return null != getState() && "ANL".equals(getState().trim());
    }

    public boolean isPending() {
        return null != getState() && "PEN".equals(getState().trim());
    }

    public void nullify() {
        setState("ANL");
    }

    @Override
    public String toString() {
        return "Voucher{" +
                "transactionNumber='" + transactionNumber + '\'' +
                ", companyNumber='" + companyNumber + '\'' +
                ", form='" + form + '\'' +
                ", documentType='" + documentType + '\'' +
                ", documentNumber='" + documentNumber + '\'' +
                ", bankAccountCode='" + bankAccountCode + '\'' +
                ", employeeName='" + employeeName + '\'' +
                ", source='" + source + '\'' +
                ", amount=" + amount +
                ", currency=" + currency +
                ", exchangeRateAmount=" + exchangeRateAmount +
                ", description='" + description + '\'' +
                ", date=" + date +
                ", state='" + state + '\'' +
                ", userNumber='" + userNumber + '\'' +
                ", gloss='" + gloss + '\'' +
                '}';
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public List<VoucherDetail2> getVoucherDetailList() {
        return voucherDetailList;
    }

    public void setVoucherDetailList(List<VoucherDetail2> voucherDetailList) {
        this.voucherDetailList = voucherDetailList;
    }

    /*@Override
    public Object getId() {
        return this.transactionNumber.toString() ;
    }*/

    /*public Long getIdenc() {
        return idenc;
    }

    public void setIdenc(Long idenc) {
        this.idenc = idenc;
    }*/
}