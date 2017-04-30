package com.encens.khipus.service.warehouse;

import com.encens.khipus.exception.EntryNotFoundException;
import com.encens.khipus.framework.service.GenericServiceBean;
import com.encens.khipus.model.finances.Provider;
import com.encens.khipus.model.warehouse.ProductItem;
import com.encens.khipus.model.warehouse.ProductItemByProviderHistory;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;

/**
 * Service implementation of ProductItemByProviderHistoryService
 *
 * @author
 * @version 2.27
 */
@Stateless
@Name("productItemByProviderHistoryService")
@AutoCreate
public class ProductItemByProviderHistoryServiceBean extends GenericServiceBean implements ProductItemByProviderHistoryService {
    @In(value = "#{listEntityManager}")
    private EntityManager eventEm;

    /**
     * Finds the last ProductItemByProviderHistory entry registered for a given provider and productItem
     *
     * @param provider      The provider to filter in the history
     * @param productItem   The productItem to filter in the history
     * @param entityManager the entityManager to use if specified
     * @return The last ProductItemByProviderHistory entry registered for a given provider and productItem
     */
    public ProductItemByProviderHistory findLastUnitCostByProductItem(Provider provider, ProductItem productItem, EntityManager entityManager) {
        if (null == entityManager) {
            entityManager = getEntityManager();
        }
        ProductItemByProviderHistory productItemByProviderHistory = null;
        try {

            Long lastHistoryId = (Long) entityManager.createNamedQuery("ProductItemByProviderHistory.findLastByProductItemAndProvider")
                    .setParameter("provider", provider)
                    .setParameter("productItem", productItem).getSingleResult();
            if (lastHistoryId != null) {
                productItemByProviderHistory = findById(ProductItemByProviderHistory.class, lastHistoryId);
            }
        } catch (EntityNotFoundException ignored) {
        } catch (EntryNotFoundException ignored) {
        }
        return productItemByProviderHistory;
    }

}
