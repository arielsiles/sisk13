package com.encens.khipus.model.xproduction;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyListener;
import com.encens.khipus.model.admin.Company;
import com.encens.khipus.model.production.ProductionProduct;
import com.encens.khipus.model.production.Supply;
import org.hibernate.annotations.Filter;

import javax.persistence.*;

/**
 * Entity fo Product
 *
 * @author:
 */

@NamedQueries({
        /*@NamedQuery(name = "ProductionProduct.findProductionByProductItem",
                query = "Select productionProduct " +
                        "from ProductionProduct productionProduct " +
                        "left join productionProduct.productionPlan productionPlan " +
                        "where productionPlan.date between :startDate and :endDate " +
                        "and productionProduct.productItemCode =:productItemCode "),
        @NamedQuery(name = "ProductionProduct.findProductionByDates",
                query = "Select productionProduct " +
                        "from ProductionProduct productionProduct " +
                        "left join productionProduct.productionPlan productionPlan " +
                        "where productionPlan.date between :startDate and :endDate ")*/
})

@TableGenerator(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "ProductAffected.tableGenerator",
        table = com.encens.khipus.util.Constants.SEQUENCE_TABLE_NAME,
        pkColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_PK_COLUMN_NAME,
        valueColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_VALUE_COLUMN_NAME,
        pkColumnValue = "pr_prodafectado",
        allocationSize = com.encens.khipus.util.Constants.SEQUENCE_ALLOCATION_SIZE)

@Entity
@Filter(name = com.encens.khipus.util.Constants.COMPANY_FILTER_NAME)
@EntityListeners(CompanyListener.class)
@Table(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "pr_prodafectado")
public class ProductAffected implements BaseModel {

    @Id
    @Column(name = "idprodafectado")
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "ProductAffected.tableGenerator")
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idinsumo", nullable = false, updatable = false, insertable = true)
    private Supply supply;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idproducto", nullable = false, updatable = false, insertable = true)
    private ProductionProduct productionProduct;

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

    public Supply getSupply() {
        return supply;
    }

    public void setSupply(Supply supply) {
        this.supply = supply;
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

    public ProductionProduct getProductionProduct() {
        return productionProduct;
    }

    public void setProductionProduct(ProductionProduct productionProduct) {
        this.productionProduct = productionProduct;
    }
}
