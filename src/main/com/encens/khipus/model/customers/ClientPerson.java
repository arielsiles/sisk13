package com.encens.khipus.model.customers;

import com.encens.khipus.util.Constants;

import javax.persistence.*;

/**
 * Entity for ClientPerson
 *
 * @author Ariel Siles
 */

@NamedQueries(
        {
        }
)

@Entity
@Table(name = "PERSONAS", schema = Constants.CASHBOX_SCHEMA)
public class ClientPerson {

    @Id
    @Column(name = "PI_ID", nullable = false, updatable = false)
    private String id;

    @Column(name = "NRO_DOC", nullable = false)
    private String documentNumber;

    @Column(name = "AP", nullable = false)
    private String lastName;

    @Column(name = "AM", nullable = false)
    private String maidenName;

    @Column(name = "NOM", nullable = false)
    private String name;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDocumentNumber() {
        return documentNumber;
    }

    public void setDocumentNumber(String documentNumber) {
        this.documentNumber = documentNumber;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMaidenName() {
        return maidenName;
    }

    public void setMaidenName(String maidenName) {
        this.maidenName = maidenName;
    }
}