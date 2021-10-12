package com.encens.khipus.service.customers;


import com.encens.khipus.model.customers.CustomerOrderType;

import javax.ejb.Local;

@Local
public interface CustomerOrderTypeService {

    CustomerOrderType findCustomerOrderTypeDefault();

    CustomerOrderType findCustomerOrderTypeSpecial();

}
