package com.encens.khipus.service.production;

import com.encens.khipus.exception.ConcurrencyException;
import com.encens.khipus.exception.EntryDuplicatedException;
import com.encens.khipus.exception.warehouse.ProductItemNotFoundException;
import com.encens.khipus.framework.service.GenericServiceBean;
import com.encens.khipus.model.production.ProcessedProduct;
import com.encens.khipus.model.production.ProductComposition;
import com.encens.khipus.model.warehouse.ProductItem;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;

import static javax.ejb.TransactionAttributeType.REQUIRES_NEW;

/**
 * Created with IntelliJ IDEA.
 * User: david
 * Date: 6/24/13
 * Time: 10:17 AM
 * To change this template use File | Settings | File Templates.
 */
@Name("processedProductService")
@Stateless
@AutoCreate
public class ProcessedProductServiceBean extends GenericServiceBean implements ProcessedProductService {

    @In(value = "#{entityManager}")
    private EntityManager em;

    @Override
    public ProcessedProduct find(long id) {
        return (ProcessedProduct)em.createNamedQuery("ProcessedProduct.withProductCompositionFind")
                                   .setParameter("id", id)
                                   .getSingleResult();
    }

    public ProcessedProduct findByCode(String code) {
        return (ProcessedProduct)em.createNamedQuery("ProcessedProduct.findByCode")
                .setParameter("code", code)
                .getSingleResult();
    }

    @Override
    public void createProductionProduct(ProductItem productItem){

        ProcessedProduct processedProduct = new ProcessedProduct();
        processedProduct.setName(productItem.getName());
        processedProduct.setCode(productItem.getId().getProductItemCode());
        processedProduct.setCollectable(false);
        processedProduct.setDescription(productItem.getName() + " - " + processedProduct.getCode());
        processedProduct.setProductItemCode(productItem.getProductItemCode());
        processedProduct.setCompanyNumber(productItem.getCompanyNumber());
        processedProduct.setProductItem(productItem);
        em.persist(processedProduct);

    }

    @Override
    public void updateProductionProduct(ProductItem productItem){

        ProcessedProduct processedProduct = findByCode(productItem.getId().getProductItemCode());
        processedProduct.setName(productItem.getName());
        processedProduct.setDescription(productItem.getName() + " - " + processedProduct.getCode());
        try{
            super.update(processedProduct);
        }catch (Exception e){}

    }

    @Override
    public ProductComposition getProductComposite(Long id) {
        ProductComposition composition = new ProductComposition();
        try {

            composition = (ProductComposition) em.createQuery("select composite from ProductComposition composite where composite.processedProduct.id = :id and composite.active = :estado")
                                                 .setParameter("id",id)
                                                 .setParameter("estado",true)
                                                 .getSingleResult();

        }catch (NoResultException e){
            return null;
        }
        return composition;
    }
}


