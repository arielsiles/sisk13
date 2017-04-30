package com.encens.khipus.model.customers;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyListener;
import com.encens.khipus.model.admin.Company;
import com.encens.khipus.model.contacts.Entity;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.validator.NotNull;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Customer entity
 *
 * @author
 * @version $Id: Customer.java 2008-9-9 14:17:00 $
 */

@NamedQueries(
        {
                @NamedQuery(name = "Customer.findByEntity", query = "select c from Customer c where c.entity=:entity"),
                @NamedQuery(name = "Customer.findByIdNumber", query = "select c from Customer c where c.entity.idNumber =:number"),
                @NamedQuery(name = "Customer.findByCustomerNumber", query = "select c from Customer c where c.number=:number"),
                @NamedQuery(name = "Customer.findByNumberExists", query = "select c from Customer c where c.number=:number and not (c.entity.idNumber =:idNumber)"),
                @NamedQuery(name = "Customer.findAll", query = "select c from Customer c")
        }
)

@GenericGenerator(name = "foreign", strategy = "foreign", parameters = {
        @Parameter(name = "property", value = "entity")})

@javax.persistence.Entity
@Filter(name = com.encens.khipus.util.Constants.COMPANY_FILTER_NAME)
@EntityListeners(CompanyListener.class)
@Table(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "cliente", uniqueConstraints = @UniqueConstraint(columnNames = {"idcompania", "nocliente"}))
public class Customer implements BaseModel {

    @Id
    @GeneratedValue(generator = "foreign")
    @Column(name = "idcliente")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, cascade = {CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
    @PrimaryKeyJoinColumn(name = "idcliente")
    private Entity entity;

    @Column(name = "nocliente", length = 100)
    private String number;

    @Temporal(value = TemporalType.TIMESTAMP)
    @Column(name = "fechaprimeracompra")
    private Date firstPurchase;

    @Temporal(value = TemporalType.TIMESTAMP)
    @Column(name = "fechaultimacompra")
    private Date lastPurchase;

    @Column(name = "totalarticulosadquiridos")
    private Integer totalPurchasedProducts;

    @Column(name = "totalimporteadquirido", precision = 13, scale = 2)
    private BigDecimal totalPurchasedAmount;

    @OneToMany(mappedBy = "customer", fetch = FetchType.LAZY)
    @Filter(name = com.encens.khipus.util.Constants.COMPANY_FILTER_NAME)
    private List<CustomerDiscount> discounts = new ArrayList<CustomerDiscount>(0);

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

    public Entity getEntity() {
        return entity;
    }

    public void setEntity(Entity entity) {
        this.entity = entity;
    }

    public Date getFirstPurchase() {
        return firstPurchase;
    }

    public void setFirstPurchase(Date firstPurchase) {
        this.firstPurchase = firstPurchase;
    }

    public Date getLastPurchase() {
        return lastPurchase;
    }

    public void setLastPurchase(Date lastPurchase) {
        this.lastPurchase = lastPurchase;
    }

    public Integer getTotalPurchasedProducts() {
        return totalPurchasedProducts;
    }

    public void setTotalPurchasedProducts(Integer totalPurchasedProducts) {
        this.totalPurchasedProducts = totalPurchasedProducts;
    }

    public BigDecimal getTotalPurchasedAmount() {
        return totalPurchasedAmount;
    }

    public void setTotalPurchasedAmount(BigDecimal totalPurchasedAmount) {
        this.totalPurchasedAmount = totalPurchasedAmount;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public List<CustomerDiscount> getDiscounts() {
        return discounts;
    }

    public void setDiscounts(List<CustomerDiscount> discounts) {
        this.discounts = discounts;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }
}
