package com.encens.khipus.model.production;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyListener;
import com.encens.khipus.model.admin.Company;
import com.encens.khipus.util.Constants;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.Filter;
import org.hibernate.validator.NotNull;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Diego
 * Date: 12/11/13
 * Time: 16:40
 * To change this template use File | Settings | File Templates.
 */

@TableGenerator(name = "SingleProduct_Generator",
        table = "secuencia",
        pkColumnName = "tabla",
        valueColumnName = "valor",
        pkColumnValue = "productosimple",
        allocationSize = Constants.SEQUENCE_ALLOCATION_SIZE)

@Entity
@Table(name = "productosimple")
@Filter(name = "companyFilter")
@EntityListeners(CompanyListener.class)
public class SingleProduct implements BaseModel {

    @Id
    @Column(name = "idproductosimple", nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "SingleProduct_Generator")
    private Long id;

    @Column(name = "cantidad", nullable = true)
    private Integer amount = 0;

    @Column(name = "cantidadproducidaresponsable", nullable = true)
    private Integer amountResponsible = 0;

    @Column(name = "estado", length = 20, nullable = false)
    @Enumerated(EnumType.STRING)
    private ProductionPlanningState state = ProductionPlanningState.PENDING;

    /*@OneToOne(fetch = FetchType.LAZY)
    @PrimaryKeyJoinColumn(name = "IDMETAPRODUCTOPRODUCCION")
    private MetaProduct metaProduct;
*/
    @Column(name = "no_trans",nullable = true)
    private String numberTransaction;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idcompania", nullable = false, updatable = false, insertable = true)
    @NotNull
    private Company company;

    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.REFRESH})
    @JoinColumn(name = "idproductobase", nullable = true, updatable = false, insertable = true)
    private BaseProduct baseProduct;

    @OneToOne(fetch = FetchType.LAZY, mappedBy = "singleProduct", cascade = CascadeType.ALL)
    @Cascade(org.hibernate.annotations.CascadeType.DELETE_ORPHAN)
    private ProductProcessingSingle productProcessingSingle;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "singleProduct", cascade = CascadeType.ALL)
    @Cascade(org.hibernate.annotations.CascadeType.DELETE_ORPHAN)
    private List<OrderMaterial> orderMaterials = new ArrayList<OrderMaterial>();

    @Column(name = "costototalmateriales", nullable = true, columnDefinition = "DECIMAL(16,6)")
    private BigDecimal totalMaterial = new BigDecimal(0.0);

    @Column(name = "costototalinsumos", nullable = true, columnDefinition = "DECIMAL(16,6)")
    private BigDecimal totalInput = new BigDecimal(0.0);

    @Column(name = "costototalmanoobra", nullable = true, columnDefinition = "DECIMAL(16,6)")
    private BigDecimal costLabor = new BigDecimal(0.0);

    @Column(name = "costototalindirecto", nullable = true, columnDefinition = "DECIMAL(16,6)")
    private BigDecimal totalIndirecCost = new BigDecimal(0.0);

    @Column(name = "costounitario", nullable = true, columnDefinition = "DECIMAL(16,6)")
    private BigDecimal unitCost = new BigDecimal(0.0);

    @Column(name = "porcentajegrasa", nullable = true, columnDefinition = "DECIMAL(16,6)")
    private BigDecimal greasePorentage = new BigDecimal(0.0);

    @Column(name = "costototalproduccion", nullable = true, columnDefinition = "DECIMAL(16,6)")
    private BigDecimal totalCostProduction = new BigDecimal(0.0);

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "singleProduct", cascade = CascadeType.ALL)
    @Cascade(org.hibernate.annotations.CascadeType.DELETE_ORPHAN)
    private List<IndirectCosts> indirectCostses;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getAmount() {
        return amount;
    }

    public void setAmount(Integer amount) {
        this.amount = amount;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public BaseProduct getBaseProduct() {
        return baseProduct;
    }

    public void setBaseProduct(BaseProduct baseProduct) {
        this.baseProduct = baseProduct;
    }

    public BigDecimal getTotalMaterial() {
        return totalMaterial;
    }

    public void setTotalMaterial(BigDecimal totalMaterial) {
        this.totalMaterial = totalMaterial;
    }

    public BigDecimal getTotalInput() {
        return totalInput;
    }

    public void setTotalInput(BigDecimal totalInput) {
        this.totalInput = totalInput;
    }

    public ProductionPlanningState getState() {
        return state;
    }

    public void setState(ProductionPlanningState state) {
        this.state = state;
    }

    public List<OrderMaterial> getOrderMaterials() {
        return orderMaterials;
    }

    public void setOrderMaterials(List<OrderMaterial> orderMaterials) {
        this.orderMaterials = orderMaterials;
    }

    public BigDecimal getTotalIndirecCost() {
        return totalIndirecCost;
    }

    public void setTotalIndirecCost(BigDecimal totalIndirecCost) {
        this.totalIndirecCost = totalIndirecCost;
    }

    public BigDecimal getUnitCost() {
        return unitCost;
    }

    public void setUnitCost(BigDecimal unitCost) {
        this.unitCost = unitCost;
    }

    public BigDecimal getGreasePorentage() {
        return greasePorentage;
    }

    public void setGreasePorentage(BigDecimal greasePorentage) {
        this.greasePorentage = greasePorentage;
    }

    public BigDecimal getCostLabor() {
        return costLabor;
    }

    public void setCostLabor(BigDecimal costLabor) {
        this.costLabor = costLabor;
    }

    public BigDecimal getTotalCostProduction() {
        return totalCostProduction;
    }

    public void setTotalCostProduction(BigDecimal totalCostProduction) {
        this.totalCostProduction = totalCostProduction;
    }

    /*public List<IndirectCosts> getIndirectCostses() {

        if(indirectCostses == null)
            return new ArrayList<IndirectCosts>();

        return indirectCostses;
    }

    public void setIndirectCostses(List<IndirectCosts> indirectCostses) {
        this.indirectCostses.clear();
        if(indirectCostses != null)
        this.indirectCostses = indirectCostses;
    }*/

    public List<IndirectCosts> getIndirectCostses() {

        if(indirectCostses == null)
            return new ArrayList<IndirectCosts>();

        return indirectCostses;
    }

    public void setIndirectCostses(List<IndirectCosts> indirectCostses) {
        //this.indirectCostses.clear();
        //this.indirectCostses = new ArrayList<IndirectCosts>();
        this.indirectCostses.addAll(indirectCostses);
    }

    public ProductProcessingSingle getProductProcessingSingle() {
        return productProcessingSingle;
    }

    public void setProductProcessingSingle(ProductProcessingSingle productProcessingSingle) {
        this.productProcessingSingle = productProcessingSingle;
    }

    public String getNumberTransaction() {
        return numberTransaction;
    }

    public void setNumberTransaction(String numberTransaction) {
        this.numberTransaction = numberTransaction;
    }

    public Integer getAmountResponsible() {
        return amountResponsible;
    }

    public void setAmountResponsible(Integer amountResponsible) {
        this.amountResponsible = amountResponsible;
    }
}
