package com.encens.khipus.model.finances;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.admin.Company;
import com.encens.khipus.util.Constants;
import org.hibernate.annotations.Filter;
import org.hibernate.validator.NotNull;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @author
 * @version 3.5
 */


@NamedQueries({
        @NamedQuery(name = "RotatoryFundMovement.sumAmountByRotatoryFundAndMovementDate",
                query = "select sum(o.paymentAmount-o.collectionAmount)" +
                        " from RotatoryFundMovement o " +
                        " left join o.rotatoryFund rotatoryFund" +
                        " where o.rotatoryFund.id=:rotatoryFundId and o.date<:movementDate and o.state=:state")
})

@Entity
@Filter(name = com.encens.khipus.util.Constants.COMPANY_FILTER_NAME)
@Table(name = "movfondorota", schema = Constants.KHIPUS_SCHEMA)
public class RotatoryFundMovement implements BaseModel {
    @Id
    @Column(name = "idmovimiento")
    private Long id;

    @Column(name = "clasemov")
    @Enumerated(EnumType.STRING)
    private RotatoryFundMovementClass movementClass;

    @Column(name = "codigo")
    private Integer code;

    @Column(name = "fecha")
    @Temporal(TemporalType.DATE)
    private Date date;

    @Column(name = "tipomov")
    @Enumerated(EnumType.STRING)
    private RotatoryFundMovementType movementType;

    @Column(name = "tipocompro")
    private String voucherType;

    @Column(name = "nocompro")
    private String voucherNumber;

    @Column(name = "fechacompro")
    @Temporal(TemporalType.DATE)
    private Date voucherDate;

    @Column(name = "pagodocbantipodoc")
    private String bankPaymentDocumentType;

    @Column(name = "pagodocbannodoc")
    private String bankPaymentDocumentNumber;

    @Column(name = "pagocajanodoc")
    private String cashBoxPaymentDocumentNumber;

    @Column(name = "pagocajacuenta")
    private String cashBoxPaymentAccountNumber;

    @Column(name = "pagocajanombre")
    private String cashBoxPaymentAccountName;

    @Column(name = "cobrodocbantipodoc")
    private String documentBankCollectionDocumentType;

    @Column(name = "cobrodocbannodoc")
    private String documentBankCollectionDocumentNumber;

    @Column(name = "cobrodoctipodoc")
    @Enumerated(EnumType.STRING)
    private CollectionDocumentType documentCollectionDocumentType;

    @Column(name = "cobrodocnodoc")
    private String documentCollectionDocumentNumber;

    @Column(name = "cobroajtctactbcuenta")
    private String cashAccountAdjustmentCollectionAccount;

    @Column(name = "cobroajtctactbnombre")
    private String cashAccountAdjustmentCollectionNumber;

    @Column(name = "cobroajtdeptipodoc")
    private String depositAdjustmentCollectionDocumentType;

    @Column(name = "cobroajtdepnodoc")
    private String depositAdjustmentCollectionDocumentNumber;

    @Column(name = "cobroocnoorden")
    private String purchaseOrderCollectionOrderNumber;

    @Column(name = "cobroctacajacuenta")
    private String cashBoxCollectionAccountNumber;

    @Column(name = "cobroctacajanombre")
    private String cashBoxCollectionAccountName;

    @Column(name = "cobroplanombreges")
    private String payrollCollectionName;

    @Column(name = "descripcion")
    private String description;

    @Column(name = "observacion")
    private String observation;

    @Column(name = "monedapago")
    @Enumerated(EnumType.STRING)
    private FinancesCurrencyType paymentCurrency;

    @Column(name = "montopago", precision = 12, scale = 2)
    private BigDecimal paymentAmount;

    @Column(name = "monedacobro")
    @Enumerated(EnumType.STRING)
    private FinancesCurrencyType collectionCurrency;

    @Column(name = "montocobro", precision = 12, scale = 2)
    private BigDecimal collectionAmount;

    @Column(name = "tipocambio", precision = 12, scale = 2)
    private BigDecimal exchangeRate;

    @Column(name = "estado", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private RotatoryFundMovementState state;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idcompania", nullable = false, updatable = false, insertable = true)
    @NotNull
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idfondorotatorio")
    private RotatoryFund rotatoryFund;

    @Column(name = "notrans")
    private String transactionNumber;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public RotatoryFundMovementClass getMovementClass() {
        return movementClass;
    }

    public void setMovementClass(RotatoryFundMovementClass movementClass) {
        this.movementClass = movementClass;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public RotatoryFundMovementType getMovementType() {
        return movementType;
    }

    public void setMovementType(RotatoryFundMovementType movementType) {
        this.movementType = movementType;
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

    public Date getVoucherDate() {
        return voucherDate;
    }

    public void setVoucherDate(Date voucherDate) {
        this.voucherDate = voucherDate;
    }

    public String getBankPaymentDocumentType() {
        return bankPaymentDocumentType;
    }

    public void setBankPaymentDocumentType(String bankPaymentDocumentType) {
        this.bankPaymentDocumentType = bankPaymentDocumentType;
    }

    public String getBankPaymentDocumentNumber() {
        return bankPaymentDocumentNumber;
    }

    public void setBankPaymentDocumentNumber(String bankPaymentDocumentNumber) {
        this.bankPaymentDocumentNumber = bankPaymentDocumentNumber;
    }

    public String getCashBoxPaymentDocumentNumber() {
        return cashBoxPaymentDocumentNumber;
    }

    public void setCashBoxPaymentDocumentNumber(String cashBoxPaymentDocumentNumber) {
        this.cashBoxPaymentDocumentNumber = cashBoxPaymentDocumentNumber;
    }

    public String getCashBoxPaymentAccountNumber() {
        return cashBoxPaymentAccountNumber;
    }

    public void setCashBoxPaymentAccountNumber(String cashBoxPaymentAccountNumber) {
        this.cashBoxPaymentAccountNumber = cashBoxPaymentAccountNumber;
    }

    public String getCashBoxPaymentAccountName() {
        return cashBoxPaymentAccountName;
    }

    public void setCashBoxPaymentAccountName(String cashBoxPaymentAccountName) {
        this.cashBoxPaymentAccountName = cashBoxPaymentAccountName;
    }

    public String getDocumentBankCollectionDocumentType() {
        return documentBankCollectionDocumentType;
    }

    public void setDocumentBankCollectionDocumentType(String documentBankCollectionDocumentType) {
        this.documentBankCollectionDocumentType = documentBankCollectionDocumentType;
    }

    public String getDocumentBankCollectionDocumentNumber() {
        return documentBankCollectionDocumentNumber;
    }

    public void setDocumentBankCollectionDocumentNumber(String documentBankCollectionDocumentNumber) {
        this.documentBankCollectionDocumentNumber = documentBankCollectionDocumentNumber;
    }

    public CollectionDocumentType getDocumentCollectionDocumentType() {
        return documentCollectionDocumentType;
    }

    public void setDocumentCollectionDocumentType(CollectionDocumentType documentCollectionDocumentType) {
        this.documentCollectionDocumentType = documentCollectionDocumentType;
    }

    public String getDocumentCollectionDocumentNumber() {
        return documentCollectionDocumentNumber;
    }

    public void setDocumentCollectionDocumentNumber(String documentCollectionDocumentNumber) {
        this.documentCollectionDocumentNumber = documentCollectionDocumentNumber;
    }

    public String getCashAccountAdjustmentCollectionAccount() {
        return cashAccountAdjustmentCollectionAccount;
    }

    public void setCashAccountAdjustmentCollectionAccount(String cashAccountAdjustmentCollectionAccount) {
        this.cashAccountAdjustmentCollectionAccount = cashAccountAdjustmentCollectionAccount;
    }

    public String getCashAccountAdjustmentCollectionNumber() {
        return cashAccountAdjustmentCollectionNumber;
    }

    public void setCashAccountAdjustmentCollectionNumber(String cashAccountAdjustmentCollectionNumber) {
        this.cashAccountAdjustmentCollectionNumber = cashAccountAdjustmentCollectionNumber;
    }

    public String getDepositAdjustmentCollectionDocumentType() {
        return depositAdjustmentCollectionDocumentType;
    }

    public void setDepositAdjustmentCollectionDocumentType(String depositAdjustmentCollectionDocumentType) {
        this.depositAdjustmentCollectionDocumentType = depositAdjustmentCollectionDocumentType;
    }

    public String getDepositAdjustmentCollectionDocumentNumber() {
        return depositAdjustmentCollectionDocumentNumber;
    }

    public void setDepositAdjustmentCollectionDocumentNumber(String depositAdjustmentCollectionDocumentNumber) {
        this.depositAdjustmentCollectionDocumentNumber = depositAdjustmentCollectionDocumentNumber;
    }

    public String getPurchaseOrderCollectionOrderNumber() {
        return purchaseOrderCollectionOrderNumber;
    }

    public void setPurchaseOrderCollectionOrderNumber(String purchaseOrderCollectionOrderNumber) {
        this.purchaseOrderCollectionOrderNumber = purchaseOrderCollectionOrderNumber;
    }

    public String getCashBoxCollectionAccountNumber() {
        return cashBoxCollectionAccountNumber;
    }

    public void setCashBoxCollectionAccountNumber(String cashBoxCollectionAccountNumber) {
        this.cashBoxCollectionAccountNumber = cashBoxCollectionAccountNumber;
    }

    public String getCashBoxCollectionAccountName() {
        return cashBoxCollectionAccountName;
    }

    public void setCashBoxCollectionAccountName(String cashBoxCollectionAccountName) {
        this.cashBoxCollectionAccountName = cashBoxCollectionAccountName;
    }

    public String getPayrollCollectionName() {
        return payrollCollectionName;
    }

    public void setPayrollCollectionName(String payrollCollectionName) {
        this.payrollCollectionName = payrollCollectionName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getObservation() {
        return observation;
    }

    public void setObservation(String observation) {
        this.observation = observation;
    }

    public FinancesCurrencyType getPaymentCurrency() {
        return paymentCurrency;
    }

    public void setPaymentCurrency(FinancesCurrencyType paymentCurrency) {
        this.paymentCurrency = paymentCurrency;
    }

    public BigDecimal getPaymentAmount() {
        return paymentAmount;
    }

    public void setPaymentAmount(BigDecimal paymentAmount) {
        this.paymentAmount = paymentAmount;
    }

    public FinancesCurrencyType getCollectionCurrency() {
        return collectionCurrency;
    }

    public void setCollectionCurrency(FinancesCurrencyType collectionCurrency) {
        this.collectionCurrency = collectionCurrency;
    }

    public BigDecimal getCollectionAmount() {
        return collectionAmount;
    }

    public void setCollectionAmount(BigDecimal collectionAmount) {
        this.collectionAmount = collectionAmount;
    }

    public BigDecimal getExchangeRate() {
        return exchangeRate;
    }

    public void setExchangeRate(BigDecimal exchangeRate) {
        this.exchangeRate = exchangeRate;
    }

    public RotatoryFundMovementState getState() {
        return state;
    }

    public void setState(RotatoryFundMovementState state) {
        this.state = state;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public RotatoryFund getRotatoryFund() {
        return rotatoryFund;
    }

    public void setRotatoryFund(RotatoryFund rotatoryFund) {
        this.rotatoryFund = rotatoryFund;
    }

    public String getTransactionNumber() {
        return transactionNumber;
    }

    public void setTransactionNumber(String transactionNumber) {
        this.transactionNumber = transactionNumber;
    }
}