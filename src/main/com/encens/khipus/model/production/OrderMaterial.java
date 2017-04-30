package com.encens.khipus.model.production;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyListener;
import com.encens.khipus.model.admin.Company;
import com.encens.khipus.model.warehouse.ProductItem;
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
        table = "SECUENCIA",
        pkColumnName = "TABLA",
        valueColumnName = "VALOR",
        pkColumnValue = "ORDENMATERIAL",
        allocationSize = 10)

@Entity
@Table(name = "ORDENMATERIAL")
@Filter(name = "companyFilter")
@EntityListeners(CompanyListener.class)

public class OrderMaterial implements BaseModel {

    @Id
    @Column(name = "IDORDENMATERIAL", nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "OrderMaterial_Generator")
    private Long id;

    @Column(name = "CANTIDADPESOSOLICITADA", nullable = true, columnDefinition = "DECIMAL(16,2)")
    private Double amountRequired = 0.0;

    @Column(name = "CANTIDADUNIDADSOLICITADA", nullable = true, columnDefinition = "DECIMAL(16,2)")
    private Double amountRequiredUnit = 0.0;

    @Column(name = "CANTIDADPESOUSADA", nullable = true, columnDefinition = "DECIMAL(16,2)")
    private Double amountUsed = 0.0;

    @Column(name = "CANTIDADPESORETORNADA", nullable = true, columnDefinition = "DECIMAL(16,2)")
    private Double amountReturned = 0.0;

    @Column(name = "COSTOTOTAL", nullable = true, columnDefinition = "DECIMAL(16,6)")
    private BigDecimal costTotal = new BigDecimal(0.0);

    @Column(name = "COSTOUNITARIO", nullable = true, columnDefinition = "DECIMAL(16,6)")
    private BigDecimal costUnit = new BigDecimal(0.0);

    @Column(name = "COD_ART", insertable = false, updatable = false, nullable = false)
    private String productItemCode;

    @Column(name = "NO_CIA", insertable = false, updatable = false, nullable = false)
    @Length(max = 2)
    private String companyNumber;

    @OneToOne(cascade = {CascadeType.MERGE, CascadeType.REFRESH})
    @JoinColumns({
            @JoinColumn(name = "NO_CIA", referencedColumnName = "NO_CIA"),
            @JoinColumn(name = "COD_ART", referencedColumnName = "COD_ART")
    })
    private ProductItem productItem;

    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    @JoinColumn(name = "IDORDENPRODUCCION", nullable = true, updatable = false, insertable = true)
    private ProductionOrder productionOrder;

    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    @JoinColumn(name = "IDPRODUCTOSIMPLE", nullable = true, updatable = false, insertable = true)
    private SingleProduct singleProduct;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "IDCOMPANIA", nullable = false, updatable = false, insertable = true)
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

    public ProductionOrder getProductionOrder() {
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

    public SingleProduct getSingleProduct() {
        return singleProduct;
    }

    public void setSingleProduct(SingleProduct singleProduct) {
        this.singleProduct = singleProduct;
    }
}
