package com.encens.khipus.model.production;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyListener;
import com.encens.khipus.model.admin.Company;
import com.encens.khipus.util.DateUtils;
import com.encens.khipus.util.MessageUtils;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.Filter;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: david
 * Date: 6/20/13
 * Time: 10:29 AM
 * To change this template use File | Settings | File Templates.
 */
@NamedQueries({
        @NamedQuery(name = "ProductionPlanning.widthProductionOrderAndProductCompositionAndProcessedProductFind",
                query = "select productionPlanning " +
                        "from ProductionPlanning productionPlanning " +
                        "left join fetch productionPlanning.productionOrderList productionOrder " +
                        "left join fetch productionOrder.productComposition productComposition " +
                        "left join fetch productComposition.processedProduct " +
                        "where productionPlanning.id = :id"),
        @NamedQuery(name = "ProductionPlanning.findByDate",
                query = "select productionPlanning " +
                        "from ProductionPlanning productionPlanning " +
                        "where productionPlanning.date = :date")
})
@TableGenerator(name = "ProductionPlanning_Generator",
        table = "SECUENCIA",
        pkColumnName = "TABLA",
        valueColumnName = "VALOR",
        pkColumnValue = "PLANIFICACIONPRODUCCION",
        allocationSize = 10)

@Entity
@Table(name = "PLANIFICACIONPRODUCCION", uniqueConstraints = @UniqueConstraint(columnNames = {"FECHA", "IDCOMPANIA"}))
@Filter(name = "companyFilter")
@EntityListeners(CompanyListener.class)
public class ProductionPlanning implements BaseModel {

    @Transient
    public static final String UNIQUE_DATE = "UNIQUE_DATE";

    @Id
    @Column(name = "IDPLANIFICACIONPRODUCCION", nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "ProductionPlanning_Generator")
    private Long id;

    @Column(name = "FECHA", nullable = false, columnDefinition = "DATE")
    private Date date;

    @Version
    @Column(name = "VERSION", nullable = false)
    private long version;

    @Column(name = "OBSERVACIONES", nullable = true, length = 1000)
    private String observations;

    @Column(name = "ESTADO", length = 50, nullable = false)
    @Enumerated(EnumType.STRING)
    private ProductionPlanningState state = ProductionPlanningState.PENDING;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "IDCOMPANIA", nullable = false, updatable = false, insertable = true)
    private Company company;

    @OneToMany(mappedBy = "productionPlanning", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @Cascade(org.hibernate.annotations.CascadeType.DELETE_ORPHAN)
    @OrderBy("code desc")
    private List<ProductionOrder> productionOrderList = new ArrayList<ProductionOrder>();

    @OneToMany(mappedBy = "productionPlanningBase", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @Cascade(org.hibernate.annotations.CascadeType.DELETE_ORPHAN)
    //@OrderBy("code desc")
    private List<BaseProduct> baseProducts = new ArrayList<BaseProduct>();

    @Column(name = "TOTALLECHEPRODUCIDA", nullable = true, columnDefinition = "DECIMAL(9,0)")
    private BigDecimal totalMilk = new BigDecimal(0.0);

    @Column(name = "TOTALLECHEQUESO", nullable = true, columnDefinition = "DECIMAL(9,0)")
    private BigDecimal totalMilkCheese = new BigDecimal(0.0);

    @Column(name = "TOTALLECHEUHT", nullable = true, columnDefinition = "DECIMAL(9,0)")
    private BigDecimal totalMilkUHT = new BigDecimal(0.0);

    @Column(name = "TOTALLECHEYOGURT", nullable = true, columnDefinition = "DECIMAL(9,0)")
    private BigDecimal totalMilkYogurt = new BigDecimal(0.0);

    @Column(name = "TOTALLECHEREPROCESO", nullable = true, columnDefinition = "DECIMAL(9,0)")
    private BigDecimal totalMilkReprocessed = new BigDecimal(0.0);

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public String getObservations() {
        return observations;
    }

    public void setObservations(String observations) {
        this.observations = observations;
    }

    public List<ProductionOrder> getProductionOrderList() {
        return productionOrderList;
    }

    public void setProductionOrderList(List<ProductionOrder> productionOrderList) {
        this.productionOrderList = productionOrderList;
    }

    public ProductionPlanningState getState() {
        return state;
    }

    public void setState(ProductionPlanningState state) {
        this.state = state;
    }

    public String getLabelDate() {
        return DateUtils.format(this.date, MessageUtils.getMessage("patterns.date"));
    }

    public List<BaseProduct> getBaseProducts() {
        return baseProducts;
    }

    public void setBaseProducts(List<BaseProduct> baseProducts) {
        this.baseProducts = baseProducts;
    }

    public BigDecimal getTotalMilk() {
        return totalMilk;
    }

    public void setTotalMilk(BigDecimal totalMilk) {
        this.totalMilk = totalMilk;
    }

    public BigDecimal getTotalMilkCheese() {
        return totalMilkCheese;
    }

    public void setTotalMilkCheese(BigDecimal totalMilkCheese) {
        this.totalMilkCheese = totalMilkCheese;
    }

    public BigDecimal getTotalMilkUHT() {
        return totalMilkUHT;
    }

    public void setTotalMilkUHT(BigDecimal totalMilkUHT) {
        this.totalMilkUHT = totalMilkUHT;
    }

    public BigDecimal getTotalMilkYogurt() {
        return totalMilkYogurt;
    }

    public void setTotalMilkYogurt(BigDecimal totalMilkYogurt) {
        this.totalMilkYogurt = totalMilkYogurt;
    }

    public BigDecimal getTotalMilkReprocessed() {
        return totalMilkReprocessed;
    }

    public void setTotalMilkReprocessed(BigDecimal totalMilkReprocessed) {
        this.totalMilkReprocessed = totalMilkReprocessed;
    }

    public List<SingleProduct> getSingleProducts(){
        List<SingleProduct> singleProductList = new ArrayList<SingleProduct>();

        for (BaseProduct baseProduct:baseProducts){
            for (SingleProduct singleProduct:baseProduct.getSingleProducts())
            singleProductList.add(singleProduct);
        }

        return  singleProductList;
    }
}
