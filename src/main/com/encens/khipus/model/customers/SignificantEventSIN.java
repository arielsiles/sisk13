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
@Table(schema = Constants.KHIPUS_SCHEMA, name = "sin_eventosignificativo")
public class SignificantEventSIN {

    @Id
    @Column(name = "codigo")
    private Integer code;

    @Column(name = "descripcion")
    private String description;

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String toString(){
        return getCode() + "." + getDescription();
    }
}
