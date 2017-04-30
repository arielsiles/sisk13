package com.encens.khipus.model.finances;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyListener;
import com.encens.khipus.model.UpperCaseStringListener;
import com.encens.khipus.model.admin.Company;
import org.hibernate.annotations.Filter;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity for Salary
 *
 * @author
 */

@NamedQueries(
        {
                @NamedQuery(name = "OrganizationalLevel.findOrganizationalLevelByName", query = "select o from OrganizationalLevel o " +
                        "where o.name =:name")
        }
)

@TableGenerator(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "OrganizationalLevel.tableGenerator",
        table = com.encens.khipus.util.Constants.SEQUENCE_TABLE_NAME,
        pkColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_PK_COLUMN_NAME,
        valueColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_VALUE_COLUMN_NAME,
        pkColumnValue = "nivelorganizacional",
        allocationSize = com.encens.khipus.util.Constants.SEQUENCE_ALLOCATION_SIZE)

@Entity
@Filter(name = com.encens.khipus.util.Constants.COMPANY_FILTER_NAME)
@EntityListeners({CompanyListener.class, UpperCaseStringListener.class})
@Table(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "nivelorganizacional")
public class OrganizationalLevel implements BaseModel {

    @Id
    @Column(name = "idnivelorganizacional", nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "OrganizationalLevel.tableGenerator")
    private Long id;

    @ManyToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "nivelorganizacionalraiz", nullable = true)
    private OrganizationalLevel organizationalLevelRoot;

    @OneToMany(mappedBy = "organizationalLevelRoot", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @Filter(name = com.encens.khipus.util.Constants.COMPANY_FILTER_NAME)
    private List<OrganizationalLevel> organizationalLevelList = new ArrayList<OrganizationalLevel>(0);

    @Column(name = "nombre", length = 200)
    @Length(max = 200)
    private String name;

    @Column(name = "sigla", nullable = false, length = 200)
    @Length(max = 200)
    @NotNull
    private String acronym;

    @Column(name = "descripcion", nullable = true, length = 200)
    @Length(max = 200)
    @NotNull
    private String description;

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

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
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

    public String getAcronym() {
        return acronym;
    }

    public void setAcronym(String acronym) {
        this.acronym = acronym;
    }

    public OrganizationalLevel getOrganizationalLevelRoot() {
        return organizationalLevelRoot;
    }

    public void setOrganizationalLevelRoot(OrganizationalLevel organizationalLevelRoot) {
        this.organizationalLevelRoot = organizationalLevelRoot;
    }

    public List<OrganizationalLevel> getOrganizationalLevelList() {
        return organizationalLevelList;
    }

    public void setOrganizationalLevelList(List<OrganizationalLevel> organizationalLevelList) {
        this.organizationalLevelList = organizationalLevelList;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }
}