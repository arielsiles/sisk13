package com.encens.khipus.service.warehouse;

import com.encens.khipus.framework.service.GenericService;
import com.encens.khipus.model.finances.Provide;
import com.encens.khipus.model.finances.Provider;
import com.encens.khipus.model.warehouse.ProductItem;

import javax.ejb.Local;
import javax.persistence.EntityManager;

/**
 * This class implements the Provide service local interface
 *
 * @author
 * @version 2.27
 */
@Local
public interface ProvideService extends GenericService {
    /**
     * Finds the provide entry registered for a given provider and productItem
     *
     * @param provider      The provider to filter
     * @param productItem   The productItem to filter
     * @param entityManager the entityManager to use if specified
     * @return The provide entry registered for a given provider and productItem
     */
    Provide findByProviderAndProductItem(Provider provider, ProductItem productItem, EntityManager entityManager);

}