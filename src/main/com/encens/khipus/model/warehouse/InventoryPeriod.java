package com.encens.khipus.model.warehouse;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.util.Constants;
import org.hibernate.validator.Length;

import javax.persistence.*;
import java.math.BigDecimal;

/**
 * @author
 * @version 2.2
 */
@NamedQueries({
})

@TableGenerator(schema = Constants.KHIPUS_SCHEMA, name = "InventoryPeriod.tableGenerator",
        table = Constants.SEQUENCE_TABLE_NAME,
        pkColumnName = Constants.SEQUENCE_TABLE_PK_COLUMN_NAME,
        valueColumnName = Constants.SEQUENCE_TABLE_VALUE_COLUMN_NAME,
        pkColumnValue = "inv_periodo",
        allocationSize = Constants.SEQUENCE_ALLOCATION_SIZE)

@Entity
@Table(name = "inv_periodo", schema = Constants.FINANCES_SCHEMA)
public class InventoryPeriod implements BaseModel {
    @Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "InventoryPeriod.tableGenerator")
    @Column(name = "id_inv_periodo", nullable = false)
    private Long id;

    @Column(name = "cod_art", length = 6, nullable = false)
    @Length(max = 6)
    private String productItemCode;

    @Column(name = "saldofis", precision = 12, scale = 2)
    private BigDecimal quantity;

    @Column(name = "saldoval", precision = 12, scale = 2, nullable = false)
    private BigDecimal amount;

    @Column(name = "costouni", precision = 16, scale = 6, nullable = false)
    private BigDecimal unitCost;

    @Column(name = "mes", nullable = false)
    private Integer month;

    @Column(name = "gestion", nullable = false)
    private Integer year;

    @Column(name = "cod_alm", length = 6, nullable = false)
    @Length(max = 6)
    private String warehouseCode;

    @Column(name = "no_cia")
    @Length(max = 2)
    private String companyNumber;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumns({
            @JoinColumn(name = "no_cia", nullable = false, insertable = false, updatable = false),
            @JoinColumn(name = "cod_art", nullable = false, insertable = false, updatable = false)
    })
    private ProductItem productItem;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }


    public String getProductItemCode() {
        return productItemCode;
    }

    public void setProductItemCode(String productItemCode) {
        this.productItemCode = productItemCode;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getUnitCost() {
        return unitCost;
    }

    public void setUnitCost(BigDecimal unitCost) {
        this.unitCost = unitCost;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public String getWarehouseCode() {
        return warehouseCode;
    }

    public void setWarehouseCode(String warehouseCode) {
        this.warehouseCode = warehouseCode;
    }

    public Integer getMonth() {
        return month;
    }

    public void setMonth(Integer month) {
        this.month = month;
    }

    public ProductItem getProductItem() {
        return productItem;
    }

    public void setProductItem(ProductItem productItem) {
        this.productItem = productItem;
    }

    public String getCompanyNumber() {
        return companyNumber;
    }

    public void setCompanyNumber(String companyNumber) {
        this.companyNumber = companyNumber;
    }
}
