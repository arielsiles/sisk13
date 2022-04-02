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
                @NamedQuery(name = "TeacherEvaluation.findTeacherEvaluation", query = "select t from TeacherEvaluation t where t.employeeCode=:employeeCode " +
                        "and t.period=:period and t.gestion=:gestion")
        }
)

@Entity
@EntityListeners(UpperCaseStringListener.class)
@Table(name = "evaluaciondocente", schema = Constants.KHIPUS_SCHEMA)
public class TeacherEvaluation {

    @Id
    @Column(name = "idevaluaciondocente", updatable = false, insertable = false)
    private String id;

    @Column(name = "unidadacadadm", updatable = false, insertable = false)
    private Integer administrativeAcademicUnit;

    @Column(name = "sede", updatable = false, insertable = false)
    private String city;

    @Column(name = "codempleado", updatable = false, insertable = false)
    private Long employeeCode;

    @Column(name = "documento", updatable = false, insertable = false)
    private String idNumber;

    @Column(name = "apellido_paterno", updatable = false, insertable = false)
    private String lastName;

    @Column(name = "apellido_materno", updatable = false, insertable = false)
    private String maidenName;

    @Column(name = "nombres", updatable = false, insertable = false)
    private String firstName;

    @Column(name = "cant_estudiantes", updatable = false, insertable = false)
    private Integer numberOfStudents;

    @Column(name = "cant_carreras", updatable = false, insertable = false)
    private Integer numberOfCareers;

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

    public Long getEmployeeCode() {
        return employeeCode;
    }

    public void setEmployeeCode(Long employeeCode) {
        this.employeeCode = employeeCode;
    }

    public String getIdNumber() {
        return idNumber;
    }

    public void setIdNumber(String idNumber) {
        this.idNumber = idNumber;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getMaidenName() {
        return maidenName;
    }

    public void setMaidenName(String maidenName) {
        this.maidenName = maidenName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public Integer getNumberOfStudents() {
        return numberOfStudents;
    }

    public void setNumberOfStudents(Integer numberOfStudents) {
        this.numberOfStudents = numberOfStudents;
    }

    public Integer getNumberOfCareers() {
        return numberOfCareers;
    }

    public void setNumberOfCareers(Integer numberOfCareers) {
        this.numberOfCareers = numberOfCareers;
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
