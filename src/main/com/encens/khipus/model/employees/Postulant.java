package com.encens.khipus.model.employees;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyListener;
import com.encens.khipus.model.UpperCaseStringListener;
import com.encens.khipus.model.admin.Company;
import com.encens.khipus.model.common.Text;
import com.encens.khipus.model.contacts.Gender;
import org.hibernate.annotations.Filter;
import org.hibernate.validator.Email;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;
import org.hibernate.validator.Range;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author
 */
@NamedQueries(
        {
                @NamedQuery(name = "Postulant.findAll", query = "select o from Postulant o order by o.id"),
                @NamedQuery(name = "Postulant.findAllByLeftJoin", query = "select distinct postulant from Postulant postulant " +
                        "LEFT JOIN postulant.academicFormationList academicFormation " +
                        "LEFT JOIN postulant.experienceList experience " +
                        "LEFT JOIN postulant.subjectList subject")
        })

@TableGenerator(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "Postulant.tableGenerator",
        table = com.encens.khipus.util.Constants.SEQUENCE_TABLE_NAME,
        pkColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_PK_COLUMN_NAME,
        valueColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_VALUE_COLUMN_NAME,
        pkColumnValue = "postulante",
        allocationSize = com.encens.khipus.util.Constants.SEQUENCE_ALLOCATION_SIZE)
@Entity
@Filter(name = com.encens.khipus.util.Constants.COMPANY_FILTER_NAME)
@EntityListeners({CompanyListener.class, UpperCaseStringListener.class})
@Table(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "postulante")
public class Postulant implements BaseModel {

    @Id
    @Column(name = "idpostulante", nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "Postulant.tableGenerator")
    private Long id;

    @Column(name = "apellidopaterno", length = 200)
    @Length(max = 200)
    private String lastName;

    @Column(name = "apellidomaterno", length = 200)
    @Length(max = 200)
    private String maidenName;

    @Column(name = "nombres")
    @Length(max = 250)
    private String firstName;

    @Column(name = "fechanacimiento")
    @Temporal(TemporalType.DATE)
    private Date birthDay;

    @Column(name = "lugarnacimiento")
    @Length(max = 250)
    private String birthPlace;

    @Column(name = "noidentificacion")
    @NotNull
    @Range(min = 9999)
    private Integer idNumber;

    @Column(name = "lugarextension")
    @Length(max = 250)
    private String expendedPlace;

    @Column(name = "email")
    @Email
    @Length(max = 100)
    private String email;

    @Column(name = "telefono")
    @Range(min = 9999)
    private Integer phoneNumber;

    @Column(name = "celular")
    @NotNull
    @Range(min = 9999)
    private Integer cellPhoneNumber;

    @Column(name = "fecharegistro")
    @Temporal(TemporalType.DATE)
    private Date registryDate = new Date();

    @Column(name = "genero")
    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Column(name = "TIPO", nullable = false, length = 20)
    @NotNull
    @Enumerated(EnumType.STRING)
    private PostulantType postulantType;

    @OneToOne(optional = true, fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "idpremiosinternacionales", nullable = true)
    private Text internationalPrise;

    @OneToOne(optional = true, fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "idpremiosnacionales", nullable = true)
    private Text nationalPrise;

    @OneToOne(optional = true, fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "idlibros", nullable = true)
    private Text books;

    @OneToOne(optional = true, fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "idarticulosinternacional", nullable = true)
    private Text internationalArticles;

    @OneToOne(optional = true, fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "idarticulosnacional", nullable = true)
    private Text nationalArticles;

    @Column(name = "salariotiempocompleto", precision = 13, scale = 2)
    private BigDecimal salaryFullTime;

    @Column(name = "salariomediotiempo", precision = 13, scale = 2)
    private BigDecimal salaryMiddleTime;

    @OneToMany(mappedBy = "postulant", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<PostulantAcademicFormation> academicFormationList = new ArrayList<PostulantAcademicFormation>(0);

    @OneToMany(mappedBy = "postulant", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Experience> experienceList = new ArrayList<Experience>(0);

    @OneToMany(mappedBy = "postulant", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<HourAvailable> hourAvailableList = new ArrayList<HourAvailable>(0);

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "postulanteasigna",
            joinColumns = @JoinColumn(name = "idpostulante"),
            inverseJoinColumns = @JoinColumn(name = "idasignatura"),
            schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA
    )
    private List<Subject> subjectList = new ArrayList<Subject>(0);

    @OneToMany(mappedBy = "postulant", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<PostulantCharge> postulantChargeList = new ArrayList<PostulantCharge>(0);

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idcompania", nullable = false, updatable = false, insertable = true)
    @NotNull
    private Company company;

    @Version
    @Column(name = "version", nullable = false)
    private long version;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getBirthPlace() {
        return birthPlace;
    }

    public void setBirthPlace(String birthPlace) {
        this.birthPlace = birthPlace;
    }

    public Integer getIdNumber() {
        return idNumber;
    }

    public void setIdNumber(Integer idNumber) {
        this.idNumber = idNumber;
    }

    public String getExpendedPlace() {
        return expendedPlace;
    }

    public void setExpendedPlace(String expendedPlace) {
        this.expendedPlace = expendedPlace;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Integer getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(Integer phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public Integer getCellPhoneNumber() {
        return cellPhoneNumber;
    }

    public void setCellPhoneNumber(Integer cellPhoneNumber) {
        this.cellPhoneNumber = cellPhoneNumber;
    }

    public Date getRegistryDate() {
        return registryDate;
    }

    public void setRegistryDate(Date registryDate) {
        this.registryDate = registryDate;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public Text getInternationalPrise() {
        return internationalPrise;
    }

    public void setInternationalPrise(Text internationalPrise) {
        this.internationalPrise = internationalPrise;
    }

    public Text getNationalPrise() {
        return nationalPrise;
    }

    public void setNationalPrise(Text nationalPrise) {
        this.nationalPrise = nationalPrise;
    }

    public Text getBooks() {
        return books;
    }

    public void setBooks(Text books) {
        this.books = books;
    }

    public Text getInternationalArticles() {
        return internationalArticles;
    }

    public void setInternationalArticles(Text internationalArticles) {
        this.internationalArticles = internationalArticles;
    }

    public Text getNationalArticles() {
        return nationalArticles;
    }

    public void setNationalArticles(Text nationalArticles) {
        this.nationalArticles = nationalArticles;
    }

    public BigDecimal getSalaryFullTime() {
        return salaryFullTime;
    }

    public void setSalaryFullTime(BigDecimal salaryFullTime) {
        this.salaryFullTime = salaryFullTime;
    }

    public BigDecimal getSalaryMiddleTime() {
        return salaryMiddleTime;
    }

    public void setSalaryMiddleTime(BigDecimal salaryMiddleTime) {
        this.salaryMiddleTime = salaryMiddleTime;
    }

    public List<PostulantAcademicFormation> getAcademicFormationList() {
        return academicFormationList;
    }

    public void setAcademicFormationList(List<PostulantAcademicFormation> academicFormationList) {
        this.academicFormationList = academicFormationList;
    }

    public List<Experience> getExperienceList() {
        return experienceList;
    }

    public void setExperienceList(List<Experience> experienceList) {
        this.experienceList = experienceList;
    }

    public List<HourAvailable> getHourAvailableList() {
        return hourAvailableList;
    }

    public void setHourAvailableList(List<HourAvailable> hourAvailableList) {
        this.hourAvailableList = hourAvailableList;
    }

    public List<Subject> getSubjectList() {
        return subjectList;
    }

    public void setSubjectList(List<Subject> subjectList) {
        this.subjectList = subjectList;
    }

    public List<PostulantCharge> getPostulantChargeList() {
        return postulantChargeList;
    }

    public void setPostulantChargeList(List<PostulantCharge> postulantChargeList) {
        this.postulantChargeList = postulantChargeList;
    }

    public PostulantType getPostulantType() {
        return postulantType;
    }

    public void setPostulantType(PostulantType postulantType) {
        this.postulantType = postulantType;
    }

    public String getFullName() {
        return getLastName() + " " + getMaidenName() + " " + getFirstName();
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }
}