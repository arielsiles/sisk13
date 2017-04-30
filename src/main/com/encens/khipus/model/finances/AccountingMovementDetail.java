package com.encens.khipus.model.finances;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyNumberListener;
import com.encens.khipus.model.UpperCaseStringListener;
import com.encens.khipus.model.admin.BusinessUnit;
import com.encens.khipus.model.purchases.PurchaseOrderPayment;
import com.encens.khipus.util.Constants;
import org.hibernate.validator.Length;

import javax.persistence.*;
import java.math.BigDecimal;

/**
 * AccountingMovementDetail
 *
 * @author
 * @version 2.5
 */
@NamedQueries({
        @NamedQuery(name = "AccountingMovementDetail.countByTransactionNumber",
                query = "select count(accountMovementDetail.companyNumber) from AccountingMovementDetail accountMovementDetail where accountMovementDetail.transactionNumber =:transactionNumber and accountMovementDetail.companyNumber =:companyNumber"),
        @NamedQuery(name = "AccountingMovementDetail.sumDetail",
                query = "select sum(movementDetail.amount) from AccountingMovementDetail movementDetail where " +
                        " movementDetail.type=:type and movementDetail.accountingMovement=:accountingMovement and movementDetail.transactionNumber=:transactionNumber"),
        @NamedQuery(name = "AccountingMovementDetail.findDetail",
                query = "select movementDetail from AccountingMovementDetail movementDetail where " +
                        " movementDetail.type=:type and movementDetail.accountingMovement=:accountingMovement and movementDetail.transactionNumber=:transactionNumber"),
        @NamedQuery(name = "AccountingMovementDetail.sumByMovementDetail",
                query = "select sum(movementDetail.amount) from AccountingMovementDetail movementDetail where " +
                        " movementDetail.type=:type and movementDetail.accountingMovement=:accountingMovement and movementDetail.transactionNumber=:transactionNumber"),
        @NamedQuery(name = "AccountingMovementDetail.findTransactionNumber",
                query = "select max(movementDetail.transactionNumber) from AccountingMovementDetail movementDetail where " +
                        " movementDetail.accountingMovement=:accountingMovement"),
        @NamedQuery(name = "AccountingMovementDetail.findByPurchaseOrderPaymentType",
                query = "select distinct accountingMovement , purchaseOrderPayment " +
                        "from AccountingMovementDetail movementDetail" +
                        " left join movementDetail.accountingMovement accountingMovement" +
                        " left join movementDetail.purchaseOrderPayment purchaseOrderPayment" +
                        " where movementDetail.accountingMovement=:accountingMovement and purchaseOrderPayment.paymentType=:paymentType")

})


@Entity
@EntityListeners({CompanyNumberListener.class, UpperCaseStringListener.class})
@Table(name = "cg_movdet", schema = Constants.FINANCES_SCHEMA)
public class AccountingMovementDetail implements BaseModel {

    @EmbeddedId
    private AccountingMovementDetailPk id = new AccountingMovementDetailPk();

    @Column(name = "NO_CIA", nullable = false, updatable = false, insertable = false)
    private String companyNumber;

    @Column(name = "NO_TRANS", nullable = false, insertable = false, updatable = false)
    @Length(max = 10)
    private String transactionNumber;

    @Column(name = "NO_LINEA", nullable = false, insertable = false, updatable = false)
    @Length(max = 10)
    private String detailNumber;

    @Column(name = "TIPO_COMPRO", nullable = false)
    private String voucherType;

    @Column(name = "NO_COMPRO", nullable = false)
    private String voucherNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "NO_CIA", referencedColumnName = "NO_CIA", nullable = false, updatable = false, insertable = false),
            @JoinColumn(name = "TIPO_COMPRO", referencedColumnName = "TIPO_COMPRO", nullable = false, insertable = false, updatable = false),
            @JoinColumn(name = "NO_COMPRO", referencedColumnName = "NO_COMPRO", nullable = false, insertable = false, updatable = false)
    })
    private AccountingMovement accountingMovement;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "NO_CIA", referencedColumnName = "NO_CIA", nullable = false, updatable = false, insertable = false),
            @JoinColumn(name = "CUENTA", referencedColumnName = "CUENTA", nullable = false, insertable = false, updatable = false)
    })
    private CashAccount account;

    @Column(name = "COD_UNI", length = 8)
    private String executorUnitCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "COD_UNI", referencedColumnName = "codunidadejecutora", updatable = false, insertable = false)
    private BusinessUnit businessUnit;

    @Column(name = "COD_CC", length = 8)
    private String costCenterCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "NO_CIA", referencedColumnName = "NO_CIA", updatable = false, insertable = false),
            @JoinColumn(name = "COD_CC", referencedColumnName = "COD_CC", updatable = false, insertable = false)
    })
    private CostCenter costCenter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "NO_TRANS", referencedColumnName = "NO_TRANS", updatable = false, insertable = false)
    private Voucher voucher;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "NO_TRANS", referencedColumnName = "NOTRANS", updatable = false, insertable = false)
    private PurchaseOrderPayment purchaseOrderPayment;

    @Column(name = "TIPO_MOV", length = 1)
    @Enumerated(EnumType.STRING)
    private FinanceMovementType type;

    @Column(name = "MONTO_MN", precision = 16, scale = 2)
    private BigDecimal amount;

    @Column(name = "MONEDA", length = 3, nullable = false)
    @Enumerated(EnumType.STRING)
    private FinancesCurrencyType currency;

    @Column(name = "TC", precision = 10, scale = 6)
    private BigDecimal exchangeRate;

    @Column(name = "REFERENCIA")
    private String referencedDocument;

    public AccountingMovementDetailPk getId() {
        return id;
    }

    public void setId(AccountingMovementDetailPk id) {
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

    public String getDetailNumber() {
        return detailNumber;
    }

    public void setDetailNumber(String detailNumber) {
        this.detailNumber = detailNumber;
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

    public PurchaseOrderPayment getPurchaseOrderPayment() {
        return purchaseOrderPayment;
    }

    public void setPurchaseOrderPayment(PurchaseOrderPayment purchaseOrderPayment) {
        this.purchaseOrderPayment = purchaseOrderPayment;
    }

    public AccountingMovement getAccountingMovement() {
        return accountingMovement;
    }

    public void setAccountingMovement(AccountingMovement accountingMovement) {
        this.accountingMovement = accountingMovement;
    }

    public CashAccount getAccount() {
        return account;
    }

    public void setAccount(CashAccount account) {
        this.account = account;
    }

    public String getExecutorUnitCode() {
        return executorUnitCode;
    }

    public void setExecutorUnitCode(String executorUnitCode) {
        this.executorUnitCode = executorUnitCode;
    }

    public String getCostCenterCode() {
        return costCenterCode;
    }

    public void setCostCenterCode(String costCenterCode) {
        this.costCenterCode = costCenterCode;
    }

    public CostCenter getCostCenter() {
        return costCenter;
    }

    public void setCostCenter(CostCenter costCenter) {
        this.costCenter = costCenter;
    }

    public FinanceMovementType getType() {
        return type;
    }

    public void setType(FinanceMovementType type) {
        this.type = type;
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

    public String getReferencedDocument() {
        return referencedDocument;
    }

    public void setReferencedDocument(String referencedDocument) {
        this.referencedDocument = referencedDocument;
    }

    public BusinessUnit getBusinessUnit() {
        return businessUnit;
    }

    public void setBusinessUnit(BusinessUnit businessUnit) {
        this.businessUnit = businessUnit;
    }

    public Voucher getVoucher() {
        return voucher;
    }

    public void setVoucher(Voucher voucher) {
        this.voucher = voucher;
    }

    @Override
    public String toString() {
        return "\n\tAccountingMovementDetail{" +
                "companyNumber='" + companyNumber + '\'' +
                ", transactionNumber='" + transactionNumber + '\'' +
                ", detailNumber='" + detailNumber + '\'' +
                ", voucherType='" + voucherType + '\'' +
                ", voucherNumber='" + voucherNumber + '\'' +
                ", account=" + account +
                ", executorUnitCode='" + executorUnitCode + '\'' +
                ", costCenterCode='" + costCenterCode + '\'' +
                ", type=" + type +
                ", amount=" + amount +
                ", currency=" + currency +
                ", exchangeRate=" + exchangeRate +
                ", referencedDocument='" + referencedDocument + '\'' +
                '}';
    }
}
