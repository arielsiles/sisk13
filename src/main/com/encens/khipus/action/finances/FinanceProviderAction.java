package com.encens.khipus.action.finances;

import com.encens.khipus.exception.ConcurrencyException;
import com.encens.khipus.exception.EntryDuplicatedException;
import com.encens.khipus.exception.EntryNotFoundException;
import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.framework.action.Outcome;
import com.encens.khipus.model.finances.FinancesEntity;
import com.encens.khipus.model.finances.ModuleProviderType;
import com.encens.khipus.model.finances.Provider;
import com.encens.khipus.model.finances.ProviderPk;
import com.encens.khipus.service.finances.CashAccountService;
import com.encens.khipus.service.finances.FinanceProviderService;
import com.encens.khipus.util.Constants;
import com.encens.khipus.util.ELEvaluator;
import com.encens.khipus.util.JSFUtil;
import com.encens.khipus.util.ValidatorUtil;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;
import org.jboss.seam.core.Manager;
import org.jboss.seam.international.StatusMessage;

/**
 * @author
 * @version 2.25
 */
@Name("financeProviderAction")
@Scope(ScopeType.CONVERSATION)
public class FinanceProviderAction extends GenericAction<Provider> {

    @In
    private FinanceProviderService financeProviderService;

    @In
    private CashAccountService cashAccountService;

    @In(create = true)
    private ELEvaluator elEvaluator;

    private String postCreateAction;

    private String postUpdateAction;


    @Factory(value = "financeProvider", scope = ScopeType.STATELESS)
    public Provider initProvider() {
        return getInstance();
    }

    @Begin(nested = true, flushMode = FlushModeType.MANUAL)
    public void newInstanceInModalPanel() {
        setOp(OP_CREATE);
        createInstance();
        getInstance().setEntity(new FinancesEntity());
    }

    @Begin(nested = true, flushMode = FlushModeType.MANUAL)
    public void selectInstanceByEntityInModalPanel(FinancesEntity financesEntity) {
        setOp(OP_UPDATE);
        try {
            setInstance(financeProviderService.findById(new ProviderPk(Constants.defaultCompanyNumber, financesEntity.getId().toString())));
        } catch (EntryNotFoundException e) {
            getInstance().setEntity(financesEntity);
        }
    }

    @Begin(nested = true, flushMode = FlushModeType.MANUAL)
    public void selectInstanceInModalPanel(Provider provider) {
        setOp(OP_UPDATE);
        try {
            setInstance(financeProviderService.findById(provider.getId()));
        } catch (EntryNotFoundException e) {
            throw new RuntimeException("Cannot read the related provider instance from the database, "
                    + "please check the database integrity", e);
        }
    }

    public void createFinanceProvider() {
        if (validate(getInstance())) {
            try {
                financeProviderService.createProvider(getInstance(), findModuleProviderType());
                elEvaluator.evaluateMethodBinding(getPostCreateAction());
                Manager.instance().endConversation(true);
            } catch (ConcurrencyException e) {
                addUpdateConcurrencyMessage();
            } catch (EntryDuplicatedException e) {
                addDuplicatedMessage();
            }
        }
    }

    public void updateFinanceProvider() {
        if (validate(getInstance())) {
            try {
                financeProviderService.updateProvider(getInstance());
                elEvaluator.evaluateMethodBinding(getPostUpdateAction());
                Manager.instance().endConversation(true);
            } catch (ConcurrencyException e) {
                selectInstanceInModalPanel(getInstance());
                addUpdateConcurrencyMessage();
            } catch (EntryDuplicatedException e) {
                selectInstanceInModalPanel(getInstance());
                addDuplicatedMessage();
            }
        }
    }

    public Boolean validate(Provider provider) {
        Boolean valid = true;
        if (!financeProviderService.validateAcronym(provider.getEntity())) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "FinanceProvider.message.duplicatedAcronym", provider.getEntity().getAcronym());
            valid = false;
        }
        if (ValidatorUtil.isBlankOrNull(provider.getPayableAccountCode()) ||
                provider.getPayableAccount() == null ||
                !cashAccountService.existsAccount(provider.getPayableAccountCode())) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "FinanceProvider.message.cashAccount");
            valid = false;
        }


        return valid;
    }

    public String cancel() {
        Manager.instance().endConversation(true);
        return Outcome.REDISPLAY;
    }

    public String getPostCreateAction() {
        return postCreateAction;
    }

    public void setPostCreateAction(String postCreateAction) {
        this.postCreateAction = postCreateAction;
    }

    public String getPostUpdateAction() {
        return postUpdateAction;
    }

    public void setPostUpdateAction(String postUpdateAction) {
        this.postUpdateAction = postUpdateAction;
    }

    public ModuleProviderType findModuleProviderType() {
        ModuleProviderType moduleProviderType = null;
        String requestURI = JSFUtil.getHttpServletRequest().getRequestURI();

        if (!ValidatorUtil.isBlankOrNull(requestURI)) {
            if (requestURI.contains(Constants.WAREHOUSE_MODULE_PATH)) {
                moduleProviderType = ModuleProviderType.WAREHOUSE;
            } else if (requestURI.contains(Constants.FIXEDASSET_MODULE_PATH)) {
                moduleProviderType = ModuleProviderType.FIXEDASSET;
            }
        }

        return moduleProviderType;
    }
}
