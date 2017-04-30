package com.encens.khipus.model.finances;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyNumberListener;
import com.encens.khipus.model.UpperCaseStringListener;
import com.encens.khipus.model.usertype.StringBooleanUserType;
import com.encens.khipus.util.Constants;
import com.encens.khipus.util.FormatUtils;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

import static com.encens.khipus.model.usertype.StringBooleanUserType.*;

/**
 * @author
 * @version 2.25
 */

@NamedQueries({
        @NamedQuery(name = "FinanceDocument.findByAccountingMovement",
                query = "select financesDocument from FinanceDocument financesDocument where financesDocument.accountingMovement=:accountingMovement"),
        @NamedQuery(name = "FinanceDocument.findByTransactionNumber",
                query = "select financesDocument from FinanceDocument financesDocument where financesDocument.id.transactionNumber=:transactionNumber" +
                        " order by financesDocument.voucherType,financesDocument.voucherNumber")
})
@Entity
@EntityListeners({CompanyNumberListener.class, UpperCaseStringListener.class})
@Table(name = "ck_docus", schema = Constants.FINANCES_SCHEMA, uniqueConstraints = {
        @UniqueConstraint(columnNames = {"NO_CIA", "CTA_BCO", "PROCEDENCIA", "TIPO_DOC", "NO_DOC"})
})
public class FinanceDocument implements BaseModel {

    @EmbeddedId
    private FinanceDocumentPk id = new FinanceDocumentPk();

    @Column(name = "NO_CIA", length = 2, updatable = false, insertable = false)
    private String companyNumber;

    @Column(name = "NO_TRANS", length = 10, updatable = false, insertable = false)
    private String transactionNumber;

    @Column(name = "CTA_BCO", length = 20)
    private String bankAccountCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "CTA_BCO", referencedColumnName = "CTA_BCO", updatable = false, insertable = false),
            @JoinColumn(name = "NO_CIA", referencedColumnName = "NO_CIA", updatable = false, insertable = false)
    })
    private FinancesBankAccount bankAccount;

    @Column(name = "PROCEDENCIA", length = 1)
    private String provenance;

    @Column(name = "TIPO_DOC", length = 3)
    private String documentTypeCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "TIPO_DOC", referencedColumnName = "TIPO_DOC", updatable = false, insertable = false),
            @JoinColumn(name = "NO_CIA", referencedColumnName = "NO_CIA", updatable = false, insertable = false)
    })
    private FinancesDocumentType documentType;

    @Column(name = "NO_DOC", nullable = false, length = 20)
    private String documentNumber;

    @Column(name = "FECHA")
    @Temporal(TemporalType.DATE)
    private Date date;

    @Column(name = "BENEFICIARIO", length = 100)
    private String beneficiaryName;

    @Column(name = "MONTO", precision = 16, scale = 4)
    private BigDecimal amount;

    @Column(name = "MONEDA", length = 3)
    @Enumerated(EnumType.STRING)
    private FinancesCurrencyType currency;

    @Column(name = "TC", precision = 10, scale = 6)
    private BigDecimal exchangeRate;

    @Column(name = "NO_CONCI", length = 10)
    private String conciliationNumber;

    @Column(name = "CUENTA", length = 31)
    private String cashAccountCode;

    @Column(name = "TIPO_COMPRO", length = 2)
    private String voucherType;

    @Column(name = "NO_COMPRO", length = 10)
    private String voucherNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "TIPO_COMPRO", referencedColumnName = "TIPO_COMPRO", updatable = false, insertable = false),
            @JoinColumn(name = "NO_COMPRO", referencedColumnName = "NO_COMPRO", updatable = false, insertable = false),
            @JoinColumn(name = "NO_CIA", referencedColumnName = "NO_CIA", updatable = false, insertable = false)
    })
    private AccountingMovement accountingMovement;

    @Column(name = "ORIGEN", length = 6)
    private String source;

    @Column(name = "MOD_AUT", length = 3)
    @Type(type = StringBooleanUserType.NAME, parameters = {
            @Parameter(name = TRUE_PARAMETER, value = TRUE_VALUE),
            @Parameter(name = FALSE_PARAMETER, value = FALSE_VALUE)
    })
    private Boolean hasUpdateAuthorization;

    @Column(name = "COD_BENEF", length = 6)
    private String beneficiaryCode;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "COD_BENEF", referencedColumnName = "COD_ENTI", updatable = false, insertable = false)
    private FinancesEntity beneficiary;

    @Column(name = "ENTREGADO", length = 20)
    @Type(type = StringBooleanUserType.NAME, parameters = {
            @Parameter(name = TRUE_PARAMETER, value = TRUE_VALUE),
            @Parameter(name = FALSE_PARAMETER, value = FALSE_VALUE)
    })
    private Boolean isDelivered;

    @Column(name = "FECHAENTREGA")
    @Temporal(TemporalType.DATE)
    private Date deliveryDate;

    @Column(name = "ENTREGADOPOR", length = 4)
    private String deliveredByUserCode;

    @Column(name = "OBSENTREGA", length = 4)
    private String deliveryObservation;

    @Column(name = "ESTADO", nullable = false, length = 3)
    @Enumerated(EnumType.STRING)
    private FinanceDocumentState state;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "NO_CIA", referencedColumnName = "NO_CIA", updatable = false, insertable = false),
            @JoinColumn(name = "NO_TRANS", referencedColumnName = "NO_TRANS", updatable = false, insertable = false),
            @JoinColumn(name = "ESTADO", referencedColumnName = "ESTADO", updatable = false, insertable = false)
    })
    private FinanceDocumentMovement financeDocumentMovement;

    public FinanceDocumentPk getId() {
        return id;
    }

    public void setId(FinanceDocumentPk id) {
        this.id = id;
    }

    public String getCompanyNumber() {
        return companyNumber;
    }

    public void setCompanyNumber(String companyNumber) {
        this.companyNumber = companyNumber;
    }

    public String getTransactionNumber() {
        return transactionNumber;
    }

    public void setTransactionNumber(String transactionNumber) {
        this.transactionNumber = transactionNumber;
    }

    public String getBankAccountCode() {
        return bankAccountCode;
    }

    public void setBankAccountCode(String bankAccountCode) {
        this.bankAccountCode = bankAccountCode;
    }

    public FinancesBankAccount getBankAccount() {
        return bankAccount;
    }

    public void setBankAccount(FinancesBankAccount bankAccount) {
        this.bankAccount = bankAccount;
    }

    public String getProvenance() {
        return provenance;
    }

    public void setProvenance(String provenance) {
        this.provenance = provenance;
    }

    public String getDocumentTypeCode() {
        return documentTypeCode;
    }

    public void setDocumentTypeCode(String documentTypeCode) {
        this.documentTypeCode = documentTypeCode;
    }

    public FinancesDocumentType getDocumentType() {
        return documentType;
    }

    public void setDocumentType(FinancesDocumentType documentType) {
        this.documentType = documentType;
    }

    public String getDocumentNumber() {
        return documentNumber;
    }

    public void setDocumentNumber(String documentNumber) {
        this.documentNumber = documentNumber;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getBeneficiaryName() {
        return beneficiaryName;
    }

    public void setBeneficiaryName(String beneficiaryName) {
        this.beneficiaryName = beneficiaryName;
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

    public BigDecimal getExchangeRate() {
        return exchangeRate;
    }

    public void setExchangeRate(BigDecimal exchangeRate) {
        this.exchangeRate = exchangeRate;
    }

    public String getConciliationNumber() {
        return conciliationNumber;
    }

    public void setConciliationNumber(String conciliationNumber) {
        this.conciliationNumber = conciliationNumber;
    }

    public String getCashAccountCode() {
        return cashAccountCode;
    }

    public void setCashAccountCode(String cashAccountCode) {
        this.cashAccountCode = cashAccountCode;
    }

    public String getVoucherType() {
        return voucherType;
    }

    public void setVoucherType(String voucherType) {
        this.voucherType = voucherType;
    }

    public String getVoucherNumber() {
        return voucherNumber;
    }

    public void setVoucherNumber(String voucherNumber) {
        this.voucherNumber = voucherNumber;
    }

    public AccountingMovement getAccountingMovement() {
        return accountingMovement;
    }

    public void setAccountingMovement(AccountingMovement accountingMovement) {
        this.accountingMovement = accountingMovement;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public Boolean getHasUpdateAuthorization() {
        return hasUpdateAuthorization;
    }

    public void setHasUpdateAuthorization(Boolean hasUpdateAuthorization) {
        this.hasUpdateAuthorization = hasUpdateAuthorization;
    }

    public String getBeneficiaryCode() {
        return beneficiaryCode;
    }

    public void setBeneficiaryCode(String beneficiaryCode) {
        this.beneficiaryCode = beneficiaryCode;
    }

    public FinancesEntity getBeneficiary() {
        return beneficiary;
    }

    public void setBeneficiary(FinancesEntity beneficiary) {
        this.beneficiary = beneficiary;
    }

    public Boolean getDelivered() {
        return isDelivered;
    }

    public void setDelivered(Boolean delivered) {
        isDelivered = delivered;
    }

    public Date getDeliveryDate() {
        return deliveryDate;
    }

    public void setDeliveryDate(Date deliveryDate) {
        this.deliveryDate = deliveryDate;
    }

    public String getDeliveredByUserCode() {
        return deliveredByUserCode;
    }

    public void setDeliveredByUserCode(String deliveredByUserCode) {
        this.deliveredByUserCode = deliveredByUserCode;
    }

    public String getDeliveryObservation() {
        return deliveryObservation;
    }

    public void setDeliveryObservation(String deliveryObservation) {
        this.deliveryObservation = deliveryObservation;
    }

    public FinanceDocumentState getState() {
        return state;
    }

    public void setState(FinanceDocumentState state) {
        this.state = state;
    }

    public String getFullNumber() {
        return FormatUtils.concatBySeparator("-", getDocumentTypeCode(), getDocumentNumber());
    }

    public Boolean isNullified() {
        return null != getState() && FinanceDocumentState.ANL.equals(getState());
    }

    public FinanceDocumentMovement getFinanceDocumentMovement() {
        return financeDocumentMovement;
    }

    public void setFinanceDocumentMovement(FinanceDocumentMovement financeDocumentMovement) {
        this.financeDocumentMovement = financeDocumentMovement;
    }

    public String getFullName() {
        return getDocumentTypeCode() + Constants.HYPHEN_SEPARATOR + getDocumentNumber();
    }
}
