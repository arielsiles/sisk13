package com.encens.khipus.model.employees;

import com.encens.khipus.model.UpperCaseStringListener;
import com.encens.khipus.util.Constants;

import javax.persistence.*;

/**
 * Entity for TeacherEvaluation
 *
 * @author Ariel Siles
 */

@NamedQueries(
        {
                @NamedQuery(name = "TeachersForCarrer.findCareer", query = "select t from TeachersForCareer t where t.administrativeAcademicUnit=:administrativeAcademicUnit " +
                        "and t.studyPlan=:studyPlan and t.period=:period and t.gestion=:gestion")
        }
)

@Entity
@EntityListeners(UpperCaseStringListener.class)
@Table(name = "docentesporcarrera", schema = Constants.KHIPUS_SCHEMA)
public class TeachersForCareer {

    @Id
    @Column(name = "iddocentesporcarrera", updatable = false, insertable = false)
    private String id;

    @Column(name = "unidad_acad_adm", updatable = false, insertable = false)
    private Integer administrativeAcademicUnit;

    @Column(name = "sede", length = 200, updatable = false, insertable = false)
    private String city;

    @Column(name = "plan_estudio", length = 200, updatable = false, insertable = false)
    private String studyPlan;

    @Column(name = "carrera", length = 200, updatable = false, insertable = false)
    private String carrerName;

    @Column(name = "cant_docentes", updatable = false, insertable = false)
    private Integer numberOfTeachers;

    @Column(name = "gestion", updatable = false, insertable = false)
    private Integer gestion;

    @Column(name = "periodo", updatable = false, insertable = false)
    private Integer period;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Integer getAdministrativeAcademicUnit() {
        return administrativeAcademicUnit;
    }

    public void setAdministrativeAcademicUnit(Integer administrativeAcademicUnit) {
        this.administrativeAcademicUnit = administrativeAcademicUnit;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getStudyPlan() {
        return studyPlan;
    }

    public void setStudyPlan(String studyPlan) {
        this.studyPlan = studyPlan;
    }

    public String getCarrerName() {
        return carrerName;
    }

    public void setCarrerName(String carrerName) {
        this.carrerName = carrerName;
    }

    public Integer getNumberOfTeachers() {
        return numberOfTeachers;
    }

    public void setNumberOfTeachers(Integer numberOfTeachers) {
        this.numberOfTeachers = numberOfTeachers;
    }

    public Integer getGestion() {
        return gestion;
    }

    public void setGestion(Integer gestion) {
        this.gestion = gestion;
    }

    public Integer getPeriod() {
        return period;
    }

    public void setPeriod(Integer period) {
        this.period = period;
    }
}