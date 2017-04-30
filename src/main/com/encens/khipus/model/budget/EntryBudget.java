package com.encens.khipus.model.budget;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyListener;
import com.encens.khipus.model.UpperCaseStringListener;
import com.encens.khipus.model.admin.BusinessUnit;
import com.encens.khipus.model.admin.Company;
import com.encens.khipus.model.admin.User;
import com.encens.khipus.model.employees.Gestion;
import com.encens.khipus.model.finances.CostCenter;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.Type;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;
import org.jboss.seam.Component;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

/**
 * This class enclose the types
 *
 * @author
 * @version 2.0
 */
@TableGenerator(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "EntryBudget.tableGenerator",
        table = com.encens.khipus.util.Constants.SEQUENCE_TABLE_NAME,
        pkColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_PK_COLUMN_NAME,
        valueColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_VALUE_COLUMN_NAME,
        pkColumnValue = "entryBudget",
        allocationSize = com.encens.khipus.util.Constants.SEQUENCE_ALLOCATION_SIZE)

@Entity
@Filter(name = com.encens.khipus.util.Constants.COMPANY_FILTER_NAME)
@EntityListeners({CompanyListener.class, UpperCaseStringListener.class})
@Table(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "presupuestoingreso", uniqueConstraints = @UniqueConstraint(columnNames = {"idcompania", "idunidadnegocio", "codigocencos", "idgestion", "idclasificador"}))
public class EntryBudget implements BaseModel {
    @Id
    @Column(name = "idpresupuestoingreso", nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "EntryBudget.tableGenerator")
    private Long id;

    @Column(name = "importe", nullable = false, precision = 13, scale = 2)
    private BigDecimal amount;

    @Column(name = "estado", length = 30, nullable = false)
    @Enumerated(EnumType.STRING)
    private BudgetState state;

    @Column(name = "editable", nullable = false)
    @Type(type = com.encens.khipus.model.usertype.IntegerBooleanUserType.NAME)
    private Boolean editable = true;

    @Column(name = "fechacreacion", nullable = false, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date creationDate = new Date();

    @Column(name = "fechamodificacion")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updateDate;

    @Version
    @Column(name = "version", nullable = false)
    private long version;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idcompania", nullable = false, updatable = false)
    @NotNull
    private Company company;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idusuariocreador", nullable = false, updatable = false)
    @NotNull
    private User creatorUser;

    @ManyToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "idusuarioeditor")
    private User updaterUser;

    @ManyToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "idunidadnegocio", nullable = false)
    @NotNull
    private BusinessUnit businessUnit;

    @ManyToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "numerocompania", referencedColumnName = "no_cia", updatable = false, insertable = false),
            @JoinColumn(name = "codigocencos", referencedColumnName = "cod_cc", updatable = false, insertable = false)
    })
    private CostCenter costCenter;

    @Column(name = "numerocompania", updatable = false)
    @Length(max = 2)
    private String companyNumber;

    @Column(name = "codigocencos", length = 6, nullable = false)
    @Length(max = 6)
    private String costCenterCode;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idgestion", nullable = false)
    @NotNull
    private Gestion gestion;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idclasificador", nullable = false)
    @NotNull
    private Classifier classifier;

    @OneToOne(fetch = FetchType.LAZY, cascade = {})
    @JoinColumn(name = "iddistpresupuesto", referencedColumnName = "iddistpresupuesto")
    private BudgetDistribution budgetDistribution;

    public EntryBudget() {
    }

    public EntryBudget(EntryBudget entryBudget) {
        setBusinessUnit(entryBudget.getBusinessUnit());
        setCostCenter(entryBudget.getCostCenter());
        setGestion(entryBudget.getGestion());
    }

    @PrePersist
    private void defineCreateValues() {
        setCreatorUser((User) Component.getInstance("currentUser"));
        setCreationDate(new Date());
    }

    @PreUpdate
    private void defineUpdateValues() {
        setUpdaterUser((User) Component.getInstance("currentUser"));
        setUpdateDate(new Date());
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public BudgetState getState() {
        return state;
    }

    public void setState(BudgetState state) {
        this.state = state;
    }

    public Boolean getEditable() {
        return editable;
    }

    public void setEditable(Boolean editable) {
        this.editable = editable;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public Date getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(Date updateDate) {
        this.updateDate = updateDate;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public User getCreatorUser() {
        return creatorUser;
    }

    public void setCreatorUser(User creatorUser) {
        this.creatorUser = creatorUser;
    }

    public User getUpdaterUser() {
        return updaterUser;
    }

    public void setUpdaterUser(User updaterUser) {
        this.updaterUser = updaterUser;
    }

    public BusinessUnit getBusinessUnit() {
        return businessUnit;
    }

    public void setBusinessUnit(BusinessUnit businessUnit) {
        this.businessUnit = businessUnit;
    }

    public CostCenter getCostCenter() {
        return costCenter;
    }

    public void setCostCenter(CostCenter costCenter) {
        this.costCenter = costCenter;
        setCompanyNumber(costCenter != null ? costCenter.getCompanyNumber() : null);
        setCostCenterCode(costCenter != null ? costCenter.getCode() : null);
    }

    public String getCompanyNumber() {
        return companyNumber;
    }

    public void setCompanyNumber(String companyNumber) {
        this.companyNumber = companyNumber;
    }

    public String getCostCenterCode() {
        return costCenterCode;
    }

    public void setCostCenterCode(String costCenterCode) {
        this.costCenterCode = costCenterCode;
    }

    public Gestion getGestion() {
        return gestion;
    }

    public void setGestion(Gestion gestion) {
        this.gestion = gestion;
    }

    public Classifier getClassifier() {
        return classifier;
    }

    public void setClassifier(Classifier classifier) {
        this.classifier = classifier;
    }

    public BudgetDistribution getBudgetDistribution() {
        return budgetDistribution;
    }

    public void setBudgetDistribution(BudgetDistribution budgetDistribution) {
        this.budgetDistribution = budgetDistribution;
    }
}
