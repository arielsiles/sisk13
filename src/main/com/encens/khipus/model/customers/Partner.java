package com.encens.khipus.model.customers;

import com.encens.khipus.model.UpperCaseStringListener;
import com.encens.khipus.model.admin.Company;
import com.encens.khipus.model.contacts.Department;
import com.encens.khipus.model.contacts.Person;
import com.encens.khipus.model.production.ProductiveZone;
import org.hibernate.validator.NotNull;

import javax.persistence.*;
import javax.persistence.Entity;

/**
 * Customer entity
 *
 * @author
 * @version $Id: Customer.java 2008-9-9 14:17:00 $
 */

@NamedQueries({
        @NamedQuery(name = "Partner.findNumberCredit", query = "select partner.numberCredit from Partner partner where partner.id=:id"),
        @NamedQuery(name = "Partner.findPartnerById", query = "select o from Partner o WHERE o.id=:id order by o.id")
})

@Entity
@Table(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "socio")
@DiscriminatorValue("socio")
@PrimaryKeyJoinColumns(value = {
        @PrimaryKeyJoinColumn(name = "idsocio", referencedColumnName = "idpersona")
})
@EntityListeners(UpperCaseStringListener.class)
public class Partner extends Person {


    @Column(name = "nosocio", length = 100)
    private String number;

    @Column(name = "nsocio", length = 10)
    private String nPartner;

    @Column(name = "estado", nullable = true, length = 3)
    @Enumerated(EnumType.STRING)
    private PartnerState state;

    @Column(name = "nocred", nullable = true)
    private Integer numberCredit = 0;

    @Column(name = "telfijo", nullable = true, length = 20)
    private String phone;

    @Column(name = "telcelular", nullable = true, length = 20)
    private String cellphone;

    @Column(name = "conyuge", nullable = true, length = 100)
    private String spouse;

    @Column(name = "nrohijos", nullable = true)
    private Integer numberChildren;

    @Column(name = "contacto", nullable = true, length = 100)
    private String contactPerson;

    @Column(name = "telcontacto", nullable = true, length = 20)
    private String phoneContact;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idzonaproductiva", referencedColumnName = "idzonaproductiva", nullable = true)
    private ProductiveZone productiveZone;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "iddepartamento", nullable = false)
    private Department department;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idcompania", nullable = false, updatable = false, insertable = true)
    @NotNull
    private Company company;


    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    @Override
    public String toString() {
        return "Partner{" +
                "getId()='" + getId() + '\'' +
                ", getIdNumber='" + getIdNumber() + '\'' +
                ", getLastName='" + getLastName() + '\'' +
                ", getMaidenName='" + getMaidenName() + '\'' +
                ", getFirstName='" + getFirstName() + '\'' +
                '}';
    }

    public PartnerState getState() {
        return state;
    }

    public void setState(PartnerState state) {
        this.state = state;
    }

    public ProductiveZone getProductiveZone() {
        return productiveZone;
    }

    public void setProductiveZone(ProductiveZone productiveZone) {
        this.productiveZone = productiveZone;
    }

    public Integer getNumberChildren() {
        return numberChildren;
    }

    public void setNumberChildren(Integer numberChildren) {
        this.numberChildren = numberChildren;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getCellphone() {
        return cellphone;
    }

    public void setCellphone(String cellphone) {
        this.cellphone = cellphone;
    }

    public String getSpouse() {
        return spouse;
    }

    public void setSpouse(String spouse) {
        this.spouse = spouse;
    }

    public String getContactPerson() {
        return contactPerson;
    }

    public void setContactPerson(String contactPerson) {
        this.contactPerson = contactPerson;
    }

    public String getPhoneContact() {
        return phoneContact;
    }

    public void setPhoneContact(String phoneContact) {
        this.phoneContact = phoneContact;
    }

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    public Integer getNumberCredit() {
        return numberCredit;
    }

    public void setNumberCredit(Integer numberCredit) {
        this.numberCredit = numberCredit;
    }

    public String getnPartner() {
        return nPartner;
    }

    public void setnPartner(String nPartner) {
        this.nPartner = nPartner;
    }
}
