package com.encens.khipus.model.finances;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyListener;
import com.encens.khipus.model.UpperCaseStringListener;
import com.encens.khipus.model.admin.BusinessUnit;
import com.encens.khipus.model.admin.Company;
import com.encens.khipus.model.admin.User;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.Type;
import org.hibernate.validator.NotNull;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Entity for Cash box
 *
 * @author:
 */

@TableGenerator(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "CashBox.tableGenerator",
        table = com.encens.khipus.util.Constants.SEQUENCE_TABLE_NAME,
        pkColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_PK_COLUMN_NAME,
        valueColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_VALUE_COLUMN_NAME,
        pkColumnValue = "caja",
        allocationSize = com.encens.khipus.util.Constants.SEQUENCE_ALLOCATION_SIZE)

@Entity
@Filter(name = com.encens.khipus.util.Constants.COMPANY_FILTER_NAME)
@EntityListeners({CompanyListener.class, UpperCaseStringListener.class})
@Table(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "caja", uniqueConstraints = @UniqueConstraint(columnNames = {"idcompania", "descripcion"}))
public class CashBox implements BaseModel {

    @Id
    @Column(name = "idcaja", nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "CashBox.tableGenerator")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "idtipocaja", nullable = false)
    private CashBoxType type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idusuario", referencedColumnName = "idusuario")
    private User user;

    @Column(name = "descripcion", nullable = false, length = 150)
    private String description;

    @Column(name = "fechacreacion", nullable = false, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date creationDate = new Date();

    @Column(name = "fechaestado")
    @Temporal(TemporalType.TIMESTAMP)
    private Date stateDate;

    @Column(name = "autorizacionrequerida", nullable = false)
    @Type(type = com.encens.khipus.model.usertype.IntegerBooleanUserType.NAME)
    private Boolean requiredAuthorization = false;

    @Column(name = "estado", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private CashBoxState state;

    @ManyToOne
    @JoinColumn(name = "idunidadnegocio", referencedColumnName = "idunidadnegocio")
    private BusinessUnit businessUnit;

    @OneToMany(mappedBy = "cashBox", fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
    @Filter(name = com.encens.khipus.util.Constants.COMPANY_FILTER_NAME)
    private List<UserCashBox> userCashBoxList = new ArrayList<UserCashBox>(0);

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

    public CashBoxType getType() {
        return type;
    }

    public void setType(CashBoxType type) {
        this.type = type;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public Date getStateDate() {
        return stateDate;
    }

    public void setStateDate(Date stateDate) {
        this.stateDate = stateDate;
    }

    public Boolean getRequiredAuthorization() {
        return requiredAuthorization;
    }

    public void setRequiredAuthorization(Boolean requiredAuthorization) {
        this.requiredAuthorization = requiredAuthorization;
    }

    public CashBoxState getState() {
        return state;
    }

    public void setState(CashBoxState state) {
        this.state = state;
    }

    public BusinessUnit getBusinessUnit() {
        return businessUnit;
    }

    public void setBusinessUnit(BusinessUnit businessUnit) {
        this.businessUnit = businessUnit;
    }

    public List<UserCashBox> getUserCashBoxList() {
        return userCashBoxList;
    }

    public void setUserCashBoxList(List<UserCashBox> userCashBoxList) {
        this.userCashBoxList = userCashBoxList;
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
