package com.encens.khipus.model.academics;

import com.encens.khipus.util.Constants;

import javax.persistence.*;

/**
 * AcademicStudentPlanning
 *
 * @author
 * @version 2.24
 */

@NamedQueries({
        @NamedQuery(name = "AcademicStudentPlanning.findByStudentCodeAndGestionAndPeriod",
                query = " select planning from AcademicStudentPlanning planning where planning.studentCode=:studentCode and planning.gestion=:gestion and planning.period=:period" +
                        " order by planning.seatName,planning.facultyName,planning.careerName,planning.subjectName")
})

@Entity
@Table(name = "estudiantesporasig", schema = Constants.KHIPUS_SCHEMA)
public class AcademicStudentPlanning extends AcademicGeneralPlanning {

    @Id
    @Column(name = "idestudientesporasig")
    private String id;

    @Column(name = "codigoestudiante", insertable = false, updatable = false)
    private Integer studentCode;

    @Column(name = "identificacionest", length = 20, updatable = false)
    private String studentIdNumber;

    @Column(name = "tipoidentificacionest", length = 5, updatable = false)
    private String studentIdNumberType;

    @Column(name = "apellidopaternoest", length = 30, updatable = false)
    private String studentLastName;

    @Column(name = "apellidomaternoest", length = 30, updatable = false)
    private String studentMaidenName;

    @Column(name = "nombresest", length = 50, updatable = false)
    private String studentFirstName;

    @Column(name = "generoest", length = 1, updatable = false)
    private String studentGender;

    @Column(name = "idasignatura", length = 20, updatable = false)
    private String subjectId;

    @Column(name = "nombreasig", length = 300, updatable = false)
    private String subjectName;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Integer getStudentCode() {
        return studentCode;
    }

    public void setStudentCode(Integer studentCode) {
        this.studentCode = studentCode;
    }

    public String getStudentIdNumber() {
        return studentIdNumber;
    }

    public void setStudentIdNumber(String studentIdNumber) {
        this.studentIdNumber = studentIdNumber;
    }

    public String getStudentIdNumberType() {
        return studentIdNumberType;
    }

    public void setStudentIdNumberType(String studentIdNumberType) {
        this.studentIdNumberType = studentIdNumberType;
    }

    public String getStudentLastName() {
        return studentLastName;
    }

    public void setStudentLastName(String studentLastName) {
        this.studentLastName = studentLastName;
    }

    public String getStudentMaidenName() {
        return studentMaidenName;
    }

    public void setStudentMaidenName(String studentMaidenName) {
        this.studentMaidenName = studentMaidenName;
    }

    public String getStudentFirstName() {
        return studentFirstName;
    }

    public void setStudentFirstName(String studentFirstName) {
        this.studentFirstName = studentFirstName;
    }

    public String getStudentGender() {
        return studentGender;
    }

    public void setStudentGender(String studentGender) {
        this.studentGender = studentGender;
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

    @Override
    public String toString() {
        return "AcademicStudentPlanning{" +
                "studentCode=" + studentCode +
                ", studentIdNumber='" + studentIdNumber + '\'' +
                ", studentIdNumberType='" + studentIdNumberType + '\'' +
                ", studentLastName='" + studentLastName + '\'' +
                ", studentMaidenName='" + studentMaidenName + '\'' +
                ", studentFirstName='" + studentFirstName + '\'' +
                ", studentGender='" + studentGender + '\'' +
                ", subjectId='" + subjectId + '\'' +
                ", subjectName='" + subjectName + '\'' +
                ',' + super.toString() +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        AcademicStudentPlanning that = (AcademicStudentPlanning) o;

        if (studentCode != null ? !studentCode.equals(that.studentCode) : that.studentCode != null) {
            return false;
        }
        if (studentFirstName != null ? !studentFirstName.equals(that.studentFirstName) : that.studentFirstName != null) {
            return false;
        }
        if (studentGender != null ? !studentGender.equals(that.studentGender) : that.studentGender != null) {
            return false;
        }
        if (studentIdNumber != null ? !studentIdNumber.equals(that.studentIdNumber) : that.studentIdNumber != null) {
            return false;
        }
        if (studentIdNumberType != null ? !studentIdNumberType.equals(that.studentIdNumberType) : that.studentIdNumberType != null) {
            return false;
        }
        if (studentLastName != null ? !studentLastName.equals(that.studentLastName) : that.studentLastName != null) {
            return false;
        }
        if (studentMaidenName != null ? !studentMaidenName.equals(that.studentMaidenName) : that.studentMaidenName != null) {
            return false;
        }
        if (subjectId != null ? !subjectId.equals(that.subjectId) : that.subjectId != null) {
            return false;
        }
        if (subjectName != null ? !subjectName.equals(that.subjectName) : that.subjectName != null) {
            return false;
        }

        return super.equals(o);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (studentCode != null ? studentCode.hashCode() : 0);
        result = 31 * result + (studentIdNumber != null ? studentIdNumber.hashCode() : 0);
        result = 31 * result + (studentIdNumberType != null ? studentIdNumberType.hashCode() : 0);
        result = 31 * result + (studentLastName != null ? studentLastName.hashCode() : 0);
        result = 31 * result + (studentMaidenName != null ? studentMaidenName.hashCode() : 0);
        result = 31 * result + (studentFirstName != null ? studentFirstName.hashCode() : 0);
        result = 31 * result + (studentGender != null ? studentGender.hashCode() : 0);
        result = 31 * result + (subjectId != null ? subjectId.hashCode() : 0);
        result = 31 * result + (subjectName != null ? subjectName.hashCode() : 0);
        return result;
    }
}
