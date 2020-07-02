package com.encens.khipus.model.customers;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyListener;
import com.encens.khipus.util.Constants;
import org.hibernate.annotations.Filter;

import javax.persistence.*;

/**
 * Created with IntelliJ IDEA.
 *
 */
@TableGenerator(schema = Constants.KHIPUS_SCHEMA, name = "CompanyBranch.tableGenerator",
        table = Constants.SEQUENCE_TABLE_NAME,
        pkColumnName = Constants.SEQUENCE_TABLE_PK_COLUMN_NAME,
        valueColumnName = Constants.SEQUENCE_TABLE_VALUE_COLUMN_NAME,
        pkColumnValue = "sucursal",
        allocationSize = Constants.SEQUENCE_ALLOCATION_SIZE)

@Entity
@Filter(name = Constants.COMPANY_FILTER_NAME)
@EntityListeners(CompanyListener.class)
@Table(schema = Constants.KHIPUS_SCHEMA, name = "sucursal")
public class CompanyBranch implements BaseModel {

    @Id
    @Column(name = "IDSUCURSAL", nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "CompanyBranch.tableGenerator")
    private Long id;

    @Column(name="NOMBRE")
    private String key;

    @Column(name="descripcion")
    private String state;

    @Override
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }
}
