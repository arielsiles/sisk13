package com.encens.khipus.model.employees;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyListener;
import com.encens.khipus.model.UpperCaseStringListener;
import com.encens.khipus.model.admin.Company;
import org.hibernate.annotations.Filter;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;

import javax.persistence.*;
import java.util.Date;

/**
 * Encens Team
 *
 * @author
 * @version 1.2.3
 */
@TableGenerator(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "WorkExperience.tableGenerator",
        table = com.encens.khipus.util.Constants.SEQUENCE_TABLE_NAME,
        pkColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_PK_COLUMN_NAME,
        valueColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_VALUE_COLUMN_NAME,
        pkColumnValue = "experiencia",
        allocationSize = com.encens.khipus.util.Constants.SEQUENCE_ALLOCATION_SIZE)
@Entity
@Filter(name = com.encens.khipus.util.Constants.COMPANY_FILTER_NAME)
@EntityListeners({CompanyListener.class, UpperCaseStringListener.class})
@Table(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "experiencia")
public class Experience implements BaseModel {
    @Id
    @Column(name = "idexperiencia", nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "WorkExperience.tableGenerator")
    private Long id;

    @Column(name = "lugar")
    @Length(max = 250)
    private String place;

    @Column(name = "cargo")
    @Length(max = 250)
    private String position;

    @Column(name = "fechainicio")
    @Temporal(TemporalType.DATE)
    private Date initDate = new Date();

    @Column(name = "fechafin")
    @Temporal(TemporalType.DATE)
    private Date endDate = new Date();

    @Column(name = "duracion")
    private String duration;

    @ManyToOne
    @JoinColumn(name = "idpostulante")
    private Postulant postulant;

    @Column(name = "tipo")
    @Enumerated(EnumType.STRING)
    private ExperienceType experienceType;

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

    public String getPlace() {
        return place;
    }

    public void setPlace(String place) {
        this.place = place;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public Date getInitDate() {
        return initDate;
    }

    public void setInitDate(Date initDate) {
        this.initDate = initDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public Postulant getPostulant() {
        return postulant;
    }

    public void setPostulant(Postulant postulant) {
        this.postulant = postulant;
    }

    public ExperienceType getExperienceType() {
        return experienceType;
    }

    public void setExperienceType(ExperienceType experienceType) {
        this.experienceType = experienceType;
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
