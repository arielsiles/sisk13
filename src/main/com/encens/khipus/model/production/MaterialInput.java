package com.encens.khipus.model.production;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyListener;
import com.encens.khipus.model.admin.Company;
import com.encens.khipus.model.warehouse.ProductItem;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.math.BigDecimal;

/**
 * Entity fo Product
 *
 * @author:
 */

@NamedQueries({

})

@TableGenerator(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "MaterialInput.tableGenerator",
        table = com.encens.khipus.util.Constants.SEQUENCE_TABLE_NAME,
        pkColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_PK_COLUMN_NAME,
        valueColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_VALUE_COLUMN_NAME,
        pkColumnValue = "pr_material",
        allocationSize = com.encens.khipus.util.Constants.SEQUENCE_ALLOCATION_SIZE)

@Entity
@Filter(name = com.encens.khipus.util.Constants.COMPANY_FILTER_NAME)
@EntityListeners(CompanyListener.class)
@Table(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "pr_material")
public class MaterialInput implements BaseModel {

    @Id
    @Column(name = "idmaterial")
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "MaterialInput.tableGenerator")
    private Long id;

    @Column(name = "cod_art")
    private String productItemCode;

    @Column(name = "cod_art_mat")
    private String productItemMaterialCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cod_art", referencedColumnName = "cod_art", updatable = false, insertable = false)
    private ProductItem productItem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cod_art_mat", referencedColumnName = "cod_art", updatable = false, insertable = false)
    private ProductItem productItemMaterial;

    @Column(name = "flag_cant", nullable = false)
    @Type(type = com.encens.khipus.model.usertype.IntegerBooleanUserType.NAME)
    private Boolean quantityFlag = Boolean.FALSE;

    @Column(name = "tipo", nullable = true)
    @Enumerated(EnumType.STRING)
    private SupplyType type;

    @Column(name = "vol1")
    private BigDecimal volumeOne;

    @Column(name = "peso1")
    private BigDecimal weightOne;

    @Column(name = "vol2")
    private BigDecimal volumeTwo;

    @Version
    @Column(name = "version", nullable = false)
    private long version;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idcompania", nullable = false, updatable = false, insertable = true)
    private Company company;

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

    public String getProductItemMaterialCode() {
        return productItemMaterialCode;
    }

    public void setProductItemMaterialCode(String productItemMaterialCode) {
        this.productItemMaterialCode = productItemMaterialCode;
    }

    public ProductItem getProductItem() {
        return productItem;
    }

    public void setProductItem(ProductItem productItem) {
        this.productItem = productItem;
        if (productItem != null) setProductItemCode(productItem.getProductItemCode());
    }

    public ProductItem getProductItemMaterial() {
        return productItemMaterial;
    }

    public void setProductItemMaterial(ProductItem productItemMaterial) {
        this.productItemMaterial = productItemMaterial;
        if (productItemMaterial != null) setProductItemMaterialCode(productItemMaterial.getProductItemCode());
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public Boolean getQuantityFlag() {
        return quantityFlag;
    }

    public void setQuantityFlag(Boolean quantityFlag) {
        this.quantityFlag = quantityFlag;
    }

    public SupplyType getType() {
        return type;
    }

    public void setType(SupplyType type) {
        this.type = type;
    }

    public BigDecimal getVolumeOne() {
        return volumeOne;
    }

    public void setVolumeOne(BigDecimal volumeOne) {
        this.volumeOne = volumeOne;
    }

    public BigDecimal getWeightOne() {
        return weightOne;
    }

    public void setWeightOne(BigDecimal weightOne) {
        this.weightOne = weightOne;
    }

    public BigDecimal getVolumeTwo() {
        return volumeTwo;
    }

    public void setVolumeTwo(BigDecimal volumeTwo) {
        this.volumeTwo = volumeTwo;
    }
}
