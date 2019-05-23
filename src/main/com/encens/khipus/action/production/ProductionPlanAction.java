package com.encens.khipus.action.production;

import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.framework.action.Outcome;
import com.encens.khipus.model.production.Production;
import com.encens.khipus.model.production.ProductionPlan;
import com.encens.khipus.model.production.ProductionProduct;
import com.encens.khipus.model.production.Supply;
import com.encens.khipus.model.warehouse.ProductItem;
import com.encens.khipus.service.production.ProductionPlanService;
import com.encens.khipus.util.BigDecimalUtil;
import com.encens.khipus.util.Constants;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;

import java.math.BigDecimal;
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
    @Begin(nested=true, ifOutcome = Outcome.SUCCESS, flushMode = FlushModeType.MANUAL)
    public String select(ProductionPlan instance) {
        String outCome = super.select(instance);
        setProductList(getInstance().getProductionProductList());

        return outCome;
    }

    @Override
    @Begin(nested=true, ifOutcome = Outcome.SUCCESS, flushMode = FlushModeType.MANUAL)
    public String create() {
        String outcome = super.create();
        setOp(OP_UPDATE);
        return outcome;
    }

    @Override
    public String update() {
        productionPlanService.updateProductionPlan(getInstance(), productList);
        addUpdatedMessage();
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

    public String hasProduction(ProductionProduct productionProduct){

        String result = "NO";
        if (productionProduct.getProduction() != null)
            result = "SI";

        return  result;
    }

    public BigDecimal getDestinedMilk(Production production){

        BigDecimal result = BigDecimal.ZERO;

        for (Supply supply : production.getSupplyList()){
            if (supply.getProductItemCode().equals(Constants.ID_ART_RAW_MILK))
                result = BigDecimalUtil.sum(result, supply.getQuantity(), 2);
        }
        return result;
    }

    public BigDecimal calculateTotalRawMaterial(ProductionPlan productionPlan){

        BigDecimal result = BigDecimal.ZERO;

        for (Production production : productionPlan.getProductionList()){
            productionAction.setInstance(production);
            productionAction.setIngredientSupplyList(production.getSupplyList());
            result = BigDecimalUtil.sum(result, productionAction.calculateRawMaterial(), 2);
        }
        return result;

    }

    public double getTotalWeighed(ProductionPlan productionPlan){

        double result = productionPlanService.findTotalWeighed(productionPlan.getDate());
        return result;

    }


}
