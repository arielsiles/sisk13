package com.encens.khipus.model.xproduction;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyListener;
import com.encens.khipus.model.admin.Company;
import com.encens.khipus.model.xproduction.FormulationInput;
import com.encens.khipus.model.xproduction.FormulationState;
import com.encens.khipus.model.xproduction.ProductionCategory;
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

@TableGenerator(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "Formulation.tableGenerator",
        table = com.encens.khipus.util.Constants.SEQUENCE_TABLE_NAME,
        pkColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_PK_COLUMN_NAME,
        valueColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_VALUE_COLUMN_NAME,
        pkColumnValue = "pr_formula",
        allocationSize = com.encens.khipus.util.Constants.SEQUENCE_ALLOCATION_SIZE)

@Entity
@Filter(name = com.encens.khipus.util.Constants.COMPANY_FILTER_NAME)
@EntityListeners(CompanyListener.class)
@Table(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "pr_formula")
public class Formulation implements BaseModel {

    @Id
    @Column(name = "idformula")
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "Formulation.tableGenerator")
    private Long id;

    @Column(name = "nombre", nullable = false)
    private String name;

    @Column(name = "estado")
    @Enumerated(EnumType.STRING)
    private FormulationState state = FormulationState.PEN;

    @Column(name = "totaleq", precision = 16, scale = 2)
    private BigDecimal totalEquivalent;

    @Column(name = "capacidad", precision = 16, scale = 2)
    private BigDecimal capacity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idcategoria", nullable = true, updatable = false, insertable = true)
    private ProductionCategory category;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "formulation")
    private List<com.encens.khipus.model.xproduction.FormulationInput> formulationInputList = new ArrayList<com.encens.khipus.model.xproduction.FormulationInput>(0);

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getCapacity() {
        return capacity;
    }

    public void setCapacity(BigDecimal capacity) {
        this.capacity = capacity;
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

    public List<com.encens.khipus.model.xproduction.FormulationInput> getFormulationInputList() {
        return formulationInputList;
    }

    public void setFormulationInputList(List<FormulationInput> formulationInputList) {
        this.formulationInputList = formulationInputList;
    }

    public ProductionCategory getCategory() {
        return category;
    }

    public void setCategory(ProductionCategory category) {
        this.category = category;
    }

    public FormulationState getState() {
        return state;
    }

    public void setState(FormulationState state) {
        this.state = state;
    }

    public BigDecimal getTotalEquivalent() {
        return totalEquivalent;
    }

    public void setTotalEquivalent(BigDecimal totalEquivalent) {
        this.totalEquivalent = totalEquivalent;
    }
}
