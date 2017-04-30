package com.encens.khipus.model.employees;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyListener;
import com.encens.khipus.model.UpperCaseStringListener;
import com.encens.khipus.model.admin.Company;
import com.encens.khipus.model.admin.User;
import com.encens.khipus.model.customers.DiscountPolicy;
import com.encens.khipus.model.customers.DiscountRuleState;
import org.hibernate.annotations.Filter;
import org.hibernate.validator.NotNull;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Entity for Product Discount Rule
 *
 * @author: Ariel Siles Encinas
 */


@NamedQueries(
        {
                @NamedQuery(name = "EmployeeDiscount.findDiscountByRule", query = "select e from EmployeeDiscountRule ed inner join ed.employees e where e =:employee and ed =:rule"),
                @NamedQuery(name = "EmployeeDiscountRule.findAll", query = "select o from EmployeeDiscountRule o order by o.id")
        }
)


@TableGenerator(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "EmployeeDiscountRule.tableGenerator",
        table = com.encens.khipus.util.Constants.SEQUENCE_TABLE_NAME,
        pkColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_PK_COLUMN_NAME,
        valueColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_VALUE_COLUMN_NAME,
        pkColumnValue = "rdescuentoempleado",
        allocationSize = com.encens.khipus.util.Constants.SEQUENCE_ALLOCATION_SIZE)
@Entity
@Filter(name = com.encens.khipus.util.Constants.COMPANY_FILTER_NAME)
@EntityListeners({CompanyListener.class, UpperCaseStringListener.class})
@Table(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "rdescuentoempleado")
public class EmployeeDiscountRule implements BaseModel {

    @Id
    @Column(name = "iddescuentoempleado", nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "EmployeeDiscountRule.tableGenerator")
    private Long id;

    @Column(name = "descripcion", nullable = true)
    @Lob
    private String description;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "fechaactivacion")
    private Date activationDate = new Date();

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "fechaestado")
    private Date stateDate;

    @Column(name = "cantidad", nullable = false)
    private int quantity;

    @Column(name = "monto", precision = 13, scale = 2, nullable = false)
    private BigDecimal amount;

    @Column(name = "notas")
    @Lob
    private String notes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idusuario", referencedColumnName = "idusuario")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idpoliticadescuento", nullable = false)
    private DiscountPolicy discountPolicy;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "descuentoempleado",
            joinColumns = @JoinColumn(name = "iddescuentoempleado"),
            inverseJoinColumns = @JoinColumn(name = "idempleado"),
            schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA,
            uniqueConstraints = {@UniqueConstraint(columnNames = {"iddescuentoempleado", "idempleado"})
            }
    )
    //@OrderBy("firstName asc")
    @Filter(name = com.encens.khipus.util.Constants.COMPANY_FILTER_NAME)
    private List<Employee> employees = new ArrayList<Employee>(0);

    @Column(name = "estadoregladescuento", nullable = false)
    @Enumerated(EnumType.STRING)
    private DiscountRuleState discountRuleState;

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

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getActivationDate() {
        return activationDate;
    }

    public void setActivationDate(Date activationDate) {
        this.activationDate = activationDate;
    }

    public Date getStateDate() {
        return stateDate;
    }

    public void setStateDate(Date stateDate) {
        this.stateDate = stateDate;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public DiscountPolicy getDiscountPolicy() {
        return discountPolicy;
    }

    public void setDiscountPolicy(DiscountPolicy discountPolicy) {
        this.discountPolicy = discountPolicy;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public DiscountRuleState getDiscountRuleState() {
        return discountRuleState;
    }

    public void setDiscountRuleState(DiscountRuleState discountRuleState) {
        this.discountRuleState = discountRuleState;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public List<Employee> getEmployees() {
        return employees;
    }

    public void setEmployees(List<Employee> employees) {
        this.employees = employees;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }
}