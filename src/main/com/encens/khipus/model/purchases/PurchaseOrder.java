package com.encens.khipus.model.purchases;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyNumberListener;
import com.encens.khipus.model.UpperCaseStringListener;
import com.encens.khipus.model.admin.BusinessUnit;
import com.encens.khipus.model.employees.Employee;
import com.encens.khipus.model.employees.Month;
import com.encens.khipus.model.finances.CollectionDocumentType;
import com.encens.khipus.model.finances.CostCenter;
import com.encens.khipus.model.finances.JobContract;
import com.encens.khipus.model.finances.Provider;
import com.encens.khipus.model.fixedassets.PurchaseOrderCause;
import com.encens.khipus.model.fixedassets.PurchaseOrderFixedAssetPart;
import com.encens.khipus.model.warehouse.Warehouse;
import com.encens.khipus.util.Constants;
import com.encens.khipus.util.ValidatorUtil;
import org.hibernate.annotations.Filter;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * PurchaseOrder
 *
 * @author
 * @version 2.26
 */

@TableGenerator(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "PurchaseOrder.tableGenerator",
        table = com.encens.khipus.util.Constants.SEQUENCE_TABLE_NAME,
        pkColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_PK_COLUMN_NAME,
        valueColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_VALUE_COLUMN_NAME,
        pkColumnValue = "com_encoc",
        allocationSize = com.encens.khipus.util.Constants.SEQUENCE_ALLOCATION_SIZE)

@NamedQueries({
        @NamedQuery(name = "PurchaseOrder.findAll", query = "select purchase.id from PurchaseOrder purchase"),
        @NamedQuery(name = "PurchaseOrder.countByCompanyNumber", query = "select count(purchase.orderNumber) from PurchaseOrder purchase" +
                " where purchase.companyNumber=:companyNumber"),
        @NamedQuery(name = "PurchaseOrder.countByCompanyNumberAndType",
                query = "select count(purchase.orderNumber) from PurchaseOrder purchase where purchase.companyNumber=:companyNumber and purchase.orderType =:orderType")
})
@Entity
@EntityListeners({CompanyNumberListener.class, UpperCaseStringListener.class})
@Filter(name = com.encens.khipus.util.Constants.BUSINESS_UNIT_FILTER_NAME)
@Table(name = "com_encoc", schema = Constants.FINANCES_SCHEMA)
public class PurchaseOrder implements BaseModel {

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "PurchaseOrder.tableGenerator")
    @Column(name = "ID_COM_ENCOC", nullable = false)
    private Long id;

    @Column(name = "NO_CIA", length = 2, nullable = false)
    private String companyNumber;

    @Column(name = "NO_ORDEN", length = 100, nullable = false)
    @NotNull
    @Length(max = 100)
    private String orderNumber;

    @Column(name = "FECHA", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    @NotNull
    private Date date;

    @Column(name = "ESTADO", nullable = false)
    @Enumerated(EnumType.STRING)
    @NotNull
    private PurchaseOrderState state;

    @Column(name = "TIPO_RECEPCION")
    @Enumerated(EnumType.STRING)
    private PurchaseOrderReceivedType receivedType;

    @Column(name = "COD_ALM", length = 5)
    @Length(max = 5)
    private String warehouseCode;

    @ManyToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "NO_CIA", referencedColumnName = "NO_CIA", updatable = false, insertable = false),
            @JoinColumn(name = "COD_CC", referencedColumnName = "COD_CC", updatable = false, insertable = false)
    })
    private CostCenter costCenter;

    @Column(name = "COD_CC", length = 6, nullable = false)
    @Length(max = 6)
    private String costCenterCode;

    @Column(name = "COD_PROV", length = 6, nullable = false)
    @Length(max = 6)
    private String providerCode;

    @Column(name = "GLOSA", length = 250, nullable = false)
    @Length(max = 250)
    private String gloss;

    @Column(name = "FECHA_RECEPCION")
    @Temporal(TemporalType.DATE)
    private Date receptionDate;

    @Column(name = "MES_CONSUMO", length = 20)
    @Enumerated(EnumType.STRING)
    private Month consumeMonth;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "IDLUGARRECEPCION", nullable = false)
    private ReceptionPlace receptionPlace;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "IDCONDICIONPAGO", nullable = false)
    private PayConditions payConditions;

    @Column(name = "SUB_TOTAL", precision = 16, scale = 2)
    private BigDecimal subTotalAmount = BigDecimal.ZERO;

    @Column(name = "PORC_DESC", precision = 7, scale = 4)
    private BigDecimal discountPercent = BigDecimal.ZERO;

    @Column(name = "DESCUENTO", precision = 16, scale = 2)
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(name = "TOTAL", precision = 16, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Column(name = "NUMERO_FACTURA", nullable = true, length = 150)
    @Length(max = 150)
    private String invoiceNumber;

    @Column(name = "CONFACTURA",nullable = true,length = 50)
    @Length(max = 50)
    private String withBill;

    @Column(name = "TIPO", nullable = false, length = 25)
    @Enumerated(EnumType.STRING)
    private PurchaseOrderType orderType;

    @Column(name = "TIPODOCCOMPRA", nullable = false, length = 30)
    @Enumerated(EnumType.STRING)
    private CollectionDocumentType documentType;

    @com.encens.khipus.validator.BusinessUnit
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "IDUNIDADNEGOCIO", updatable = true, insertable = true)
    private BusinessUnit executorUnit;

    @Version
    @Column(name = "VERSION", nullable = false)
    private long version;

    @Transient
    private PurchaseOrderDetail defaultDetail = new PurchaseOrderDetail();

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumns({
            @JoinColumn(name = "NO_CIA", nullable = false, updatable = false, insertable = false),
            @JoinColumn(name = "COD_PROV", nullable = false, updatable = false, insertable = false)
    })
    private Provider provider;

    @ManyToOne
    @JoinColumn(name = "ID_RESPONSABLE", nullable = false)
    private Employee responsible;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "NO_CIA", updatable = false, insertable = false),
            @JoinColumn(name = "COD_ALM", updatable = false, insertable = false)
    })
    private Warehouse warehouse;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "purchaseOrder", cascade = CascadeType.ALL)
    @OrderBy("detailNumber asc")
    private List<PurchaseOrderDetail> purchaseOrderDetailList = new ArrayList<PurchaseOrderDetail>(0);

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "purchaseOrder", cascade = CascadeType.ALL)
    @OrderBy("detailNumber asc")
    private List<FixedAssetPurchaseOrderDetail> fixedAssetPurchaseOrderDetailList = new ArrayList<FixedAssetPurchaseOrderDetail>(0);

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "purchaseOrder", cascade = CascadeType.ALL)
    private List<PurchaseOrderFixedAssetPart> purchaseOrderFixedAssetPartList = new ArrayList<PurchaseOrderFixedAssetPart>(0);

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IDCONTRATOPUESTOSOL", referencedColumnName = "idcontratopuesto")
    private JobContract petitionerJobContract;

    @Column(name = "ESTADOPAGO", nullable = false)
    @Enumerated(EnumType.STRING)
    private PurchaseOrderPaymentStatus paymentStatus;

    @Column(name = "MONTOSALDO", nullable = false, precision = 16, scale = 2)
    private BigDecimal balanceAmount = BigDecimal.ZERO;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idmotivoordenc", referencedColumnName = "idmotivoordenc")
    private PurchaseOrderCause purchaseOrderCause;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCompanyNumber() {
        return companyNumber;
    }

    public void setCompanyNumber(String companyNumber) {
        this.companyNumber = companyNumber;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public PurchaseOrderState getState() {
        return state;
    }

    public void setState(PurchaseOrderState state) {
        this.state = state;
    }

    public PurchaseOrderReceivedType getReceivedType() {
        return receivedType;
    }

    public void setReceivedType(PurchaseOrderReceivedType receivedType) {
        this.receivedType = receivedType;
    }

    public Employee getResponsible() {
        return responsible;
    }

    public void setResponsible(Employee responsible) {
        this.responsible = responsible;
    }

    public Warehouse getWarehouse() {
        return warehouse;
    }

    public void setWarehouse(Warehouse warehouse) {
        this.warehouse = warehouse;
        setWarehouseCode(warehouse != null ? warehouse.getWarehouseCode() : null);
    }

    public String getWarehouseCode() {
        return warehouseCode;
    }

    public void setWarehouseCode(String warehouseCode) {
        this.warehouseCode = warehouseCode;
    }

    public CostCenter getCostCenter() {
        return costCenter;
    }

    public void setCostCenter(CostCenter costCenter) {
        this.costCenter = costCenter;
        setCostCenterCode(costCenter != null ? costCenter.getCode() : null);
    }

    public String getCostCenterCode() {
        return costCenterCode;
    }

    public void setCostCenterCode(String costCenterCode) {
        this.costCenterCode = costCenterCode;
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

    public String getGloss() {
        return gloss;
    }

    public void setGloss(String gloss) {
        this.gloss = gloss;
    }

    public Date getReceptionDate() {
        return receptionDate;
    }

    public void setReceptionDate(Date receptionDate) {
        this.receptionDate = receptionDate;
    }

    public ReceptionPlace getReceptionPlace() {
        return receptionPlace;
    }

    public void setReceptionPlace(ReceptionPlace receptionPlace) {
        this.receptionPlace = receptionPlace;
    }

    public PayConditions getPayConditions() {
        return payConditions;
    }

    public void setPayConditions(PayConditions payConditions) {
        this.payConditions = payConditions;
    }

    public BigDecimal getSubTotalAmount() {
        return subTotalAmount;
    }

    public void setSubTotalAmount(BigDecimal subTotalAmount) {
        this.subTotalAmount = subTotalAmount;
    }

    public BigDecimal getDiscountPercent() {
        return discountPercent;
    }

    public void setDiscountPercent(BigDecimal discountPercent) {
        this.discountPercent = discountPercent;
    }

    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(BigDecimal discountAmount) {
        this.discountAmount = discountAmount;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public List<PurchaseOrderDetail> getPurchaseOrderDetailList() {
        return purchaseOrderDetailList;
    }

    public void setPurchaseOrderDetailList(List<PurchaseOrderDetail> purchaseOrderDetailList) {
        this.purchaseOrderDetailList = purchaseOrderDetailList;
    }

    public PurchaseOrderDetail getDefaultDetail() {
        return defaultDetail;
    }

    public void setDefaultDetail(PurchaseOrderDetail defaultDetail) {
        this.defaultDetail = defaultDetail;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }

    public PurchaseOrderType getOrderType() {
        return orderType;
    }

    public void setOrderType(PurchaseOrderType orderType) {
        this.orderType = orderType;
    }

    public CollectionDocumentType getDocumentType() {
        return documentType;
    }

    public void setDocumentType(CollectionDocumentType documentType) {
        this.documentType = documentType;
    }

    public boolean isPurchaseOrderApproved() {
        return null != state && PurchaseOrderState.APR.equals(state);
    }

    public boolean isPurchaseOrderPending() {
        return null != state && PurchaseOrderState.PEN.equals(state);
    }

    public boolean isPurchaseOrderFinalized() {
        return null != state && PurchaseOrderState.FIN.equals(state);
    }

    public boolean isPurchaseOrderLiquidated() {
        return null != state && PurchaseOrderState.LIQ.equals(state);
    }

    public boolean isPurchaseOrderNullified() {
        return null != state && PurchaseOrderState.ANL.equals(state);
    }

    public List<FixedAssetPurchaseOrderDetail> getFixedAssetPurchaseOrderDetailList() {
        return fixedAssetPurchaseOrderDetailList;
    }

    public void setFixedAssetPurchaseOrderDetailList(List<FixedAssetPurchaseOrderDetail> fixedAssetPurchaseOrderDetailList) {
        this.fixedAssetPurchaseOrderDetailList = fixedAssetPurchaseOrderDetailList;
    }

    public List<PurchaseOrderFixedAssetPart> getPurchaseOrderFixedAssetPartList() {
        return purchaseOrderFixedAssetPartList;
    }

    public void setPurchaseOrderFixedAssetPartList(List<PurchaseOrderFixedAssetPart> purchaseOrderFixedAssetPartList) {
        this.purchaseOrderFixedAssetPartList = purchaseOrderFixedAssetPartList;
    }

    public BusinessUnit getExecutorUnit() {
        return executorUnit;
    }

    public void setExecutorUnit(BusinessUnit executorUnit) {
        this.executorUnit = executorUnit;
    }

    public Month getConsumeMonth() {
        return consumeMonth;
    }

    public void setConsumeMonth(Month consumeMonth) {
        this.consumeMonth = consumeMonth;
    }

    public JobContract getPetitionerJobContract() {
        return petitionerJobContract;
    }

    public void setPetitionerJobContract(JobContract petitionerJobContract) {
        this.petitionerJobContract = petitionerJobContract;
    }

    public PurchaseOrderPaymentStatus getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(PurchaseOrderPaymentStatus paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public BigDecimal getBalanceAmount() {
        return balanceAmount;
    }

    public void setBalanceAmount(BigDecimal balanceAmount) {
        this.balanceAmount = balanceAmount;
    }

    public boolean isNullified() {
        return null != getState() && PurchaseOrderState.ANL.equals(getState());
    }

    public boolean isFinalized() {
        return null != getState() && PurchaseOrderState.FIN.equals(getState());
    }

    public boolean isLiquidated() {
        return null != getState() && PurchaseOrderState.LIQ.equals(getState());
    }

    public PurchaseOrderCause getPurchaseOrderCause() {
        return purchaseOrderCause;
    }

    public void setPurchaseOrderCause(PurchaseOrderCause purchaseOrderCause) {
        this.purchaseOrderCause = purchaseOrderCause;
    }

    /*state helpers*/
    public boolean isAnnulled() {
        return state != null && state.equals(PurchaseOrderState.ANL);
    }

    public boolean isPurchaseDocumentRegisterPending() {
        return ValidatorUtil.isBlankOrNull(getInvoiceNumber()) && null != state &&
                !PurchaseOrderState.ANL.equals(state) &&
                !PurchaseOrderState.PEN.equals(state);
    }

    public String getWithBill() {
        return withBill;
    }

    public void setWithBill(String withBill) {
        this.withBill = withBill;
    }
}
