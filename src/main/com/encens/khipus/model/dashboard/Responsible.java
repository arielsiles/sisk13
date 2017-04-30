package com.encens.khipus.model.dashboard;

import com.encens.khipus.model.CompanyListener;
import com.encens.khipus.model.admin.BusinessUnit;
import com.encens.khipus.model.admin.Company;
import com.encens.khipus.model.finances.JobContract;
import org.hibernate.validator.NotNull;

import javax.persistence.*;

/**
 * Represents a relationship of responsability for a <code>Widget</code> with a <code>JobContract</code> and with an
 * <code>BusinessUnit</code>
 *
 * @author
 * @version 2.26
 */
@TableGenerator(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "Responsible.tableGenerator",
        table = com.encens.khipus.util.Constants.SEQUENCE_TABLE_NAME,
        pkColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_PK_COLUMN_NAME,
        valueColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_VALUE_COLUMN_NAME,
        pkColumnValue = "responsablecomppnl",
        allocationSize = com.encens.khipus.util.Constants.SEQUENCE_ALLOCATION_SIZE)

@Entity
@org.hibernate.annotations.Filter(name = com.encens.khipus.util.Constants.COMPANY_FILTER_NAME)
@EntityListeners(CompanyListener.class)
@javax.persistence.Table(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "RESPONSABLECOMPPNL")
public class Responsible {

    @Id
    @Column(name = "IDRESPONSABLECOMPPNL", nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "Responsible.tableGenerator")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IDCONTRATOPUESTO")
    private JobContract responsible;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IDUNIDADNEGOCIO")
    private BusinessUnit businessUnit;

    @Version
    @Column(name = "VERSION", nullable = false)
    private Long version;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "IDCOMPANIA", nullable = false, updatable = false, insertable = true)
    @NotNull
    private Company company;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public JobContract getResponsible() {
        return responsible;
    }

    public void setResponsible(JobContract responsible) {
        this.responsible = responsible;
    }

    public BusinessUnit getBusinessUnit() {
        return businessUnit;
    }

    public void setBusinessUnit(BusinessUnit businessUnit) {
        this.businessUnit = businessUnit;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }
}
