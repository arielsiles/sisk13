package com.encens.khipus.service.customers;

import com.encens.khipus.exception.ConcurrencyException;
import com.encens.khipus.exception.EntryDuplicatedException;
import com.encens.khipus.exception.ReferentialIntegrityException;
import com.encens.khipus.exception.finances.CompanyConfigurationNotFoundException;
import com.encens.khipus.exception.finances.FinancesCurrencyNotFoundException;
import com.encens.khipus.exception.finances.FinancesExchangeRateNotFoundException;
import com.encens.khipus.exception.warehouse.*;
import com.encens.khipus.framework.service.GenericService;
import com.encens.khipus.model.customers.ArticleOrder;
import com.encens.khipus.model.employees.Gestion;
import com.encens.khipus.model.warehouse.*;

import javax.ejb.Local;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author
 * @version 2.0
 */
@Local
public interface ArticleOrderService extends GenericService {

    public List<ArticleOrder> findCashSaleDetailByCodeAndDate(String productItemCode, Date startDate, Date endDate);
    public List<ArticleOrder> findOrderDetailByCodeAndDate(String productItemCode, Date startDate, Date endDate);
    public List findCashSaleDetailListGroupBy(Date startDate, Date endDate);
    public List findCustomerOrderDetailListGroupBy(Date startDate, Date endDate);
}
