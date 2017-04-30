package com.encens.khipus.service.customers;

import com.encens.khipus.model.contacts.Entity;
import com.encens.khipus.model.customers.Customer;

import javax.ejb.Local;
import java.util.List;

/**
 * @author
 * @version $Id: CustomerService.java 2008-9-10 10:31:33 $
 */
@Local
public interface CustomerService {
    void createCustomer();

    List<Customer> getAllCustomers();

    void createOrUpdate(Customer customer);

    Customer findByIdNumber(String idNumber);

    Customer findByCustomerNumber(String customerNumber);

    boolean numberExists(String customerNumber, String idNumber);

    Customer findByEntity(Entity entity);

    Entity findEntityByIdNumber(String idNumber);
}
