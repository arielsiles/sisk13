package com.encens.khipus.service.customers;

import com.encens.khipus.model.customers.CustomerCategory;
import com.encens.khipus.model.customers.CustomerCategoryType;

import javax.ejb.Local;

/**
 * @author
 * @version $Id: ClientService.java $
 */
@Local
public interface CustomerCategoryService {

    CustomerCategory getCustomerCategory(CustomerCategoryType customerCategoryType);

}
