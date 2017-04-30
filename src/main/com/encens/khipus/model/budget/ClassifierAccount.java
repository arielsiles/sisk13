package com.encens.khipus.model.budget;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyListener;
import com.encens.khipus.model.UpperCaseStringListener;
import com.encens.khipus.model.admin.Company;
import org.hibernate.annotations.Filter;
import org.hibernate.validator.NotNull;

import javax.persistence.*;

/**
 * ClassifierAccount
 *
 * @author
 * @version 2.1
 */
@TableGenerator(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA,
        name = "ClassifierAccount.tableGenerator",
        table = com.encens.khipus.util.Constants.SEQUENCE_TABLE_NAME,
        pkColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_PK_COLUMN_NAME,
        valueColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_VALUE_COLUMN_NAME,
        pkColumnValue = "clasifcuenta",
        allocationSize = com.encens.khipus.util.Constants.SEQUENCE_ALLOCATION_SIZE
)
@Entity
@Filter(name = com.encens.khipus.util.Constants.COMPANY_FILTER_NAME)
@EntityListeners({CompanyListener.class, UpperCaseStringListener.class})
@Table(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "clasifcuenta", uniqueConstraints = @UniqueConstraint(columnNames = {"idclasificador", "codigocuenta"}))
public class ClassifierAccount implements BaseModel {
    @Id
    @Column(name = "idclasifcuenta", nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "ClassifierAccount.tableGenerator")
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idclasificador", nullable = false)
    @NotNull
    private Classifier classifier;

    @Column(name = "codigocuenta", length = 20, nullable = false)
    private String accountCode;

    @Version
    @Column(name = "version", nullable = false)
    private long version;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idcompania", nullable = false, updatable = false, insertable = true)
    @NotNull
    private Company company;

    public ClassifierAccount() {
    }

    public ClassifierAccount(Classifier classifier, String accountCode) {
        this.classifier = classifier;
        this.accountCode = accountCode;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Classifier getClassifier() {
        return classifier;
    }

    public void setClassifier(Classifier classifier) {
        this.classifier = classifier;
    }

    public String getAccountCode() {
        return accountCode;
    }

    public void setAccountCode(String accountCode) {
        this.accountCode = accountCode;
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
}
