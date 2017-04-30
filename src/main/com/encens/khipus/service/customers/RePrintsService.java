package com.encens.khipus.service.customers;

import com.encens.khipus.framework.service.GenericService;
import com.encens.khipus.model.customers.CustomerOrder;
import com.encens.khipus.model.customers.RePrints;

import javax.ejb.Local;

/**
 * Created with IntelliJ IDEA.
 * User: Diego
 * Date: 26/12/14
 * Time: 1:06
 * To change this template use File | Settings | File Templates.
 */
@Local
public interface RePrintsService extends GenericService {
    RePrints findReprintByCustomerOrder(CustomerOrder order);

    String findNameClient(CustomerOrder order);

    /*String findNitClient(CustomerOrder order);*/
}
