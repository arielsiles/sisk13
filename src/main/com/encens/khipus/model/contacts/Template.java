package com.encens.khipus.model.contacts;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyListener;
import com.encens.khipus.model.UpperCaseStringListener;
import com.encens.khipus.model.admin.Company;
import com.encens.khipus.model.common.File;
import org.hibernate.annotations.Filter;
import org.hibernate.validator.NotNull;

import javax.persistence.*;
import javax.persistence.Entity;

/**
 * Encens S.R.L.
 *
 * @author
 * @version $Id: Template.java  24-feb-2010 14:27:23$
 */

@TableGenerator(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "Template.tableGenerator",
        table = com.encens.khipus.util.Constants.SEQUENCE_TABLE_NAME,
        pkColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_PK_COLUMN_NAME,
        valueColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_VALUE_COLUMN_NAME,
        pkColumnValue = "plantilla",
        allocationSize = com.encens.khipus.util.Constants.SEQUENCE_ALLOCATION_SIZE)
@Entity
@Filter(name = com.encens.khipus.util.Constants.COMPANY_FILTER_NAME)
@EntityListeners({CompanyListener.class, UpperCaseStringListener.class})
@Table(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "plantilla")
public class Template implements BaseModel {
    @Id
    @Column(name = "idplantilla", nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "Template.tableGenerator")
    private Long id;

    @OneToOne(optional = false, fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "idarchivo", nullable = false)
    private File file = new File();

    @Column(name = "nombre", nullable = false)
    @NotNull
    private String name;

    @Column(name = "tipo", length = 50, nullable = false)
    @Enumerated(EnumType.STRING)
    private TemplateType templateType;

    @Column(name = "formato", length = 50, nullable = false)
    @Enumerated(EnumType.STRING)
    private FileFormat fileFormat;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idcompania", nullable = false, updatable = false, insertable = true)
    private Company company;

    @Version
    @Column(name = "version", nullable = false)
    private long version;

    public Template() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public TemplateType getTemplateType() {
        return templateType;
    }

    public void setTemplateType(TemplateType templateType) {
        this.templateType = templateType;
    }

    public FileFormat getFileFormat() {
        return fileFormat;
    }

    public void setFileFormat(FileFormat fileFormat) {
        this.fileFormat = fileFormat;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }
}