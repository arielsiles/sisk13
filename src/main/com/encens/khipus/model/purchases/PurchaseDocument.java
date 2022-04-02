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

        /*@NamedQuery(name = "PurchaseDocument.findByVoucher",
                query = "select purchaseDocument from PurchaseDocument purchaseDocument where purchaseDocument.voucher =:voucher"),*/

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
@Table(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "documentocompra")
public class PurchaseDocument extends AccountingDocument {
    @Column(name = "tipo", length = 25)
    @Enumerated(EnumType.STRING)
    private CollectionDocumentType type;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idcompania", nullable = false, updatable = false, insertable = true)
    private Company company;

    @Column(name = "estado", length = 25)
    @Enumerated(EnumType.STRING)
    private PurchaseDocumentState state;

    @Column(name = "moneda", updatable = false)
    @Enumerated(EnumType.STRING)
    private FinancesCurrencyType currency;

    @Column(name = "tipocambio", precision = 16, scale = 6, updatable = true)
    private BigDecimal exchangeRate;

    @Column(name = "idordencompra", nullable = true, insertable = false, updatable = false)
    private Long purchaseOrderId;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "idordencompra", nullable = true, insertable = true, updatable = false)
    private PurchaseOrder purchaseOrder;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "idtmpenc", nullable = true, insertable = true, updatable = true)
    private Voucher voucher;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "identidad", referencedColumnName = "cod_enti", nullable = true, insertable = true, updatable = true)
    private FinancesEntity financesEntity;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "id_tmpdet", nullable = true, insertable = true, updatable = true)
    private VoucherDetail voucherDetailFiscalCredit;

    @Column(name = "no_cia", length = 2)
    @Length(max = 2)
    private String companyNumber;

    @Column(name = "cuentaajuste", length = 20)
    @Length(max = 20)
    private String cashAccountCodeAdjustment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "no_cia", referencedColumnName = "no_cia", updatable = false, insertable = false),
            @JoinColumn(name = "cuentaajuste", referencedColumnName = "cuenta", updatable = false, insertable = false)
    })
    private CashAccount cashAccountAdjustment;

    @Transient
    private String financesEntityFullName;

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

    public String getFinancesEntityFullName() {
        return financesEntityFullName;
    }

    public void setFinancesEntityFullName(String financesEntityFullName) {
        this.financesEntityFullName = financesEntityFullName;
    }

    public String getFullName(){
        return getNit() + " " + getName();
    }

    public VoucherDetail getVoucherDetailFiscalCredit() {
        return voucherDetailFiscalCredit;
    }

    public void setVoucherDetailFiscalCredit(VoucherDetail voucherDetailFiscalCredit) {
        this.voucherDetailFiscalCredit = voucherDetailFiscalCredit;
    }
}
