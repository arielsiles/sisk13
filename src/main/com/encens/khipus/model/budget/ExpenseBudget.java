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
 * ExpenseBudget
 *
 * @author
 * @version 2.0
 */
@NamedQueries({
        @NamedQuery(name = "ExpenseBudget.sumAmountsByActivity", query = "select sum(e.amount) from ExpenseBudget e where e.budgetActivity=:budgetActivity"),
        @NamedQuery(name = "ExpenseBudget.findByProgram", query = "select e from ExpenseBudget e  where e.budgetActivity.budgetProgram=:budgetProgram"),
        @NamedQuery(name = "ExpenseBudget.sumAccumulatedExecutionBetween", query = "select SUM(amd.amount) " +
                "from AccountingMovementDetail amd LEFT JOIN amd.accountingMovement am LEFT JOIN amd.account account " +
                "where amd.costCenterCode=:costCenterCode and amd.executorUnitCode=:executorUnitCode and " +
                "am.recordDate<:endMovementDateParam and am.recordDate>=:startMovementDateParam and " +
                "account.accountCode IN " +
                " (select ca.accountCode from ExpenseBudget eb" +
                " LEFT JOIN eb.businessUnit bu" +
                " LEFT JOIN eb.classifier cl" +
                " LEFT JOIN cl.classifierAccountList ca " +
                " where eb.id=:expenseBudgetIdParam and eb.costCenterCode=:costCenterCode and bu.executorUnitCode=:executorUnitCode)"),
        @NamedQuery(name = "ExpenseBudget.sumAccumulatedExecutionByBusinessUnit", query = "select SUM(amd.amount) " +
                "from AccountingMovementDetail amd LEFT JOIN amd.accountingMovement am LEFT JOIN amd.account account " +
                "where amd.executorUnitCode=:executorUnitCode and " +
                "am.recordDate<:endMovementDate and am.recordDate>=:startMovementDate and " +
                "account.accountCode IN " +
                " (select ca.accountCode from Classifier cl" +
                " LEFT JOIN cl.classifierAccountList ca " +
                " where cl.id=:classifierId)"),
        @NamedQuery(name = "ExpenseBudget.sumAccumulatedExecutionByClassifier", query = "select SUM(amd.amount) " +
                "from AccountingMovementDetail amd LEFT JOIN amd.accountingMovement am LEFT JOIN amd.account account " +
                "where am.recordDate<:endMovementDate and am.recordDate>=:startMovementDate and " +
                "account.accountCode IN " +
                " (select ca.accountCode from Classifier cl" +
                " LEFT JOIN cl.classifierAccountList ca " +
                " where cl.id=:classifierId)"),
        @NamedQuery(name = "ExpenseBudget.sumExpenseBudgetByBusinessUnit", query = "select sum(e.amount) " +
                " from ExpenseBudget e " +
                " where e.businessUnit.id=:businessUnitId and e.classifier.id=:classifierId and e.gestion.id=:gestionId"),
        @NamedQuery(name = "ExpenseBudget.sumExpenseBudgetByClassifier", query = "select sum(e.amount) " +
                " from ExpenseBudget e " +
                " where e.classifier.id=:classifierId and e.gestion.id=:gestionId")
})
@TableGenerator(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "ExpenseBudget.tableGenerator",
        table = com.encens.khipus.util.Constants.SEQUENCE_TABLE_NAME,
        pkColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_PK_COLUMN_NAME,
        valueColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_VALUE_COLUMN_NAME,
        pkColumnValue = "presupuestogasto",
        allocationSize = com.encens.khipus.util.Constants.SEQUENCE_ALLOCATION_SIZE)
@Entity
@Filter(name = com.encens.khipus.util.Constants.COMPANY_FILTER_NAME)
@EntityListeners({CompanyListener.class, UpperCaseStringListener.class})
@Table(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "presupuestogasto", uniqueConstraints = @UniqueConstraint(columnNames = {"idcompania", "idunidadnegocio", "codigocencos", "idgestion", "idactividad", "idclasificador"}))
public class ExpenseBudget implements BaseModel {

    @Id
    @Column(name = "idpresupuesto", nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "ExpenseBudget.tableGenerator")
    private Long id;

    @Column(name = "importe", precision = 13, scale = 2, nullable = false)
    @NotNull
    private BigDecimal amount;

    @Column(name = "editable", nullable = false)
    @Type(type = com.encens.khipus.model.usertype.IntegerBooleanUserType.NAME)
    private Boolean editable = true;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idactividad", nullable = false)
    @NotNull
    private BudgetActivity budgetActivity;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idclasificador", nullable = false)
    @NotNull
    private Classifier classifier;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idusuariocreador", nullable = false, updatable = false)
    @NotNull
    private User creatorUser;

    @Column(name = "fechacreacion", nullable = false, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    @NotNull
    private Date creationDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idusuarioeditor")
    private User updaterUser;

    @Column(name = "fechamodificacion")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updateDate;

    @Column(name = "estado", length = 30, nullable = false)
    @Enumerated(EnumType.STRING)
    private BudgetState state;

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

    @OneToOne(fetch = FetchType.LAZY, cascade = {})
    @JoinColumn(name = "iddistpresupuesto", referencedColumnName = "iddistpresupuesto")
    private BudgetDistribution budgetDistribution;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idcompania", nullable = false, updatable = false, insertable = true)
    @NotNull
    private Company company;

    @Version
    @Column(name = "version", nullable = false)
    private long version;

    public ExpenseBudget() {
    }

    public ExpenseBudget(ExpenseBudget expenseBudget) {
        setBudgetActivity(expenseBudget.getBudgetActivity());
        setGestion(expenseBudget.getGestion());
        setBusinessUnit(expenseBudget.getBusinessUnit());
        setCostCenter(expenseBudget.getCostCenter());
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

    public Boolean getEditable() {
        return editable;
    }

    public void setEditable(Boolean editable) {
        this.editable = editable;
    }

    public BudgetActivity getBudgetActivity() {
        return budgetActivity;
    }

    public void setBudgetActivity(BudgetActivity budgetActivity) {
        this.budgetActivity = budgetActivity;
    }

    public Classifier getClassifier() {
        return classifier;
    }

    public void setClassifier(Classifier classifier) {
        this.classifier = classifier;
    }

    public User getCreatorUser() {
        return creatorUser;
    }

    public void setCreatorUser(User creatorUser) {
        this.creatorUser = creatorUser;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public User getUpdaterUser() {
        return updaterUser;
    }

    public void setUpdaterUser(User updaterUser) {
        this.updaterUser = updaterUser;
    }

    public Date getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(Date updateDate) {
        this.updateDate = updateDate;
    }

    public BudgetState getState() {
        return state;
    }

    public void setState(BudgetState state) {
        this.state = state;
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

    public BudgetDistribution getBudgetDistribution() {
        return budgetDistribution;
    }

    public void setBudgetDistribution(BudgetDistribution budgetDistribution) {
        this.budgetDistribution = budgetDistribution;
    }

    @Override
    public String toString() {
        return "ExpenseBudget{" +
                "id=" + id +
                ", amount=" + amount +
                ", editable=" + editable +
                ", budgetActivity=" + budgetActivity +
                ", classifier=" + classifier +
                ", creatorUser=" + creatorUser +
                ", creationDate=" + creationDate +
                ", updaterUser=" + updaterUser +
                ", updateDate=" + updateDate +
                ", gestion=" + gestion +
                ", company=" + company +
                ", version=" + version +
                '}';
    }
}
