package com.encens.khipus.model.employees;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyListener;
import com.encens.khipus.model.UpperCaseStringListener;
import com.encens.khipus.model.admin.Company;
import org.hibernate.annotations.Filter;
import org.hibernate.validator.Length;

import javax.persistence.*;

/**
 * Encens Team
 *
 * @author
 * @version 1.2.3
 */

@TableGenerator(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "AcademicFormation.tableGenerator",
        table = com.encens.khipus.util.Constants.SEQUENCE_TABLE_NAME,
        pkColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_PK_COLUMN_NAME,
        valueColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_VALUE_COLUMN_NAME,
        pkColumnValue = "formacionacademica",
        allocationSize = com.encens.khipus.util.Constants.SEQUENCE_ALLOCATION_SIZE)
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Filter(name = com.encens.khipus.util.Constants.COMPANY_FILTER_NAME)
@EntityListeners({CompanyListener.class, UpperCaseStringListener.class})
@Table(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "formacionacademica")
public class AcademicFormation implements BaseModel {

    @Id
    @Column(name = "idformacionacademica", nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "AcademicFormation.tableGenerator")
    private Long id;

    @Column(name = "nombre")
    @Length(max = 250)
    private String name;

    @Column(name = "universidad")
    @Length(max = 250)
    private String university;

    @Column(name = "tipo")
    @Enumerated(EnumType.STRING)
    private AcademicFormationType academicFormationType;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idcompania", nullable = false, updatable = false, insertable = true)
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

    public String getUniversity() {
        return university;
    }

    public void setUniversity(String university) {
        this.university = university;
    }

    public AcademicFormationType getAcademicFormationType() {
        return academicFormationType;
    }

    public void setAcademicFormationType(AcademicFormationType academicFormationType) {
        this.academicFormationType = academicFormationType;
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
}
