package com.encens.khipus.service.customers;

import com.encens.khipus.model.admin.User;
import com.encens.khipus.model.contacts.Entity;
import com.encens.khipus.model.customers.Customer;
import com.encens.khipus.model.customers.CustomerDiscount;
import com.encens.khipus.model.customers.CustomerDiscountRule;
import com.encens.khipus.model.customers.DiscountPolicyMeasurementType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import java.math.BigDecimal;
import java.util.List;

/**
 * Customer discount service implementation class
 *
 * @author:
 */

@Stateless
@Name("customerDiscountService")
@AutoCreate
public class CustomerDiscountServiceBean implements CustomerDiscountService {

    @In(required = false)
    private User currentUser;

    @In(value = "#{entityManager}")
    private EntityManager em;

    @In
    private CustomerService customerService;

    public CustomerDiscount findDiscountByRule(Customer customer, CustomerDiscountRule rule) {
        try {
            Query query = em.createNamedQuery("CustomerDiscount.findDiscountByRule");
            query.setParameter("customer", customer);
            query.setParameter("rule", rule);
            return (CustomerDiscount) query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    @SuppressWarnings({"unchecked"})
    public List<CustomerDiscountRule> findDiscountRulesByCustomer(Customer customer) {
        try {
            return em.createNamedQuery("CustomerDiscount.findDiscountRulesByCustomer")
                    .setParameter("customer", customer).getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    public void newDiscount(CustomerDiscountRule rule, Customer customer) {
        if (findDiscountByRule(customer, rule) == null) {
            //CustomerDiscount discount = new CustomerDiscount(rule, customer, currentUser);
            rule.getDiscounts().add(new CustomerDiscount(rule, customer, currentUser));
        }
    }

    public void deleteDiscount(CustomerDiscountRule rule, Customer customer) {
        CustomerDiscount discount = findDiscountByRule(customer, rule);
        if (discount != null) {
            em.remove(discount);
        }
    }

    public void updateDiscounts(Entity entity, List<CustomerDiscountRule> discountRules) {
        try {
            Customer customer = customerService.findByEntity(entity);
            List<CustomerDiscountRule> customerDiscountRules = findDiscountRulesByCustomer(customer);

            for (CustomerDiscountRule customerDiscountRule : customerDiscountRules) {
                if (!discountRules.contains(customerDiscountRule)) {
                    CustomerDiscount discount = findDiscountByRule(customer, customerDiscountRule);
                    customer.getDiscounts().remove(discount);
                    em.remove(discount);
                }
            }

            for (CustomerDiscountRule newDiscountRule : discountRules) {
                if (!customerDiscountRules.contains(newDiscountRule)) {
                    CustomerDiscount newDiscount = new CustomerDiscount(newDiscountRule, customer, currentUser);
                    customer.getDiscounts().add(newDiscount);
                    em.persist(newDiscount);
                }
            }
            em.merge(entity);
            em.flush();

        }
        catch (NullPointerException e) {
        }
    }

    public BigDecimal getTotalDiscountPercentage(Customer customer) {
        Query query = em.createQuery("select sum(cd.discountRule.amount) from CustomerDiscount cd where cd.customer =:customer " +
                "and cd.discountRule.discountPolicy.discountPolicyType.measurement =:measurement");
        query.setParameter("customer", customer);
        query.setParameter("measurement", DiscountPolicyMeasurementType.PERCENTAGE);
        return (BigDecimal) query.getSingleResult();
    }

    public BigDecimal getTotalDiscountAmount(Customer customer) {
        Query query = em.createQuery("select sum(cd.discountRule.amount) from CustomerDiscount cd where cd.customer =:customer " +
                "and cd.discountRule.discountPolicy.discountPolicyType.measurement =:measurement");
        query.setParameter("customer", customer);
        query.setParameter("measurement", DiscountPolicyMeasurementType.AMOUNT);
        return (BigDecimal) query.getSingleResult();
    }

}
