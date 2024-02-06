package com.encens.khipus.model.xproduction;


import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyListener;
import com.encens.khipus.model.admin.Company;
import com.encens.khipus.model.production.MetaProduct;
import com.encens.khipus.model.production.ProductionOrder;
import com.encens.khipus.util.Constants;
import org.hibernate.annotations.Filter;

import javax.persistence.*;

@TableGenerator(name = "InputProductionVoucher_Generator",
        table = "secuencia",
        pkColumnName = "tabla",
        valueColumnName = "valor",
        pkColumnValue = "valeinsumosrequeridos",
        allocationSize = Constants.SEQUENCE_ALLOCATION_SIZE)

@Entity
@Table(name = "valeinsumosrequeridos")
@Filter(name = "companyFilter")
@EntityListeners(CompanyListener.class)
public class InputProductionVoucher implements BaseModel {
    @Id
    @Column(name = "idvaleinsumosrequeridos", nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "InputProductionVoucher_Generator")
    private Long id;

    @Column(name = "cantidad", nullable = false, columnDefinition = "DECIMAL(24,0)")
    private Double amount;

    @Column(name = "preciocostototal", nullable = true, columnDefinition = "DECIMAL(16,2)")
    private Double priceCostTotal = 0.0;

    @OneToOne(cascade = {CascadeType.MERGE, CascadeType.REFRESH})
    @JoinColumn(name = "idmetaproductoproduccion", nullable = false, updatable = false, insertable = true)
    private com.encens.khipus.model.production.MetaProduct metaProduct;

    @OneToOne(cascade = {CascadeType.MERGE, CascadeType.REFRESH})
    @JoinColumn(name = "idordenproduccion", nullable = false, updatable = false, insertable = true)
    private com.encens.khipus.model.production.ProductionOrder productionOrder;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idcompania", nullable = false, updatable = false, insertable = true)
    private Company company;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public com.encens.khipus.model.production.MetaProduct getMetaProduct() {
        return metaProduct;
    }

    public void setMetaProduct(MetaProduct metaProduct) {
        this.metaProduct = metaProduct;
    }

    public com.encens.khipus.model.production.ProductionOrder getProductionOrder() {
        return productionOrder;
    }

    public void setProductionOrder(ProductionOrder productionOrder) {
        this.productionOrder = productionOrder;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public Double getPriceCostTotal() {
        return priceCostTotal;
    }

    public void setPriceCostTotal(Double priceCostTotal) {
        this.priceCostTotal = priceCostTotal;
    }
}
