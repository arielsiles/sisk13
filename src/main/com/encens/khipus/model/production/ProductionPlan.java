package com.encens.khipus.model.production;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyListener;
import com.encens.khipus.model.admin.Company;
import org.hibernate.annotations.Filter;

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

@TableGenerator(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "ProductionPlan.tableGenerator",
        table = com.encens.khipus.util.Constants.SEQUENCE_TABLE_NAME,
        pkColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_PK_COLUMN_NAME,
        valueColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_VALUE_COLUMN_NAME,
        pkColumnValue = "pr_plan",
        allocationSize = com.encens.khipus.util.Constants.SEQUENCE_ALLOCATION_SIZE)

@Entity
@Filter(name = com.encens.khipus.util.Constants.COMPANY_FILTER_NAME)
@EntityListeners(CompanyListener.class)
@Table(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "pr_plan")
public class ProductionPlan implements BaseModel {

    @Id
    @Column(name = "idplan")
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "ProductionPlan.tableGenerator")
    private Long id;

    @Column(name = "nombre")
    private String name;

    @Column(name = "fecha")
    private Date date;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "productionPlan")
    private List<Production> productionList = new ArrayList<Production>(0);

    @OneToMany(mappedBy = "productionPlan", fetch = FetchType.LAZY)
    private List<ProductionProduct> productionProductList = new ArrayList<ProductionProduct>(0);

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

    public List<Production> getProductionList() {
        return productionList;
    }

    public void setProductionList(List<Production> productionList) {
        this.productionList = productionList;
    }

    public List<ProductionProduct> getProductionProductList() {
        return productionProductList;
    }

    public void setProductionProductList(List<ProductionProduct> productionProductList) {
        this.productionProductList = productionProductList;
    }
}
