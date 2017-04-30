package com.encens.khipus.model.purchases;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyNumberListener;
import com.encens.khipus.model.UpperCaseStringListener;
import com.encens.khipus.model.finances.FinancesCurrencyType;
import com.encens.khipus.model.fixedassets.FixedAssetSubGroup;
import com.encens.khipus.model.fixedassets.Model;
import com.encens.khipus.model.fixedassets.PurchaseOrderDetailPart;
import com.encens.khipus.model.fixedassets.Trademark;
import com.encens.khipus.util.Constants;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;
import org.hibernate.validator.Range;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * FixedAssetPurchaseOrderDetail
 *
 * @author
 * @version 2.2
 */
@NamedQueries({
        @NamedQuery(name = "FixedAssetPurchaseOrderDetail.maxByPurchaseOrder",
                query = "select max(p.detailNumber) from FixedAssetPurchaseOrderDetail p where p.purchaseOrder=:purchaseOrder"),
        @NamedQuery(name = "FixedAssetPurchaseOrderDetail.findByPurchaseOrder", query = "select p from FixedAssetPurchaseOrderDetail p" +
                " where p.purchaseOrder=:purchaseOrder"),
        @NamedQuery(name = "FixedAssetPurchaseOrderDetail.countByPurchaseOrder", query = "select count(p) from FixedAssetPurchaseOrderDetail p" +
                " where p.purchaseOrder=:purchaseOrder"),
        @NamedQuery(name = "FixedAssetPurchaseOrderDetail.sumBsTotalAmounts",
                query = "select sum(detail.bsTotalAmount) from FixedAssetPurchaseOrderDetail detail where detail.purchaseOrder =:purchaseOrder")
})
@TableGenerator(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "FixedAssetPurchaseOrderDetail.tableGenerator",
        table = com.encens.khipus.util.Constants.SEQUENCE_TABLE_NAME,
        pkColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_PK_COLUMN_NAME,
        valueColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_VALUE_COLUMN_NAME,
        pkColumnValue = "com_af_detoc",
        allocationSize = com.encens.khipus.util.Constants.SEQUENCE_ALLOCATION_SIZE)
@Entity
@EntityListeners({CompanyNumberListener.class, UpperCaseStringListener.class})
@Table(name = "com_af_detoc", schema = Constants.FINANCES_SCHEMA, uniqueConstraints = @UniqueConstraint(columnNames = {"NO_CIA", "NO_ORDEN", "NRO"}))
public class FixedAssetPurchaseOrderDetail implements BaseModel {

    @Id
    @Column(name = "ID_COM_AF_DETOC", nullable = false, updatable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "FixedAssetPurchaseOrderDetail.tableGenerator")
    private Long id;

    @Column(name = "NO_CIA", nullable = false, updatable = false)
    @NotNull
    private String companyNumber;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumns({
            @JoinColumn(name = "ID_COM_ENCOC", referencedColumnName = "ID_COM_ENCOC", nullable = false, updatable = false, insertable = true)
    })
    private PurchaseOrder purchaseOrder;

    @Column(name = "NO_ORDEN", nullable = false)
    @NotNull
    private String orderNumber;

    /* The number of the item in the list*/
    @Column(name = "NRO", nullable = false, updatable = false)
    @NotNull
    private Long detailNumber;

    @Column(name = "CANT_SOL", precision = 16, scale = 2, nullable = false)
    @Range(min = 1, max = 1000)
    @NotNull
    private Integer requestedQuantity;

    @Column(name = "TOTAL_BS", precision = 16, scale = 2)
    private BigDecimal bsTotalAmount;

    @Column(name = "TOTAL_SUS", precision = 16, scale = 2)
    private BigDecimal susTotalAmount;

    @Column(name = "TOTAL_UFV", precision = 16, scale = 2)
    private BigDecimal ufvTotalAmount;

    /* Individual detail for each fixedAsset*/
    @Column(name = "detalle", length = 250, nullable = false)
    @Length(max = 250)
    @NotNull
    private String detail;

    /* the dimensions of the fixedAsset */
    @Column(name = "medida", nullable = false, length = 250)
    @Length(max = 250)
    @NotNull
    private String measurement;

    @Column(name = "marca", length = 30)
    @Length(max = 30)
    private String trademark;

    @Column(name = "modelo", length = 30)
    @Length(max = 30)
    private String model;

    @Column(name = "voBs", nullable = false, precision = 16, scale = 2)
    @NotNull
    private BigDecimal bsUnitPriceValue;

    @Column(name = "voSus", nullable = false, precision = 16, scale = 2)
    @NotNull
    private BigDecimal susUnitPriceValue;

    @Column(name = "voUfv", nullable = false, precision = 16, scale = 2)
    @NotNull
    private BigDecimal ufvUnitPriceValue;

    @Column(name = "desecho", precision = 12, scale = 2)
    private BigDecimal rubbish;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumns({
            /*already defined in pk, so properties in null*/
            @JoinColumn(name = "NO_CIA", referencedColumnName = "no_cia", nullable = false, updatable = false, insertable = false),
            @JoinColumn(name = "grupo", referencedColumnName = "grupo", nullable = false, insertable = false, updatable = false),
            @JoinColumn(name = "subgrupo", referencedColumnName = "subgrupo", nullable = false, updatable = false, insertable = false)
    })
    private FixedAssetSubGroup fixedAssetSubGroup;

    @Column(name = "subgrupo", length = 3, nullable = false)
    @Length(max = 3)
    private String fixedAssetSubGroupCode;

    @Column(name = "grupo", length = 3, nullable = false)
    @Length(max = 3)
    private String fixedAssetGroupCode;

    /* payment currency */
    @Column(name = "moneda")
    @Enumerated(EnumType.STRING)
    private FinancesCurrencyType paymentCurrencyType;

    @Column(name = "tasaBsSus", nullable = false, precision = 16, scale = 6)
    @NotNull
    private BigDecimal bsSusRate;

    @Column(name = "tasaBsUfv", nullable = false, precision = 16, scale = 6)
    @NotNull
    private BigDecimal bsUfvRate;

    @Column(name = "MESESGARANTIA")
    @Range(min = 1, max = 999)
    private Integer monthsGuaranty;

    @Version
    @Column(name = "VERSION", nullable = false)
    private long version;

    @ManyToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "IDMARCA", nullable = true, updatable = true, insertable = true)
    private Trademark trademarkEntity;

    @Transient
    private String trademarkName;

    @ManyToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "IDMODELO", nullable = true, updatable = true, insertable = true)
    private Model modelEntity;

    @Transient
    private String modelName;

    @Column(name = "DURACION_TOTAL", nullable = false, updatable = false)
    @NotNull
    private Integer totalDuration;

    @Column(name = "DURACION_USADA", nullable = false, updatable = false)
    @NotNull
    private Integer usageDuration;

    @Column(name = "DURACION_NETA", nullable = false, updatable = false)
    @NotNull
    private Integer netDuration;

    @OneToMany(mappedBy = "detail", fetch = FetchType.LAZY)
    private List<PurchaseOrderDetailPart> orderDetailPartList = new ArrayList<PurchaseOrderDetailPart>(0);

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

    public PurchaseOrder getPurchaseOrder() {
        return purchaseOrder;
    }

    public void setPurchaseOrder(PurchaseOrder purchaseOrder) {
        this.purchaseOrder = purchaseOrder;
        setOrderNumber(this.purchaseOrder != null ? this.purchaseOrder.getOrderNumber() : null);
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public Long getDetailNumber() {
        return detailNumber;
    }

    public void setDetailNumber(Long detailNumber) {
        this.detailNumber = detailNumber;
    }

    public Integer getRequestedQuantity() {
        return requestedQuantity;
    }

    public void setRequestedQuantity(Integer requestedQuantity) {
        this.requestedQuantity = requestedQuantity;
    }

    public BigDecimal getBsTotalAmount() {
        return bsTotalAmount;
    }

    public void setBsTotalAmount(BigDecimal bsTotalAmount) {
        this.bsTotalAmount = bsTotalAmount;
    }

    public BigDecimal getSusTotalAmount() {
        return susTotalAmount;
    }

    public void setSusTotalAmount(BigDecimal susTotalAmount) {
        this.susTotalAmount = susTotalAmount;
    }

    public BigDecimal getUfvTotalAmount() {
        return ufvTotalAmount;
    }

    public void setUfvTotalAmount(BigDecimal ufvTotalAmount) {
        this.ufvTotalAmount = ufvTotalAmount;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public String getTrademark() {
        return trademark;
    }

    public void setTrademark(String trademark) {
        this.trademark = trademark;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public BigDecimal getRubbish() {
        return rubbish;
    }

    public void setRubbish(BigDecimal rubbish) {
        this.rubbish = rubbish;
    }

    public BigDecimal getBsUnitPriceValue() {
        return bsUnitPriceValue;
    }

    public void setBsUnitPriceValue(BigDecimal bsUnitPriceValue) {
        this.bsUnitPriceValue = bsUnitPriceValue;
    }

    public BigDecimal getSusUnitPriceValue() {
        return susUnitPriceValue;
    }

    public void setSusUnitPriceValue(BigDecimal susUnitPriceValue) {
        this.susUnitPriceValue = susUnitPriceValue;
    }

    public BigDecimal getUfvUnitPriceValue() {
        return ufvUnitPriceValue;
    }

    public void setUfvUnitPriceValue(BigDecimal ufvUnitPriceValue) {
        this.ufvUnitPriceValue = ufvUnitPriceValue;
    }

    public BigDecimal getBsSusRate() {
        return bsSusRate;
    }

    public void setBsSusRate(BigDecimal bsSusRate) {
        this.bsSusRate = bsSusRate;
    }

    public BigDecimal getBsUfvRate() {
        return bsUfvRate;
    }

    public void setBsUfvRate(BigDecimal bsUfvRate) {
        this.bsUfvRate = bsUfvRate;
    }

    public String getMeasurement() {
        return measurement;
    }

    public void setMeasurement(String measurement) {
        this.measurement = measurement;
    }

    public FixedAssetSubGroup getFixedAssetSubGroup() {
        return fixedAssetSubGroup;
    }

    public void setFixedAssetSubGroup(FixedAssetSubGroup fixedAssetSubGroup) {
        this.fixedAssetSubGroup = fixedAssetSubGroup;
        if (null != fixedAssetSubGroup) {
            this.setFixedAssetSubGroupCode(fixedAssetSubGroup.getId().getFixedAssetSubGroupCode());
            this.setFixedAssetGroupCode(fixedAssetSubGroup.getId().getFixedAssetGroupCode());
        }
    }

    public String getFixedAssetSubGroupCode() {
        return fixedAssetSubGroupCode;
    }

    public void setFixedAssetSubGroupCode(String fixedAssetSubGroupCode) {
        this.fixedAssetSubGroupCode = fixedAssetSubGroupCode;
    }

    public String getFixedAssetGroupCode() {
        return fixedAssetGroupCode;
    }

    public void setFixedAssetGroupCode(String fixedAssetGroupCode) {
        this.fixedAssetGroupCode = fixedAssetGroupCode;
    }

    public FinancesCurrencyType getPaymentCurrencyType() {
        return paymentCurrencyType;
    }

    public void setPaymentCurrencyType(FinancesCurrencyType paymentCurrencyType) {
        this.paymentCurrencyType = paymentCurrencyType;
    }

    public Integer getMonthsGuaranty() {
        return monthsGuaranty;
    }

    public void setMonthsGuaranty(Integer monthsGuaranty) {
        this.monthsGuaranty = monthsGuaranty;
    }

    public List<PurchaseOrderDetailPart> getOrderDetailPartList() {
        return orderDetailPartList;
    }

    public void setOrderDetailPartList(List<PurchaseOrderDetailPart> orderDetailPartList) {
        this.orderDetailPartList = orderDetailPartList;
    }

    public Trademark getTrademarkEntity() {
        return trademarkEntity;
    }

    public void setTrademarkEntity(Trademark trademarkEntity) {
        this.trademarkEntity = trademarkEntity;
    }

    public String getTrademarkName() {
        return trademarkName;
    }

    public void setTrademarkName(String trademarkName) {
        this.trademarkName = trademarkName;
    }

    public Model getModelEntity() {
        return modelEntity;
    }

    public void setModelEntity(Model modelEntity) {
        this.modelEntity = modelEntity;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public Integer getTotalDuration() {
        return totalDuration;
    }

    public void setTotalDuration(Integer totalDuration) {
        this.totalDuration = totalDuration;
    }

    public Integer getUsageDuration() {
        return usageDuration;
    }

    public void setUsageDuration(Integer usageDuration) {
        this.usageDuration = usageDuration;
    }

    public Integer getNetDuration() {
        return netDuration;
    }

    public void setNetDuration(Integer netDuration) {
        this.netDuration = netDuration;
    }

    public void putModelName() {
        if (null != modelEntity) {
            this.modelName = modelEntity.getName();
        } else {
            this.modelName = null;
        }
    }

    public void putTrademarkName() {
        if (null != trademarkEntity) {
            this.trademarkName = trademarkEntity.getName();
        } else {
            this.trademarkName = null;
        }
    }
}
