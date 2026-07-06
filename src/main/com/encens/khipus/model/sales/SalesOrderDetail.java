package com.encens.khipus.model.sales;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyNumberListener;
import com.encens.khipus.model.warehouse.ProductItem;
import com.encens.khipus.util.Constants;
import org.hibernate.validator.Length;

import javax.persistence.*;
import java.math.BigDecimal;

/**
 * Linea de una {@link SalesOrder}. Referencia un producto terminado
 * ({@link ProductItem}) y un centro de costo comercial. La descripcion y la
 * unidad de medida se copian del producto pero quedan editables.
 *
 * @author
 * @version 1.0
 */
@NamedQueries({
        @NamedQuery(name = "SalesOrderDetail.maxBySalesOrder",
                query = "select max(d.detailNumber) from SalesOrderDetail d where d.salesOrder =:salesOrder"),
        @NamedQuery(name = "SalesOrderDetail.sumTotalAmounts",
                query = "select sum(d.totalAmount) from SalesOrderDetail d where d.salesOrder =:salesOrder")
})

@TableGenerator(schema = Constants.KHIPUS_SCHEMA, name = "SalesOrderDetail.tableGenerator",
        table = Constants.SEQUENCE_TABLE_NAME,
        pkColumnName = Constants.SEQUENCE_TABLE_PK_COLUMN_NAME,
        valueColumnName = Constants.SEQUENCE_TABLE_VALUE_COLUMN_NAME,
        pkColumnValue = "ordenventadetalle",
        allocationSize = Constants.SEQUENCE_ALLOCATION_SIZE)

@Entity
@EntityListeners({CompanyNumberListener.class})
@Table(schema = Constants.KHIPUS_SCHEMA, name = "ordenventadetalle")
public class SalesOrderDetail implements BaseModel {

    @Id
    @Column(name = "idordenventadetalle", nullable = false, updatable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "SalesOrderDetail.tableGenerator")
    private Long id;

    @Column(name = "no_cia", length = 2, nullable = false, updatable = false)
    private String companyNumber;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "idordenventa", nullable = false, updatable = false)
    private SalesOrder salesOrder;

    @Column(name = "nro", nullable = false)
    private Long detailNumber;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumns({
            @JoinColumn(name = "no_cia", nullable = false, updatable = false, insertable = false),
            @JoinColumn(name = "cod_art", nullable = false, updatable = false, insertable = false)
    })
    private ProductItem productItem;

    @Column(name = "cod_art", nullable = false, length = 20)
    @Length(max = 20)
    private String productItemCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idcentrocosto_comercial")
    private CommercialCostCenter costCenter;

    @Column(name = "descripcion", length = 500)
    @Length(max = 500)
    private String description;

    @Column(name = "unidad_medida", length = 50)
    @Length(max = 50)
    private String measureUnit;

    @Column(name = "cantidad", precision = 16, scale = 2, nullable = false)
    private BigDecimal quantity = BigDecimal.ZERO;

    @Column(name = "precio_unitario", precision = 16, scale = 2, nullable = false)
    private BigDecimal unitPrice = BigDecimal.ZERO;

    @Column(name = "total", precision = 16, scale = 2, nullable = false)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Version
    @Column(name = "version", nullable = false)
    private long version;

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

    public SalesOrder getSalesOrder() {
        return salesOrder;
    }

    public void setSalesOrder(SalesOrder salesOrder) {
        this.salesOrder = salesOrder;
    }

    public Long getDetailNumber() {
        return detailNumber;
    }

    public void setDetailNumber(Long detailNumber) {
        this.detailNumber = detailNumber;
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

    public CommercialCostCenter getCostCenter() {
        return costCenter;
    }

    public void setCostCenter(CommercialCostCenter costCenter) {
        this.costCenter = costCenter;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getMeasureUnit() {
        return measureUnit;
    }

    public void setMeasureUnit(String measureUnit) {
        this.measureUnit = measureUnit;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }
}
