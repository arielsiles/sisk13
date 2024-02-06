package com.encens.khipus.model.xproduction;


import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.production.ProcessedProduct;
import com.encens.khipus.model.production.ProductionOrder;
import com.encens.khipus.util.Constants;

import javax.persistence.*;

@TableGenerator(name = "ProductOrder_Generator",
        table = "secuencia",
        pkColumnName = "tabla",
        valueColumnName = "valor",
        pkColumnValue = "productoorden",
        allocationSize = Constants.SEQUENCE_ALLOCATION_SIZE)

@Entity
@Table(name = "productoorden")
public class ProductOrder implements BaseModel {

    @Id
    @Column(name = "idproductoorden", nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "ProductOrder_Generator")
    private Long id;

    @OneToOne(cascade = {CascadeType.MERGE, CascadeType.REFRESH})
    @JoinColumn(name = "idproductoprocesado", nullable = false, updatable = false, insertable = true)
    private ProcessedProduct processedProduct;

    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.REFRESH})
    @JoinColumn(name = "idordenproduccion", nullable = false, updatable = false, insertable = true)
    private com.encens.khipus.model.production.ProductionOrder productionOrder;

    @Column(name = "nombreproducto",nullable = false,columnDefinition = "VARCHAR2(100 BYTE)")
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

    public com.encens.khipus.model.production.ProductionOrder getProductionOrder() {
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
