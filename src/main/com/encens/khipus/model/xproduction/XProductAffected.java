package com.encens.khipus.model.xproduction;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyListener;
import com.encens.khipus.model.admin.Company;
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

@TableGenerator(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "XProductAffected.tableGenerator",
        table = com.encens.khipus.util.Constants.SEQUENCE_TABLE_NAME,
        pkColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_PK_COLUMN_NAME,
        valueColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_VALUE_COLUMN_NAME,
        pkColumnValue = "xpr_prodafectado",
        allocationSize = com.encens.khipus.util.Constants.SEQUENCE_ALLOCATION_SIZE)

@Entity
@Filter(name = com.encens.khipus.util.Constants.COMPANY_FILTER_NAME)
@EntityListeners(CompanyListener.class)
@Table(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "xpr_prodafectado")
public class XProductAffected implements BaseModel {

    @Id
    @Column(name = "idprodafectado")
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "XProductAffected.tableGenerator")
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idinsumo", nullable = false, updatable = false, insertable = true)
    private XSupply supply;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idproducto", nullable = false, updatable = false, insertable = true)
    private XProductionProduct productionProduct;

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

    public XSupply getSupply() {
        return supply;
    }

    public void setSupply(XSupply supply) {
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

    public XProductionProduct getProductionProduct() {
        return productionProduct;
    }

    public void setProductionProduct(XProductionProduct productionProduct) {
        this.productionProduct = productionProduct;
    }
}
