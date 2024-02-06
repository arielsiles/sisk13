package com.encens.khipus.model.xproduction;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyListener;
import com.encens.khipus.model.admin.Company;
import com.encens.khipus.model.production.ProductionOrder;
import com.encens.khipus.model.production.SingleProduct;
import com.encens.khipus.model.warehouse.ProductItem;
import com.encens.khipus.util.Constants;
import org.hibernate.annotations.Filter;
import org.hibernate.validator.Length;

import javax.persistence.*;
import java.math.BigDecimal;

/**
 * Created with IntelliJ IDEA.
 * User: Diego
 * Date: 31/10/13
 * Time: 19:42
 * To change this template use File | Settings | File Templates.
 */

@TableGenerator(name = "OrderMaterial_Generator",
        table = "secuencia",
        pkColumnName = "tabla",
        valueColumnName = "valor",
        pkColumnValue = "ordenmaterial",
        allocationSize = Constants.SEQUENCE_ALLOCATION_SIZE)

@Entity
@Table(name = "ordenmaterial")
@Filter(name = "companyFilter")
@EntityListeners(CompanyListener.class)

public class OrderMaterial implements BaseModel {

    @Id
    @Column(name = "idordenmaterial", nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "OrderMaterial_Generator")
    private Long id;

    @Column(name = "cantidadpesosolicitada", nullable = true, columnDefinition = "DECIMAL(16,2)")
    private Double amountRequired = 0.0;

    @Column(name = "cantidadunidadsolicitada", nullable = true, columnDefinition = "DECIMAL(16,2)")
    private Double amountRequiredUnit = 0.0;

    @Column(name = "cantidadpesousada", nullable = true, columnDefinition = "DECIMAL(16,2)")
    private Double amountUsed = 0.0;

    @Column(name = "cantidadpesoretornada", nullable = true, columnDefinition = "DECIMAL(16,2)")
    private Double amountReturned = 0.0;

    @Column(name = "costototal", nullable = true, columnDefinition = "DECIMAL(16,6)")
    private BigDecimal costTotal = new BigDecimal(0.0);

    @Column(name = "costounitario", nullable = true, columnDefinition = "DECIMAL(16,6)")
    private BigDecimal costUnit = new BigDecimal(0.0);

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

    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    @JoinColumn(name = "idordenproduccion", nullable = true, updatable = false, insertable = true)
    private com.encens.khipus.model.production.ProductionOrder productionOrder;

    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    @JoinColumn(name = "idproductosimple", nullable = true, updatable = false, insertable = true)
    private com.encens.khipus.model.production.SingleProduct singleProduct;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idcompania", nullable = false, updatable = false, insertable = true)
    private Company company;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Double getAmountRequired() {
        return amountRequired;
    }

    public void setAmountRequired(Double amountRequired) {
        this.amountRequired = amountRequired;
    }

    public Double getAmountRequiredUnit() {
        return amountRequiredUnit;
    }

    public void setAmountRequiredUnit(Double amountRequiredUnit) {
        this.amountRequiredUnit = amountRequiredUnit;
    }

    public Double getAmountUsed() {
        return amountUsed;
    }

    public void setAmountUsed(Double amountUsed) {
        this.amountUsed = amountUsed;
    }

    public Double getAmountReturned() {
        return amountReturned;
    }

    public void setAmountReturned(Double amountReturned) {
        this.amountReturned = amountReturned;
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

    public ProductItem getProductItem() {
        return productItem;
    }

    public void setProductItem(ProductItem productItem) {
        this.productItem = productItem;
    }

    public com.encens.khipus.model.production.ProductionOrder getProductionOrder() {
        return productionOrder;
    }

    public void setProductionOrder(ProductionOrder productionOrder) {
        this.productionOrder = productionOrder;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public BigDecimal getCostTotal() {
        return costTotal;
    }

    public void setCostTotal(BigDecimal costTotal) {
        this.costTotal = costTotal;
    }

    public BigDecimal getCostUnit() {
        return costUnit;
    }

    public void setCostUnit(BigDecimal costUnit) {
        this.costUnit = costUnit;
    }

    public com.encens.khipus.model.production.SingleProduct getSingleProduct() {
        return singleProduct;
    }

    public void setSingleProduct(SingleProduct singleProduct) {
        this.singleProduct = singleProduct;
    }
}
