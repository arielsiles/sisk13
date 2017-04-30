package com.encens.khipus.model.products;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyListener;
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
 * @author:
 */

@NamedQueries(
        {
                @NamedQuery(name = "ProductDiscount.findDiscountByRule", query = "select p from ProductDiscountRule pd inner join pd.products p where p =:product and pd =:rule")
        }
)

@TableGenerator(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "ProductDiscountRule.tableGenerator",
        table = com.encens.khipus.util.Constants.SEQUENCE_TABLE_NAME,
        pkColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_PK_COLUMN_NAME,
        valueColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_VALUE_COLUMN_NAME,
        pkColumnValue = "rdescuentoproducto",
        allocationSize = com.encens.khipus.util.Constants.SEQUENCE_ALLOCATION_SIZE)

@Entity
@Filter(name = com.encens.khipus.util.Constants.COMPANY_FILTER_NAME)
@EntityListeners(CompanyListener.class)
@Table(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "rdescuentoproducto", uniqueConstraints = @UniqueConstraint(columnNames = {"idcompania", "nombre"}))
public class ProductDiscountRule implements BaseModel {

    @Id
    @Column(name = "iddescuentoproducto", nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "ProductDiscountRule.tableGenerator")
    private Long id;

    @Column(name = "nombre", nullable = false, length = 150)
    private String name;

    @Column(name = "estadoregladescuento", nullable = false)
    @Enumerated(EnumType.STRING)
    private DiscountRuleState discountRuleState;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idpoliticadescuento", nullable = false)
    private DiscountPolicy discountPolicy;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "fechaactivacion")
    private Date activationDate = new Date();

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "fechaestado")
    private Date stateDate;

    @Column(name = "notas")
    @Lob
    private String notes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idusuario", referencedColumnName = "idusuario")
    private User user;

    @Column(name = "monto", precision = 13, scale = 2, nullable = false)
    private BigDecimal amount;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "descuentoproducto",
            joinColumns = @JoinColumn(name = "iddescuentoproducto"),
            inverseJoinColumns = @JoinColumn(name = "idproducto"),
            schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA,
            uniqueConstraints = {@UniqueConstraint(columnNames = {"iddescuentoproducto", "idproducto"})}
    )
    @OrderBy("name asc")
    @Filter(name = com.encens.khipus.util.Constants.COMPANY_FILTER_NAME)
    private List<Product> products = new ArrayList<Product>(0);

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

    public DiscountRuleState getDiscountRuleState() {
        return discountRuleState;
    }

    public void setDiscountRuleState(DiscountRuleState discountRuleState) {
        this.discountRuleState = discountRuleState;
    }

    public DiscountPolicy getDiscountPolicy() {
        return discountPolicy;
    }

    public void setDiscountPolicy(DiscountPolicy discountPolicy) {
        this.discountPolicy = discountPolicy;
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

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public BigDecimal getAmount() {
        if (discountPolicy != null && amount == null) {
            return discountPolicy.getAmount();
        } else {
            return amount;
        }
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public List<Product> getProducts() {
        return products;
    }

    public void setProducts(List<Product> products) {
        this.products = products;
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
