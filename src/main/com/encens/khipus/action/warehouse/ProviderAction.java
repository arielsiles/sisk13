package com.encens.khipus.action.warehouse;

import com.encens.khipus.action.finances.FinanceProviderAction;
import com.encens.khipus.exception.ConcurrencyException;
import com.encens.khipus.exception.EntryDuplicatedException;
import com.encens.khipus.exception.EntryNotFoundException;
import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.framework.action.Outcome;
import com.encens.khipus.model.finances.ModuleProviderType;
import com.encens.khipus.model.finances.Provider;
import com.encens.khipus.service.finances.FinanceProviderService;
import com.encens.khipus.service.finances.ModuleProviderService;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;
import org.jboss.seam.annotations.security.Restrict;

import java.util.List;

/**
 * @author
 * @version 2.2
 */

@Name("providerAction")
@Scope(ScopeType.CONVERSATION)
public class ProviderAction extends GenericAction<Provider> {

    @In
    private FinanceProviderService financeProviderService;

    @In(create = true)
    private FinanceProviderAction financeProviderAction;

    @In
    private ModuleProviderService moduleProviderService;

    private List<ModuleProviderType> moduleProviderTypeList;

    @Factory(value = "provider", scope = ScopeType.STATELESS)
    @Restrict("#{s:hasPermission('WAREHOUSEPROVIDERMAN','VIEW')}")
    public Provider initProvider() {
        return getInstance();
    }

    public Provider getProvider() {
        return getInstance();
    }

    @Override
    @Begin(ifOutcome = Outcome.SUCCESS, flushMode = FlushModeType.MANUAL)
    @Restrict("#{s:hasPermission('WAREHOUSEPROVIDERMAN','VIEW')}")
    public String select(Provider provider) {
        String result = super.select(provider);
        if (com.encens.khipus.framework.action.Outcome.SUCCESS.equals(result)) {
            moduleProviderTypeList = moduleProviderService.readModuleProviders(provider);
        }

        return result;
    }

    @End
    @Restrict("#{s:hasPermission('WAREHOUSEPROVIDERMAN','UPDATE')}")
    public String updateProvider() {
        if (financeProviderAction.validate(getInstance())) {
            try {
                financeProviderService.updateProvider(getInstance());
                moduleProviderService.manageModuleProviders(getInstance(), moduleProviderTypeList);
                addUpdatedMessage();
                return Outcome.SUCCESS;
            } catch (EntryDuplicatedException e) {
                addDuplicatedMessage();
                return Outcome.REDISPLAY;
            } catch (EntryNotFoundException e) {
                addNotFoundMessage();
                return Outcome.FAIL;
            } catch (ConcurrencyException e) {
                addUpdateConcurrencyMessage();
                return Outcome.FAIL;
            }
        }
        return Outcome.REDISPLAY;
    }

    public List<ModuleProviderType> getModuleProviderTypeList() {
        return moduleProviderTypeList;
    }

    public void setModuleProviderTypeList(List<ModuleProviderType> moduleProviderTypeList) {
        this.moduleProviderTypeList = moduleProviderTypeList;
    }

    @Override
    protected String getDisplayNameProperty() {
        return "fullName";
    }
}
