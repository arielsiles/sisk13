package com.encens.khipus.util.academic;

import com.encens.khipus.model.academics.Student;
import com.encens.khipus.model.employees.*;

import java.io.Serializable;

/**
 * AcademicStructure
 *
 * @author
 * @version 2.24
 */
public class AcademicStructure implements Serializable {
    private Location location;
    private Faculty faculty;
    private Career career;
    private Subject subject;
    private AcademicPeriod academicPeriod;
    private Employee employee;
    private Student student;

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public Faculty getFaculty() {
        return faculty;
    }

    public void setFaculty(Faculty faculty) {
        this.faculty = faculty;
    }

    public Career getCareer() {
        return career;
    }

    public void setCareer(Career career) {
        this.career = career;
    }

    public Subject getSubject() {
        return subject;
    }

    public void setSubject(Subject subject) {
        this.subject = subject;
    }

    public AcademicPeriod getAcademicPeriod() {
        return academicPeriod;
    }

    public void setAcademicPeriod(AcademicPeriod academicPeriod) {
        this.academicPeriod = academicPeriod;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public Student getStudent() {
        return student;
    }

    public void setStudent(Student student) {
        this.student = student;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        AcademicStructure that = (AcademicStructure) o;

        if (career != null ? !career.equals(that.career) : that.career != null) {
            return false;
        }
        if (employee != null ? !employee.equals(that.employee) : that.employee != null) {
            return false;
        }
        if (faculty != null ? !faculty.equals(that.faculty) : that.faculty != null) {
            return false;
        }
        if (location != null ? !location.equals(that.location) : that.location != null) {
            return false;
        }
        if (student != null ? !student.equals(that.student) : that.student != null) {
            return false;
        }
        if (subject != null ? !subject.equals(that.subject) : that.subject != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = location != null ? location.hashCode() : 0;
        result = 31 * result + (faculty != null ? faculty.hashCode() : 0);
        result = 31 * result + (career != null ? career.hashCode() : 0);
        result = 31 * result + (subject != null ? subject.hashCode() : 0);
        result = 31 * result + (employee != null ? employee.hashCode() : 0);
        result = 31 * result + (student != null ? student.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "AcademicStructure{" +
                "location=" + location +
                ", faculty=" + faculty +
                ", career=" + career +
                ", subject=" + subject +
                ", employee=" + employee +
                ", student=" + student +
                '}';
    }
}
