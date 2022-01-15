package com.encens.khipus.model.customers;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.util.Constants;

import javax.persistence.*;


@TableGenerator(schema = Constants.KHIPUS_SCHEMA, name = "CancellationReason.tableGenerator",
        table = Constants.SEQUENCE_TABLE_NAME,
        pkColumnName = Constants.SEQUENCE_TABLE_PK_COLUMN_NAME,
        valueColumnName = Constants.SEQUENCE_TABLE_VALUE_COLUMN_NAME,
        pkColumnValue = "sin_motivoanulacion",
        allocationSize = Constants.SEQUENCE_ALLOCATION_SIZE)
@NamedQueries(
        {
                @NamedQuery(name = "CancellationReason.findAll", query = "select o from CancellationReason o ")
        }
)

@Entity
@Table(schema = Constants.KHIPUS_SCHEMA, name = "sin_motivoanulacion")
public class CancellationReason implements BaseModel {

    @Id
    @Column(name = "idmotivoanulacion", nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "CancellationReason.tableGenerator")
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
