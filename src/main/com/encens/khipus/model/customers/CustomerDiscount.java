package com.encens.khipus.model.customers;

import com.encens.khipus.model.CompanyListener;
import com.encens.khipus.model.admin.Company;
import com.encens.khipus.model.admin.User;
import org.hibernate.annotations.Filter;
import org.hibernate.validator.NotNull;

import javax.persistence.*;

/**
 * Entity for Customer discount
 *
 * @author:
 */

@NamedQueries(
        {
                @NamedQuery(name = "CustomerDiscount.findDiscountByRule", query = "select cd from CustomerDiscount cd where cd.customer =:customer and cd.discountRule =:rule"),
                @NamedQuery(name = "CustomerDiscount.findDiscountRulesByCustomer", query = "select cd.discountRule from CustomerDiscount cd where cd.customer =:customer")}
)

@javax.persistence.Entity
@Filter(name = com.encens.khipus.util.Constants.COMPANY_FILTER_NAME)
@EntityListeners(CompanyListener.class)
@Table(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "descuentocliente")
public class CustomerDiscount {

    @EmbeddedId
    private CustomerDiscountPk id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "iddescuentocliente", referencedColumnName = "iddescuentocliente", nullable = false, insertable = false, updatable = false)
    private CustomerDiscountRule discountRule;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idcliente", referencedColumnName = "idcliente", nullable = false, insertable = false, updatable = false)
    private Customer customer;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idusuario", referencedColumnName = "idusuario")
    private User user;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idcompania", nullable = false, updatable = false, insertable = true)
    @NotNull
    private Company company;


    public CustomerDiscount() {
    }

    public CustomerDiscount(CustomerDiscountRule discountRule, Customer customer, User user) {
        this.id = new CustomerDiscountPk(discountRule.getId(), customer.getId());
        this.user = user;
    }

    public CustomerDiscountPk getId() {
        return id;
    }

    public void setId(CustomerDiscountPk id) {
        this.id = id;
    }

    public CustomerDiscountRule getDiscountRule() {
        return discountRule;
    }

    public void setDiscountRule(CustomerDiscountRule discountRule) {
        this.discountRule = discountRule;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }
}
