package com.encens.khipus.service.xproduction;

import com.encens.khipus.model.xproduction.XProductionPlan;
import com.encens.khipus.model.xproduction.XProductionProduct;

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
public interface XProductionPlanService {

    void updateProductionPlan(XProductionPlan productionPlan, List<XProductionProduct> productList);
    void updateProductionPlan(XProductionPlan productionPlan);
    void removeProduct(XProductionProduct product);
    List<XProductionProduct> getProductionProductList(XProductionPlan productionPlan);
    double findTotalWeighed(Date date);
    List<XProductionPlan> getProductionPlanList(Date startDate, Date endDate);

    void updateProductForProduction(XProductionProduct product);
    void updateProductItemRemoveFromProduction(XProductionProduct product);

}
