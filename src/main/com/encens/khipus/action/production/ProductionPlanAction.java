package com.encens.khipus.action.production;

import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.framework.action.Outcome;
import com.encens.khipus.model.production.ProductionPlan;
import com.encens.khipus.model.production.ProductionProduct;
import com.encens.khipus.model.warehouse.ProductItem;
import com.encens.khipus.service.production.ProductionPlanService;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;

import java.util.ArrayList;
import java.util.List;


@Name("productionPlanAction")
@Scope(ScopeType.CONVERSATION)
public class ProductionPlanAction extends GenericAction<ProductionPlan> {

    @In(create = true)
    private ProductionAction productionAction;

    @In
    private ProductionPlanService productionPlanService;

    private List<ProductionProduct> productList = new ArrayList<ProductionProduct>();

    @Factory(value = "productionPlan", scope = ScopeType.STATELESS)
    public ProductionPlan initProductionPlan() {
        return getInstance();
    }


    @Override
    public String update() {

        productionPlanService.updateProductionPlan(getInstance(), productList);
        return Outcome.SUCCESS;
    }

    @Begin(nested = true, flushMode = FlushModeType.MANUAL)
    public String addProduction() {
        productionAction.setProductionPlan(getInstance());
        /*setOp(OP_UPDATE);*/
        return Outcome.SUCCESS;
    }

    public List<ProductionProduct> getProductList() {
        return productList;
    }

    public void setProductList(List<ProductionProduct> productList) {
        this.productList = productList;
    }

    public void addProductItems(List<ProductItem> productItems){

        for (ProductItem productItem : productItems) {
            ProductionProduct product = new ProductionProduct();
            product.setProductItemCode(productItem.getProductItemCode());
            product.setProductItem(productItem);
            productList.add(product);
        }
    }
}
