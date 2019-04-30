package com.encens.khipus.service.production;

import com.encens.khipus.model.production.Production;
import com.encens.khipus.model.production.ProductionProduct;
import com.encens.khipus.model.production.Supply;
import com.encens.khipus.model.production.SupplyType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import java.util.List;

/**
 * Products services
 *
 * @author
 * @version $Id: ProductServiceBean.java 2008-9-11 13:50:57 $
 */
@Stateless
@Name("productionService")
@AutoCreate
public class ProductionServiceBean implements ProductionService {

    @In(value = "#{entityManager}")
    private EntityManager em;


    public void createProduction(Production production, List<Supply> ingredientSupplyList, List<Supply> materialSupplyList){

        em.persist(production);
        em.flush();
        for (Supply supply : ingredientSupplyList){
            supply.setProduction(production);
            supply.setType(SupplyType.INGREDIENT);
            em.persist(supply);
            em.flush();
        }

        for (Supply supply : materialSupplyList){
            supply.setProduction(production);
            supply.setType(SupplyType.MATERIAL);
            em.persist(supply);
            em.flush();
        }
    }

    public void updateProduction(Production production, List<Supply> ingredientSupplyList, List<Supply> materialSupplyList){

        for (Supply supply : ingredientSupplyList){

            if (supply.getId() == null){
                supply.setProduction(production);
                supply.setType(SupplyType.INGREDIENT);
                em.persist(supply);
                em.flush();
            }else {
                em.merge(supply);
                em.flush();
            }

        }

        for (Supply supply : materialSupplyList){
            if (supply.getId() == null){
                supply.setProduction(production);
                supply.setType(SupplyType.MATERIAL);
                em.persist(supply);
                em.flush();
            }else {
                em.merge(supply);
                em.flush();
            }
        }
    }

    public void assignProduct(Production production, ProductionProduct product){

        //System.out.println("----> Asignando producto terminado: " + product.getProductItem().getFullName());
        //System.out.println("----> Produccion: " + production.getId());

        product.setProduction(production);
        em.merge(product);
        em.flush();

        em.refresh(production);
        em.flush();

    }

    public List<Supply> getSupplyList(Production production, SupplyType type) {

        List<Supply> supplyList = em.createQuery("select supply from Supply supply " +
                " where supply.production = :production " +
                " and supply.type = :type ")
                .setParameter("production", production)
                .setParameter("type", type)
                .getResultList();

        return supplyList;
    }

    public void removeProductionProduct(ProductionProduct product, Production production){
        product.setProduction(null);
        em.merge(product);
        em.flush();
        em.refresh(production);
        em.flush();
    }
}
