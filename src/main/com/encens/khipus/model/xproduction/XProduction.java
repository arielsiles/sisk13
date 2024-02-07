package com.encens.khipus.model.xproduction;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyListener;
import com.encens.khipus.model.admin.Company;
import com.encens.khipus.model.finances.Voucher;
import com.encens.khipus.model.production.ProductionState;
import org.hibernate.annotations.Filter;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity fo Product
 *
 * @author:
 */

@NamedQueries({})

@TableGenerator(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "XProduction.tableGenerator",
        table = com.encens.khipus.util.Constants.SEQUENCE_TABLE_NAME,
        pkColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_PK_COLUMN_NAME,
        valueColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_VALUE_COLUMN_NAME,
        pkColumnValue = "xpr_produccion",
        allocationSize = com.encens.khipus.util.Constants.SEQUENCE_ALLOCATION_SIZE)

@Entity
@Filter(name = com.encens.khipus.util.Constants.COMPANY_FILTER_NAME)
@EntityListeners(CompanyListener.class)
@Table(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "xpr_produccion")
public class XProduction implements BaseModel {

    @Id
    @Column(name = "idproduccion")
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "XProduction.tableGenerator")
    private Long id;

    @Column(name = "codigo") private Integer code;

    @Column(name = "estado", nullable = true)
    @Enumerated(EnumType.STRING)
    private ProductionState state;

    @Column(name = "costototal")
    private BigDecimal totalCost;

    @Column(name = "totalmp")
    private BigDecimal totalRawMaterial;

    @Column(name = "descripcion")
    private String description;

    @ManyToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "idformula", nullable = true, updatable = false, insertable = true)
    private XFormulation formulation;

    @ManyToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "idtanque", nullable = true, updatable = false, insertable = true)
    private XProductionTank productionTank;

    @ManyToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "idplan", nullable = true, updatable = false, insertable = true)
    private XProductionPlan productionPlan;

    @OneToMany(mappedBy = "production", fetch = FetchType.LAZY)
    private List<XSupply> supplyList = new ArrayList<XSupply>(0);

    @OneToMany(mappedBy = "production", fetch = FetchType.LAZY)
    private List<XProductionProduct> productionProductList = new ArrayList<XProductionProduct>(0);

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_tmpenc")
    private Voucher voucher;

    @Version
    @Column(name = "version", nullable = false)
    private long version;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idcompania", nullable = false, updatable = false, insertable = true)
    private Company company;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public XFormulation getFormulation() {
        return formulation;
    }

    public void setFormulation(XFormulation formulation) {
        this.formulation = formulation;
    }

    public XProductionTank getProductionTank() {
        return productionTank;
    }

    public void setProductionTank(XProductionTank productionTank) {
        this.productionTank = productionTank;
    }

    public XProductionPlan getProductionPlan() {
        return productionPlan;
    }

    public void setProductionPlan(XProductionPlan productionPlan) {
        this.productionPlan = productionPlan;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public List<XSupply> getSupplyList() {
        return supplyList;
    }

    public void setSupplyList(List<XSupply> supplyList) {
        this.supplyList = supplyList;
    }

    public ProductionState getState() {
        return state;
    }

    public void setState(ProductionState state) {
        this.state = state;
    }

    public List<XProductionProduct> getProductionProductList() {
        return productionProductList;
    }

    public void setProductionProductList(List<XProductionProduct> productionProductList) {
        this.productionProductList = productionProductList;
    }

    public BigDecimal getTotalCost() {
        return totalCost;
    }

    public void setTotalCost(BigDecimal totalCost) {
        this.totalCost = totalCost;
    }

    public BigDecimal getTotalRawMaterial() {
        return totalRawMaterial;
    }

    public void setTotalRawMaterial(BigDecimal totalRawMaterial) {
        this.totalRawMaterial = totalRawMaterial;
    }

    public Voucher getVoucher() {
        return voucher;
    }

    public void setVoucher(Voucher voucher) {
        this.voucher = voucher;
    }
}
