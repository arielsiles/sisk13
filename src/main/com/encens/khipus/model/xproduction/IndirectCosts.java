package com.encens.khipus.model.xproduction;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyListener;
import com.encens.khipus.model.admin.Company;
import com.encens.khipus.model.production.IndirectCostsConfig;
import com.encens.khipus.model.production.PeriodIndirectCost;
import com.encens.khipus.model.production.ProductionOrder;
import com.encens.khipus.model.production.SingleProduct;
import com.encens.khipus.util.Constants;
import org.hibernate.annotations.Filter;

import javax.persistence.*;
import java.math.BigDecimal;

/**
 * Entity for Employee Time CArd
 *
 * @author Diego Loza
 * @version 1.2.1
 */
@TableGenerator(name = "IndirectCosts.tableGenerator",
        table = "secuencia",
        pkColumnName = "tabla",
        valueColumnName = "valor",
        pkColumnValue = "costosindirectos",
        allocationSize = Constants.SEQUENCE_ALLOCATION_SIZE)

@Entity
@Table(name = "costosindirectos")
@Filter(name = Constants.COMPANY_FILTER_NAME)
@EntityListeners(CompanyListener.class)
public class IndirectCosts implements BaseModel {

    @Id
    @Column(name = "idcostosindirectos", nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "IndirectCosts.tableGenerator")
    private Long id;

    @Column(name = "nombre", nullable = true)
    private String name;

    @Column(name = "montobs", nullable = false)
    private BigDecimal amountBs;

    @ManyToOne(optional = true, fetch = FetchType.LAZY, cascade = {CascadeType.REFRESH})
    @JoinColumn(name = "idperiodocostoindirecto")
    private PeriodIndirectCost periodIndirectCost;

    @ManyToOne(optional = true, fetch = FetchType.LAZY, cascade = {CascadeType.REFRESH})
    @JoinColumn(name = "idordenproduccion")
    private ProductionOrder productionOrder;

    @ManyToOne(optional = true, fetch = FetchType.LAZY, cascade = {CascadeType.REFRESH})
    @JoinColumn(name = "idproductosimple")
    private SingleProduct singleProduct;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idcompania", nullable = false, updatable = false, insertable = true)
    private Company company;

    @OneToOne(optional = false, fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "idcostosindirectosconf")
    private IndirectCostsConfig costsConifg;

    @Version
    @Column(name = "version", nullable = false)
    private long version;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getAmountBs() {
        return amountBs;
    }

    public void setAmountBs(BigDecimal amountBs) {
        this.amountBs = amountBs;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public PeriodIndirectCost getPeriodIndirectCost() {
        return periodIndirectCost;
    }

    public void setPeriodIndirectCost(PeriodIndirectCost periodIndirectCost) {
        this.periodIndirectCost = periodIndirectCost;
    }

    public ProductionOrder getProductionOrder() {
        return productionOrder;
    }

    public void setProductionOrder(ProductionOrder productionOrder) {
        this.productionOrder = productionOrder;
    }

    public IndirectCostsConfig getCostsConifg() {
        return costsConifg;
    }

    public void setCostsConifg(IndirectCostsConfig costsConifg) {
        this.costsConifg = costsConifg;
    }

    public SingleProduct getSingleProduct() {
        return singleProduct;
    }

    public void setSingleProduct(SingleProduct singleProduct) {
        this.singleProduct = singleProduct;
    }
}
