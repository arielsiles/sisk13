package com.encens.khipus.service.customers;

import com.encens.khipus.framework.service.GenericService;
import com.encens.khipus.model.admin.User;
import com.encens.khipus.model.customers.CustomerOrder;

import javax.ejb.Local;

@Local
public interface SaleService extends GenericService {

    String createSale(CustomerOrder customerOrder);

    void updateCustomerOrder(CustomerOrder customerOrder);

    Long findLastSaleId(User user);

    CustomerOrder findSaleById(Long id);

}
