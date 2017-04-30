package com.encens.khipus.model.production;


import com.encens.khipus.model.BaseModel;

import javax.persistence.*;

@TableGenerator(name = "ProductOrder_Generator",
        table = "SECUENCIA",
        pkColumnName = "TABLA",
        valueColumnName = "VALOR",
        pkColumnValue = "PRODUCTOORDEN",
        allocationSize = 10)

@Entity
@Table(name = "PRODUCTOORDEN")
public class ProductOrder implements BaseModel {

    @Id
    @Column(name = "IDPRODUCTOORDEN", nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "ProductOrder_Generator")
    private Long id;

    @OneToOne(cascade = {CascadeType.MERGE, CascadeType.REFRESH})
    @JoinColumn(name = "IDPRODUCTOPROCESADO", nullable = false, updatable = false, insertable = true)
    private ProcessedProduct processedProduct;

    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.REFRESH})
    @JoinColumn(name = "IDORDENPRODUCCION", nullable = false, updatable = false, insertable = true)
    private ProductionOrder productionOrder;

    @Column(name = "NOMBREPRODUCTO",nullable = false,columnDefinition = "VARCHAR2(100 BYTE)")
    private String fullName;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ProcessedProduct getProcessedProduct() {
        return processedProduct;
    }

    public void setProcessedProduct(ProcessedProduct processedProduct) {
        this.processedProduct = processedProduct;
    }

    public ProductionOrder getProductionOrder() {
        return productionOrder;
    }

    public void setProductionOrder(ProductionOrder productionOrder) {
        this.productionOrder = productionOrder;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
}
