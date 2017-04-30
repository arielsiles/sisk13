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
        name = "Model.tableGenerator",
        table = com.encens.khipus.util.Constants.SEQUENCE_TABLE_NAME,
        pkColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_PK_COLUMN_NAME,
        valueColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_VALUE_COLUMN_NAME,
        pkColumnValue = "modelo",
        allocationSize = com.encens.khipus.util.Constants.SEQUENCE_ALLOCATION_SIZE)
@NamedQueries({@NamedQuery(name = "Model.findByName",
        query = "select model from Model model where lower(model.name) = lower(:name)")})


@Entity
@EntityListeners({CompanyListener.class, UpperCaseStringListener.class})
@Table(name = "MODELO", schema = Constants.KHIPUS_SCHEMA)
public class Model implements BaseModel {

    @Id
    @Column(name = "IDMODELO", nullable = false, updatable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "Model.tableGenerator")
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
