package com.encens.khipus.model.customers;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyListener;
import com.encens.khipus.model.UpperCaseStringListener;
import com.encens.khipus.model.admin.Company;
import com.encens.khipus.util.Constants;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.Type;
import org.hibernate.validator.NotNull;

import javax.persistence.*;

/**
 * Created by IntelliJ IDEA.
 * Time: 16:42:25
 * To change this template use File | Settings | File Templates.
 */

@TableGenerator(schema = Constants.KHIPUS_SCHEMA, name = "ClientType.tableGenerator",
        table = Constants.SEQUENCE_TABLE_NAME,
        pkColumnName = Constants.SEQUENCE_TABLE_PK_COLUMN_NAME,
        valueColumnName = Constants.SEQUENCE_TABLE_VALUE_COLUMN_NAME,
        pkColumnValue = "tipocliente",
        allocationSize = Constants.SEQUENCE_ALLOCATION_SIZE)
@NamedQueries(
        {
                @NamedQuery(name = "ClientType.findAll", query = "select o from ClientType o ")
        }
)

@Entity
@Table(schema = Constants.KHIPUS_SCHEMA, name = "tipocliente")
public class ClientType implements BaseModel {

    @Id
    @Column(name = "idtipocliente", nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "ClientType.tableGenerator")
    private Long id;

    @Column(name = "nombre", nullable = false, length = 150)
    private String name;

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

}
