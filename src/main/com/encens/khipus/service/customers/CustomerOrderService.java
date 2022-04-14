package com.encens.khipus.service.customers;

import com.encens.khipus.model.customers.SaleTypeEnum;
import com.encens.khipus.model.warehouse.Warehouse;

import javax.ejb.Local;
import java.util.Date;
import java.util.List;

/**
 * @author
 * @version $Id
 */
@Local
public interface CustomerOrderService {

    List<Object[]> getSalesCustomer(Date startDate, Date endDate, SaleTypeEnum saleType, Warehouse warehouse);
    List<Object[]> getSalesCustomerProduct(Date startDate, Date endDate, SaleTypeEnum saleType, Warehouse warehouse);
    List<Object[]> getSalesProduct(Date startDate, Date endDate, SaleTypeEnum saleType, Warehouse warehouse);

}
