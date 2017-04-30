package com.encens.khipus.model.accounting;

import com.encens.khipus.model.BaseModel;

import javax.persistence.*;

/**
 * Entity for Accounting Document Types
 *
 * @author: Ariel Siles
 * version:
 */

@TableGenerator(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "DocType.tableGenerator",
        table = com.encens.khipus.util.Constants.SEQUENCE_TABLE_NAME,
        pkColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_PK_COLUMN_NAME,
        valueColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_VALUE_COLUMN_NAME,
        pkColumnValue = "tipodoc",
        allocationSize = com.encens.khipus.util.Constants.SEQUENCE_ALLOCATION_SIZE)

@NamedQueries(
        {
                @NamedQuery(name = "DocType.findDocumentTypeDefault", query = "select o from DocType o where o.id=:id"),
                @NamedQuery(name = "DocType.findDocumentByName", query = "select o from DocType o where o.name=:name")
        }
)

@Entity
@Table(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "tipodoc")
public class DocType implements BaseModel {
    @Id
    @Column(name = "idtipodoc", nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "DocType.tableGenerator")
    private Long id;

    @Column(name = "nombre", nullable = false, length = 150)
    private String name = "";

    @Column(name = "descripcion", nullable = false, length = 150)
    private String description;

    public String getFullName() {
        return name + " - " + description;
    }

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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
