package com.encens.khipus.model.admin;


import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyListener;
import com.encens.khipus.model.employees.Employee;
import com.encens.khipus.model.finances.UserCashBox;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;
import org.hibernate.validator.Email;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Class that represents the user model entity
 *
 * @author
 * @version 1.0
 */


@NamedQueries(
        {
                @NamedQuery(name = "User.findByUsernameAndPasswordAndCompany", query = "select u from User u " +
                        "where u.username=:username and u.password=:password " +
                        "and u.company.login=:companyLogin "),
                @NamedQuery(name = "User.findByIdAndPassword", query = "select u from User u " +
                        "where u.id=:id and u.password=:password")}
)

@Entity
@GenericGenerator(name = "foreign", strategy = "foreign", parameters = {
        @Parameter(name = "property", value = "employee")})

@Table(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "usuario", uniqueConstraints = @UniqueConstraint(columnNames = {"idcompania", "usuario"}))
@Filter(name = com.encens.khipus.util.Constants.COMPANY_FILTER_NAME)
@EntityListeners(CompanyListener.class)
@Name("currentUser")
@Scope(ScopeType.SESSION)
public class User implements BaseModel {

    @Id
    @GeneratedValue(generator = "foreign")
    @Column(name = "idusuario")
    private Long id;

    @OneToOne
    @PrimaryKeyJoinColumn(name = "idusuario")
    private Employee employee;

    @Column(name = "usuario", length = 50)
    @Length(max = 50)
    @NotNull
    private String username;

    @Column(name = "clave", nullable = false, length = 200)
    @Length(min = 6, max = 200)
    @NotNull
    private String password;

    @Transient
    private String confirmPassword;

    @Transient
    private String previousPassword;

    @Column(name = "email")
    @Email
    @Length(max = 100)
    private String email;

    @Column(name = "fechacreacion", nullable = false, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date creationDate = new Date();


    @Column(name = "numerousuario", length = 4)
    @Length(max = 4)
    private String financesCode;

    @Column(name = "usuariofinanzas")
    @Type(type = com.encens.khipus.model.usertype.IntegerBooleanUserType.NAME)
    private Boolean financesUser;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "usuariorol",
            joinColumns = @JoinColumn(name = "idusuario"),
            inverseJoinColumns = @JoinColumn(name = "idrol"),
            schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA
    )
    @Filter(name = com.encens.khipus.util.Constants.COMPANY_FILTER_NAME)
    @OrderBy("name asc")
    private List<Role> roles;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
    @Filter(name = com.encens.khipus.util.Constants.COMPANY_FILTER_NAME)
    private List<UserCashBox> userCashBoxList = new ArrayList<UserCashBox>(0);

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private List<UserBusinessUnit> businessUnits;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idcompania", nullable = false, updatable = false, insertable = true)
    private Company company;


    @Version
    @Column(name = "version", nullable = false)
    private long version;


    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public List<Role> getRoles() {
        return roles;
    }

    public void setRoles(List<Role> roles) {
        this.roles = roles;
    }

    public List<UserCashBox> getUserCashBoxList() {
        return userCashBoxList;
    }

    public void setUserCashBoxList(List<UserCashBox> userCashBoxList) {
        this.userCashBoxList = userCashBoxList;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public String getFinancesCode() {
        return financesCode;
    }

    public void setFinancesCode(String financesCode) {
        this.financesCode = financesCode;
    }

    public Boolean getFinancesUser() {
        return financesUser;
    }

    public void setFinancesUser(Boolean financesUser) {
        this.financesUser = financesUser;
    }

    public String getPreviousPassword() {
        return previousPassword;
    }

    public void setPreviousPassword(String previousPassword) {
        this.previousPassword = previousPassword;
    }

    public List<UserBusinessUnit> getBusinessUnits() {
        return businessUnits;
    }

    public void setBusinessUnits(List<UserBusinessUnit> businessUnits) {
        this.businessUnits = businessUnits;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof User && this.getId().equals(((User) obj).getId());
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
