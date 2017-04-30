package com.encens.khipus.model.employees;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyListener;
import com.encens.khipus.model.UpperCaseStringListener;
import com.encens.khipus.model.admin.Company;
import org.hibernate.annotations.Filter;

import javax.persistence.*;

/**
 * Encens Team
 *
 * @author
 * @version : AcademicPeriod, 12-11-2009 09:31:12 AM
 */
@NamedQueries({
        @NamedQuery(name = "AcademicPeriod.findByReferenceId",
                query = "select ap from AcademicPeriod ap where ap.referenceId=:referenceId")
})

@TableGenerator(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "AcademicPeriod.tableGenerator",
        table = com.encens.khipus.util.Constants.SEQUENCE_TABLE_NAME,
        pkColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_PK_COLUMN_NAME,
        valueColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_VALUE_COLUMN_NAME,
        pkColumnValue = "periodoacademico",
        allocationSize = com.encens.khipus.util.Constants.SEQUENCE_ALLOCATION_SIZE)

@Entity
@Filter(name = com.encens.khipus.util.Constants.COMPANY_FILTER_NAME)
@EntityListeners({CompanyListener.class, UpperCaseStringListener.class})
@Table(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "periodoacademico", uniqueConstraints = {@UniqueConstraint(columnNames = {"idcompania", "nombre"})})
public class AcademicPeriod implements BaseModel {
    @Id
    @Column(name = "idperiodocad", nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "AcademicPeriod.tableGenerator")
    private Long id;

    @Column(name = "indice", nullable = false)
    private Integer sequence;

    @Column(name = "nombre", length = 200, nullable = false)
    private String name;

    @Version
    @Column(name = "version", nullable = false)
    private long version;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idcompania", nullable = false, updatable = false, insertable = true)
    private Company company;

    @Column(name = "idreferencia", length = 100)
    private String referenceId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getSequence() {
        return sequence;
    }

    public void setSequence(Integer sequence) {
        this.sequence = sequence;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public String getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(String referenceId) {
        this.referenceId = referenceId;
    }
}
