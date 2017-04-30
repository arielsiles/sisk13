package com.encens.khipus.service.production;

import com.encens.khipus.framework.service.GenericServiceBean;
import com.encens.khipus.model.production.ProductionInput;
import com.encens.khipus.model.warehouse.ProductItem;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;

/**
 * Created by IntelliJ IDEA.
 * User: Ariel
 * Date: 23/09/13
 * Time: 22:29
 * To change this template use File | Settings | File Templates.
 */
@Name("productionInputService")
@Stateless
@AutoCreate
public class ProductionInputServiceBean extends GenericServiceBean implements ProductionInputService {

    @In(value = "#{entityManager}")
    private EntityManager em;

    @Override
    public void createProductionInput(ProductItem productItem) {

        ProductionInput productionInput = new ProductionInput();
        productionInput.setName(productItem.getName());
        productionInput.setCode(productItem.getId().getProductItemCode());
        productionInput.setCollectable(false);
        productionInput.setDescription(productItem.getName() + " - " + productionInput.getCode());
        productionInput.setProductItemCode(productItem.getProductItemCode());
        productionInput.setCompanyNumber(productItem.getCompanyNumber());
        productionInput.setProductItem(productItem);
        em.persist(productionInput);

    }

    @Override
    public void updateProductionInput(ProductItem productItem) {

        ProductionInput productionInput = findByCode(productItem.getId().getProductItemCode());
        productionInput.setName(productItem.getName());
        productionInput.setDescription(productItem.getName() + " - " + productionInput.getCode());
        try {
            super.update(productionInput);
        } catch (Exception e) {
        }
    }

    public ProductionInput findByCode(String code) {
        return (ProductionInput) em.createNamedQuery("ProductionInput.findByCode")
                .setParameter("code", code)
                .getSingleResult();
    }
}
