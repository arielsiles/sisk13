package com.encens.khipus.model.contacts;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyListener;
import com.encens.khipus.model.UpperCaseStringListener;
import com.encens.khipus.model.admin.Company;
import org.hibernate.annotations.Filter;

import javax.persistence.*;
import javax.persistence.Entity;

/**
 * Created by IntelliJ IDEA.
 * User: macmac
 * Date: 08-ene-2009
 * Time: 15:15:12
 * To change this template use File | Settings | File Templates.
 */


@TableGenerator(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "Address.tableGenerator",
        table = com.encens.khipus.util.Constants.SEQUENCE_TABLE_NAME,
        pkColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_PK_COLUMN_NAME,
        valueColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_VALUE_COLUMN_NAME,
        pkColumnValue = "direccion",
        allocationSize = com.encens.khipus.util.Constants.SEQUENCE_ALLOCATION_SIZE)

@Entity
@Filter(name = com.encens.khipus.util.Constants.COMPANY_FILTER_NAME)
@EntityListeners({CompanyListener.class, UpperCaseStringListener.class})
@Table(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "direccion")
//, uniqueConstraints = {@UniqueConstraint(columnNames = {"ID_CALLE"})})
public class Address implements BaseModel {

    @Id
    @Column(name = "iddireccion", nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "Address.tableGenerator")
    private Long id;

    /*@Column(name = "NOMBRE", nullable = false, length = 200)
    private String name;  */

    @Column(name = "numero", nullable = true)
    private Integer number;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idcalle", nullable = false)
    private Street street;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idcompania", nullable = false, updatable = false, insertable = true)
    private Company company;

    @Version
    @Column(name = "version")
    private long version;

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }
    /*
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }  */

    public Street getStreet() {
        return street;
    }

    public void setStreet(Street street) {
        this.street = street;
    }

    public Integer getNumber() {
        return number;
    }

    public void setNumber(Integer number) {
        this.number = number;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}

