package com.encens.khipus.model.purchases;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyNumberListener;
import com.encens.khipus.model.UpperCaseStringListener;
import com.encens.khipus.model.finances.MeasureUnit;
import com.encens.khipus.model.warehouse.ProductItem;
import com.encens.khipus.model.warehouse.ProductItemByProviderHistory;
import com.encens.khipus.util.Constants;
import org.hibernate.validator.Length;

import javax.persistence.*;
import java.math.BigDecimal;

/**
 * PurchaseOrderDetail
 *
 * @author
 * @version 2.0
 */

@NamedQueries({
        @NamedQuery(name = "PurchaseOrderDetail.maxByPurchaseOrder",
                query = "select max(p.detailNumber) from PurchaseOrderDetail p where p.purchaseOrder=:purchaseOrder"),
        @NamedQuery(name = "PurchaseOrderDetail.findByPurchaseOrder", query = "select p from PurchaseOrderDetail p" +
                " where p.purchaseOrder=:purchaseOrder"),
        @NamedQuery(name = "PurchaseOrderDetail.countByPurchaseOrder", query = "select count(p) from PurchaseOrderDetail p" +
                " where p.purchaseOrder=:purchaseOrder"),
        @NamedQuery(name = "PurchaseOrderDetail.sumTotalAmounts",
                query = "select sum(detail.totalAmount) from PurchaseOrderDetail detail where detail.purchaseOrder =:purchaseOrder"),
        @NamedQuery(name = "PurchaseOrderDetail.countByProductItemAndPurchaseOrder",
                query = "select sum(detail.id) from PurchaseOrderDetail detail" +
                        " where detail.purchaseOrder =:purchaseOrder and detail.productItemCode=:productItemCode"),
        @NamedQuery(name = "PurchaseOrderDetail.countByProductItemAndPurchaseOrderDetail",
                query = "select sum(detail.id) from PurchaseOrderDetail detail" +
                        " where detail<>:detail and detail.purchaseOrder =:purchaseOrder and detail.productItemCode=:productItemCode")
})
@TableGenerator(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "PurchaseOrderDetail.tableGenerator",
        table = com.encens.khipus.util.Constants.SEQUENCE_TABLE_NAME,
        pkColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_PK_COLUMN_NAME,
        valueColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_VALUE_COLUMN_NAME,
        pkColumnValue = "com_detoc",
        allocationSize = com.encens.khipus.util.Constants.SEQUENCE_ALLOCATION_SIZE)
@Entity
@EntityListeners({CompanyNumberListener.class, UpperCaseStringListener.class})
@Table(name = "com_detoc", schema = Constants.FINANCES_SCHEMA, uniqueConstraints = @UniqueConstraint(columnNames = {"no_cia", "no_orden", "nro"}))
public class PurchaseOrderDetail implements BaseModel {

    @Id
    @Column(name = "id_com_detoc", nullable = false, updatable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "PurchaseOrderDetail.tableGenerator")
    private Long id;

    @Column(name = "no_cia", nullable = false, updatable = false)
    private String companyNumber;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumns({
            @JoinColumn(name = "id_com_encoc", nullable = false, updatable = false, insertable = true)
    })
    private PurchaseOrder purchaseOrder;

    @Column(name = "cod_med", nullable = true, length = 6)
    @Length(max = 6)
    private String purchaseMeasureCode;

    @Column(name = "no_orden", nullable = false)
    private String orderNumber;

    @Column(name = "nro", nullable = false, updatable = false)
    private Long detailNumber;

    @Column(name = "cant_sol", precision = 16, scale = 2, nullable = false)
    private BigDecimal requestedQuantity;

    @Column(name = "costo_uni", precision = 16, scale = 6)
    private BigDecimal unitCost;

    @Column(name = "total", precision = 16, scale = 6)
    private BigDecimal totalAmount;

    @Column(name = "advertencia", length = 250)
    @Length(max = 250)
    private String warning;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumns({
            @JoinColumn(name = "no_cia", nullable = false, updatable = false, insertable = false),
            @JoinColumn(name = "cod_art", nullable = false, updatable = false, insertable = false)
    })
    private ProductItem productItem;

    @Column(name = "cod_art", nullable = false)
    private String productItemCode;

    @Version
    @Column(name = "version", nullable = false)
    private long version;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumns({
            @JoinColumn(name = "no_cia", nullable = false, updatable = false, insertable = false),
            @JoinColumn(name = "cod_med", nullable = false, updatable = false, insertable = false)
    })
    private MeasureUnit purchaseMeasureUnit;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idhistorialarticuloprov")
    private ProductItemByProviderHistory productItemByProviderHistory;

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

    public BigDecimal getRequestedQuantity() {
        return requestedQuantity;
    }

    public void setRequestedQuantity(BigDecimal requestedQuantity) {
        this.requestedQuantity = requestedQuantity;
    }

    public BigDecimal getUnitCost() {
        return unitCost;
    }

    public void setUnitCost(BigDecimal unitCost) {
        this.unitCost = unitCost;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public ProductItem getProductItem() {
        return productItem;
    }

    public void setProductItem(ProductItem productItem) {
        this.productItem = productItem;
        setProductItemCode(this.productItem != null ? this.productItem.getId().getProductItemCode() : null);
    }

    public String getProductItemCode() {
        return productItemCode;
    }

    public void setProductItemCode(String productItemCode) {
        this.productItemCode = productItemCode;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public String getPurchaseMeasureCode() {
        return purchaseMeasureCode;
    }

    public void setPurchaseMeasureCode(String purchaseMeasureCode) {
        this.purchaseMeasureCode = purchaseMeasureCode;
    }

    public MeasureUnit getPurchaseMeasureUnit() {
        return purchaseMeasureUnit;
    }

    public void setPurchaseMeasureUnit(MeasureUnit purchaseMeasureUnit) {
        this.purchaseMeasureUnit = purchaseMeasureUnit;
        if (null != purchaseMeasureUnit) {
            setPurchaseMeasureCode(purchaseMeasureUnit.getId().getMeasureUnitCode());
        } else {
            setPurchaseMeasureCode(null);
        }
    }

    public ProductItemByProviderHistory getProductItemByProviderHistory() {
        return productItemByProviderHistory;
    }

    public void setProductItemByProviderHistory(ProductItemByProviderHistory productItemByProviderHistory) {
        this.productItemByProviderHistory = productItemByProviderHistory;
    }

    public String getWarning() {
        return warning;
    }

    public void setWarning(String warning) {
        this.warning = warning;
    }

    @Override
    public String toString() {
        return "PurchaseOrderDetail{" +
                "id=" + id +
                ", companyNumber='" + companyNumber + '\'' +
                ", purchaseOrder=" + purchaseOrder +
                ", orderNumber='" + orderNumber + '\'' +
                ", detailNumber=" + detailNumber +
                ", requestedQuantity=" + requestedQuantity +
                ", unitCost=" + unitCost +
                ", totalAmount=" + totalAmount +
                ", productItem=" + productItem +
                ", productItemCode='" + productItemCode + '\'' +
                ", version=" + version +
                '}';
    }
}
