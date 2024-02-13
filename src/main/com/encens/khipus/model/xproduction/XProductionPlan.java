package com.encens.khipus.model.xproduction;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyListener;
import com.encens.khipus.model.admin.Company;
import org.hibernate.annotations.Filter;
import com.encens.khipus.model.production.ProductionPlanState;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Entity fo Product
 *
 * @author:
 */

@NamedQueries({})

@TableGenerator(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "XProductionPlan.tableGenerator",
        table = com.encens.khipus.util.Constants.SEQUENCE_TABLE_NAME,
        pkColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_PK_COLUMN_NAME,
        valueColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_VALUE_COLUMN_NAME,
        pkColumnValue = "xpr_plan",
        allocationSize = com.encens.khipus.util.Constants.SEQUENCE_ALLOCATION_SIZE)

@Entity
@Filter(name = com.encens.khipus.util.Constants.COMPANY_FILTER_NAME)
@EntityListeners(CompanyListener.class)
@Table(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "xpr_plan")
public class XProductionPlan implements BaseModel {

    @Id
    @Column(name = "idplan")
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "XProductionPlan.tableGenerator")
    private Long id;

    @Column(name = "nombre")
    private String name;

    @Column(name = "fecha")
    private Date date;

    @Column(name = "estado", nullable = true)
    @Enumerated(EnumType.STRING)
    private ProductionPlanState state;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "productionPlan")
    private List<XProduction> productionList = new ArrayList<XProduction>(0);

    @OneToMany(mappedBy = "productionPlan", fetch = FetchType.LAZY)
    private List<XProductionProduct> productionProductList = new ArrayList<XProductionProduct>(0);

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


    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public List<XProduction> getProductionList() {
        return productionList;
    }

    public void setProductionList(List<XProduction> productionList) {
        this.productionList = productionList;
    }

    public List<XProductionProduct> getProductionProductList() {
        return productionProductList;
    }

    public void setProductionProductList(List<XProductionProduct> productionProductList) {
        this.productionProductList = productionProductList;
    }

    public com.encens.khipus.model.production.ProductionPlanState getState() {
        return state;
    }

    public void setState(ProductionPlanState state) {
        this.state = state;
    }
}
