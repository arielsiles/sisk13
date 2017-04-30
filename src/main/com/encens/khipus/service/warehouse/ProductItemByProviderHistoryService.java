package com.encens.khipus.service.warehouse;

import com.encens.khipus.framework.service.GenericService;
import com.encens.khipus.model.finances.Provider;
import com.encens.khipus.model.warehouse.ProductItem;
import com.encens.khipus.model.warehouse.ProductItemByProviderHistory;

import javax.ejb.Local;
import javax.persistence.EntityManager;

/**
 * This class implements the ProductItemByProviderHistory service local interface
 *
 * @author
 * @version 2.27
 */
@Local
public interface ProductItemByProviderHistoryService extends GenericService {
    /**
     * Finds the last ProductItemByProviderHistory entry registered for a given provider and productItem
     *
     * @param provider      The provider to filter in the history
     * @param productItem   The productItem to filter in the history
     * @param entityManager the entityManager to use if specified
     * @return The last ProductItemByProviderHistory entry registered for a given provider and productItem
     */
    ProductItemByProviderHistory findLastUnitCostByProductItem(Provider provider, ProductItem productItem, EntityManager entityManager);
}
