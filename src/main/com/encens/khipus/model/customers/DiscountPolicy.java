package com.encens.khipus.model.customers;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyListener;
import com.encens.khipus.model.admin.Company;
import com.encens.khipus.model.admin.User;
import com.encens.khipus.model.employees.Creditor;
import org.hibernate.annotations.Filter;
import org.hibernate.validator.NotNull;

import javax.persistence.*;
import java.math.BigDecimal;

/**
 * Entity for Discount Policy
 *
 * @author:
 */

@TableGenerator(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "DiscountPolicy.tableGenerator",
        table = com.encens.khipus.util.Constants.SEQUENCE_TABLE_NAME,
        pkColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_PK_COLUMN_NAME,
        valueColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_VALUE_COLUMN_NAME,
        pkColumnValue = "politicadescuento",
        allocationSize = com.encens.khipus.util.Constants.SEQUENCE_ALLOCATION_SIZE)
@NamedQueries(
        {
                @NamedQuery(name = "DiscountPolicy.findAll", query = "select o from DiscountPolicy o "),
                @NamedQuery(name = "DiscountPolicy.findDiscountPolicyByCreditor", query = "select o from DiscountPolicy o " +
                        "where o.creditor=:creditor")
        }
)
@Entity
@Filter(name = com.encens.khipus.util.Constants.COMPANY_FILTER_NAME)
@EntityListeners(CompanyListener.class)
@Table(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "politicadescuento", uniqueConstraints = @UniqueConstraint(columnNames = {"idcompania", "nombre"}))
public class DiscountPolicy implements BaseModel {

    @Id
    @Column(name = "idpoliticadescuento", nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "DiscountPolicy.tableGenerator")
    private Long id;

    @Column(name = "nombre", nullable = false, length = 150)
    private String name;

    @Transient
    private DiscountPolicyTargetType target;

    @ManyToOne
    @JoinColumn(name = "idtipopoliticadescuento", nullable = false)
    private DiscountPolicyType discountPolicyType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idusuario")
    private User user;

    @ManyToOne
    @JoinColumn(name = "idacreedor", nullable = true)
    private Creditor creditor;

    @Column(name = "monto", precision = 13, scale = 2, nullable = false)
    private BigDecimal amount;

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public DiscountPolicyTargetType getTarget() {
        return target;
    }

    public void setTarget(DiscountPolicyTargetType target) {
        this.target = target;
    }

    public DiscountPolicyType getDiscountPolicyType() {
        return discountPolicyType;
    }

    public void setDiscountPolicyType(DiscountPolicyType discountPolicyType) {
        this.discountPolicyType = discountPolicyType;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public Creditor getCreditor() {
        return creditor;
    }

    public void setCreditor(Creditor creditor) {
        this.creditor = creditor;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public String getFullDiscount() {
        return (this.name + " (" + this.amount + (discountPolicyType.getMeasurement().toString().equals("PERCENTAGE") ? '%' : "") + ")");
    }
}

