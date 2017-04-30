package com.encens.khipus.model.employees;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyListener;
import com.encens.khipus.model.UpperCaseStringListener;
import com.encens.khipus.model.admin.Company;
import com.encens.khipus.util.Constants;
import org.hibernate.annotations.Filter;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;

import javax.persistence.*;

/**
 * Entity for Charge catalog
 *
 * @author
 * @version 2.5
 */
@NamedQueries({
        @NamedQuery(name = "Charge.countByName",
                query = "select count(c.id) from Charge c where lower(c.name)=lower(:name)")
})
@TableGenerator(schema = Constants.KHIPUS_SCHEMA,
        name = "Charge.tableGenerator",
        table = com.encens.khipus.util.Constants.SEQUENCE_TABLE_NAME,
        pkColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_PK_COLUMN_NAME,
        valueColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_VALUE_COLUMN_NAME,
        pkColumnValue = "cargo",
        allocationSize = com.encens.khipus.util.Constants.SEQUENCE_ALLOCATION_SIZE)

@Entity
@Filter(name = com.encens.khipus.util.Constants.COMPANY_FILTER_NAME)
@EntityListeners({CompanyListener.class, UpperCaseStringListener.class})
@Table(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "cargo",
        uniqueConstraints = @UniqueConstraint(columnNames = {"nombre", "idcompania"}))
public class Charge implements BaseModel {

    @Id
    @Column(name = "idcargo", nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "Charge.tableGenerator")
    private Long id;

    @Column(name = "codigo", nullable = false)
    private Long code;

    @Column(name = "nombre", nullable = false, length = 200)
    @Length(max = 200)
    private String name;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idcompania", nullable = false, updatable = false, insertable = true)
    @NotNull
    private Company company;

    @Version
    @Column(name = "version", nullable = false)
    private long version;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public Long getCode() {
        return code;
    }

    public void setCode(Long code) {
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

    @Override
    public String toString() {
        return "Charge{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", company=" + company +
                ", version=" + version +
                '}';
    }
}