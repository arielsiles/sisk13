package com.encens.khipus.service.customers;

import com.encens.khipus.model.contacts.Entity;
import com.encens.khipus.model.customers.Customer;
import com.encens.khipus.model.customers.CustomerDiscount;
import com.encens.khipus.model.customers.CustomerDiscountRule;

import javax.ejb.Local;
import java.math.BigDecimal;
import java.util.List;

/**
 * Customer discount service interface
 *
 * @author:
 */

@Local
public interface CustomerDiscountService {

    CustomerDiscount findDiscountByRule(Customer customer, CustomerDiscountRule rule);

    List<CustomerDiscountRule> findDiscountRulesByCustomer(Customer customer);

    void updateDiscounts(Entity entity, List<CustomerDiscountRule> discountRules);

    BigDecimal getTotalDiscountPercentage(Customer customer);

    BigDecimal getTotalDiscountAmount(Customer customer);

    void newDiscount(CustomerDiscountRule rule, Customer customer);

    void deleteDiscount(CustomerDiscountRule rule, Customer customer);

}
