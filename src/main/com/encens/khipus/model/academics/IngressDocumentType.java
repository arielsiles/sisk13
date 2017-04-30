package com.encens.khipus.model.academics;

import com.encens.khipus.util.Constants;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * @author
 * @version 2.26
 */
@Entity
@Table(name = "TIPOS_DOCUMENTO_INGRESO", schema = Constants.ACADEMIC_SCHEMA)
public class IngressDocumentType {
    @Id
    @Column(name = "TIPO_DOCUMENTO_INGRESO", nullable = false, updatable = false)
    private String ingressDocumentTypeId;

    @Column(name = "DESCRIPCION", nullable = false, updatable = false, insertable = false)
    private String description;

    @Column(name = "SIGLA", nullable = true, updatable = false, insertable = false)
    private String acronym;

    @Column(name = "OBLIGADO", nullable = false, updatable = false, insertable = false)
    private String mandatory;


    public String getIngressDocumentTypeId() {
        return ingressDocumentTypeId;
    }

    public void setIngressDocumentTypeId(String ingressDocumentTypeId) {
        this.ingressDocumentTypeId = ingressDocumentTypeId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAcronym() {
        return acronym;
    }

    public void setAcronym(String acronym) {
        this.acronym = acronym;
    }

    public String getMandatory() {
        return mandatory;
    }

    public void setMandatory(String mandatory) {
        this.mandatory = mandatory;
    }
}
