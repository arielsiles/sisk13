package com.encens.khipus.model.budget;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyListener;
import com.encens.khipus.model.UpperCaseStringListener;
import com.encens.khipus.model.admin.BusinessUnit;
import com.encens.khipus.model.admin.Company;
import com.encens.khipus.model.employees.Gestion;
import org.hibernate.annotations.Filter;
import org.hibernate.validator.NotNull;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * BudgetDistribution
 *
 * @author
 * @version 2.5
 */
@NamedQueries({
        @NamedQuery(name = "BudgetDistribution.sumDuplicated", query = "select count(bd) from BudgetDistribution bd " +
                "where bd.businessUnit=:businessUnit and bd.gestion=:gestion and bd.type=:type and bd.budgetDistributionType=:budgetDistributionType"),
        @NamedQuery(name = "BudgetDistribution.sumDuplicatedWithoutReferences", query = "select count(bd) from BudgetDistribution bd " +
                "where bd<>:budgetDistribution and bd.businessUnit=:businessUnit and bd.gestion=:gestion and bd.type=:type and bd.budgetDistributionType=:budgetDistributionType"),
        @NamedQuery(name = "BudgetDistribution.findByGestionAndBudgetDistributionTypeAndType",
                query = "select bd from BudgetDistribution bd where bd.gestion=:gestion and bd.type=:budgetDistributionTypeParam " +
                        "and bd.budgetDistributionType=:budgetDistributionType ")
})


@TableGenerator(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "BudgetDistribution.tableGenerator",
        table = com.encens.khipus.util.Constants.SEQUENCE_TABLE_NAME,
        pkColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_PK_COLUMN_NAME,
        valueColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_VALUE_COLUMN_NAME,
        pkColumnValue = "distpresupuesto",
        allocationSize = com.encens.khipus.util.Constants.SEQUENCE_ALLOCATION_SIZE)
@Entity
@Filter(name = com.encens.khipus.util.Constants.COMPANY_FILTER_NAME)
@EntityListeners({CompanyListener.class, UpperCaseStringListener.class})
@Table(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "distpresupuesto")
public class BudgetDistribution implements BaseModel {

    @Id
    @Column(name = "iddistpresupuesto", nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "BudgetDistribution.tableGenerator")
    private Long id;

    @ManyToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "idunidadnegocio", nullable = false)
    @NotNull
    private BusinessUnit businessUnit;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idgestion", nullable = false)
    @NotNull
    private Gestion gestion;

    @Column(name = "porcentajedist", nullable = false, precision = 5, scale = 2)
    @NotNull
    private BigDecimal percentDistribution;

    @Column(name = "tipo", length = 10, nullable = false)
    @Enumerated(EnumType.STRING)
    @NotNull
    private BudgetType type;

    @Column(name = "tipodistribucion", length = 10, nullable = false)
    @Enumerated(EnumType.STRING)
    @NotNull
    private BudgetDistributionType budgetDistributionType;

    @OneToMany(mappedBy = "budgetDistribution", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @Filter(name = com.encens.khipus.util.Constants.COMPANY_FILTER_NAME)
    private List<BudgetDistributionDetail> budgetDistributionDetailList = new ArrayList<BudgetDistributionDetail>(0);

    @Version
    @Column(name = "version", nullable = false)
    private long version;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idcompania", nullable = false, updatable = false)
    @NotNull
    private Company company;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BusinessUnit getBusinessUnit() {
        return businessUnit;
    }

    public void setBusinessUnit(BusinessUnit businessUnit) {
        this.businessUnit = businessUnit;
    }

    public Gestion getGestion() {
        return gestion;
    }

    public void setGestion(Gestion gestion) {
        this.gestion = gestion;
    }

    public BigDecimal getPercentDistribution() {
        return percentDistribution;
    }

    public void setPercentDistribution(BigDecimal percentDistribution) {
        this.percentDistribution = percentDistribution;
    }

    public BudgetType getType() {
        return type;
    }

    public void setType(BudgetType type) {
        this.type = type;
    }

    public List<BudgetDistributionDetail> getBudgetDistributionDetailList() {
        return budgetDistributionDetailList;
    }

    public void setBudgetDistributionDetailList(List<BudgetDistributionDetail> budgetDistributionDetailList) {
        this.budgetDistributionDetailList = budgetDistributionDetailList;
    }

    public BudgetDistributionType getBudgetDistributionType() {
        return budgetDistributionType;
    }

    public void setBudgetDistributionType(BudgetDistributionType budgetDistributionType) {
        this.budgetDistributionType = budgetDistributionType;
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

    @Override
    public String toString() {
        return "BudgetDistribution{" +
                "\n id=" + id +
                "\n type=" + type +
                "\n percentDistribution=" + percentDistribution +
                "\n  businessUnit=" + businessUnit +
                "\n  gestion=" + gestion +
                "\n  budgetDistributionDetailList=" + budgetDistributionDetailList +
                "\n  version=" + version +
                "\n  company=" + company +
                '}';
    }
}
