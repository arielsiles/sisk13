package com.encens.khipus.model.customers;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyListener;
import com.encens.khipus.model.UpperCaseStringListener;
import com.encens.khipus.model.admin.Company;
import com.encens.khipus.model.contacts.*;
import com.encens.khipus.model.production.ProductiveZone;
import org.hibernate.annotations.Filter;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;

import javax.persistence.*;
import javax.persistence.Entity;
import java.util.Date;

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

@TableGenerator(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "Partner.tableGenerator",
        table = com.encens.khipus.util.Constants.SEQUENCE_TABLE_NAME,
        pkColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_PK_COLUMN_NAME,
        valueColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_VALUE_COLUMN_NAME,
        pkColumnValue = "socio",
        allocationSize = com.encens.khipus.util.Constants.SEQUENCE_ALLOCATION_SIZE)

@Entity
@Filter(name = com.encens.khipus.util.Constants.COMPANY_FILTER_NAME)
@EntityListeners({CompanyListener.class, UpperCaseStringListener.class})
@Table(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "socio")
public class Partner implements BaseModel {

    @Id
    @Column(name = "idsocio", nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "Partner.tableGenerator")
    private Long id;

    @Column(name = "noidentificacion", nullable = false, length = 100)
    @Length(max = 100)
    private String idNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idexttipodocumento", referencedColumnName = "IDEXTTIPODOCUMENTO")
    private Extension extensionSite;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idtipodocumento", nullable = false)
    private DocumentType documentType;

    @Column(name = "apellidopaterno", length = 200)
    private String lastName;

    @Column(name = "apellidomaterno", length = 200)
    private String maidenName;

    @Column(name = "nombres", nullable = false)
    @NotNull
    private String firstName;

    @Column(name = "fechanacimiento")
    @Temporal(TemporalType.DATE)
    private Date birthDay;

    @Column(name = "profesion", length = 100)
    private String profession;

    @ManyToOne
    @JoinColumn(name = "idpersona", nullable = true)
    private Person person;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idsaludo")
    private Salutation salutation;

    @Column(name = "genero")
    @Enumerated(EnumType.STRING)
    private Gender gender;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idestadocivil")
    private MaritalStatus maritalStatus;

    @Column(name = "domicilio", length = 500)
    @Length(max = 500)
    private String homeAddress;

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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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
        return "";
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


    public String getIdNumber() {
        return idNumber;
    }

    public void setIdNumber(String idNumber) {
        this.idNumber = idNumber;
    }

    public Extension getExtensionSite() {
        return extensionSite;
    }

    public void setExtensionSite(Extension extensionSite) {
        this.extensionSite = extensionSite;
    }

    public DocumentType getDocumentType() {
        return documentType;
    }

    public void setDocumentType(DocumentType documentType) {
        this.documentType = documentType;
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

    public String getFullName() {
        return (firstName != null ? firstName + " " : "") + (lastName != null ? lastName + " " : "") + (maidenName != null ? maidenName : "");
    }

    public String getFirstAndMaidenName() {
        return (firstName != null ? firstName + " " : "") + (lastName != null ? lastName : "");
    }

    public Person getPerson() {
        return person;
    }

    public void setPerson(Person person) {
        this.person = person;
    }
}
