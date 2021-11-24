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
@Table(schema = Constants.KHIPUS_SCHEMA, name = "sin_actividadeconomica")
public class EconomicActivity {

    @Id
    @Column(name = "codigo")
    private String code;

    @Column(name = "nombre")
    private String name;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFullName(){
        return getCode() + " - " + getName();
    }
}
