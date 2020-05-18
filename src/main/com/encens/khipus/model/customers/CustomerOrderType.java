package com.encens.khipus.model.customers;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.util.Constants;
import org.hibernate.annotations.Filter;

import javax.persistence.*;

/**
 * Entity for Document Types
 *
 * @author:
 * version: 2.7
 */

@TableGenerator(schema = Constants.KHIPUS_SCHEMA, name = "CustomerOrderType.tableGenerator",
        table = Constants.SEQUENCE_TABLE_NAME,
        pkColumnName = Constants.SEQUENCE_TABLE_PK_COLUMN_NAME,
        valueColumnName = Constants.SEQUENCE_TABLE_VALUE_COLUMN_NAME,
        pkColumnValue = "tipopedido",
        allocationSize = Constants.SEQUENCE_ALLOCATION_SIZE)

@Entity
@Filter(name = Constants.COMPANY_FILTER_NAME)
@Table(schema = Constants.KHIPUS_SCHEMA, name = "tipopedido")
public class CustomerOrderType implements BaseModel {
    @Id
    @Column(name = "idtipopedido", nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "CustomerOrderType.tableGenerator")
    private Long id;

    @Column(name = "nombre")
    private String name;

    @Column(name = "tipo")
    @Enumerated(EnumType.STRING)
    private CustomerOrderTypeEnum type;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public CustomerOrderTypeEnum getType() {
        return type;
    }

    public void setType(CustomerOrderTypeEnum type) {
        this.type = type;
    }
}