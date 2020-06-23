package com.encens.khipus.model.customers;

import com.encens.khipus.model.admin.Company;
import com.encens.khipus.model.contacts.Person;

import javax.persistence.*;

//@NamedQueries ({})

@Entity
@Table(name = "distribuidor")
@PrimaryKeyJoinColumns(value = {@PrimaryKeyJoinColumn(name = "iddistribuidor", referencedColumnName = "idpersona")})
public class Distributor extends Person {

    @Column(name = "descripcion", nullable = true)
    private String description;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idcompania", nullable = false, updatable = false, insertable = true)
    private Company company;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public Company getCompany() {
        return company;
    }

    @Override
    public void setCompany(Company company) {
        this.company = company;
    }
}


