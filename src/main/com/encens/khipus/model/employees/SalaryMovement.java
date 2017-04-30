package com.encens.khipus.model.employees;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyListener;
import com.encens.khipus.model.UpperCaseStringListener;
import com.encens.khipus.model.admin.Company;
import com.encens.khipus.model.admin.User;
import org.hibernate.annotations.Filter;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;
import org.jboss.seam.Component;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

/**
 * Entity for Incomes and outcomes
 *
 * @author
 */

@NamedQueries(
        {
                @NamedQuery(name = "SalaryMovement.loadSalaryMovement", query = "select salaryMovement from SalaryMovement salaryMovement" +
                        " left join fetch salaryMovement.gestionPayroll gestionPayroll" +
                        " left join fetch salaryMovement.currency currency" +
                        " left join fetch salaryMovement.employee employee" +
                        " left join fetch salaryMovement.salaryMovementType salaryMovementType" +
                        " where salaryMovement.id=:id"),
                @NamedQuery(name = "SalaryMovement.findByEmployeeAndInitDateEndDate", query = "select o from SalaryMovement o " +
                        "where o.employee=:employee and o.date >=:initDate and o.date <=:endDate"),
                @NamedQuery(name = "SalaryMovement.findByEmployeeAndGestionPayroll",
                        query = "select salaryMovement from SalaryMovement salaryMovement" +
                                " left join fetch salaryMovement.salaryMovementType salaryMovementType" +
                                " left join fetch salaryMovement.currency currency" +
                                " where salaryMovement.employee=:employee and salaryMovement.gestionPayroll=:gestionPayroll"),
                @NamedQuery(name = "SalaryMovement.findByManagersPayrollIdList",
                        query = "select salaryMovement from SalaryMovement salaryMovement" +
                                " left join fetch salaryMovement.salaryMovementType salaryMovementType" +
                                " left join fetch salaryMovementType.cashAccount cashAccount" +
                                " left join fetch salaryMovement.currency currency" +
                                " left join fetch salaryMovement.employee employee" +
                                " where salaryMovement.gestionPayroll=:gestionPayroll" +
                                " and salaryMovementType.movementType in (:movementTypeList)" +
                                " and salaryMovement.employee.id in(SELECT element.employee.id FROM ManagersPayroll element WHERE element.id IN (:payrollGenerationIdList))"),
                @NamedQuery(name = "SalaryMovement.findByGeneralPayrollIdList",
                        query = "select salaryMovement from SalaryMovement salaryMovement" +
                                " left join fetch salaryMovement.salaryMovementType salaryMovementType" +
                                " left join fetch salaryMovementType.cashAccount cashAccount" +
                                " left join fetch salaryMovement.currency currency" +
                                " left join fetch salaryMovement.employee employee" +
                                " where salaryMovement.gestionPayroll=:gestionPayroll" +
                                " and salaryMovementType.movementType in (:movementTypeList)" +
                                " and salaryMovement.employee.id in(SELECT element.employee.id FROM GeneralPayroll element WHERE element.id IN (:payrollGenerationIdList))"),
                @NamedQuery(name = "SalaryMovement.findByFiscalProfessorPayrollIdList",
                        query = "select salaryMovement from SalaryMovement salaryMovement" +
                                " left join fetch salaryMovement.salaryMovementType salaryMovementType" +
                                " left join fetch salaryMovementType.cashAccount cashAccount" +
                                " left join fetch salaryMovement.currency currency" +
                                " left join fetch salaryMovement.employee employee" +
                                " where salaryMovement.gestionPayroll=:gestionPayroll" +
                                " and salaryMovementType.movementType in (:movementTypeList)" +
                                " and salaryMovement.employee.id in(SELECT element.employee.id FROM FiscalProfessorPayroll element WHERE element.id IN (:payrollGenerationIdList))"),
                @NamedQuery(name = "SalaryMovement.deleteSalaryMovementByMovementTypeAndEmployeeAndGestionPayroll",
                        query = "delete from SalaryMovement salaryMovement" +
                                " where salaryMovement.salaryMovementType.id in (select salaryMovementType.id from SalaryMovementType salaryMovementType where salaryMovementType.movementType=:movementType) and salaryMovement.employee=:employee and salaryMovement.gestionPayroll=:gestionPayroll"),
                @NamedQuery(name = "SalaryMovement.findSalaryMovementByMovementTypeAndEmployeeAndGestionPayroll",
                        query = "select salaryMovement from SalaryMovement salaryMovement" +
                                " where salaryMovement.salaryMovementType.movementType=:movementType and salaryMovement.employee=:employee and salaryMovement.gestionPayroll=:gestionPayroll")
        }
)

@TableGenerator(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "SalaryMovement.tableGenerator",
        table = com.encens.khipus.util.Constants.SEQUENCE_TABLE_NAME,
        pkColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_PK_COLUMN_NAME,
        valueColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_VALUE_COLUMN_NAME,
        pkColumnValue = "movimientosueldo",
        allocationSize = com.encens.khipus.util.Constants.SEQUENCE_ALLOCATION_SIZE)

@Entity
@Filter(name = com.encens.khipus.util.Constants.COMPANY_FILTER_NAME)
@EntityListeners({CompanyListener.class, UpperCaseStringListener.class})
@Table(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "movimientosueldo")
public class SalaryMovement implements BaseModel {

    @Id
    @Column(name = "idmovimientosueldo", nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "SalaryMovement.tableGenerator")
    private Long id;

    @Column(name = "fecha", nullable = false)
    @Temporal(TemporalType.DATE)
    private Date date;

    @Column(name = "descripcion", length = 200)
    @Length(max = 200)
    private String description;

    @Column(name = "cantidad", nullable = false, precision = 13, scale = 2)
    private BigDecimal amount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idgestionplanilla", updatable = false)
    private GestionPayroll gestionPayroll;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idmoneda", nullable = false)
    private Currency currency;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idtipomovimientosueldo", nullable = false)
    private SalaryMovementType salaryMovementType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idempleado", nullable = false)
    private Employee employee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idusuariocreador", updatable = false)
    private User creatorUser;

    @Column(name = "fechacreacion", updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date creationDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idusuarioeditor")
    private User updaterUser;

    @Column(name = "fechamodificacion")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updateDate;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idcompania", nullable = false, updatable = false, insertable = true)
    @NotNull
    private Company company;

    @Version
    @Column(name = "version", nullable = false)
    private long version;


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

    public SalaryMovement() {
    }

    public SalaryMovement(Date date, String description, BigDecimal amount, GestionPayroll gestionPayroll, Currency currency, SalaryMovementType salaryMovementType, Employee employee) {
        this.date = date;
        this.description = description;
        this.amount = amount;
        this.gestionPayroll = gestionPayroll;
        this.currency = currency;
        this.salaryMovementType = salaryMovementType;
        this.employee = employee;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public GestionPayroll getGestionPayroll() {
        return gestionPayroll;
    }

    public void setGestionPayroll(GestionPayroll gestionPayroll) {
        this.gestionPayroll = gestionPayroll;
    }

    public Currency getCurrency() {
        return currency;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }

    public SalaryMovementType getSalaryMovementType() {
        return salaryMovementType;
    }

    public void setSalaryMovementType(SalaryMovementType salaryMovementType) {
        this.salaryMovementType = salaryMovementType;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
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