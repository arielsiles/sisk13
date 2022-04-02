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
        @UniqueConstraint(columnNames = {"no_cia", "cta_bco", "procedencia", "tipo_doc", "no_doc"})
})
public class FinanceDocument implements BaseModel {

    @EmbeddedId
    private FinanceDocumentPk id = new FinanceDocumentPk();

    @Column(name = "no_cia", length = 2, updatable = false, insertable = false)
    private String companyNumber;

    @Column(name = "no_trans", length = 10, updatable = false, insertable = false)
    private String transactionNumber;

    @Column(name = "cta_bco", length = 20)
    private String bankAccountCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "cta_bco", referencedColumnName = "cta_bco", updatable = false, insertable = false),
            @JoinColumn(name = "no_cia", referencedColumnName = "no_cia", updatable = false, insertable = false)
    })
    private FinancesBankAccount bankAccount;

    @Column(name = "procedencia", length = 1)
    private String provenance;

    @Column(name = "tipo_doc", length = 3)
    private String documentTypeCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "tipo_doc", referencedColumnName = "tipo_doc", updatable = false, insertable = false),
            @JoinColumn(name = "no_cia", referencedColumnName = "no_cia", updatable = false, insertable = false)
    })
    private FinancesDocumentType documentType;

    @Column(name = "no_doc", nullable = false, length = 20)
    private String documentNumber;

    @Column(name = "fecha")
    @Temporal(TemporalType.DATE)
    private Date date;

    @Column(name = "beneficiario", length = 100)
    private String beneficiaryName;

    @Column(name = "monto", precision = 16, scale = 4)
    private BigDecimal amount;

    @Column(name = "moneda", length = 3)
    @Enumerated(EnumType.STRING)
    private FinancesCurrencyType currency;

    @Column(name = "tc", precision = 10, scale = 6)
    private BigDecimal exchangeRate;

    @Column(name = "no_conci", length = 10)
    private String conciliationNumber;

    @Column(name = "cuenta", length = 31)
    private String cashAccountCode;

    @Column(name = "tipo_compro", length = 2)
    private String voucherType;

    @Column(name = "no_compro", length = 10)
    private String voucherNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "tipo_compro", referencedColumnName = "tipo_compro", updatable = false, insertable = false),
            @JoinColumn(name = "no_compro", referencedColumnName = "no_compro", updatable = false, insertable = false),
            @JoinColumn(name = "no_cia", referencedColumnName = "no_cia", updatable = false, insertable = false)
    })
    private AccountingMovement accountingMovement;

    @Column(name = "origen", length = 6)
    private String source;

    @Column(name = "mod_aut", length = 3)
    @Type(type = StringBooleanUserType.NAME, parameters = {
            @Parameter(name = TRUE_PARAMETER, value = TRUE_VALUE),
            @Parameter(name = FALSE_PARAMETER, value = FALSE_VALUE)
    })
    private Boolean hasUpdateAuthorization;

    @Column(name = "cod_benef", length = 6)
    private String beneficiaryCode;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cod_benef", referencedColumnName = "cod_enti", updatable = false, insertable = false)
    private FinancesEntity beneficiary;

    @Column(name = "entregado", length = 20)
    @Type(type = StringBooleanUserType.NAME, parameters = {
            @Parameter(name = TRUE_PARAMETER, value = TRUE_VALUE),
            @Parameter(name = FALSE_PARAMETER, value = FALSE_VALUE)
    })
    private Boolean isDelivered;

    @Column(name = "fechaentrega")
    @Temporal(TemporalType.DATE)
    private Date deliveryDate;

    @Column(name = "entregadopor", length = 4)
    private String deliveredByUserCode;

    @Column(name = "obsentrega", length = 4)
    private String deliveryObservation;

    @Column(name = "estado", nullable = false, length = 3)
    @Enumerated(EnumType.STRING)
    private FinanceDocumentState state;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "no_cia", referencedColumnName = "no_cia", updatable = false, insertable = false),
            @JoinColumn(name = "no_trans", referencedColumnName = "no_trans", updatable = false, insertable = false),
            @JoinColumn(name = "estado", referencedColumnName = "estado", updatable = false, insertable = false)
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
