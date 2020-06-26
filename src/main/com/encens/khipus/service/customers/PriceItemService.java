package com.encens.khipus.service.customers;

import com.encens.khipus.framework.service.GenericService;
import com.encens.khipus.model.customers.CustomerCategory;
import com.encens.khipus.model.customers.PriceItem;

import javax.ejb.Local;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * PriceItemService service interface
 *
 * @author
 * @version 2.7
 */

@Local
public interface PriceItemService extends GenericService {

    List<PriceItem> getPriceItems(CustomerCategory customerCategory);
    Map<String, BigDecimal> getPriceItemsMap(CustomerCategory customerCategory);
    Map<String, BigDecimal> getConsumerPrices();

}