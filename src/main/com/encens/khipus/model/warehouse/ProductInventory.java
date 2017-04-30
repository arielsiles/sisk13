package com.encens.khipus.model.warehouse;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyNumberListener;
import com.encens.khipus.model.UpperCaseStringListener;
import com.encens.khipus.model.admin.BusinessUnit;
import com.encens.khipus.model.finances.CostCenter;
import com.encens.khipus.util.Constants;
import org.hibernate.validator.Length;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Collection;

/**
 * @author
 * @version 2.2
 */
@TableGenerator(schema = Constants.KHIPUS_SCHEMA, name = "ProductInventory.tableGenerator",
        table = Constants.SEQUENCE_TABLE_NAME,
        pkColumnName = Constants.SEQUENCE_TABLE_PK_COLUMN_NAME,
        valueColumnName = Constants.SEQUENCE_TABLE_VALUE_COLUMN_NAME,
        pkColumnValue = "inv_product",
        allocationSize = Constants.SEQUENCE_ALLOCATION_SIZE)


@NamedQueries({
        @NamedQuery(name = "ProductInventory.findByCode",
                query = "select productInventory from ProductInventory productInventory where productInventory.productItemCode =:productItemCode")
})


@Entity
@EntityListeners({CompanyNumberListener.class, UpperCaseStringListener.class})
@Table(name = "inv_product", schema = Constants.FINANCES_SCHEMA)
public class ProductInventory implements BaseModel {
    @Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "ProductInventory.tableGenerator")
    @Column(name = "ID_INV_PROD", nullable = false)
    private Long id;

    @Column(name = "NO_CIA", length = 2, nullable = false)
    private String companyNumber;

    @Column(name = "COD_ALM", length = 6, nullable = false)
    @Length(max = 6)
    private String warehouseCode;

    @Column(name = "COD_ART", length = 6, nullable = false)
    @Length(max = 6)
    private String productItemCode;

    @Column(name = "COD_CC", length = 8, nullable = false)
    @Length(max = 8)
    private String costCenterCode;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "IDUNIDADNEGOCIO", updatable = true, insertable = true)
    private BusinessUnit executorUnit;

    @Column(name = "CANTIDAD", precision = 12, scale = 2)
    private BigDecimal quantity;

    @Version
    @Column(name = "VERSION")
    private long version;

    @Transient
    private String productItemFullName;

    @ManyToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "NO_CIA", referencedColumnName = "NO_CIA", updatable = false, insertable = false),
            @JoinColumn(name = "COD_CC", referencedColumnName = "COD_CC", updatable = false, insertable = false)
    })
    private CostCenter costCenter;


    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumns({
            @JoinColumn(name = "NO_CIA", nullable = false, insertable = false, updatable = false),
            @JoinColumn(name = "COD_ART", nullable = false, insertable = false, updatable = false)
    })
    private ProductItem productItem;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "productInventory")
    private Collection<ProductInventoryRecord> productInventoryRecords;

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

    public String getWarehouseCode() {
        return warehouseCode;
    }

    public void setWarehouseCode(String warehouseCode) {
        this.warehouseCode = warehouseCode;
    }

    public String getProductItemCode() {
        return productItemCode;
    }

    public void setProductItemCode(String productItemCode) {
        this.productItemCode = productItemCode;
    }

    public CostCenter getCostCenter() {
        return costCenter;
    }

    public void setCostCenter(CostCenter costCenter) {
        this.costCenter = costCenter;
        setCompanyNumber(costCenter != null ? costCenter.getCompanyNumber() : null);
        setCostCenterCode(costCenter != null ? costCenter.getCode() : null);
    }

    public String getCostCenterCode() {
        return costCenterCode;
    }

    public void setCostCenterCode(String costCenterCode) {
        this.costCenterCode = costCenterCode;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public ProductItem getProductItem() {
        return productItem;
    }

    public void setProductItem(ProductItem productItem) {
        this.productItem = productItem;
        if (null != productItem) {
            setProductItemCode(productItem.getId().getProductItemCode());
        } else {
            setProductItemCode(null);
        }
    }

    public BusinessUnit getExecutorUnit() {
        return executorUnit;
    }

    public void setExecutorUnit(BusinessUnit executorUnit) {
        this.executorUnit = executorUnit;
    }

    public Collection<ProductInventoryRecord> getProductInventoryRecords() {
        return productInventoryRecords;
    }

    public void setProductInventoryRecords(Collection<ProductInventoryRecord> productInventoryRecords) {
        this.productInventoryRecords = productInventoryRecords;
    }

    public String getProductItemFullName() {
        if (productItemFullName == null && getProductItem() != null) {
            productItemFullName = getProductItem().getFullName();
        }
        return productItemFullName;
    }

    public void setProductItemFullName(String productItemFullName) {
        this.productItemFullName = productItemFullName;
    }
}
