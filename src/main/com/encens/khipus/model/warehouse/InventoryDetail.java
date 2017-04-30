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

/**
 * @author
 * @version 2.2
 */
@TableGenerator(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "InventoryDetail.tableGenerator",
        table = com.encens.khipus.util.Constants.SEQUENCE_TABLE_NAME,
        pkColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_PK_COLUMN_NAME,
        valueColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_VALUE_COLUMN_NAME,
        pkColumnValue = "inv_inventario_detalle",
        allocationSize = com.encens.khipus.util.Constants.SEQUENCE_ALLOCATION_SIZE)


@NamedQueries({
        @NamedQuery(name = "InventoryDetail.findByExecutorUnitAndCostCenter",
                query = "select detail from InventoryDetail detail where detail.companyNumber =:companyNumber and detail.productItemCode =:productItemCode and detail.warehouseCode =:warehouseCode and detail.executorUnit =:executorUnit and detail.costCenterCode =:costCenterCode"),
        @NamedQuery(name = "InventoryDetail.findByInventoryAndExecutorUnitCode",
                query = "select detail from InventoryDetail detail where detail.companyNumber =:companyNumber and detail.productItemCode =:productItemCode and detail.warehouseCode =:warehouseCode and detail.executorUnit =:executorUnit"),
        @NamedQuery(name = "InventoryDetail.findByWarehouseCode",
                query = "select detail from InventoryDetail detail where detail.companyNumber =:companyNumber and detail.warehouseCode =:warehouseCode and detail.executorUnit =:executorUnit")
})


@Entity
@EntityListeners({CompanyNumberListener.class, UpperCaseStringListener.class})
@Table(name = "inv_inventario_detalle", schema = Constants.FINANCES_SCHEMA)
public class InventoryDetail implements BaseModel {
    @Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "InventoryDetail.tableGenerator")
    @Column(name = "id_inv_det", nullable = false)
    private Long id;

    @Column(name = "no_cia", length = 2, nullable = true)
    private String companyNumber;

    @Column(name = "cod_alm", length = 6, nullable = true)
    @Length(max = 6)
    private String warehouseCode;

    @Column(name = "cod_art", length = 6, nullable = true)
    @Length(max = 6)
    private String productItemCode;

    @Column(name = "cod_cc", length = 8, nullable = true)
    @Length(max = 8)
    private String costCenterCode;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "idunidadnegocio", updatable = true, insertable = true)
    private BusinessUnit executorUnit;

    @Column(name = "cantidad", precision = 12, scale = 2)
    private BigDecimal quantity;

    @Version
    @Column(name = "version")
    private long version;


    @ManyToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "NO_CIA", referencedColumnName = "NO_CIA", updatable = false, insertable = false),
            @JoinColumn(name = "COD_CC", referencedColumnName = "COD_CC", updatable = false, insertable = false)
    })
    private CostCenter costCenter;


    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumns({
            @JoinColumn(name = "NO_CIA", nullable = true, insertable = false, updatable = false),
            @JoinColumn(name = "COD_ART", nullable = true, insertable = false, updatable = false)
    })
    private ProductItem productItem;


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
}
