package com.encens.khipus.model.production;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyListener;
import com.encens.khipus.model.admin.Company;
import com.encens.khipus.model.warehouse.IncomingProductionOrder;
import org.hibernate.annotations.Filter;

import javax.persistence.*;

@TableGenerator(name = "OutputProductionVoucher_Generator",
        table = "SECUENCIA",
        pkColumnName = "TABLA",
        valueColumnName = "VALOR",
        pkColumnValue = "VALEPRODUCTOTERMINADO",
        allocationSize = 10)

@Entity
@Table(name = "VALEPRODUCTOTERMINADO")
@Filter(name = "companyFilter")
@EntityListeners(CompanyListener.class)
public class OutputProductionVoucher implements BaseModel {

    @Id
    @Column(name = "IDVALEPRODUCTOTERMINADO", nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "OutputProductionVoucher_Generator")
    private Long id;

    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.REMOVE})
    @JoinColumn(name = "IDORDENPRODUCCION", nullable = false, updatable = false, insertable = true)
    private ProductionOrder productionOrder;

    @OneToOne(cascade = {CascadeType.MERGE, CascadeType.REMOVE})
    @JoinColumn(name = "IDPRODUCTOPROCESADO", nullable = false, updatable = false, insertable = true)
    private ProcessedProduct processedProduct;

    @OneToOne(cascade = {CascadeType.MERGE, CascadeType.REMOVE})
    @JoinColumn(name = "IDENTRADAORDENPRODUCCION" ,nullable = true, updatable = true, insertable = true)
    private IncomingProductionOrder incomingProductionOrder;

    @Column(name = "CANTIDADPRODUCIDA", nullable = false, columnDefinition="DECIMAL(24,0)")
    private Double producedAmount;

    @Column(name = "OBSERVACIONES", nullable = true, length = 1500)
    private String observations;

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

    public ProductionOrder getProductionOrder() {
        return productionOrder;
    }

    public void setProductionOrder(ProductionOrder productionOrder) {
        this.productionOrder = productionOrder;
    }

    public IncomingProductionOrder getIncomingProductionOrder() {
        return incomingProductionOrder;
    }

    public void setIncomingProductionOrder(IncomingProductionOrder incomingProductionOrder) {
        this.incomingProductionOrder = incomingProductionOrder;
    }

    public Double getProducedAmount() {
        return producedAmount;
    }

    public void setProducedAmount(Double producedAmount) {
        this.producedAmount = producedAmount;
    }

    public String getObservations() {
        return observations;
    }

    public void setObservations(String observations) {
        this.observations = observations;
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

    public ProcessedProduct getProcessedProduct() {
        return processedProduct;
    }

    public void setProcessedProduct(ProcessedProduct processedProduct) {
        this.processedProduct = processedProduct;
    }
}
