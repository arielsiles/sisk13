package com.encens.khipus.model.xproduction;


import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.xproduction.BaseProduct;
import com.encens.khipus.model.xproduction.ProductionOrder;
import com.encens.khipus.model.warehouse.ProductItem;
import com.encens.khipus.util.Constants;
import org.hibernate.validator.Length;

import javax.persistence.*;
import java.math.BigDecimal;

@TableGenerator(name = "OrderInput_Generator",
        table = "secuencia",
        pkColumnName = "tabla",
        valueColumnName = "valor",
        pkColumnValue = "ordeninsumo",
        allocationSize = Constants.SEQUENCE_ALLOCATION_SIZE)

@Entity
@Table(name = "ordeninsumo")
public class OrderInput implements BaseModel {

    @Id
    @Column(name = "idordeninsumo", nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "OrderInput_Generator")
    private Long id;

    @Column(name = "cantidad", nullable = false, columnDefinition = "DECIMAL(16,6)")
    private Double amount;

    /* TODO: cambiar nullable a false */
    @Column(name = "cantidadstock", nullable = true, columnDefinition = "DECIMAL(24,0)")
    private BigDecimal amountStock = new BigDecimal(0.0);

    @Column(name = "costounitario", nullable = true, columnDefinition = "DECIMAL(16,6)")
    private BigDecimal costUnit = new BigDecimal(0.0);

    @Column(name = "costototal", nullable = true, columnDefinition = "DECIMAL(16,6)")
    private BigDecimal costTotal = new BigDecimal(0.0);

    @Column(name = "formulamatematica", nullable = true, length = 500)
    private String mathematicalFormula;

    @Column(name = "tipo",nullable = true)
    private String type = "FORMULATED";

    @Column(name = "cod_art", insertable = false, updatable = false, nullable = false)
    private String productItemCode;

    @Column(name = "no_cia", insertable = false, updatable = false, nullable = false)
    @Length(max = 2)
    private String companyNumber;

    @OneToOne(cascade = {CascadeType.MERGE, CascadeType.REFRESH})
    @JoinColumns({
            @JoinColumn(name = "no_cia", referencedColumnName = "no_cia"),
            @JoinColumn(name = "cod_art", referencedColumnName = "cod_art")
    })
    private ProductItem productItem;

    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.REFRESH})
    @JoinColumn(name = "idordenproduccion", nullable = true, updatable = false, insertable = true)
    private com.encens.khipus.model.xproduction.ProductionOrder productionOrder;

    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.REFRESH})
    @JoinColumn(name = "idproductobase", nullable = true, updatable = false, insertable = true)
    private com.encens.khipus.model.xproduction.BaseProduct baseProductInput;

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

    public ProductItem getProductItem() {
        return productItem;
    }

    public void setProductItem(ProductItem productItem) {
        this.productItem = productItem;
    }

    public com.encens.khipus.model.xproduction.ProductionOrder getProductionOrder() {
        return productionOrder;
    }

    public void setProductionOrder(ProductionOrder productionOrder) {
        this.productionOrder = productionOrder;
    }

    public BigDecimal getAmountStock() {
        return amountStock;
    }

    public void setAmountStock(BigDecimal amountStock) {
        this.amountStock = amountStock;
    }

    public String getProductItemCode() {
        return productItemCode;
    }

    public void setProductItemCode(String productItemCode) {
        this.productItemCode = productItemCode;
    }

    public String getCompanyNumber() {
        return companyNumber;
    }

    public void setCompanyNumber(String companyNumber) {
        this.companyNumber = companyNumber;
    }

    public String getMathematicalFormula() {
        return mathematicalFormula;
    }

    public void setMathematicalFormula(String mathematicalFormula) {
        this.mathematicalFormula = mathematicalFormula;
    }

    public BigDecimal getCostUnit() {
        return costUnit;
    }

    public void setCostUnit(BigDecimal costUnit) {
        this.costUnit = costUnit;
    }

    public BigDecimal getCostTotal() {
        return costTotal;
    }

    public void setCostTotal(BigDecimal costTotal) {
        this.costTotal = costTotal;
    }

    public com.encens.khipus.model.xproduction.BaseProduct getBaseProductInput() {
        return baseProductInput;
    }

    public void setBaseProductInput(BaseProduct baseProductInput) {
        this.baseProductInput = baseProductInput;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
