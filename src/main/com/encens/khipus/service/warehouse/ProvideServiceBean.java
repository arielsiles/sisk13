package com.encens.khipus.service.warehouse;

import com.encens.khipus.framework.service.GenericServiceBean;
import com.encens.khipus.model.finances.Provide;
import com.encens.khipus.model.finances.Provider;
import com.encens.khipus.model.warehouse.ProductItem;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;

/**
 * Service implementation of ProvideService
 *
 * @author
 * @version 2.27
 */
@Stateless
@Name("provideService")
@AutoCreate
public class ProvideServiceBean extends GenericServiceBean implements ProvideService {
    @In(value = "#{listEntityManager}")
    private EntityManager eventEm;

    /**
     * Finds the provide entry registered for a given provider and productItem
     *
     * @param provider      The provider to filter
     * @param productItem   The productItem to filter
     * @param entityManager the entityManager to use if specified
     * @return The provide entry registered for a given provider and productItem
     */
    public Provide findByProviderAndProductItem(Provider provider, ProductItem productItem, EntityManager entityManager) {
        if (null == entityManager) {
            entityManager = getEntityManager();
        }
        Provide provide = null;
        try {

            provide = (Provide) entityManager.createNamedQuery("Provide.findByProviderAndProductItem")
                    .setParameter("provider", provider)
                    .setParameter("productItem", productItem).getSingleResult();
        } catch (EntityNotFoundException e) {
            return null;
        }
        return provide;
    }

}