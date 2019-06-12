package com.encens.khipus.service.production;

import com.encens.khipus.model.production.CollectionForm;
import com.encens.khipus.model.production.ProductionPlan;
import com.encens.khipus.model.production.ProductionProduct;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Products services
 *
 * @author
 * @version $Id:
 */
@Stateless
@Name("productionPlanService")
@AutoCreate
public class ProductionPlanServiceBean implements ProductionPlanService {

    @In(value = "#{entityManager}")
    private EntityManager em;

    public void updateProductionPlan(ProductionPlan productionPlan, List<ProductionProduct> productList){

        for (ProductionProduct product : productList){
            if (product.getId() == null){
                product.setProductionPlan(productionPlan);
                em.persist(product);
                em.flush();
            }else {
                em.merge(product);
                em.flush();
            }
        }
        em.merge(productionPlan);
        em.flush();
    }

    public void updateProductionPlan(ProductionPlan productionPlan){
        em.merge(productionPlan);
        em.flush();
    }

    public List<ProductionProduct> getProductionProductList(ProductionPlan productionPlan){

        List<ProductionProduct> productionProductList = new ArrayList<ProductionProduct>();

        productionProductList =  (List<ProductionProduct>) em.createQuery("select p from ProductionProduct p " +
                " where p.productionPlan =:productionPlan " +
                " and p.production is null ")
                .setParameter("productionPlan", productionPlan)
                .getResultList();

        return  productionProductList;
    }

    public double findTotalWeighed(Date date){

        double result = 0.0;
        try {
            CollectionForm collectionForm = (CollectionForm)em.createQuery(" select collectionForm from CollectionForm collectionForm" +
                    " where collectionForm.date = :date ")
                    .setParameter("date",date)
                    .getSingleResult();

            if (collectionForm != null)
                result = collectionForm.getTotalWeighed();

        }catch (NoResultException exception){
            result = 0;
        }

        return result;
    }

    public List<ProductionPlan> getProductionPlanList(Date startDate, Date endDate){

        List<ProductionPlan> productionPlanList = (List<ProductionPlan>) em.createQuery("select p from ProductionPlan p " +
                "where p.date between :startDate and :endDate")
                .setParameter("startDate", startDate)
                .setParameter("endDate", endDate)
                .getResultList();
        return productionPlanList;
    }

}
