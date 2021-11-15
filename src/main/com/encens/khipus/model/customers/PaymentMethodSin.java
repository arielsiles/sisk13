package com.encens.khipus.model.customers;

import com.encens.khipus.util.Constants;

import javax.persistence.*;

@TableGenerator(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "PaymentMethodSin.tableGenerator",
        table = com.encens.khipus.util.Constants.SEQUENCE_TABLE_NAME,
        pkColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_PK_COLUMN_NAME,
        valueColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_VALUE_COLUMN_NAME,
        pkColumnValue = "sin_metodopago",
        allocationSize = com.encens.khipus.util.Constants.SEQUENCE_ALLOCATION_SIZE)

@Entity
@Table(schema = Constants.KHIPUS_SCHEMA, name = "sin_metodopago")
public class PaymentMethodSin {

    @Id
    @Column(name = "idmetodopago", nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "PaymentMethodSin.tableGenerator")
    private Long id;

    @Column(name = "codigo")
    private Integer code;

    @Column(name = "descripcion")
    private String description;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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
}
