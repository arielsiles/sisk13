package com.encens.khipus.model.budget;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyListener;
import com.encens.khipus.model.UpperCaseStringListener;
import com.encens.khipus.model.admin.Company;
import com.encens.khipus.model.admin.User;
import org.hibernate.annotations.Filter;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;
import org.jboss.seam.Component;

import javax.persistence.*;
import java.util.Date;

/**
 * BudgetProgram
 *
 * @author
 * @version 2.0
 */
@TableGenerator(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "BudgetProgram.tableGenerator",
        table = com.encens.khipus.util.Constants.SEQUENCE_TABLE_NAME,
        pkColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_PK_COLUMN_NAME,
        valueColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_VALUE_COLUMN_NAME,
        pkColumnValue = "programa",
        allocationSize = com.encens.khipus.util.Constants.SEQUENCE_ALLOCATION_SIZE)
@Entity
@Filter(name = com.encens.khipus.util.Constants.COMPANY_FILTER_NAME)
@EntityListeners({CompanyListener.class, UpperCaseStringListener.class})
@Table(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "programa", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"idcompania", "nombre"}),
        @UniqueConstraint(columnNames = {"idcompania", "codigo"})
})
public class BudgetProgram implements BaseModel {

    @Id
    @Column(name = "idprograma", nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "BudgetProgram.tableGenerator")
    private Long id;

    @Column(name = "nombre", length = 150, nullable = false)
    @Length(max = 150)
    @NotNull
    private String name;

    @Column(name = "codigo", length = 30, nullable = false)
    @Length(max = 30)
    @NotNull
    private String code;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idusuariocreador", nullable = false, updatable = false)
    @NotNull
    private User creatorUser;

    @Column(name = "fechacreacion", nullable = false, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    @NotNull
    private Date creationDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idusuarioeditor")
    private User updaterUser;

    @Column(name = "fechamodificacion")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updateDate;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idcompania", nullable = false, updatable = false, insertable = true)
    @NotNull
    private Company company;

    @Version
    @Column(name = "version", nullable = false)
    private long version;

    @PrePersist
    private void defineCreateValues() {
        setCreatorUser((User) Component.getInstance("currentUser"));
        setCreationDate(new Date());
    }

    @PreUpdate
    private void defineUpdateValues() {
        setUpdaterUser((User) Component.getInstance("currentUser"));
        setUpdateDate(new Date());
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

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public User getCreatorUser() {
        return creatorUser;
    }

    public void setCreatorUser(User creatorUser) {
        this.creatorUser = creatorUser;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public User getUpdaterUser() {
        return updaterUser;
    }

    public void setUpdaterUser(User updaterUser) {
        this.updaterUser = updaterUser;
    }

    public Date getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(Date updateDate) {
        this.updateDate = updateDate;
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

    public String getFullName() {
        return getCode() + " - " + getName();
    }
}
