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
@Table(name = "MOVFONDOROTA", schema = Constants.KHIPUS_SCHEMA)
public class RotatoryFundMovement implements BaseModel {
    @Id
    @Column(name = "IDMOVIMIENTO")
    private Long id;

    @Column(name = "CLASEMOV")
    @Enumerated(EnumType.STRING)
    private RotatoryFundMovementClass movementClass;

    @Column(name = "CODIGO")
    private Integer code;

    @Column(name = "FECHA")
    @Temporal(TemporalType.DATE)
    private Date date;

    @Column(name = "TIPOMOV")
    @Enumerated(EnumType.STRING)
    private RotatoryFundMovementType movementType;

    @Column(name = "TIPOCOMPRO")
    private String voucherType;

    @Column(name = "NOCOMPRO")
    private String voucherNumber;

    @Column(name = "FECHACOMPRO")
    @Temporal(TemporalType.DATE)
    private Date voucherDate;

    @Column(name = "PAGODOCBANTIPODOC")
    private String bankPaymentDocumentType;

    @Column(name = "PAGODOCBANNODOC")
    private String bankPaymentDocumentNumber;

    @Column(name = "PAGOCAJANODOC")
    private String cashBoxPaymentDocumentNumber;

    @Column(name = "PAGOCAJACUENTA")
    private String cashBoxPaymentAccountNumber;

    @Column(name = "PAGOCAJANOMBRE")
    private String cashBoxPaymentAccountName;

    @Column(name = "COBRODOCBANTIPODOC")
    private String documentBankCollectionDocumentType;

    @Column(name = "COBRODOCBANNODOC")
    private String documentBankCollectionDocumentNumber;

    @Column(name = "COBRODOCTIPODOC")
    @Enumerated(EnumType.STRING)
    private CollectionDocumentType documentCollectionDocumentType;

    @Column(name = "COBRODOCNODOC")
    private String documentCollectionDocumentNumber;

    @Column(name = "COBROAJTCTACTBCUENTA")
    private String cashAccountAdjustmentCollectionAccount;

    @Column(name = "COBROAJTCTACTBNOMBRE")
    private String cashAccountAdjustmentCollectionNumber;

    @Column(name = "COBROAJTDEPTIPODOC")
    private String depositAdjustmentCollectionDocumentType;

    @Column(name = "COBROAJTDEPNODOC")
    private String depositAdjustmentCollectionDocumentNumber;

    @Column(name = "COBROOCNOORDEN")
    private String purchaseOrderCollectionOrderNumber;

    @Column(name = "COBROCTACAJACUENTA")
    private String cashBoxCollectionAccountNumber;

    @Column(name = "COBROCTACAJANOMBRE")
    private String cashBoxCollectionAccountName;

    @Column(name = "COBROPLANOMBREGES")
    private String payrollCollectionName;

    @Column(name = "DESCRIPCION")
    private String description;

    @Column(name = "OBSERVACION")
    private String observation;

    @Column(name = "MONEDAPAGO")
    @Enumerated(EnumType.STRING)
    private FinancesCurrencyType paymentCurrency;

    @Column(name = "MONTOPAGO", precision = 12, scale = 2)
    private BigDecimal paymentAmount;

    @Column(name = "MONEDACOBRO")
    @Enumerated(EnumType.STRING)
    private FinancesCurrencyType collectionCurrency;

    @Column(name = "MONTOCOBRO", precision = 12, scale = 2)
    private BigDecimal collectionAmount;

    @Column(name = "TIPOCAMBIO", precision = 12, scale = 2)
    private BigDecimal exchangeRate;

    @Column(name = "ESTADO", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private RotatoryFundMovementState state;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "IDCOMPANIA", nullable = false, updatable = false, insertable = true)
    @NotNull
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IDFONDOROTATORIO")
    private RotatoryFund rotatoryFund;

    @Column(name = "NOTRANS")
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