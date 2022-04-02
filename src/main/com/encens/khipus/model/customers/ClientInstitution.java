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
@Table(name = "instituciones", schema = Constants.CASHBOX_SCHEMA)
public class ClientInstitution {

    @Id
    @Column(name = "pi_id", nullable = false, updatable = false)
    private String id;

    @Column(name = "nro_doc", nullable = false)
    private String documentNumber;

    @Column(name = "razon_soc", nullable = false)
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}