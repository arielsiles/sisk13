package com.encens.khipus.model.customers;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.util.Constants;

import javax.persistence.*;

/**
 * Created with IntelliJ IDEA.
 *
 */
@TableGenerator(schema = Constants.KHIPUS_SCHEMA, name = "BranchOffice.tableGenerator",
        table = Constants.SEQUENCE_TABLE_NAME,
        pkColumnName = Constants.SEQUENCE_TABLE_PK_COLUMN_NAME,
        valueColumnName = Constants.SEQUENCE_TABLE_VALUE_COLUMN_NAME,
        pkColumnValue = "sucursal",
        allocationSize = Constants.SEQUENCE_ALLOCATION_SIZE)

@Entity
/*@Filter(name = Constants.COMPANY_FILTER_NAME)*/
/*@EntityListeners(CompanyListener.class)*/
@Table(schema = Constants.KHIPUS_SCHEMA, name = "sucursal")
public class BranchOffice implements BaseModel {

    @Id
    @Column(name = "IDSUCURSAL", nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "BranchOffice.tableGenerator")
    private Long id;

    @Column(name="NOMBRE")
    private String name;

    @Column(name="descripcion")
    private String description;

    @Column(name="codsin")
    private Integer officeCode;

    @Column(name="codpos")
    private Integer posCode;

    @Override
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String key) {
        this.name = key;
    }


    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getOfficeCode() {
        return officeCode;
    }

    public void setOfficeCode(Integer officeCode) {
        this.officeCode = officeCode;
    }

    public Integer getPosCode() {
        return posCode;
    }

    public void setPosCode(Integer posCode) {
        this.posCode = posCode;
    }
}
