package com.encens.khipus.model.production;

import com.encens.khipus.util.Constants;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: david
 * Date: 6/17/13
 * Time: 12:32 PM
 * To change this template use File | Settings | File Templates.
 */

@TableGenerator(name = "ProductComposition_Generator",
        table = "secuencia",
        pkColumnName = "tabla",
        valueColumnName = "valor",
        pkColumnValue = "composicionproducto",
        allocationSize = Constants.SEQUENCE_ALLOCATION_SIZE)

@Entity
@Table(name = "composicionproducto")
@Filter(name = "companyFilter")
@EntityListeners(com.encens.khipus.model.CompanyListener.class)
public class ProductComposition implements com.encens.khipus.model.BaseModel {

    @Id
    @Column(name = "idcomposicionproducto", columnDefinition = "DECIMAL(24,0)", nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "ProductComposition_Generator")
    private Long id;

    @Column(name = "nombre", nullable = false, length = 100)
    private String name;

    @Column(name = "cantidadproducir", columnDefinition = "DECIMAL(24,0)", nullable = false)
    private Double producingAmount;

    @Column(name = "pesocontenedor", columnDefinition = "DECIMAL(24,0)", nullable = false)
    private Double containerWeight;

    @Column(name = "teoricoobtenido", columnDefinition = "DECIMAL(24,0)", nullable = false)
    private Double supposedAmount;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idproductoprocesado", nullable = false, updatable = false, insertable = true)
    private ProcessedProduct processedProduct;

    @OneToMany(mappedBy = "productComposition", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @Cascade(org.hibernate.annotations.CascadeType.DELETE_ORPHAN)
    private List<ProductionIngredient> productionIngredientList = new ArrayList<ProductionIngredient>();

    @Column(name = "activo", nullable = false)
    @Type(type = "IntegerBoolean")
    private Boolean active;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idcompania", nullable = false, updatable = false, insertable = true)
    private com.encens.khipus.model.admin.Company company;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Double getProducingAmount() {
        return producingAmount;
    }

    public void setProducingAmount(Double producingAmount) {
        this.producingAmount = producingAmount;
    }

    public ProcessedProduct getProcessedProduct() {
        return processedProduct;
    }

    public void setProcessedProduct(ProcessedProduct processedProduct) {
        this.processedProduct = processedProduct;
    }

    public com.encens.khipus.model.admin.Company getCompany() {
        return company;
    }

    public void setCompany(com.encens.khipus.model.admin.Company company) {
        this.company = company;
    }

    public List<ProductionIngredient> getProductionIngredientList() {
        return productionIngredientList;
    }

    public void setProductionIngredientList(List<ProductionIngredient> productionIngredients) {
        this.productionIngredientList = productionIngredients;
    }

    public boolean getActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Transient
    public String getFullNameOfProcessedProduct() {
        return (processedProduct == null ? "" : processedProduct.getFullName());
    }

    public void setFullNameOfProcessedProduct(String fullName) {

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getContainerWeight() {
        return containerWeight;
    }

    public void setContainerWeight(Double containerWeight) {
        this.containerWeight = containerWeight;
    }

    public Double getSupposedAmount() {
        return supposedAmount;
    }

    public void setSupposedAmount(Double supposedAmount) {
        this.supposedAmount = supposedAmount;
    }
}
