package com.encens.khipus.service.customers;

import com.encens.khipus.framework.service.GenericService;
import com.encens.khipus.model.admin.User;
import com.encens.khipus.model.customers.ArticleOrder;
import com.encens.khipus.model.customers.CustomerOrder;
import com.encens.khipus.model.customers.CustomerOrderTypeEnum;
import com.encens.khipus.model.customers.SaleTypeEnum;

import javax.ejb.Local;
import java.util.Date;
import java.util.List;

@Local
public interface SaleService extends GenericService {

    String createSale(CustomerOrder customerOrder);
    void updateCustomerOrder(CustomerOrder customerOrder);
    Long findLastSaleId(User user);
    CustomerOrder findSaleById(Long id);
    void updateArticleForOutputs(ArticleOrder articleOrder);
    CustomerOrder findCustomerOrderByParams(SaleTypeEnum saleType, Date date, String code);
    List<CustomerOrder> getPendingCustomerOrderList(User user, Date date);
    List<CustomerOrder> getPendingCustomerOrderList(Date date);
    List<CustomerOrder> getPendingCustomerOrderList(Date date, CustomerOrderTypeEnum customerOrderTypeEnum);

}
