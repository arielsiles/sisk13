package com.encens.khipus.model.admin;

import javax.persistence.*;

/**
 * Entity for Module
 *
 * @author:
 */
@NamedQueries({
        @NamedQuery(name = "SystemModule.findAll", query = "select m from SystemModule m"),
        @NamedQuery(name = "SystemModule.findByCompany", query = "select companyModule.systemModule from CompanyModule companyModule where companyModule.company=:company "),
        @NamedQuery(name = "SystemModule.findByCompanyAndActive", query = "select companyModule.systemModule from CompanyModule companyModule where companyModule.company=:company and companyModule.active=:active")
})
@TableGenerator(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "Module.tableGenerator",
        table = com.encens.khipus.util.Constants.SEQUENCE_TABLE_NAME,
        pkColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_PK_COLUMN_NAME,
        valueColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_VALUE_COLUMN_NAME,
        pkColumnValue = "modulo",
        allocationSize = com.encens.khipus.util.Constants.SEQUENCE_ALLOCATION_SIZE)
@Entity
@Table(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "modulo")
public class SystemModule {

    @Id
    @Column(name = "idmodulo", nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "Module.tableGenerator")
    private Long id;

    @Column(name = "nombrerecurso", length = 150, nullable = false, unique = true)
    private String resourceName;

    @Column(name = "descripcion")
    @Lob
    private String description;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getResourceName() {
        return resourceName;
    }

    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
