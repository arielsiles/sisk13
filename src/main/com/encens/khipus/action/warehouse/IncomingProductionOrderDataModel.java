package com.encens.khipus.action.warehouse;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.production.*;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import java.util.Arrays;
import java.util.List;


@Name("incomingProductionOrderDataModel")
@Scope(ScopeType.PAGE)
public class IncomingProductionOrderDataModel extends QueryDataModel<Long, ProductionOrder> {
    private static final String[] RESTRICTIONS = {
            "lower(productionOrder.code) like concat(#{incomingProductionOrderDataModel.criteria.code}, '%')",
            "productionOrder.productionPlanning.date = #{incomingProductionOrderDataModel.criteria.productionPlanning.date}",
            "productionOrder.productionPlanning.state = #{incomingProductionOrderDataModel.criteria.productionPlanning.state}",
            "lower(productionOrder.productComposition.processedProduct.name) like concat(#{incomingProductionOrderDataModel.criteria.productComposition.processedProduct.name}, '%')"
    };

    @Create
    public void init() {
        sortProperty = "productionOrder.code";
    }

    @Override
    public String getEjbql() {
        return "select productionOrder " +
                "from ProductionOrder productionOrder " +
                "left join fetch productionOrder.productionPlanning " +
                "left join fetch productionOrder.productComposition productComposition " +
                "left join fetch productComposition.processedProduct " +
                "where productionOrder.code not in (" +
                "   select productionOrderInput.productionOrderCode " +
                "   from IncomingProductionOrder productionOrderInput) ";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }

    @Override
    public ProductionOrder createInstance() {
        ProductionPlanning pp = new ProductionPlanning();
        pp.setState(ProductionPlanningState.EXECUTED);

        ProductComposition pc = new ProductComposition();
        pc.setProcessedProduct(new ProcessedProduct());

        ProductionOrder po = super.createInstance();
        po.setProductionPlanning(pp);
        po.setProductComposition(pc);
        return po;
    }
}
