package com.encens.khipus.service.production;

import com.encens.khipus.model.production.ProductionPlan;
import com.encens.khipus.model.production.ProductionProduct;

import javax.ejb.Local;
import java.util.List;

/**
 * Product service interface
 *
 * @author
 * @version $Id: ProductService.java 2008-9-11 13:50:25 $
 */
@Local
public interface ProductionPlanService {

    void updateProductionPlan(ProductionPlan productionPlan, List<ProductionProduct> productList);

}
