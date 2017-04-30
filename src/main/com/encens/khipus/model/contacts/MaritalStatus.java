package com.encens.khipus.model.contacts;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyListener;
import com.encens.khipus.model.UpperCaseStringListener;
import com.encens.khipus.model.admin.Company;
import org.hibernate.annotations.Filter;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotEmpty;
import org.hibernate.validator.NotNull;

import javax.persistence.*;

/**
 * Entity for Marital Status
 *
 * @author:
 */
@NamedQueries({
        @NamedQuery(name = "MaritalStatus.findByCode",
                query = "select maritalStatus from MaritalStatus maritalStatus " +
                        "where maritalStatus.code=:code")
})
@TableGenerator(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "MaritialStatus.tableGenerator",
        table = com.encens.khipus.util.Constants.SEQUENCE_TABLE_NAME,
        pkColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_PK_COLUMN_NAME,
        valueColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_VALUE_COLUMN_NAME,
        pkColumnValue = "estadocivil",
        allocationSize = com.encens.khipus.util.Constants.SEQUENCE_ALLOCATION_SIZE)

@javax.persistence.Entity
@Filter(name = com.encens.khipus.util.Constants.COMPANY_FILTER_NAME)
@EntityListeners({CompanyListener.class, UpperCaseStringListener.class})
@Table(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "estadocivil", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"idcompania", "codigo"}),
        @UniqueConstraint(columnNames = {"idcompania", "nombre"})
})
public class MaritalStatus implements BaseModel {

    @Id
    @Column(name = "idestadocivil", nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "MaritialStatus.tableGenerator")
    private Long id;

    @Column(name = "codigo", nullable = false, length = 10)
    @NotEmpty
    @Length(min = 1, max = 10)
    private String code;

    @Column(name = "nombre", nullable = false, length = 100)
    @NotEmpty
    @Length(min = 1, max = 100)
    private String name;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idcompania", nullable = false, updatable = false, insertable = true)
    @NotNull
    private Company company;

    @Version
    @Column(name = "version")
    private long version;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
