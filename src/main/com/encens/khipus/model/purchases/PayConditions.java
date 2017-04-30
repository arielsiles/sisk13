package com.encens.khipus.model.purchases;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyListener;
import com.encens.khipus.model.UpperCaseStringListener;
import com.encens.khipus.model.admin.Company;
import com.encens.khipus.util.Constants;
import org.hibernate.annotations.Filter;
import org.hibernate.validator.NotNull;

import javax.persistence.*;

/**
 * PayConditions
 *
 * @author
 * @version 2.17
 */
@TableGenerator(schema = Constants.KHIPUS_SCHEMA,
        name = "PayConditions.tableGenerator",
        table = com.encens.khipus.util.Constants.SEQUENCE_TABLE_NAME,
        pkColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_PK_COLUMN_NAME,
        valueColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_VALUE_COLUMN_NAME,
        pkColumnValue = "condicionpago",
        allocationSize = com.encens.khipus.util.Constants.SEQUENCE_ALLOCATION_SIZE)
@Entity
@Filter(name = com.encens.khipus.util.Constants.COMPANY_FILTER_NAME)
@EntityListeners({CompanyListener.class, UpperCaseStringListener.class})
@Table(schema = Constants.FINANCES_SCHEMA, name = "condicionpago",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"idcompania", "codigo"}),
                @UniqueConstraint(columnNames = {"idcompania", "nombre"})
        })
public class PayConditions implements BaseModel {
    @Id
    @Column(name = "idcondicionpago", nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "PayConditions.tableGenerator")
    private Long id;

    @Column(name = "codigo", length = 20, nullable = false)
    @NotNull
    private String code;

    @Column(name = "nombre", length = 250, nullable = false)
    @NotNull
    private String name;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idcompania", nullable = false, updatable = false, insertable = true)
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

    public String getFullName() {
        return getCode() + " - " + getName();
    }
}
