package com.encens.khipus.model.employees;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyListener;
import com.encens.khipus.model.UpperCaseStringListener;
import com.encens.khipus.model.admin.Company;
import org.hibernate.annotations.Filter;

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
        @NamedQuery(name = "Faculty.findByLocation", query = "select f from Faculty f where f.location =:location order by f.name asc"),
        @NamedQuery(name = "Faculty.findByReferenceIds",
                query = "select f from Faculty f left join f.location location" +
                        " where f.referenceId=:facultyRefId and location.referenceId=:locationRefId" +
                        " order by f.id")
})

@TableGenerator(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "Faculty.tableGenerator",
        table = com.encens.khipus.util.Constants.SEQUENCE_TABLE_NAME,
        pkColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_PK_COLUMN_NAME,
        valueColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_VALUE_COLUMN_NAME,
        pkColumnValue = "facultad",
        allocationSize = com.encens.khipus.util.Constants.SEQUENCE_ALLOCATION_SIZE)

@Entity
@Filter(name = com.encens.khipus.util.Constants.COMPANY_FILTER_NAME)
@EntityListeners({CompanyListener.class, UpperCaseStringListener.class})
@Table(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "facultad")
public class Faculty implements BaseModel {
    @Id
    @Column(name = "idfacultad", nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "Faculty.tableGenerator")
    private Long id;

    @Column(name = "codigo", length = 100, nullable = true)
    private String code;

    @Column(name = "nombre", length = 200, nullable = false)
    private String name;

    @ManyToOne
    @JoinColumn(name = "idsede", nullable = false)
    private Location location;

    @OneToMany(mappedBy = "faculty", fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
    @Filter(name = com.encens.khipus.util.Constants.COMPANY_FILTER_NAME)
    @OrderBy("code asc,name asc")
    private List<Career> careerList = new ArrayList<Career>(0);

    @Version
    @Column(name = "version", nullable = false)
    private long version;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idcompania", nullable = false, updatable = false, insertable = true)
    private Company company;

    @Column(name = "idreferencia", length = 100)
    private String referenceId;

    public Faculty() {

    }

    public Faculty(Long id) {
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

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public List<Career> getCareerList() {
        return careerList;
    }

    public void setCareerList(List<Career> careerList) {
        this.careerList = careerList;
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
        return "Faculty{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", referenceId='" + referenceId + '\'' +
                '}';
    }
}
