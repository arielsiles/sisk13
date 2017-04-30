package com.encens.khipus.model.admin;


import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyListener;
import com.encens.khipus.model.UpperCaseStringListener;
import org.hibernate.annotations.Filter;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Class to represents the roles of the application
 *
 * @author
 * @version 1.0
 */
@TableGenerator(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "Role.tableGenerator",
        table = com.encens.khipus.util.Constants.SEQUENCE_TABLE_NAME,
        pkColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_PK_COLUMN_NAME,
        valueColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_VALUE_COLUMN_NAME,
        pkColumnValue = "rol",
        allocationSize = com.encens.khipus.util.Constants.SEQUENCE_ALLOCATION_SIZE)
@Entity
@Filter(name = com.encens.khipus.util.Constants.COMPANY_FILTER_NAME)
@EntityListeners({CompanyListener.class, UpperCaseStringListener.class})
@Table(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "rol", uniqueConstraints = @UniqueConstraint(columnNames = {"idcompania", "nombre"}))
public class Role implements BaseModel {

    @Id
    @Column(name = "idrol", nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "Role.tableGenerator")
    private Long id;

    @Length(max = 150)
    @Column(name = "nombre", length = 150, nullable = false)
    private String name;

    @Column(name = "descripcion")
    @Lob
    private String description;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "usuariorol",
            joinColumns = @JoinColumn(name = "idrol"),
            inverseJoinColumns = @JoinColumn(name = "idusuario"),
            schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA
    )
    @Filter(name = com.encens.khipus.util.Constants.COMPANY_FILTER_NAME)
    @OrderBy("username asc")
    private List<User> users;

    @OneToMany(mappedBy = "role", fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
    @Filter(name = com.encens.khipus.util.Constants.COMPANY_FILTER_NAME)
    private List<AccessRight> accessRights = new ArrayList<AccessRight>(0);

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idcompania", nullable = false, updatable = false, insertable = true)
    @NotNull
    private Company company;

    @Version
    @Column(name = "version", nullable = false)
    private long version;


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

    public List<User> getUsers() {
        return users;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }

    public List<AccessRight> getAccessRights() {
        return accessRights;
    }

    public void setAccessRights(List<AccessRight> accessRights) {
        this.accessRights = accessRights;
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

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Role && this.getId().equals(((Role) obj).getId());
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    @Override
    public String toString() {
        return id.toString();
    }
}
