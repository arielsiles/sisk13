package com.encens.khipus.model.employees;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyListener;
import com.encens.khipus.model.UpperCaseStringListener;
import com.encens.khipus.model.admin.Company;
import org.hibernate.annotations.Filter;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;

import javax.persistence.*;
import java.util.Date;

/**
 * Entity for GestionPayroll Schedule
 *
 * @author
 * @version 2.26
 */

@TableGenerator(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "GestionPayrollSchedule.tableGenerator",
        table = com.encens.khipus.util.Constants.SEQUENCE_TABLE_NAME,
        pkColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_PK_COLUMN_NAME,
        valueColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_VALUE_COLUMN_NAME,
        pkColumnValue = "cronogramagp",
        allocationSize = com.encens.khipus.util.Constants.SEQUENCE_ALLOCATION_SIZE)

@NamedQueries(
        {
                @NamedQuery(name = "GestionPayrollSchedule.findAll", query = "select o from GestionPayrollSchedule o order by o.gestion.year asc")
        }
)

@Entity
@Filter(name = com.encens.khipus.util.Constants.COMPANY_FILTER_NAME)
@EntityListeners({CompanyListener.class, UpperCaseStringListener.class})
@Table(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "cronogramagp",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"idcompania", "idgestion"}),
                @UniqueConstraint(columnNames = {"idcompania", "nombre"})
        })

public class GestionPayrollSchedule implements BaseModel {

    @Id
    @javax.persistence.Column(name = "idcronogramagp", nullable = false)
    @NotNull
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "GestionPayrollSchedule.tableGenerator")
    private Long id;

    @Column(name = "nombre", nullable = false, length = 50)
    @Length(max = 50)
    @NotNull
    private String name;

    @Column(name = "fechacreacion", nullable = false, updatable = false)
    @Temporal(TemporalType.DATE)
    @NotNull
    private Date creationDate = new Date();

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idgestion", nullable = false, updatable = false, insertable = true)
    private Gestion gestion;

    @Version
    @Column(name = "version", nullable = false)
    @NotNull
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public Gestion getGestion() {
        return gestion;
    }

    public void setGestion(Gestion gestion) {
        this.gestion = gestion;
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