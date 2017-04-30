package com.encens.khipus.model.contacts;

import com.encens.khipus.model.UpperCaseStringListener;
import com.encens.khipus.model.admin.Company;
import org.hibernate.validator.NotNull;

import javax.persistence.*;
import java.util.Date;

/**
 * Represents the Organization entity model
 *
 * @author
 * @version 1.0
 */
@Table(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "institucion")
@PrimaryKeyJoinColumns(value = {
        @PrimaryKeyJoinColumn(name = "idinstitucion", referencedColumnName = "identidad")/*,
    @PrimaryKeyJoinColumn(name = "NO_IDENTIFICACION", referencedColumnName = "NO_IDENTIFICACION")*/
})
@javax.persistence.Entity
@EntityListeners(UpperCaseStringListener.class)
public class Organization extends Entity {
    @Column(name = "razonsocial", nullable = false, length = 200)
    @NotNull
    private String name;

    @Column(name = "aniversario")
    @Temporal(TemporalType.DATE)
    private Date anniversary;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idcompania", nullable = false, updatable = false, insertable = true)
    private Company company;


    public Company getCompany() {
        return company;
    }


    public void setCompany(Company company) {
        this.company = company;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getAnniversary() {
        return anniversary;
    }

    public void setAnniversary(Date anniversary) {
        this.anniversary = anniversary;
    }
}
