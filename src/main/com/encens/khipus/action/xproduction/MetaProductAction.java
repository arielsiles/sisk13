package com.encens.khipus.action.xproduction;

import com.encens.khipus.exception.ConcurrencyException;
import com.encens.khipus.exception.EntryDuplicatedException;
import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.framework.action.Outcome;
import com.encens.khipus.model.xproduction.MetaProduct;
import com.encens.khipus.model.warehouse.ProductItem;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;

@Name("metaProductAction")
@Scope(ScopeType.CONVERSATION)
public class MetaProductAction extends GenericAction<MetaProduct> {

    @Factory(value = "metaProduct", scope = ScopeType.STATELESS)
    public MetaProduct initProcessedProduct() {
        return getInstance();
    }

    @Override
    protected String getDisplayNameProperty() {
        return "name";
    }

    @Override
    @Begin(join=true, ifOutcome = Outcome.SUCCESS, flushMode = FlushModeType.MANUAL)
    public String select(MetaProduct instance) {
        String outCome = super.select(instance);
        return outCome;
    }

    @Override
    @End
    public String create() {

        try {
            MetaProduct metaProduct = getInstance();
            ProductItem productItem = getInstance().getProductItem();

            metaProduct.setProductItem(productItem);
            metaProduct.setName(productItem.getName());
            metaProduct.setCode(productItem.getProductItemCode());
            metaProduct.setProductItemCode(productItem.getProductItemCode());
            metaProduct.setCompanyNumber(productItem.getCompanyNumber());

            getService().create(metaProduct);

            return Outcome.SUCCESS;
        } catch (EntryDuplicatedException e) {
            addDuplicatedMessage();
            return Outcome.REDISPLAY;
        }

    }

    @Override
    public String update() {

        try {
            ProductItem productItem = getInstance().getProductItem();
            getInstance().setProductItem(productItem);
            getInstance().setName(productItem.getName());
            getInstance().setCode(productItem.getProductItemCode());
            getInstance().setProductItemCode(productItem.getProductItemCode());
            getInstance().setCompanyNumber(productItem.getCompanyNumber());

            genericService.update(getInstance());

        } catch (ConcurrencyException e) {
            return Outcome.REDISPLAY;
        } catch (EntryDuplicatedException e) {
            return Outcome.REDISPLAY;
        }
        addUpdatedMessage();
        return Outcome.SUCCESS;
    }

    public void assignProductItem(ProductItem productItem){
        getInstance().setProductItem(productItem);
    }

    public void clearProductItem(){
        getInstance().setProductItem(null);
    }

}
