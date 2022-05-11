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

@NamedQueries({

        @NamedQuery(name = "BaseProduct.findBaseProductByDate",
                query = "Select baseProduct " +
                        "from BaseProduct baseProduct " +
                        "left join baseProduct.productionPlanningBase productionPlanning " +
                        "where productionPlanning.date between :startDate and :endDate ")
})

@TableGenerator(name = "BaseProduct_Generator",
        table = "secuencia",
        pkColumnName = "tabla",
        valueColumnName = "valor",
        pkColumnValue = "productobase",
        allocationSize = Constants.SEQUENCE_ALLOCATION_SIZE)

@Entity
@Table(name = "productobase")
@Filter(name = "companyFilter")
@EntityListeners(CompanyListener.class)
public class BaseProduct implements BaseModel {

    @Id
    @Column(name = "idproductobase", nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "BaseProduct_Generator")
    private Long id;

    @Column(name = "unidades", nullable = true)
    private Integer units;

    @Column(name = "volumen", nullable = true ,columnDefinition = "DECIMAL(8,2)")
    private Double volume;

    @Column(name = "estado", length = 20, nullable = false)
    @Enumerated(EnumType.STRING)
    private ProductionPlanningState state = ProductionPlanningState.PENDING;

    @Column(name = "id_tmpenc")
    private Long voucherId;

    @Column(name = "no_trans",nullable = true)
    private String numberTransaction;

    @Column(name = "no_vale",nullable = true)
    private String numberVoucher;

    @Transient
    private Boolean selected = true;

    @OneToMany(mappedBy = "baseProduct", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @Cascade(org.hibernate.annotations.CascadeType.DELETE_ORPHAN)
    private List<ProductProcessing> productProcessings = new ArrayList<ProductProcessing>();

    @ManyToOne(optional = true, fetch = FetchType.LAZY, cascade = {CascadeType.REFRESH})
    @JoinColumn(name = "idplanificacionproduccion", nullable = false, updatable = false, insertable = true)
    private ProductionPlanning productionPlanningBase;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idcompania", nullable = false, updatable = false, insertable = true)
    @NotNull
    private Company company;

    @OneToMany(mappedBy = "baseProduct", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @Cascade(org.hibernate.annotations.CascadeType.DELETE_ORPHAN)
    private List<SingleProduct> singleProducts = new ArrayList<SingleProduct>();

    @Column(name = "costototalinsumos", nullable = true, columnDefinition = "DECIMAL(16,6)")
    private BigDecimal totalInput = new BigDecimal(0.0);

    @OneToMany(mappedBy = "baseProductInput", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @Cascade(org.hibernate.annotations.CascadeType.DELETE_ORPHAN)
    private List<OrderInput> orderInputs = new ArrayList<OrderInput>();

    @Column(name = "codigo", length = 50, nullable = false)
    private String code;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getUnits() {
        return units;
    }

    public void setUnits(Integer units) {
        this.units = units;
    }

    public Double getVolume() {
        return volume;
    }

    public void setVolume(Double volume) {
        this.volume = volume;
    }

    public List<ProductProcessing> getProductProcessings() {
        return productProcessings;
    }

    public void setProductProcessings(List<ProductProcessing> productProcessings) {
        this.productProcessings = productProcessings;
    }

    public ProductionPlanning getProductionPlanningBase() {
        return productionPlanningBase;
    }

    public void setProductionPlanningBase(ProductionPlanning productionPlanningBase) {
        this.productionPlanningBase = productionPlanningBase;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public List<SingleProduct> getSingleProducts() {
        return singleProducts;
    }

    public void setSingleProducts(List<SingleProduct> singleProducts) {
        this.singleProducts = singleProducts;
    }

    public BigDecimal getTotalInput() {
        return totalInput;
    }

    public void setTotalInput(BigDecimal totalInput) {
        this.totalInput = totalInput;
    }

    public List<OrderInput> getOrderInputs() {
        return orderInputs;
    }

    public void setOrderInputs(List<OrderInput> orderInputs) {
        this.orderInputs = orderInputs;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public ProductionPlanningState getState() {
        return state;
    }

    public void setState(ProductionPlanningState state) {
        this.state = state;
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

    public Long getVoucherId() {
        return voucherId;
    }

    public void setVoucherId(Long voucherId) {
        this.voucherId = voucherId;
    }

    /*public String getStateReprocess(){
        String result = "PENDING";

        boolean PEN = false;
        boolean EXE = false;
        boolean FIN = false;
        boolean INS = false;
        boolean TAB = false;

        for (SingleProduct singleProduct : getSingleProducts()){
            if(singleProduct.getState().equals(ProductionPlanningState.PENDING))
                PEN = true;
        }

        for (SingleProduct singleProduct : getSingleProducts()){
            if(singleProduct.getState().equals(ProductionPlanningState.EXECUTED))
                EXE = true;
        }

        for (SingleProduct singleProduct : getSingleProducts()){
            if(singleProduct.getState().equals(ProductionPlanningState.FINALIZED))
                FIN = true;
        }

        for (SingleProduct singleProduct : getSingleProducts()){
            if(singleProduct.getState().equals(ProductionPlanningState.INSTOCK))
                INS = true;
        }

        for (SingleProduct singleProduct : getSingleProducts()){
            if(singleProduct.getState().equals(ProductionPlanningState.TABULATED))
                TAB = true;
        }

        //if (PEN && ((EXE==false) && (FIN==false) && (INS==false) && (TAB == false)))
        if (PEN && (!EXE && !FIN && !INS && !TAB))
            result = ProductionPlanningState.PENDING.toString();
        if (EXE && (!PEN && !FIN && !INS && !TAB))
            result = ProductionPlanningState.EXECUTED.toString();
        if (FIN && (!PEN && !EXE && !INS && !TAB))
            result = ProductionPlanningState.FINALIZED.toString();
        if (INS && (!PEN && !EXE && !FIN && !TAB))
            result = ProductionPlanningState.INSTOCK.toString();
        if (TAB && (!PEN && !EXE && !FIN && !INS))
            result = ProductionPlanningState.TABULATED.toString();

        return  result;
    }*/


}
