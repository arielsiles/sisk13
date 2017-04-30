package com.encens.khipus.model.cashbox;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.util.Constants;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * @author
 * @version 2.13
 */
@Entity
@Table(name = "CUENTAS", schema = Constants.CASHBOX_SCHEMA)
public class CashboxAccount implements BaseModel {

    @Id
    @Column(name = "ID", nullable = false)
    private Integer id;

    @Column(name = "NOMBRE", nullable = false)
    private String name;

    public Object getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
