package com.encens.khipus.model.academics;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import java.io.Serializable;

/**
 * AcademicGeneralPlanning
 *
 * @author
 * @version 2.24
 */
@MappedSuperclass
public class AcademicGeneralPlanning implements Serializable {

    @Column(name = "idcarrera", length = 10, updatable = false)
    private String careerId;

    @Column(name = "nombrecarrera", length = 100, updatable = false)
    private String careerName;

    @Column(name = "idfacultad", updatable = false)
    private Integer facultyId;

    @Column(name = "nombrefac", length = 60, updatable = false)
    private String facultyName;

    @Column(name = "idsede", updatable = false)
    private Integer seatId;

    @Column(name = "nombresede", length = 60, updatable = false)
    private String seatName;

    @Column(name = "codigoempleado", insertable = false, updatable = false)
    private Integer employeeCode;

    @Column(name = "identificacionemp", length = 30, updatable = false)
    private String employeeIdNumber;

    @Column(name = "tipodocumentoemp", length = 30, updatable = false)
    private String employeeIdNumberType;

    @Column(name = "apellidopaternoemp", length = 25, updatable = false)
    private String employeeLastName;

    @Column(name = "apellidomaternoemp", length = 25, updatable = false)
    private String employeeMaidenName;

    @Column(name = "nombresemp", length = 40, updatable = false)
    private String employeeFirstName;

    @Column(name = "generoemp", length = 1, updatable = false)
    private String employeeGender;

    @Column(name = "gestion", updatable = false)
    private Integer gestion;

    @Column(name = "periodo", updatable = false)
    private Integer period;

    public String getCareerId() {
        return careerId;
    }

    public void setCareerId(String careerId) {
        this.careerId = careerId;
    }

    public String getCareerName() {
        return careerName;
    }

    public void setCareerName(String careerName) {
        this.careerName = careerName;
    }

    public Integer getFacultyId() {
        return facultyId;
    }

    public void setFacultyId(Integer facultyId) {
        this.facultyId = facultyId;
    }

    public String getFacultyName() {
        return facultyName;
    }

    public void setFacultyName(String facultyName) {
        this.facultyName = facultyName;
    }

    public Integer getSeatId() {
        return seatId;
    }

    public void setSeatId(Integer seatId) {
        this.seatId = seatId;
    }

    public String getSeatName() {
        return seatName;
    }

    public void setSeatName(String seatName) {
        this.seatName = seatName;
    }

    public Integer getEmployeeCode() {
        return employeeCode;
    }

    public void setEmployeeCode(Integer employeeCode) {
        this.employeeCode = employeeCode;
    }

    public String getEmployeeIdNumber() {
        return employeeIdNumber;
    }

    public void setEmployeeIdNumber(String employeeIdNumber) {
        this.employeeIdNumber = employeeIdNumber;
    }

    public String getEmployeeIdNumberType() {
        return employeeIdNumberType;
    }

    public void setEmployeeIdNumberType(String employeeIdNumberType) {
        this.employeeIdNumberType = employeeIdNumberType;
    }

    public String getEmployeeLastName() {
        return employeeLastName;
    }

    public void setEmployeeLastName(String employeeLastName) {
        this.employeeLastName = employeeLastName;
    }

    public String getEmployeeMaidenName() {
        return employeeMaidenName;
    }

    public void setEmployeeMaidenName(String employeeMaidenName) {
        this.employeeMaidenName = employeeMaidenName;
    }

    public String getEmployeeFirstName() {
        return employeeFirstName;
    }

    public void setEmployeeFirstName(String employeeFirstName) {
        this.employeeFirstName = employeeFirstName;
    }

    public String getEmployeeGender() {
        return employeeGender;
    }

    public void setEmployeeGender(String employeeGender) {
        this.employeeGender = employeeGender;
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

    @Override
    public String toString() {
        return "AcademicGeneralPlanning{" +
                "employeeCode=" + employeeCode +
                ", employeeIdNumber='" + employeeIdNumber + '\'' +
                ", employeeIdNumberType='" + employeeIdNumberType + '\'' +
                ", employeeLastName='" + employeeLastName + '\'' +
                ", employeeMaidenName='" + employeeMaidenName + '\'' +
                ", employeeFirstName='" + employeeFirstName + '\'' +
                ", employeeGender='" + employeeGender + '\'' +
                ", careerId='" + careerId + '\'' +
                ", careerName='" + careerName + '\'' +
                ", facultyId=" + facultyId +
                ", facultyName='" + facultyName + '\'' +
                ", seatId=" + seatId +
                ", seatName='" + seatName + '\'' +
                ", gestion=" + gestion +
                ", period=" + period +
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

        AcademicGeneralPlanning that = (AcademicGeneralPlanning) o;

        if (careerId != null ? !careerId.equals(that.careerId) : that.careerId != null) {
            return false;
        }
        if (careerName != null ? !careerName.equals(that.careerName) : that.careerName != null) {
            return false;
        }
        if (employeeCode != null ? !employeeCode.equals(that.employeeCode) : that.employeeCode != null) {
            return false;
        }
        if (employeeFirstName != null ? !employeeFirstName.equals(that.employeeFirstName) : that.employeeFirstName != null) {
            return false;
        }
        if (employeeGender != null ? !employeeGender.equals(that.employeeGender) : that.employeeGender != null) {
            return false;
        }
        if (employeeIdNumber != null ? !employeeIdNumber.equals(that.employeeIdNumber) : that.employeeIdNumber != null) {
            return false;
        }
        if (employeeIdNumberType != null ? !employeeIdNumberType.equals(that.employeeIdNumberType) : that.employeeIdNumberType != null) {
            return false;
        }
        if (employeeLastName != null ? !employeeLastName.equals(that.employeeLastName) : that.employeeLastName != null) {
            return false;
        }
        if (employeeMaidenName != null ? !employeeMaidenName.equals(that.employeeMaidenName) : that.employeeMaidenName != null) {
            return false;
        }
        if (facultyId != null ? !facultyId.equals(that.facultyId) : that.facultyId != null) {
            return false;
        }
        if (facultyName != null ? !facultyName.equals(that.facultyName) : that.facultyName != null) {
            return false;
        }
        if (gestion != null ? !gestion.equals(that.gestion) : that.gestion != null) {
            return false;
        }
        if (period != null ? !period.equals(that.period) : that.period != null) {
            return false;
        }
        if (seatId != null ? !seatId.equals(that.seatId) : that.seatId != null) {
            return false;
        }
        if (seatName != null ? !seatName.equals(that.seatName) : that.seatName != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = employeeCode != null ? employeeCode.hashCode() : 0;
        result = 31 * result + (employeeIdNumber != null ? employeeIdNumber.hashCode() : 0);
        result = 31 * result + (employeeIdNumberType != null ? employeeIdNumberType.hashCode() : 0);
        result = 31 * result + (employeeLastName != null ? employeeLastName.hashCode() : 0);
        result = 31 * result + (employeeMaidenName != null ? employeeMaidenName.hashCode() : 0);
        result = 31 * result + (employeeFirstName != null ? employeeFirstName.hashCode() : 0);
        result = 31 * result + (employeeGender != null ? employeeGender.hashCode() : 0);
        result = 31 * result + (careerId != null ? careerId.hashCode() : 0);
        result = 31 * result + (careerName != null ? careerName.hashCode() : 0);
        result = 31 * result + (facultyId != null ? facultyId.hashCode() : 0);
        result = 31 * result + (facultyName != null ? facultyName.hashCode() : 0);
        result = 31 * result + (seatId != null ? seatId.hashCode() : 0);
        result = 31 * result + (seatName != null ? seatName.hashCode() : 0);
        result = 31 * result + (gestion != null ? gestion.hashCode() : 0);
        result = 31 * result + (period != null ? period.hashCode() : 0);
        return result;
    }
}
