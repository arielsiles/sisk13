package com.encens.khipus.model.customers;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.util.Constants;

import javax.persistence.*;

/**
 * Entity
 *
 * @author:
 * version: 2.7
 */

@TableGenerator(schema = Constants.KHIPUS_SCHEMA, name = "MeasureUnitSIN.tableGenerator",
        table = Constants.SEQUENCE_TABLE_NAME,
        pkColumnName = Constants.SEQUENCE_TABLE_PK_COLUMN_NAME,
        valueColumnName = Constants.SEQUENCE_TABLE_VALUE_COLUMN_NAME,
        pkColumnValue = "sin_unidadmedida",
        allocationSize = Constants.SEQUENCE_ALLOCATION_SIZE)

@Entity
@Table(schema = Constants.KHIPUS_SCHEMA, name = "sin_unidadmedida")
public class MeasureUnitSIN implements BaseModel {

    @Id
    @Column(name = "idunidadmedida", nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "MeasureUnitSIN.tableGenerator")
    private Long id;

    @Column(name = "codigo")
    private Integer code;

    @Column(name = "descripcion")
    private String description;

    @Override
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
