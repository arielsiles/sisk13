package com.encens.khipus.model.customers;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.util.Constants;

import javax.persistence.*;


@TableGenerator(schema = Constants.KHIPUS_SCHEMA, name = "ProductsServices.tableGenerator",
        table = Constants.SEQUENCE_TABLE_NAME,
        pkColumnName = Constants.SEQUENCE_TABLE_PK_COLUMN_NAME,
        valueColumnName = Constants.SEQUENCE_TABLE_VALUE_COLUMN_NAME,
        pkColumnValue = "sin_produtoservicio",
        allocationSize = Constants.SEQUENCE_ALLOCATION_SIZE)
@NamedQueries(
        {
                @NamedQuery(name = "ProductsServices.findAll", query = "select o from CancellationReason o ")
        }
)

@Entity
@Table(schema = Constants.KHIPUS_SCHEMA, name = "sin_produtoservicio")
public class ProductsServices implements BaseModel {

    @Id
    @Column(name = "idprodutoservicio", nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "ProductsServices.tableGenerator")
    private Long id;

    @Column(name = "codactividad")
    private String activityCode;

    @Column(name = "codproducto")
    private Integer productCode;

    @Column(name = "descproducto")
    private String description;

    @Override
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getActivityCode() {
        return activityCode;
    }

    public void setActivityCode(String activityCode) {
        this.activityCode = activityCode;
    }

    public Integer getProductCode() {
        return productCode;
    }

    public void setProductCode(Integer productCode) {
        this.productCode = productCode;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getFullName() {
        return getProductCode().toString() + " - " + getDescription();
    }

}
