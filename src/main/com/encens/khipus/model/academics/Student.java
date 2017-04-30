package com.encens.khipus.model.academics;

import com.encens.khipus.model.UpperCaseStringListener;
import com.encens.khipus.model.admin.Company;
import com.encens.khipus.model.contacts.Person;
import com.encens.khipus.util.Constants;
import org.hibernate.validator.Length;

import javax.persistence.*;

/**
 * Student
 *
 * @author
 * @version 2.24
 */

@NamedQueries({
        @NamedQuery(name = "Student.findByCode", query = "select s from Student s where s.studentCode=:studentCode order by s.id")
})
@Entity
@Table(schema = Constants.KHIPUS_SCHEMA, name = "estudiante")
@PrimaryKeyJoinColumns(value = {
        @PrimaryKeyJoinColumn(name = "idestudiante", referencedColumnName = "idpersona")
})
@EntityListeners(UpperCaseStringListener.class)
public class Student extends Person {

    @Column(name = "codigoestudiante", length = 20)
    @Length(max = 20)
    private String studentCode;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idcompania", nullable = false, updatable = false, insertable = true)
    private Company company;

    public String getStudentCode() {
        return studentCode;
    }

    public void setStudentCode(String studentCode) {
        this.studentCode = studentCode;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    @Override
    public String toString() {
        return "Student{" +
                "getId()='" + getId() + '\'' +
                ", getIdNumber='" + getIdNumber() + '\'' +
                ", getLastName='" + getLastName() + '\'' +
                ", getMaidenName='" + getMaidenName() + '\'' +
                ", getFirstName='" + getFirstName() + '\'' +
                "studentCode='" + studentCode + '\'' +
                '}';
    }
}
