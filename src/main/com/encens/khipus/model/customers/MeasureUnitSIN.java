package com.encens.khipus.model.customers;

import com.encens.khipus.util.Constants;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Entity
 *
 * @author:
 * version: 2.7
 */

@Entity
@Table(schema = Constants.KHIPUS_SCHEMA, name = "sin_unidadmedida")
public class MeasureUnitSIN {

    @Id
    @Column(name = "nro")
    private Integer number;

    @Column(name = "codigouni")
    private String unitCode;

    @Column(name = "descripcion")
    private String description;

    public Integer getNumber() {
        return number;
    }

    public void setNumber(Integer number) {
        this.number = number;
    }

    public String getUnitCode() {
        return unitCode;
    }

    public void setUnitCode(String unitCode) {
        this.unitCode = unitCode;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String toString(){
        return getNumber() + " - " + getDescription() + " (" + getUnitCode() + ")";
    }

}
