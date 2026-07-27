package com.encens.khipus.action.warehouse;

import com.encens.khipus.action.finances.FinanceProviderAction;
import com.encens.khipus.exception.ConcurrencyException;
import com.encens.khipus.exception.EntryDuplicatedException;
import com.encens.khipus.exception.EntryNotFoundException;
import com.encens.khipus.exception.finances.CompanyAccountNotConfiguredException;
import com.encens.khipus.exception.finances.CompanyConfigurationNotFoundException;
import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.framework.action.Outcome;
import com.encens.khipus.model.finances.CashAccount;
import com.encens.khipus.model.finances.CompanyConfiguration;
import com.encens.khipus.model.finances.FinancesEntity;
import com.encens.khipus.model.finances.ModuleProviderType;
import com.encens.khipus.model.finances.Provider;
import com.encens.khipus.service.finances.FinanceProviderService;
import com.encens.khipus.service.finances.ModuleProviderService;
import com.encens.khipus.service.fixedassets.CompanyConfigurationService;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.international.StatusMessage;

import java.util.ArrayList;
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

    @In
    private CompanyConfigurationService companyConfigurationService;

    private List<ModuleProviderType> moduleProviderTypeList;

    // Destino de la cuenta elegida en el modal del plan de cuentas: "PAYABLE" o "RECEIVABLE".
    private String accountTarget;

    @Factory(value = "provider", scope = ScopeType.STATELESS)
    @Restrict("#{s:hasPermission('WAREHOUSEPROVIDERMAN','VIEW')}")
    public Provider initProvider() {
        Provider providerInstance = getInstance();
        // Alta: inicializa la entidad financiera (el form no se dibuja sin ella) y
        // pre-selecciona el modulo actual (WAREHOUSE) en el pickList.
        if (!isManaged() && providerInstance.getEntity() == null) {
            providerInstance.setEntity(new FinancesEntity());
            if (moduleProviderTypeList == null) {
                moduleProviderTypeList = new ArrayList<ModuleProviderType>();
                ModuleProviderType defaultModuleProviderType = financeProviderAction.findModuleProviderType();
                if (defaultModuleProviderType != null) {
                    moduleProviderTypeList.add(defaultModuleProviderType);
                }
            }
            // Cuenta por pagar por defecto (editable): la misma que usa la contabilidad.
            // Best-effort: si la config no esta lista, queda vacia y el usuario la elige.
            try {
                CompanyConfiguration companyConfiguration = companyConfigurationService.findCompanyConfiguration();
                providerInstance.setPayableAccount(companyConfiguration.requireAccountPayableSupplier());
            } catch (CompanyConfigurationNotFoundException e) {
                // sin default; campo obligatorio, el usuario elige
            } catch (CompanyAccountNotConfiguredException e) {
                // sin default; campo obligatorio, el usuario elige
            }
        }
        return providerInstance;
    }

    public void prepareAccountTarget(String accountTarget) {
        this.accountTarget = accountTarget;
    }

    public void assignAccount(CashAccount cashAccount) {
        if ("RECEIVABLE".equals(accountTarget)) {
            getInstance().setReceivableAccount(cashAccount);
        } else {
            getInstance().setPayableAccount(cashAccount);
        }
    }

    public void clearPayableAccount() {
        getInstance().setPayableAccount(null);
    }

    public void clearReceivableAccount() {
        getInstance().setReceivableAccount(null);
    }

    @End
    @Restrict("#{s:hasPermission('WAREHOUSEPROVIDERMAN','CREATE')}")
    public String createProvider() {
        if (financeProviderAction.validate(getInstance())) {
            try {
                // La cuenta por pagar es obligatoria; se respeta la elegida y solo se
                // completa con el default de la empresa si viniera vacia.
                if (getInstance().getPayableAccountCode() == null || getInstance().getPayableAccountCode().trim().isEmpty()) {
                    CompanyConfiguration companyConfiguration = companyConfigurationService.findCompanyConfiguration();
                    getInstance().setPayableAccountCode(companyConfiguration.requireAccountPayableSupplier().getAccountCode());
                }
                financeProviderService.createProvider(getInstance(), null);
                moduleProviderService.manageModuleProviders(getInstance(), moduleProviderTypeList);
                addCreatedMessage();
                return Outcome.SUCCESS;
            } catch (CompanyConfigurationNotFoundException e) {
                facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR, "CompanyConfiguration.notFound");
                return Outcome.REDISPLAY;
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
