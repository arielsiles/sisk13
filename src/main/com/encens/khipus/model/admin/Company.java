package com.encens.khipus.model.admin;


import org.hibernate.validator.NotNull;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Class that represents the company.
 * <p/>
 * This project is multi-company system so company is the basic of this idea.
 *
 * @author
 * @version 1.0
 */

@TableGenerator(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "Company.tableGenerator",
        table = com.encens.khipus.util.Constants.SEQUENCE_TABLE_NAME,
        pkColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_PK_COLUMN_NAME,
        valueColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_VALUE_COLUMN_NAME,
        pkColumnValue = "compania",
        allocationSize = com.encens.khipus.util.Constants.SEQUENCE_ALLOCATION_SIZE)
@Entity
@Table(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "compania")
public class Company implements Serializable {

    @Id
    @Column(name = "idcompania", nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "Company.tableGenerator")
    protected Long id;

    @Column(name = "codigo", unique = true, nullable = false)
    @NotNull
    private String login;

    public Company() {
    }

    public Company(Long id, String login) {
        this.login = login;
        this.id = id;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "Company{" +
                "id=" + id +
                ", login='" + login + '\'' +
                '}';
    }
}
