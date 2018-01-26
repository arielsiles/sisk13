package com.encens.khipus.model.production;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyListener;
import com.encens.khipus.model.admin.Company;
import com.encens.khipus.model.finances.Voucher;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.Filter;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@NamedQueries({
        @NamedQuery(name = "ProductionOrder.findBySubDateOnCode",
                query = "select productionOrder.code " +
                        "from ProductionOrder productionOrder " +
                        "where productionOrder.code like :seed"),
        @NamedQuery(name = "ProductionOrder.findById", query = "Select p from ProductionOrder p where p.id=:id"),

        @NamedQuery(name = "ProductionOrder.findProductionOrdersByProductItem",
                   query = "Select productionOrder " +
                           "from ProductionOrder productionOrder " +
                           "left join productionOrder.productionPlanning productionPlanning " +
                           "left join productionOrder.productComposition.processedProduct.productItem productItem " +
                           "where productionPlanning.date between :startDate and :endDate " +
                           "and productItem.productItemCode =:productItemCode "),

        @NamedQuery(name = "ProductionOrder.findProductionOrders",
                   query = "Select productionOrder " +
                           "from ProductionOrder productionOrder " +
                           "left join productionOrder.productionPlanning productionPlanning " +
                           "where productionPlanning.date between :startDate and :endDate ")


})

@TableGenerator(name = "ProductionOrder_Generator",
        table = "SECUENCIA",
        pkColumnName = "TABLA",
        valueColumnName = "VALOR",
        pkColumnValue = "ORDENPRODUCCION",
        allocationSize = 10)

@Entity
@Table(name = "ORDENPRODUCCION", uniqueConstraints = @UniqueConstraint(columnNames = {"CODIGO", "IDCOMPANIA"}))
@Filter(name = "companyFilter")
@EntityListeners(CompanyListener.class)
public class ProductionOrder implements BaseModel {

    @Id
    @Column(name = "IDORDENPRODUCCION", nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "ProductionOrder_Generator")
    private Long id;

    @Column(name = "CODIGO", length = 50, nullable = false)
    private String code;

    @Column(name = "ESTADOORDEN", length = 20, nullable = true)
    @Enumerated(EnumType.STRING)
    private ProductionPlanningState estateOrder = ProductionPlanningState.PENDING;

    @Column(name = "NO_VALE",nullable = true)
    private String numberVoucher;

    @Column(name = "NO_TRANS",nullable = true)
    private String numberTransaction;

    @Column(name = "CANTIDADESPERADA", nullable = false, columnDefinition = "DECIMAL(24,0)")
    private Double expendAmount;

    @Column(name = "PESOCONTENEDOR", nullable = false, columnDefinition = "DECIMAL(24,0)")
    private Double containerWeight;

    @Column(name = "CANTIDADPRODUCIDA", nullable = false, columnDefinition = "DECIMAL(24,0)")
    private Double producedAmount = 0.0;

    @Column(name = "CANTIDADPRODUCIDARESPONSABLE", nullable = true, columnDefinition = "DECIMAL(24,0)")
    private Double producedAmountResponsible = 0.0;

    @Column(name = "PRECIOTOTALMATERIAL", nullable = true, columnDefinition = "DECIMAL(16,2)")
    private Double totalPriceMaterial = 0.0;

    @Column(name = "PORCENTAJEGRASA",nullable = true,columnDefinition = "DECIMAL(16,2)")
    private Double greasePercentage = 0.0;

    @Column(name = "PRECIOTOTALINSUMO", nullable = true, columnDefinition = "DECIMAL(16,2)")
    private Double totalPriceInput = 0.0;

    @Column(name = "PRECIOTOTALMANOOBRA", nullable = true, columnDefinition = "DECIMAL(16,2)")
    private Double totalPriceJourney = 0.0;

    @Column(name = "TOTALCOSTOINDIRECTO", nullable = true, columnDefinition = "DECIMAL(16,2)")
    private Double totalIndirectCosts = 0.0;

    @Column(name = "COSTOTOALPRODUCCION", nullable = true, columnDefinition = "DECIMAL(16,2)")
    private Double totalCostProduction = 0.0;


    @Column(name = "COSTOUNITARIO", nullable = true, columnDefinition = "DECIMAL(16,6)")
    private BigDecimal unitCost = BigDecimal.ZERO;

    @Column(name = "COSTINSUMOPRINCIPAL", nullable = true,columnDefinition = "DECIMAL(16,2)")
    private Double totalCostInputMain = 0.0;

    @Transient
    private Double milk;

    @Transient
    private Boolean selected = true;

    /*@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_tmpenc")
    private Voucher voucher;*/


    @Column(name = "id_tmpenc")
    private Long voucherId;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "IDCOMPANIA", nullable = false, updatable = false, insertable = true)
    private Company company;

    @ManyToOne(optional = false, fetch = FetchType.LAZY, cascade = {CascadeType.REFRESH})
    @JoinColumn(name = "IDPLANIFICACIONPRODUCCION", nullable = false, updatable = false, insertable = true)
    private ProductionPlanning productionPlanning;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "productionOrder", cascade = CascadeType.ALL)
    @Cascade(org.hibernate.annotations.CascadeType.DELETE_ORPHAN)
    private List<InputProductionVoucher> inputProductionVoucherList = new ArrayList<InputProductionVoucher>();

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "productionOrder", cascade = CascadeType.ALL)
    @Cascade(org.hibernate.annotations.CascadeType.DELETE_ORPHAN)
    private List<OutputProductionVoucher> outputProductionVoucherList = new ArrayList<OutputProductionVoucher>();

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "productionOrder", cascade = CascadeType.ALL)
    @Cascade(org.hibernate.annotations.CascadeType.DELETE_ORPHAN)
    private List<OrderMaterial> orderMaterials = new ArrayList<OrderMaterial>();

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "productionOrder", cascade = CascadeType.ALL)
    @Cascade(org.hibernate.annotations.CascadeType.DELETE_ORPHAN)
    private List<OrderInput> orderInputs = new ArrayList<OrderInput>();

    @ManyToOne(optional = false, fetch = FetchType.LAZY, cascade = {CascadeType.REFRESH})
    @JoinColumn(name = "IDCOMPOSICIONPRODUCTO", nullable = false, updatable = true, insertable = true)
    private ProductComposition productComposition;

    @ManyToOne(optional = true, fetch = FetchType.LAZY, cascade = {CascadeType.REFRESH})
    @JoinColumn(name = "PRODUCTOPADRE", nullable = true, updatable = false, insertable = true)
    private ProductionOrder productMain;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "productionOrder", cascade = CascadeType.ALL)
    @Cascade(org.hibernate.annotations.CascadeType.DELETE_ORPHAN)
    private List<IndirectCosts> indirectCostses = new ArrayList<IndirectCosts>();

    @OneToMany(mappedBy = "productionOrder", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @Cascade(org.hibernate.annotations.CascadeType.DELETE_ORPHAN)
    private List<ProductOrder> productOrders = new ArrayList<ProductOrder>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public ProductionPlanning getProductionPlanning() {
        return productionPlanning;
    }

    public void setProductionPlanning(ProductionPlanning productionPlanning) {
        this.productionPlanning = productionPlanning;
    }

    public ProductComposition getProductComposition() {
        return productComposition;
    }

    public void setProductComposition(ProductComposition productComposition) {
        this.productComposition = productComposition;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public Double getExpendAmount() {
        return expendAmount;
    }

    public void setExpendAmount(Double producingAmount) {
        this.expendAmount = producingAmount;
    }

    public List<InputProductionVoucher> getInputProductionVoucherList() {
        return inputProductionVoucherList;
    }

    public void setInputProductionVoucherList(List<InputProductionVoucher> inputProductionVoucherList) {
        this.inputProductionVoucherList = inputProductionVoucherList;
    }

    public List<OutputProductionVoucher> getOutputProductionVoucherList() {
        return outputProductionVoucherList;
    }

    public void setOutputProductionVoucherList(List<OutputProductionVoucher> outputProductionVoucherList) {
        this.outputProductionVoucherList = outputProductionVoucherList;
    }

    public Double getContainerWeight() {
        return containerWeight;
    }

    public void setContainerWeight(Double containerWeight) {
        this.containerWeight = containerWeight;
    }

    public Double getProducedAmount() {
        return producedAmount;
    }

    public void setProducedAmount(Double supposedAmount) {
        this.producedAmount = supposedAmount;
    }

    public Double getMilk() {
        return milk;
    }

    public void setMilk(Double milk) {
        this.milk = milk;
    }

    public Double getMountMilk() {
        Double mount = 0.0;
        if (this.productComposition != null) {
            List<ProductionIngredient> ingredients = this.productComposition.getProductionIngredientList();

            for (ProductionIngredient ingredient : ingredients) {
                if (ingredient.getMetaProduct().getName().compareTo("LECHE CRUDA") == 0) {
                    mount = ingredient.getAmount();
                }
            }
        }
        return mount;
    }

    public List<OrderMaterial> getOrderMaterials() {

        if (orderMaterials == null) orderMaterials = new ArrayList<OrderMaterial>();

        return orderMaterials;
    }

    public void setOrderMaterials(List<OrderMaterial> orders) {
        orderMaterials.clear();
        if (orders != null) {
            this.orderMaterials.addAll(orders);
        }
    }

    public Double getTotalPriceMaterial() {
        return totalPriceMaterial;
    }

    public void setTotalPriceMaterial(Double totalPriceMaterial) {
        this.totalPriceMaterial = totalPriceMaterial;
    }

    public Double getTotalPriceInput() {
        return totalPriceInput;
    }

    public void setTotalPriceInput(Double totalPriceInput) {
        this.totalPriceInput = totalPriceInput;
    }

    public Double getTotalPriceJourney() {
        return totalPriceJourney;
    }

    public void setTotalPriceJourney(Double totalPriceJourney) {
        this.totalPriceJourney = totalPriceJourney;
    }

    public Double getTotalCostProduction() {
        return totalCostProduction;
    }

    public void setTotalCostProduction(Double totalCostProduction) {
        this.totalCostProduction = totalCostProduction;
    }

    public String getOrderProduct() {
        return this.code + " " + this.productComposition.getProcessedProduct().getProductItem().getName();

    }

    public ProductionPlanningState getEstateOrder() {
        return estateOrder;
    }

    public void setEstateOrder(ProductionPlanningState estateOrder) {
        this.estateOrder = estateOrder;
    }

    public List<OrderInput> getOrderInputs() {
        return orderInputs;
    }

    public void setOrderInputs(List<OrderInput> orderInputs) {
        this.orderInputs = orderInputs;
    }

    public Double getTotalIndirectCosts() {
        return totalIndirectCosts;
    }

    public void setTotalIndirectCosts(Double totalIndirectCosts) {
        this.totalIndirectCosts = totalIndirectCosts;
    }

    public BigDecimal getUnitCost() {
        return unitCost;
    }

    public void setUnitCost(BigDecimal unitCost) {
        this.unitCost = unitCost;
    }

    public List<IndirectCosts> getIndirectCostses() {

            if(indirectCostses == null)
                return new ArrayList<IndirectCosts>();

        return indirectCostses;
    }

    public void setIndirectCostses(List<IndirectCosts> indirectCostses) {
        this.indirectCostses.clear();
        if(indirectCostses != null)
        this.indirectCostses.addAll(indirectCostses);
    }

    public Double getGreasePercentage() {
        return greasePercentage;
    }

    public void setGreasePercentage(Double greasePercentage) {
        this.greasePercentage = greasePercentage;
    }

    public String getNumberVoucher() {
        return numberVoucher;
    }

    public void setNumberVoucher(String numberVoucher) {
        this.numberVoucher = numberVoucher;
    }
    public String getNumberTransaction() {
        return numberTransaction;
    }

    public void setNumberTransaction(String numberTransaction) {
        this.numberTransaction = numberTransaction;
    }

    public Boolean getSelected() {
        return selected;
    }

    public void setSelected(Boolean selected) {
        this.selected = selected;
    }

    public Double getProducedAmountResponsible() {
        return producedAmountResponsible;
    }

    public void setProducedAmountResponsible(Double producedAmountResponsible) {
        this.producedAmountResponsible = producedAmountResponsible;
    }

    public ProductionOrder getProductMain() {
        return productMain;
    }

    public void setProductMain(ProductionOrder productMain) {
        this.productMain = productMain;
    }

    public List<ProductOrder> getProductOrders() {
        return productOrders;
    }

    public void setProductOrders(List<ProductOrder> productOrders) {
        this.productOrders = productOrders;
    }

    public Double getTotalCostInputMain() {
        return totalCostInputMain;
    }

    public void setTotalCostInputMain(Double totalCostInputMain) {
        this.totalCostInputMain = totalCostInputMain;

    }

    public Long getVoucherId() {
        return voucherId;
    }

    public void setVoucherId(Long voucherId) {
        this.voucherId = voucherId;
    }
}
