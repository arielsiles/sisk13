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
        @NamedQuery(name = "Career.findByFaculty", query = "select c from Career c where c.faculty=:faculty order by c.name asc"),
        @NamedQuery(name = "Career.findByReferenceIds",
                query = "select c from Career c" +
                        " left join c.faculty faculty" +
                        " left join faculty.location location" +
                        " where c.referenceId=:careerRefId and faculty.referenceId=:facultyRefId and location.referenceId=:locationRefId" +
                        " order by c.id")
})

@TableGenerator(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "Career.tableGenerator",
        table = com.encens.khipus.util.Constants.SEQUENCE_TABLE_NAME,
        pkColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_PK_COLUMN_NAME,
        valueColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_VALUE_COLUMN_NAME,
        pkColumnValue = "carrera",
        allocationSize = com.encens.khipus.util.Constants.SEQUENCE_ALLOCATION_SIZE)

@Entity
@Filter(name = com.encens.khipus.util.Constants.COMPANY_FILTER_NAME)
@EntityListeners({CompanyListener.class, UpperCaseStringListener.class})
@Table(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "carrera")
public class Career implements BaseModel {
    @Id
    @Column(name = "idcarrera", nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "Career.tableGenerator")
    private Long id;

    @Column(name = "codigo", length = 100, nullable = true)
    private String code;

    @Column(name = "nombre", length = 200, nullable = false)
    private String name;

    @ManyToOne
    @JoinColumn(name = "idfacultad", nullable = false)
    private Faculty faculty;

    @OneToMany(mappedBy = "career", fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
    @Filter(name = com.encens.khipus.util.Constants.COMPANY_FILTER_NAME)
    @OrderBy("code asc,name asc")
    private List<Subject> subjectList = new ArrayList<Subject>(0);

    @Version
    @Column(name = "version", nullable = false)
    private long version;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idcompania", nullable = false, updatable = false, insertable = true)
    private Company company;

    @Column(name = "idreferencia", length = 100)
    private String referenceId;

    public Career() {

    }

    public Career(Long id) {
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

    public Faculty getFaculty() {
        return faculty;
    }

    public void setFaculty(Faculty faculty) {
        this.faculty = faculty;
    }

    public List<Subject> getSubjectList() {
        return subjectList;
    }

    public void setSubjectList(List<Subject> subjectList) {
        this.subjectList = subjectList;
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
        return "Career{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", referenceId='" + referenceId + '\'' +
                '}';
    }
}
