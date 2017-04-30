package com.encens.khipus.model.contacts;

import com.encens.khipus.model.UpperCaseStringListener;
import com.encens.khipus.model.admin.Company;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;

import javax.persistence.*;
import java.util.Date;

/**
 * @author
 * @version $Id: Person.java 2008-8-28 11:36:14 $
 */


@Table(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "persona")
@javax.persistence.Entity
@PrimaryKeyJoinColumns(value = {
        @PrimaryKeyJoinColumn(name = "idpersona", referencedColumnName = "identidad")/*,
    @PrimaryKeyJoinColumn(name = "NO_IDENTIFICACION", referencedColumnName = "NO_IDENTIFICACION")*/
})

@DiscriminatorValue("persona")
@EntityListeners(UpperCaseStringListener.class)
public class Person extends Entity {

    @Column(name = "apellidopaterno", length = 200)
    protected String lastName;

    @Column(name = "apellidomaterno", length = 200)
    protected String maidenName;

    @Column(name = "nombres", nullable = false)
    @NotNull
    protected String firstName;

    @Column(name = "fechanacimiento")
    @Temporal(TemporalType.DATE)
    protected Date birthDay;

    @Column(name = "profesion", length = 100)
    protected String profession;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idcompania", nullable = false, updatable = false, insertable = true)
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idsaludo")
    private Salutation salutation;

    @Column(name = "genero")
    @Enumerated(EnumType.STRING)
    private Gender gender;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idpais")
    private Country country;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idestadocivil")
    private MaritalStatus maritalStatus;

    @Column(name = "domicilio", length = 500)
    @Length(max = 500)
    protected String homeAddress;


    public Company getCompany() {
        return company;
    }


    public void setCompany(Company company) {
        this.company = company;
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

    public Date getBirthDay() {
        return birthDay;
    }

    public void setBirthDay(Date birthDay) {
        this.birthDay = birthDay;
    }

    public String getProfession() {
        return profession;
    }

    public void setProfession(String profession) {
        this.profession = profession;
    }

    public String getFullName() {
        return (lastName != null ? lastName + " " : "") + (maidenName != null ? maidenName + " " : "") + (firstName != null ? firstName : "");
    }

    public String getSingleFullName() {
        return (firstName != null ? firstName + " " : "") + (lastName != null ? lastName + " " : "") + (maidenName != null ? maidenName : "");
    }

    public String getIdNumberAndFullName() {
        return getIdNumber() + " - " + (lastName != null ? lastName + " " : "") + (maidenName != null ? maidenName + " " : "") + (firstName != null ? firstName : "");
    }

    public String getName() {
        return getFullName();
    }

    public Salutation getSalutation() {
        return salutation;
    }

    public void setSalutation(Salutation salutation) {
        this.salutation = salutation;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public Country getCountry() {
        return country;
    }

    public void setCountry(Country country) {
        this.country = country;
    }

    public MaritalStatus getMaritalStatus() {
        return maritalStatus;
    }

    public void setMaritalStatus(MaritalStatus maritalStatus) {
        this.maritalStatus = maritalStatus;
    }

    public String getHomeAddress() {
        return homeAddress;
    }

    public void setHomeAddress(String homeAddress) {
        this.homeAddress = homeAddress;
    }
}
