package com.encens.khipus.model.contacts;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyListener;
import com.encens.khipus.model.UpperCaseStringListener;
import com.encens.khipus.model.admin.Company;
import com.encens.khipus.model.customers.DocumentType;
import org.hibernate.annotations.Filter;
import org.hibernate.validator.Length;

import javax.persistence.*;
import javax.persistence.Entity;

/**
 * Entity for Limit
 *
 * @author
 * @version 2.7
 */

@TableGenerator(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "Extension.tableGenerator",
        table = com.encens.khipus.util.Constants.SEQUENCE_TABLE_NAME,
        pkColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_PK_COLUMN_NAME,
        valueColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_VALUE_COLUMN_NAME,
        pkColumnValue = "EXTTIPODOCUMENTO",
        allocationSize = com.encens.khipus.util.Constants.SEQUENCE_ALLOCATION_SIZE)

@NamedQueries(
        {
                @NamedQuery(name = "Extension.findAll", query = "select o from Extension o "),
                @NamedQuery(name = "Extension.findExtension", query = "select o from Extension o where o.id=:id"),
                @NamedQuery(name = "Extension.findExtensionsByDocumentType", query = "select o from Extension o where o.documentType=:documentType")
        }
)
@Entity
@Filter(name = com.encens.khipus.util.Constants.COMPANY_FILTER_NAME)
@EntityListeners({CompanyListener.class, UpperCaseStringListener.class})
@Table(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "EXTTIPODOCUMENTO", uniqueConstraints = @UniqueConstraint(columnNames = {"IDTIPODOCUMENTO", "IDEXTTIPODOCUMENTO"}))
public class Extension implements BaseModel {

    @Id
    @Column(name = "IDEXTTIPODOCUMENTO", nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "Extension.tableGenerator")
    private Long id;

    @Column(name = "EXTENSION", nullable = false, length = 100)
    @Length(max = 100)
    private String extension;

    @ManyToOne
    @JoinColumn(name = "IDTIPODOCUMENTO", referencedColumnName = "idtipodocumento", nullable = false)
    private DocumentType documentType;

    @Version
    @Column(name = "VERSION", nullable = false)
    private long version;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "IDCOMPANIA", nullable = false, updatable = false, insertable = true)
    private Company company;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public DocumentType getDocumentType() {
        return documentType;
    }

    public void setDocumentType(DocumentType documentType) {
        this.documentType = documentType;
    }

    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }
}