package com.encens.khipus.model.budget;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyListener;
import com.encens.khipus.model.UpperCaseStringListener;
import com.encens.khipus.model.admin.Company;
import com.encens.khipus.model.employees.Month;
import org.hibernate.annotations.Filter;
import org.hibernate.validator.NotNull;

import javax.persistence.*;
import java.math.BigDecimal;

/**
 * BudgetDistributionDetail
 *
 * @author
 * @version 2.5
 */
@TableGenerator(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "BudgetDistributionDetail.tableGenerator",
        table = com.encens.khipus.util.Constants.SEQUENCE_TABLE_NAME,
        pkColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_PK_COLUMN_NAME,
        valueColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_VALUE_COLUMN_NAME,
        pkColumnValue = "distpresdet",
        allocationSize = com.encens.khipus.util.Constants.SEQUENCE_ALLOCATION_SIZE)
@Entity
@Filter(name = com.encens.khipus.util.Constants.COMPANY_FILTER_NAME)
@EntityListeners({CompanyListener.class, UpperCaseStringListener.class})
@Table(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "distpresdet", uniqueConstraints = @UniqueConstraint(columnNames = {"iddistpresupuesto", "mes"}))
public class BudgetDistributionDetail implements BaseModel {

    @Id
    @Column(name = "iddistpresdet", nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "BudgetDistributionDetail.tableGenerator")
    private Long id;

    @ManyToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "iddistpresupuesto", nullable = false)
    @NotNull
    private BudgetDistribution budgetDistribution;

    @Column(name = "mes", nullable = false, length = 10)
    @Enumerated(EnumType.STRING)
    @NotNull
    private Month month;

    @Column(name = "porcentajedist", nullable = false, precision = 5, scale = 2)
    @NotNull
    private BigDecimal percentDistribution;

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

    public BudgetDistribution getBudgetDistribution() {
        return budgetDistribution;
    }

    public void setBudgetDistribution(BudgetDistribution budgetDistribution) {
        this.budgetDistribution = budgetDistribution;
    }

    public Month getMonth() {
        return month;
    }

    public void setMonth(Month month) {
        this.month = month;
    }

    public BigDecimal getPercentDistribution() {
        return percentDistribution;
    }

    public void setPercentDistribution(BigDecimal percentDistribution) {
        this.percentDistribution = percentDistribution;
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
        return "\n\tBudgetDistributionDetail{" +
                "id=" + id +
                ", month=" + month +
                ", percentDistribution=" + percentDistribution +
                ", version=" + version +
                ", company=" + company +
                '}';
    }
}
