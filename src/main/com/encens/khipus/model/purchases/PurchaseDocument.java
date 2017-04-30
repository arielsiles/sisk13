package com.encens.khipus.model.purchases;

import com.encens.khipus.model.CompanyNumberListener;
import com.encens.khipus.model.UpperCaseStringListener;
import com.encens.khipus.model.admin.Company;
import com.encens.khipus.model.finances.*;
import org.hibernate.validator.Length;

import javax.persistence.*;
import java.math.BigDecimal;

/**
 * @author
 * @version 2.25
 */

@NamedQueries({
        @NamedQuery(name = "PurchaseDocument.sumAmountsByPurchaseOrderAndState",
                query = "select sum(purchaseDocument.amount) from PurchaseDocument purchaseDocument" +
                        " where purchaseDocument.purchaseOrderId = :purchaseOrderId and purchaseDocument.state =:state"),
        @NamedQuery(name = "PurchaseDocument.countDistinctByPurchaseOrder",
                query = "select count(purchaseDocument) from PurchaseDocument purchaseDocument" +
                        " where purchaseDocument.purchaseOrderId =:purchaseOrderId and purchaseDocument.type not in(:typeEnumList) and purchaseDocument.state<>:state"),
        @NamedQuery(name = "PurchaseDocument.findByState",
                query = "select purchaseDocument from PurchaseDocument purchaseDocument where purchaseDocument.state =:state and purchaseDocument.purchaseOrderId =:purchaseOrderId"),
        @NamedQuery(name = "PurchaseDocument.countByState",
                query = "select count(purchaseDocument) from PurchaseDocument purchaseDocument where purchaseDocument.state=:state and purchaseDocument.purchaseOrderId=:purchaseOrderId "),
        @NamedQuery(name = "PurchaseDocument.findByStateAndVoucherState",
                query = "select purchaseDocument from PurchaseDocument purchaseDocument" +
                        " where purchaseDocument.state =:state" +
                        " and purchaseDocument.purchaseOrderId =:purchaseOrderId" +
                        " and purchaseDocument.hasVoucher =:hasVoucher"),
        @NamedQuery(name = "PurchaseDocument.findByOrderVoucher",
                query = "select purchaseDocument from PurchaseDocument purchaseDocument" +
                        " where purchaseDocument.purchaseOrderId =:purchaseOrderId"
                        )

}
)


@Entity
@PrimaryKeyJoinColumns(value = {
        @PrimaryKeyJoinColumn(name = "iddocumentocompra", referencedColumnName = "iddocumentocontable")
})


@EntityListeners({CompanyNumberListener.class, UpperCaseStringListener.class})
@Table(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "DOCUMENTOCOMPRA")
public class PurchaseDocument extends AccountingDocument {
    @Column(name = "TIPO", nullable = false, length = 25)
    @Enumerated(EnumType.STRING)
    private CollectionDocumentType type;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "IDCOMPANIA", nullable = false, updatable = false, insertable = true)
    private Company company;

    @Column(name = "ESTADO", nullable = false, length = 25)
    @Enumerated(EnumType.STRING)
    private PurchaseDocumentState state;

    @Column(name = "MONEDA", updatable = false)
    @Enumerated(EnumType.STRING)
    private FinancesCurrencyType currency;

    @Column(name = "TIPOCAMBIO", precision = 16, scale = 6, updatable = true)
    private BigDecimal exchangeRate;

    @Column(name = "IDORDENCOMPRA", nullable = true, insertable = false, updatable = false)
    private Long purchaseOrderId;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "IDORDENCOMPRA", nullable = true, insertable = true, updatable = false)
    private PurchaseOrder purchaseOrder;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "IDTMPENC", nullable = true, insertable = true, updatable = false)
    private Voucher voucher;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "IDENTIDAD", referencedColumnName = "COD_ENTI", nullable = true, insertable = true, updatable = true)
    private FinancesEntity financesEntity;

    @Column(name = "NO_CIA", length = 2)
    @Length(max = 2)
    private String companyNumber;

    @Column(name = "CUENTAAJUSTE", length = 20)
    @Length(max = 20)
    private String cashAccountCodeAdjustment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "NO_CIA", referencedColumnName = "NO_CIA", updatable = false, insertable = false),
            @JoinColumn(name = "CUENTAAJUSTE", referencedColumnName = "CUENTA", updatable = false, insertable = false)
    })
    private CashAccount cashAccountAdjustment;

    public CollectionDocumentType getType() {
        return type;
    }

    public void setType(CollectionDocumentType type) {
        this.type = type;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public PurchaseDocumentState getState() {
        return state;
    }

    public void setState(PurchaseDocumentState state) {
        this.state = state;
    }

    public PurchaseOrder getPurchaseOrder() {
        return purchaseOrder;
    }

    public void setPurchaseOrder(PurchaseOrder purchaseOrder) {
        this.purchaseOrder = purchaseOrder;
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

    public FinancesEntity getFinancesEntity() {
        return financesEntity;
    }

    public void setFinancesEntity(FinancesEntity financesEntity) {
        this.financesEntity = financesEntity;
    }

    public Long getPurchaseOrderId() {
        return purchaseOrderId;
    }

    public void setPurchaseOrderId(Long purchaseOrderId) {
        this.purchaseOrderId = purchaseOrderId;
    }

    public String getCompanyNumber() {
        return companyNumber;
    }

    public void setCompanyNumber(String companyNumber) {
        this.companyNumber = companyNumber;
    }

    public String getCashAccountCodeAdjustment() {
        return cashAccountCodeAdjustment;
    }

    public void setCashAccountCodeAdjustment(String cashAccountCodeAdjustment) {
        this.cashAccountCodeAdjustment = cashAccountCodeAdjustment;
    }

    public CashAccount getCashAccountAdjustment() {
        return cashAccountAdjustment;
    }

    public void setCashAccountAdjustment(CashAccount cashAccountAdjustment) {
        this.cashAccountAdjustment = cashAccountAdjustment;
        setCashAccountCodeAdjustment(cashAccountAdjustment != null ? cashAccountAdjustment.getAccountCode() : null);
    }

    public boolean isInvoiceDocument() {
        return null != type && CollectionDocumentType.INVOICE.equals(type);
    }

    public boolean isReceiptDocument() {
        return null != type && CollectionDocumentType.RECEIPT.equals(type);
    }

    public boolean isAdjustmentDocument() {
        return null != type && CollectionDocumentType.ADJUSTMENT.equals(type);
    }

    public boolean isApproved() {
        return null != state && PurchaseDocumentState.APPROVED.equals(state);
    }

    public boolean isPending() {
        return null != state && PurchaseDocumentState.PENDING.equals(state);
    }

    public boolean isNullified() {
        return null != state && PurchaseDocumentState.NULLIFIED.equals(state);
    }

    public boolean isLocalCurrencyUsed() {
        return null != currency && FinancesCurrencyType.P.equals(currency);
    }

    public Voucher getVoucher() {
        return voucher;
    }

    public void setVoucher(Voucher voucher) {
        this.voucher = voucher;
    }
}
