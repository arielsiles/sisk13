package com.encens.khipus.model.admin;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.util.Constants;

import javax.persistence.*;
import java.io.Serializable;

@TableGenerator(schema = Constants.KHIPUS_SCHEMA, name = "PointOfSaleType.tableGenerator",
        table = Constants.SEQUENCE_TABLE_NAME,
        pkColumnName = Constants.SEQUENCE_TABLE_PK_COLUMN_NAME,
        valueColumnName = Constants.SEQUENCE_TABLE_VALUE_COLUMN_NAME,
        pkColumnValue = "sin_tipopuntoventa",
        allocationSize = Constants.SEQUENCE_ALLOCATION_SIZE)

@Entity
@Table(schema = Constants.KHIPUS_SCHEMA, name = "sin_tipopuntoventa")
public class PointOfSaleType implements BaseModel, Serializable {

    @Id
    @Column(name = "idtipopuntoventa", nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "PointOfSaleType.tableGenerator")
    private Long id;

    @Column(name = "codigo_clasificador")
    private Integer classifierCode;

    @Column(name = "descripcion", nullable = false)
    private String descripcion;

    public PointOfSaleType() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Integer getClassifierCode() {
        return classifierCode;
    }

    public void setClassifierCode(Integer classifierCode) {
        this.classifierCode = classifierCode;
    }
}