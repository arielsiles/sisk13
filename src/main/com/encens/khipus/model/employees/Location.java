package com.encens.khipus.model.employees;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyListener;
import com.encens.khipus.model.UpperCaseStringListener;
import com.encens.khipus.model.admin.Company;
import org.hibernate.annotations.Filter;
import org.hibernate.validator.Length;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Encens Team
 *
 * @author
 * @version 1.1.7
 */

@NamedQueries({
        @NamedQuery(name = "Location.findAll", query = "select l from Location l order by l.name asc"),
        @NamedQuery(name = "Location.findByReferenceId", query = "select l from Location l" +
                " where l.referenceId=:locationRefId order by l.id")
})

@TableGenerator(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "Location.tableGenerator",
        table = com.encens.khipus.util.Constants.SEQUENCE_TABLE_NAME,
        pkColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_PK_COLUMN_NAME,
        valueColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_VALUE_COLUMN_NAME,
        pkColumnValue = "sede",
        allocationSize = com.encens.khipus.util.Constants.SEQUENCE_ALLOCATION_SIZE)

@Entity
@Filter(name = com.encens.khipus.util.Constants.COMPANY_FILTER_NAME)
@EntityListeners({CompanyListener.class, UpperCaseStringListener.class})
@Table(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "sede")
public class Location implements BaseModel {
    @Id
    @Column(name = "idsede", nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "Location.tableGenerator")
    private Long id;

    @Column(name = "codigo", length = 100, nullable = true)
    @Length(max = 100)
    private String code;

    @Column(name = "nombre", length = 200, nullable = false)
    @Length(max = 200)
    private String name;

    @OneToMany(mappedBy = "location", fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
    @Filter(name = com.encens.khipus.util.Constants.COMPANY_FILTER_NAME)
    @OrderBy("code asc,name asc")
    private List<Faculty> facultyList = new ArrayList<Faculty>(0);

    @Version
    @Column(name = "version", nullable = false)
    private long version;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idcompania", nullable = false, updatable = false, insertable = true)
    private Company company;

    @Column(name = "idreferencia", length = 100)
    @Length(max = 100)
    private String referenceId;

    public Location() {

    }

    public Location(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Faculty> getFacultyList() {
        return facultyList;
    }

    public void setFacultyList(List<Faculty> facultyList) {
        this.facultyList = facultyList;
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

    public String getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(String referenceId) {
        this.referenceId = referenceId;
    }

    @Override
    public String toString() {
        return "Location{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", referenceId='" + referenceId + '\'' +
                '}';
    }
}
