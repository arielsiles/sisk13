package com.encens.khipus.action.finances;

import com.encens.khipus.exception.ConcurrencyException;
import com.encens.khipus.exception.EntryDuplicatedException;
import com.encens.khipus.exception.EntryNotFoundException;
import com.encens.khipus.exception.ReferentialIntegrityException;
import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.framework.action.Outcome;
import com.encens.khipus.model.customers.CustomerCategory;
import com.encens.khipus.model.customers.PriceItem;
import com.encens.khipus.model.finances.CashAccount;
import com.encens.khipus.model.finances.CashAccountPk;
import com.encens.khipus.model.finances.PresetAccountingTemplate;
import com.encens.khipus.model.finances.TypePresetAccountingTemplate;
import com.encens.khipus.model.warehouse.ProductItem;
import com.encens.khipus.model.warehouse.ProductItemPK;
import com.encens.khipus.service.customers.PriceItemService;
import com.encens.khipus.service.finances.TypePresetAccountingTemplateService;
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

    private List<TypePresetAccountingTemplate> typePresetAccountingTemplates = new ArrayList<TypePresetAccountingTemplate>();
    private List<CashAccountPk> selectedCashAccounts = new ArrayList<CashAccountPk>();

    @In
    private TypePresetAccountingTemplateService typePresetAccountingTemplateService;

    @Factory(value ="presetAccountingTemplate", scope = ScopeType.STATELESS)
    //@Restrict("#{s:hasPermission('CUSTOMERCATEGORY','VIEW')}")
    public PresetAccountingTemplate initPresetAccountingTemplate() {
        //typePresetAccountingTemplates = typePresetAccountingTemplateService.getTypePresetAccountingTemplates(getInstance());
        return getInstance();
}

    @Override
    @Begin(flushMode = FlushModeType.MANUAL)
    public String select(PresetAccountingTemplate instance) {
        String outCome = super.select(instance);
        setTypePresetAccountingTemplates(typePresetAccountingTemplateService.getTypePresetAccountingTemplates(getInstance()));
        return outCome;
    }

    @Override
    protected String getDisplayNameProperty() {
        return "name";
    }

    @End
    @Override
    public String update(){

        Long currentVersion = (Long) getVersion(getInstance());
        try {
            typePresetAccountingTemplateService.updateTypePresetAccountingTemplates(typePresetAccountingTemplates);

            getService().update(getInstance());
        } catch (EntryDuplicatedException e) {
            addDuplicatedMessage();
            setVersion(getInstance(), currentVersion);
            return Outcome.REDISPLAY;
        } catch (ConcurrencyException e) {
            concurrencyLog();
            try {
                setInstance(getService().findById(getEntityClass(), getId(getInstance()), true));
            } catch (EntryNotFoundException e1) {
                entryNotFoundLog();
                addNotFoundMessage();
                return Outcome.FAIL;
            }
            addUpdateConcurrencyMessage();
            return Outcome.REDISPLAY;
        }
        addUpdatedMessage();
        return Outcome.SUCCESS;
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
    @Override
    @End
    public String delete() {
        try {
            getService().delete(getInstance());
            addDeletedMessage();
        } catch (ConcurrencyException e) {
            entryNotFoundLog();
            addDeleteConcurrencyMessage();
        } catch (ReferentialIntegrityException e) {
            referentialIntegrityLog();
            addDeleteReferentialIntegrityMessage();
        }

        return Outcome.NEXT;
    }

    public void setCashAccount(CashAccount cashAccount) {
        if (!selectedCashAccounts.contains(cashAccount.getId())) {

        selectedCashAccounts.add(cashAccount.getId());

            TypePresetAccountingTemplate item = new TypePresetAccountingTemplate();
            item.setCashAccount(cashAccount);
            item.setAccountCode(cashAccount.getAccountCode());
            item.setPresetAccountingTemplate(getInstance());
            item.setCompanyNumber(Constants.COD_COMPANY_DEFAULT);
            getTypePresetAccountingTemplates().add(item);
        }
    }

    public void removeCashAccount(TypePresetAccountingTemplate instance) {
        selectedCashAccounts.remove(instance.getCashAccount().getAccountCode());
        typePresetAccountingTemplates.remove(instance);
        typePresetAccountingTemplateService.deleteTypePresetAccountingTemplate(instance);
    }

    public List<TypePresetAccountingTemplate> getTypePresetAccountingTemplates() {
        return typePresetAccountingTemplates;
    }

    public void setTypePresetAccountingTemplates(List<TypePresetAccountingTemplate> typePresetAccountingTemplates) {
        this.typePresetAccountingTemplates = typePresetAccountingTemplates;
    }

    public List<CashAccountPk> getSelectedCashAccounts() {
        return selectedCashAccounts;
    }

    public void setSelectedCashAccounts(List<CashAccountPk> selectedCashAccounts) {
        this.selectedCashAccounts = selectedCashAccounts;
    }
}
