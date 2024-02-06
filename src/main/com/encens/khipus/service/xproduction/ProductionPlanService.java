package com.encens.khipus.service.xproduction;

import com.encens.khipus.model.xproduction.ProductionPlan;
import com.encens.khipus.model.xproduction.ProductionProduct;

import javax.ejb.Local;
import java.util.Date;
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
    void updateProductionPlan(ProductionPlan productionPlan);
    void removeProduct(ProductionProduct product);
    List<ProductionProduct> getProductionProductList(ProductionPlan productionPlan);
    double findTotalWeighed(Date date);
    List<ProductionPlan> getProductionPlanList(Date startDate, Date endDate);

    void updateProductForProduction(ProductionProduct product);
    void updateProductItemRemoveFromProduction(ProductionProduct product);

}
