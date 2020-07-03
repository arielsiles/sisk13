package com.encens.khipus.action.customers;

import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.framework.action.Outcome;
import com.encens.khipus.model.customers.CustomerCategory;
import com.encens.khipus.model.customers.PriceItem;
import com.encens.khipus.model.warehouse.ProductItem;
import com.encens.khipus.model.warehouse.ProductItemPK;
import com.encens.khipus.service.customers.PriceItemService;
import com.encens.khipus.util.Constants;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;
import org.jboss.seam.annotations.security.Restrict;

import java.util.ArrayList;
import java.util.List;

/**
 * Actions for Customer category
 *
 * @author:
 */

@Name("customerCategoryAction")
@Scope(ScopeType.CONVERSATION)
public class CustomerCategoryAction extends GenericAction<CustomerCategory> {

    private List<PriceItem> details = new ArrayList<PriceItem>();
    private List<ProductItemPK> selectedProductItems = new ArrayList<ProductItemPK>();

    @In
    private PriceItemService priceItemService;

    @Factory(value = "customerCategory", scope = ScopeType.STATELESS)
    @Restrict("#{s:hasPermission('CUSTOMERCATEGORY','VIEW')}")
    public CustomerCategory initCustomerCategory() {
        return getInstance();
    }

    @Override
    @Begin(flushMode = FlushModeType.MANUAL)
    public String select(CustomerCategory instance) {
        String outCome = super.select(instance);
        //voucher = getInstance();
        //setVoucherDetails(voucherAccoutingService.getVoucherDetailList(voucher));
        setDetails(priceItemService.getPriceItems(getInstance()));
        return outCome;
    }

    @Override
    protected String getDisplayNameProperty() {
        return "name";
    }

    @End
    @Override
    public String update(){
        String outcome = Outcome.SUCCESS;

        priceItemService.updatePriceItems(getDetails());

        return outcome;
    }

    public void addProductItems(List<ProductItem> productItems) {
        for (ProductItem productItem : productItems) {
            if (selectedProductItems.contains(productItem.getId())) {
                continue;
            }

            /*MeasureUnit measureUnit = getMeasureUnit(productItem);
            if (null == measureUnit) {
                continue;
            }*/

            selectedProductItems.add(productItem.getId());

            PriceItem item = new PriceItem();
            item.setProductItem(productItem);
            item.setProductItemCode(productItem.getProductItemCode());
            item.setCustomerCategory(getInstance());
            item.setCompanyNumber(Constants.COD_COMPANY_DEFAULT);
            getDetails().add(item);

            //MovementDetail detail = new MovementDetail();
            //detail.setProductItem(productItem);

            //detail.setMeasureUnit(measureUnit);
            //updateProductItemAccount(detail);
            //movementDetails.add(detail);
        }
    }

    public void removeMovementDetail(PriceItem instance) {
        selectedProductItems.remove(instance.getProductItem().getId());
        details.remove(instance);
        priceItemService.deletePriceItem(instance);
    }

    public List<PriceItem> getDetails() {
        return details;
    }

    public void setDetails(List<PriceItem> details) {
        this.details = details;
    }
}
