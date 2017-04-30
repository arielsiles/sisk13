package com.encens.khipus.model.employees;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyListener;
import com.encens.khipus.model.UpperCaseStringListener;
import com.encens.khipus.model.admin.Company;
import org.hibernate.annotations.Filter;

import javax.persistence.*;

/**
 * Entity for Tolerance
 *
 * @author
 */

@TableGenerator(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "Tolerance.tableGenerator",
        table = com.encens.khipus.util.Constants.SEQUENCE_TABLE_NAME,
        pkColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_PK_COLUMN_NAME,
        valueColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_VALUE_COLUMN_NAME,
        pkColumnValue = "tolerancia",
        allocationSize = com.encens.khipus.util.Constants.SEQUENCE_ALLOCATION_SIZE)

@NamedQueries(
        {
                @NamedQuery(name = "Tolerance.findAll", query = "select o from Tolerance o "),
                @NamedQuery(name = "Tolerance.findTolerance", query = "select o from Tolerance o where o.id=:id")
        }
)
@Entity
@Filter(name = com.encens.khipus.util.Constants.COMPANY_FILTER_NAME)
@EntityListeners({CompanyListener.class, UpperCaseStringListener.class})
@Table(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "tolerancia")
public class Tolerance implements BaseModel {

    @Id
    @Column(name = "idtolerancia", nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "Tolerance.tableGenerator")
    private Long id;

    @Column(name = "antesinicio", nullable = false)
    private Integer beforeInit;

    @Column(name = "despuesinicio", nullable = false)
    private Integer afterInit;

    @Column(name = "antesfin", nullable = false)
    private Integer beforeEnd;

    @Column(name = "despuesfin", nullable = false)
    private Integer afterEnd;

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

    public Integer getBeforeInit() {
        return beforeInit;
    }

    public void setBeforeInit(Integer beforeInit) {
        this.beforeInit = beforeInit;
    }

    public Integer getAfterInit() {
        return afterInit;
    }

    public void setAfterInit(Integer afterInit) {
        this.afterInit = afterInit;
    }

    public Integer getBeforeEnd() {
        return beforeEnd;
    }

    public void setBeforeEnd(Integer beforeEnd) {
        this.beforeEnd = beforeEnd;
    }

    public Integer getAfterEnd() {
        return afterEnd;
    }

    public void setAfterEnd(Integer afterEnd) {
        this.afterEnd = afterEnd;
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
}