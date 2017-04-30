package com.encens.khipus.model.academics;

import com.encens.khipus.util.Constants;

import javax.persistence.*;

/**
 * AcademicEmployeePlanning
 *
 * @author
 * @version 2.24
 */
@NamedQueries({
        @NamedQuery(name = "AcademicEmployeePlanning.findByCodeGestiondAndPeriod",
                query = "select planning from  AcademicEmployeePlanning planning where planning.employeeCode =:employeeCode and planning.gestion =:gestion and planning.period =:period" +
                        " order by planning.seatName,planning.facultyName,planning.careerName"),
        @NamedQuery(name = "AcademicEmployeePlanning.findByCareerGestiondAndPeriod",
                query = "select planning from  AcademicEmployeePlanning planning " +
                        "where planning.careerId=:careerId and planning.facultyId=:facultyId and planning.seatId=:seatId and planning.gestion =:gestion and planning.period =:period" +
                        " order by planning.seatName,planning.facultyName,planning.careerName"),
        @NamedQuery(name = "AcademicEmployeePlanning.findWithinSubjectByCodeAndGestionAndPeriod",
                query = " select distinct new AcademicEmployeePlanning(planning.subjectId,planning.subjectName,planning.careerId,planning.careerName,planning.facultyId,planning.facultyName,planning.seatId,planning.seatName,planning.employeeCode,planning.employeeIdNumber,planning.employeeIdNumberType,planning.employeeLastName,planning.employeeMaidenName,planning.employeeFirstName,planning.employeeGender,planning.gestion,planning.period)" +
                        " from AcademicStudentPlanning planning where planning.employeeCode=:employeeCode and planning.gestion=:gestion and planning.period=:period" +
                        " order by planning.seatName,planning.facultyName,planning.careerName,planning.subjectName")
})


@Entity
@Table(name = "empleadosporcarr", schema = Constants.KHIPUS_SCHEMA)
public class AcademicEmployeePlanning extends AcademicGeneralPlanning {
    @Id
    @Column(name = "idempleadosporcarr")
    private String id;

    @Transient
    private String subjectId;

    @Transient
    private String subjectName;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(String subjectId) {
        this.subjectId = subjectId;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }

    public AcademicEmployeePlanning() {

    }

    public AcademicEmployeePlanning(AcademicStudentPlanning academicStudentPlanning) {
        setId(academicStudentPlanning.getId());

        setSubjectId(academicStudentPlanning.getSubjectId());
        setSubjectName(academicStudentPlanning.getSubjectName());

        setCareerId(academicStudentPlanning.getCareerId());
        setCareerName(academicStudentPlanning.getCareerName());

        setFacultyId(academicStudentPlanning.getFacultyId());
        setFacultyName(academicStudentPlanning.getFacultyName());

        setSeatId(academicStudentPlanning.getSeatId());
        setSeatName(academicStudentPlanning.getSeatName());

        setEmployeeCode(academicStudentPlanning.getEmployeeCode());
        setEmployeeIdNumber(academicStudentPlanning.getEmployeeIdNumber());
        setEmployeeIdNumberType(academicStudentPlanning.getEmployeeIdNumberType());
        setEmployeeLastName(academicStudentPlanning.getEmployeeLastName());
        setEmployeeMaidenName(academicStudentPlanning.getEmployeeMaidenName());
        setEmployeeFirstName(academicStudentPlanning.getEmployeeFirstName());
        setEmployeeGender(academicStudentPlanning.getEmployeeGender());
        setGestion(academicStudentPlanning.getGestion());
        setPeriod(academicStudentPlanning.getPeriod());
    }

    public AcademicEmployeePlanning(String subjectId, String subjectName, String careerId, String careerName, Integer facultyId, String facultyName, Integer seatId, String seatName, Integer employeeCode, String employeeIdNumber, String employeeIdNumberType, String employeeLastName, String employeeMaidenName, String employeeFirstName, String employeeGender, Integer gestion, Integer period) {
        setId(subjectId + "" + careerId + "" + facultyId + "" + seatId + "" + employeeCode + "" + gestion + "" + period);

        setSubjectId(subjectId);
        setSubjectName(subjectName);

        setCareerId(careerId);
        setCareerName(careerName);

        setFacultyId(facultyId);
        setFacultyName(facultyName);

        setSeatId(seatId);
        setSeatName(seatName);

        setEmployeeCode(employeeCode);
        setEmployeeIdNumber(employeeIdNumber);
        setEmployeeIdNumberType(employeeIdNumberType);
        setEmployeeLastName(employeeLastName);
        setEmployeeMaidenName(employeeMaidenName);
        setEmployeeFirstName(employeeFirstName);
        setEmployeeGender(employeeGender);
        setGestion(gestion);
        setPeriod(period);
    }

    @Override
    public String toString() {
        return "AcademicEmployeePlanning{" +
                "id='" + id + '\'' +
                ", subjectId='" + subjectId + '\'' +
                ", subjectName='" + subjectName + '\'' +
                ", " + super.toString() +
                '}';
    }
}
