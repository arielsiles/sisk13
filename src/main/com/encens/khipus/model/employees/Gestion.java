package com.encens.khipus.model.employees;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyListener;
import com.encens.khipus.model.UpperCaseStringListener;
import com.encens.khipus.model.admin.Company;
import org.hibernate.annotations.Filter;

import javax.persistence.*;

/**
 * Entity for Gestion
 *
 * @author
 * @version 1.2.4
 */

@TableGenerator(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "Gestion.tableGenerator",
        table = com.encens.khipus.util.Constants.SEQUENCE_TABLE_NAME,
        pkColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_PK_COLUMN_NAME,
        valueColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_VALUE_COLUMN_NAME,
        pkColumnValue = "gestion",
        allocationSize = com.encens.khipus.util.Constants.SEQUENCE_ALLOCATION_SIZE)

@NamedQueries(
        {
                @NamedQuery(name = "Gestion.findAll", query = "select o from Gestion o order by o.year asc"),
                @NamedQuery(name = "Gestion.findLast", query = "select o from Gestion o order by o.year desc"),
                @NamedQuery(name = "Gestion.findById", query = "select o from Gestion o where o.id=:id"),
                @NamedQuery(name = "Gestion.findByYear", query = "select o from Gestion o " +
                        "where o.year=:year order by o.year asc"),
                @NamedQuery(name = "Gestion.findByYearCompanyId", query = "select o from Gestion o " +
                        "where o.year=:year" +
                        " and o.company.id =:companyId order by o.year asc")
        }
)

@Entity
@Filter(name = com.encens.khipus.util.Constants.COMPANY_FILTER_NAME)
@EntityListeners({CompanyListener.class, UpperCaseStringListener.class})
@Table(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "gestion", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"idcompania", "anio"})
})

public class Gestion implements BaseModel {
    @Id
    @javax.persistence.Column(name = "idgestion", nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "Gestion.tableGenerator")
    private Long id;

    @Column(name = "anio", nullable = false)
    private Integer year;

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

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
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
}