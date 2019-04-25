package com.encens.khipus.service.production;

import com.encens.khipus.model.production.ProductionPlan;
import com.encens.khipus.model.production.ProductionProduct;
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
    }


}
