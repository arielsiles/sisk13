package com.encens.khipus.model.employees;

import com.encens.khipus.model.UpperCaseStringListener;
import com.encens.khipus.util.Constants;

import javax.persistence.*;

/**
 * Entity for AcademicPlanningSummary
 *
 * @author Ariel Siles
 */

/*@NamedQueries(
        {
                @NamedQuery(name = "TeacherEvaluation.findTeacherEvaluation", query = "select t from AcademicPlanningSummary t where t.employeeCode=:employeeCode " +
                        "and t.period=:period and t.gestion=:gestion")
        }
)*/

@Entity
@EntityListeners(UpperCaseStringListener.class)
@Table(name = "resumenplanificacionacademica", schema = Constants.KHIPUS_SCHEMA)
public class AcademicPlanningSummary {

    @Id
    @Column(name = "idresumenplanacad", updatable = false, insertable = false)
    private String id;

    @Column(name = "unidad_acad_adm", updatable = false, insertable = false)
    private Integer administrativeAcademicUnit;

    @Column(name = "sede", length = 200, updatable = false, insertable = false)
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

    @Column(name = "plan_estudio", updatable = false, insertable = false)
    private String studyPlan;

    @Column(name = "carrera", updatable = false, insertable = false)
    private String career;

    @Column(name = "cod_asignatura", updatable = false, insertable = false)
    private String asignatureCode;

    @Column(name = "nombre_asignatura", updatable = false, insertable = false)
    private String asignatureName;

    @Column(name = "sigla", updatable = false, insertable = false)
    private String acronym;

    @Column(name = "grupo_asignatura", updatable = false, insertable = false)
    private String asignatureGroup;

    @Column(name = "tipo_grupo", updatable = false, insertable = false)
    private String groupType;

    @Column(name = "carga_teorica", updatable = false, insertable = false)
    private Integer theoreticalCharge;

    @Column(name = "carga_practica", updatable = false, insertable = false)
    private Integer practicalCharge;

    @Column(name = "carga_horaria", updatable = false, insertable = false)
    private Integer scheduleCharge;

    @Column(name = "cant_estudiantes", updatable = false, insertable = false)
    private Integer numberOfStudents;

    @Column(name = "gestion", updatable = false, insertable = false)
    private Integer gestion;

    @Column(name = "periodo", updatable = false, insertable = false)
    private Integer period;

    @Column(name = "semestre", updatable = false, insertable = false)
    private String semester;

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

    public String getStudyPlan() {
        return studyPlan;
    }

    public void setStudyPlan(String studyPlan) {
        this.studyPlan = studyPlan;
    }

    public String getCareer() {
        return career;
    }

    public void setCareer(String career) {
        this.career = career;
    }

    public String getAsignatureCode() {
        return asignatureCode;
    }

    public void setAsignatureCode(String asignatureCode) {
        this.asignatureCode = asignatureCode;
    }

    public String getAsignatureName() {
        return asignatureName;
    }

    public void setAsignatureName(String asignatureName) {
        this.asignatureName = asignatureName;
    }

    public String getAcronym() {
        return acronym;
    }

    public void setAcronym(String acronym) {
        this.acronym = acronym;
    }

    public String getAsignatureGroup() {
        return asignatureGroup;
    }

    public void setAsignatureGroup(String asignatureGroup) {
        this.asignatureGroup = asignatureGroup;
    }

    public String getGroupType() {
        return groupType;
    }

    public void setGroupType(String groupType) {
        this.groupType = groupType;
    }

    public Integer getTheoreticalCharge() {
        return theoreticalCharge;
    }

    public void setTheoreticalCharge(Integer theoreticalCharge) {
        this.theoreticalCharge = theoreticalCharge;
    }

    public Integer getPracticalCharge() {
        return practicalCharge;
    }

    public void setPracticalCharge(Integer practicalCharge) {
        this.practicalCharge = practicalCharge;
    }

    public Integer getScheduleCharge() {
        return scheduleCharge;
    }

    public void setScheduleCharge(Integer scheduleCharge) {
        this.scheduleCharge = scheduleCharge;
    }

    public Integer getNumberOfStudents() {
        return numberOfStudents;
    }

    public void setNumberOfStudents(Integer numberOfStudents) {
        this.numberOfStudents = numberOfStudents;
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

    public String getSemester() {
        return semester;
    }

    public void setSemester(String semester) {
        this.semester = semester;
    }
}