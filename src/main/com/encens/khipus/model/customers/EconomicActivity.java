package com.encens.khipus.model.customers;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.util.Constants;

import javax.persistence.*;


@TableGenerator(schema = Constants.KHIPUS_SCHEMA, name = "ProductsServices.tableGenerator",
        table = Constants.SEQUENCE_TABLE_NAME,
        pkColumnName = Constants.SEQUENCE_TABLE_PK_COLUMN_NAME,
        valueColumnName = Constants.SEQUENCE_TABLE_VALUE_COLUMN_NAME,
        pkColumnValue = "sin_actividad",
        allocationSize = Constants.SEQUENCE_ALLOCATION_SIZE)

@Entity
@Table(schema = Constants.KHIPUS_SCHEMA, name = "sin_actividad")
public class EconomicActivity implements BaseModel {

    @Id
    @Column(name = "idactividad", nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "ProductsServices.tableGenerator")
    private Long id;

    @Column(name = "codigocaeb")
    private String activityCode;

    @Column(name = "descripcion")
    private String description;

    @Column(name = "tipoactividad")
    private String activityType;

    @Override
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getActivityType() {
        return activityType;
    }

    public void setActivityType(String activityType) {
        this.activityType = activityType;
    }

    public String getActivityCode() {
        return activityCode;
    }

    public void setActivityCode(String activityCode) {
        this.activityCode = activityCode;
    }

    public String toString(){
        return getActivityCode() + " - " + getDescription();
    }
}
