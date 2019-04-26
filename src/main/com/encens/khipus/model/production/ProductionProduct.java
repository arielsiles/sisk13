package com.encens.khipus.model.production;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyListener;
import com.encens.khipus.model.admin.Company;
import com.encens.khipus.model.warehouse.ProductItem;
import org.hibernate.annotations.Filter;

import javax.persistence.*;
import java.math.BigDecimal;

/**
 * Entity fo Product
 *
 * @author:
 */

@NamedQueries({})

@TableGenerator(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "ProductionProduct.tableGenerator",
        table = com.encens.khipus.util.Constants.SEQUENCE_TABLE_NAME,
        pkColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_PK_COLUMN_NAME,
        valueColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_VALUE_COLUMN_NAME,
        pkColumnValue = "pr_producto",
        allocationSize = com.encens.khipus.util.Constants.SEQUENCE_ALLOCATION_SIZE)

@Entity
@Filter(name = com.encens.khipus.util.Constants.COMPANY_FILTER_NAME)
@EntityListeners(CompanyListener.class)
@Table(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "pr_producto")
public class ProductionProduct implements BaseModel {

    @Id
    @Column(name = "idproducto")
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "ProductionProduct.tableGenerator")
    private Long id;

    @Column(name = "cod_art")
    private String productItemCode;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "cod_art", referencedColumnName = "cod_art", updatable = false, insertable = false)
    })
    private ProductItem productItem;

    @Column(name = "cantidad")
    private BigDecimal quantity;

    @ManyToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "idproduccion", nullable = true, updatable = false, insertable = true)
    private Production production;

    @ManyToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "idplan", nullable = true, updatable = false, insertable = true)
    private ProductionPlan productionPlan;

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

    public ProductItem getProductItem() {
        return productItem;
    }

    public void setProductItem(ProductItem productItem) {
        this.productItem = productItem;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public Production getProduction() {
        return production;
    }

    public void setProduction(Production production) {
        this.production = production;
    }

    public ProductionPlan getProductionPlan() {
        return productionPlan;
    }

    public void setProductionPlan(ProductionPlan productionPlan) {
        this.productionPlan = productionPlan;
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
}