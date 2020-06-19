package com.encens.khipus.model.customers;

import com.encens.khipus.model.contacts.Person;

import javax.persistence.*;

//@NamedQueries ({})

@Entity
@Table(name = "distribuidor")
@PrimaryKeyJoinColumns(value = {@PrimaryKeyJoinColumn(name = "iddistribuidor", referencedColumnName = "idpersona")})
public class Distributor extends Person {

    @Column(name = "descripcion", nullable = false)
    private String description;


    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}


