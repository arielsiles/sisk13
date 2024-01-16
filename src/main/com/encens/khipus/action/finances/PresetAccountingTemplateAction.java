package com.encens.khipus.action.finances;

import com.encens.khipus.exception.EntryDuplicatedException;
import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.framework.action.Outcome;
import com.encens.khipus.model.customers.CustomerCategory;
import com.encens.khipus.model.customers.PriceItem;
import com.encens.khipus.model.finances.PresetAccountingTemplate;
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

@Name("presetAccountingTemplateAction")
@Scope(ScopeType.CONVERSATION)
public class PresetAccountingTemplateAction extends GenericAction<PresetAccountingTemplate> {

    private List<PriceItem> details = new ArrayList<PriceItem>();
    private List<ProductItemPK> selectedProductItems = new ArrayList<ProductItemPK>();

    @In
    private PriceItemService priceItemService;

    @Factory(value ="presetAccountingTemplate", scope = ScopeType.STATELESS)
    @Restrict("#{s:hasPermission('CUSTOMERCATEGORY','VIEW')}")
    public PresetAccountingTemplate initPresetAccountingTemplate() {
    return getInstance();
}

    @Override
    @Begin(flushMode = FlushModeType.MANUAL)
    public String select(PresetAccountingTemplate instance) {
        String outCome = super.select(instance);
        //setDetails(priceItemService.getPriceItems(getInstance()));
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

        //priceItemService.updatePriceItems(getDetails());

        return outcome;
    }

    @Override
    @End
    public String create() {

        try {
            getService().create(getInstance());
            super.select(getInstance());
            addCreatedMessage();
            return Outcome.SUCCESS;
        } catch (EntryDuplicatedException e) {
            addDuplicatedMessage();
            return Outcome.REDISPLAY;
        }
    }


    public List<PriceItem> getDetails() {
        return details;
    }

    public void setDetails(List<PriceItem> details) {
        this.details = details;
    }
}
