package com.encens.khipus.action.production;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.production.ProductionOrder;
//import com.encens.khipus.model.production.ProductionPlanning;
//import com.encens.khipus.model.production.ProductionPlanningState;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import java.util.Arrays;
import java.util.List;


@Name("productionOrderDataModel")
@Scope(ScopeType.PAGE)
public class ProductionOrderDataModel extends QueryDataModel<Long, ProductionOrder> {
/*    private static final String[] RESTRICTIONS = {
            "lower(productionOrder.code) like concat(#{productionOrderDataModel.criteria.code}, '%')",
            "productionOrder.productionPlanning.date = #{productionOrderDataModel.criteria.productionPlanning.date}",
            "productionOrder.productionPlanning.state = #{productionOrderDataModel.criteria.productionPlanning.state}"
    };
  */
    @Create
    public void init() {
        sortProperty = "productionOrder.code";
    }

    @Override
    public String getEjbql() {
        return "select productionOrder " +
                "from ProductionOrder productionOrder " +
                "left join fetch productionOrder.productionPlanning ";
    }
    /*
    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }
      */

    @Override
    public ProductionOrder createInstance() {
        //ProductionPlanning pp = new ProductionPlanning();
        //pp.setState(ProductionPlanningState.EXECUTED);

        ProductionOrder po = super.createInstance();
        //po.setProductionPlanning(pp);
        return po;
    }
}
