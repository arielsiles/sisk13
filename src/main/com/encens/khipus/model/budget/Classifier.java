package com.encens.khipus.model.budget;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyListener;
import com.encens.khipus.model.UpperCaseStringListener;
import com.encens.khipus.model.admin.Company;
import org.hibernate.annotations.Filter;
import org.hibernate.validator.NotNull;

import javax.persistence.*;
import java.util.List;

/**
 * This class represents the budget classifiers
 *
 * @author
 * @version 2.0
 */
@TableGenerator(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "Classifier.tableGenerator",
        table = com.encens.khipus.util.Constants.SEQUENCE_TABLE_NAME,
        pkColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_PK_COLUMN_NAME,
        valueColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_VALUE_COLUMN_NAME,
        pkColumnValue = "clasificador",
        allocationSize = com.encens.khipus.util.Constants.SEQUENCE_ALLOCATION_SIZE)

@Entity
@Filter(name = com.encens.khipus.util.Constants.COMPANY_FILTER_NAME)
@EntityListeners({CompanyListener.class, UpperCaseStringListener.class})
@Table(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "clasificador", uniqueConstraints = @UniqueConstraint(columnNames = {"idcompania", "tipo", "codigo"}))
public class Classifier implements BaseModel {
    @Id
    @Column(name = "idclasificador", nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "Classifier.tableGenerator")
    private Long id;

    @Column(name = "nombre", length = 150, nullable = false)
    private String name;

    @Column(name = "codigo", length = 20, nullable = false)
    private String code;

    @Transient
    private String accountCode;

    @Column(name = "tipo", length = 20, nullable = false)
    @Enumerated(EnumType.STRING)
    private ClassifierType type;

    @Version
    @Column(name = "version", nullable = false)
    private long version;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idcompania", nullable = false, updatable = false, insertable = true)
    @NotNull
    private Company company;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "classifier", cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
    @OrderBy("accountCode asc")
    private List<ClassifierAccount> classifierAccountList;

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

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getAccountCode() {
        return accountCode;
    }

    public void setAccountCode(String accountCode) {
        this.accountCode = accountCode;
    }

    public ClassifierType getType() {
        return type;
    }

    public void setType(ClassifierType type) {
        this.type = type;
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

    public List<ClassifierAccount> getClassifierAccountList() {
        return classifierAccountList;
    }

    public void setClassifierAccountList(List<ClassifierAccount> classifierAccountList) {
        this.classifierAccountList = classifierAccountList;
    }

    public String getFullName() {
        return getCode() + " - " + getName();
    }
}
