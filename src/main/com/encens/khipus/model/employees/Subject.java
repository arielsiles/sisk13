package com.encens.khipus.model.employees;

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
 * Entity for Subject
 *
 * @author
 */

@NamedQueries({
        @NamedQuery(name = "Subject.findByCareer", query = "select s from Subject s where s.career=:career order by s.name asc"),
        @NamedQuery(name = "Subject.findByReferenceIds",
                query = "select s from Subject s" +
                        " left join s.career career " +
                        " left join career.faculty faculty" +
                        " left join faculty.location location" +
                        " where s.referenceId=:subjectRefId and career.referenceId=:careerRefId " +
                        " and faculty.referenceId=:facultyRefId and location.referenceId=:locationRefId" +
                        " order by s.id")
})

@TableGenerator(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "Subject.tableGenerator",
        table = com.encens.khipus.util.Constants.SEQUENCE_TABLE_NAME,
        pkColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_PK_COLUMN_NAME,
        valueColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_VALUE_COLUMN_NAME,
        pkColumnValue = "asignatura",
        allocationSize = com.encens.khipus.util.Constants.SEQUENCE_ALLOCATION_SIZE)

@Entity
@Filter(name = com.encens.khipus.util.Constants.COMPANY_FILTER_NAME)
@EntityListeners({CompanyListener.class, UpperCaseStringListener.class})
@Table(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "asignatura")
public class Subject implements BaseModel {

    @Id
    @Column(name = "idasignatura", nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "Subject.tableGenerator")
    private Long id;

    @OneToMany(mappedBy = "subject", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @Filter(name = com.encens.khipus.util.Constants.COMPANY_FILTER_NAME)
    private List<SubjectGroup> subjectGroupList = new ArrayList<SubjectGroup>(0);

    @Column(name = "codigo", length = 100, nullable = true)
    @Length(max = 100)
    private String code;

    @Column(name = "nombre", nullable = false)
    private String name;

    @ManyToOne
    @JoinColumn(name = "idcarrera", nullable = true)
    private Career career;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idcompania", nullable = false, updatable = false, insertable = true)
    @NotNull
    private Company company;

    @Column(name = "idreferencia", length = 100)
    @Length(max = 100)
    private String referenceId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public List<SubjectGroup> getSubjectGroupList() {
        return subjectGroupList;
    }

    public void setSubjectGroupList(List<SubjectGroup> subjectGroupList) {
        this.subjectGroupList = subjectGroupList;
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

    public Career getCareer() {
        return career;
    }

    public void setCareer(Career career) {
        this.career = career;
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
        return "Subject{" +
                "id=" + id +
                ", referenceId='" + referenceId + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}