package com.encens.khipus.model.fixedassets;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyListener;
import com.encens.khipus.model.UpperCaseStringListener;
import com.encens.khipus.model.admin.Company;
import com.encens.khipus.util.Constants;

import javax.persistence.*;

/**
 * @author
 * @version 2.25
 */
@TableGenerator(schema = Constants.KHIPUS_SCHEMA,
        name = "Trademark.tableGenerator",
        table = com.encens.khipus.util.Constants.SEQUENCE_TABLE_NAME,
        pkColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_PK_COLUMN_NAME,
        valueColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_VALUE_COLUMN_NAME,
        pkColumnValue = "marca",
        allocationSize = com.encens.khipus.util.Constants.SEQUENCE_ALLOCATION_SIZE)
@NamedQueries({@NamedQuery(name = "Trademark.findByName",
        query = "select trademark from Trademark trademark where lower(trademark.name) = lower(:name)")})


@Entity
@EntityListeners({CompanyListener.class, UpperCaseStringListener.class})
@Table(name = "MARCA", schema = Constants.KHIPUS_SCHEMA)
public class Trademark implements BaseModel {
    @Id
    @Column(name = "IDMARCA", nullable = false, updatable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "Trademark.tableGenerator")
    private Long id;

    @Column(name = "NOMBRE", nullable = false, length = 200)
    private String name;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "IDCOMPANIA", nullable = false, updatable = false, insertable = true)
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
