package com.encens.khipus.model.production;


import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyListener;
import com.encens.khipus.model.admin.Company;
import org.hibernate.annotations.Filter;

import javax.persistence.*;

@TableGenerator(name = "InputProductionVoucher_Generator",
        table = "SECUENCIA",
        pkColumnName = "TABLA",
        valueColumnName = "VALOR",
        pkColumnValue = "VALEINSUMOSREQUERIDOS",
        allocationSize = 10)

@Entity
@Table(name = "VALEINSUMOSREQUERIDOS")
@Filter(name = "companyFilter")
@EntityListeners(CompanyListener.class)
public class InputProductionVoucher implements BaseModel {
    @Id
    @Column(name = "IDVALEINSUMOSREQUERIDOS", nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "InputProductionVoucher_Generator")
    private Long id;

    @Column(name = "CANTIDAD", nullable = false, columnDefinition = "DECIMAL(24,0)")
    private Double amount;

    @Column(name = "PRECIOCOSTOTOTAL", nullable = true, columnDefinition = "DECIMAL(16,2)")
    private Double priceCostTotal = 0.0;

    @OneToOne(cascade = {CascadeType.MERGE, CascadeType.REFRESH})
    @JoinColumn(name = "IDMETAPRODUCTOPRODUCCION", nullable = false, updatable = false, insertable = true)
    private MetaProduct metaProduct;

    @OneToOne(cascade = {CascadeType.MERGE, CascadeType.REFRESH})
    @JoinColumn(name = "IDORDENPRODUCCION", nullable = false, updatable = false, insertable = true)
    private ProductionOrder productionOrder;

    @Version
    @Column(name = "VERSION", nullable = false)
    private Long version;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "IDCOMPANIA", nullable = false, updatable = false, insertable = true)
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

    public MetaProduct getMetaProduct() {
        return metaProduct;
    }

    public void setMetaProduct(MetaProduct metaProduct) {
        this.metaProduct = metaProduct;
    }

    public ProductionOrder getProductionOrder() {
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
