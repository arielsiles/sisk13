package com.encens.khipus.action.admin;

import com.encens.khipus.exception.EntryNotFoundException;
import com.encens.khipus.exception.finances.CompanyConfigurationNotFoundException;
import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.framework.action.Outcome;
import com.encens.khipus.model.finances.CashAccount;
import com.encens.khipus.model.finances.CompanyConfiguration;
import com.encens.khipus.model.finances.CostCenter;
import com.encens.khipus.model.finances.FinanceUser;
import com.encens.khipus.service.fixedassets.CompanyConfigurationService;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.core.Conversation;

/**
 * CompanySettingAction
 *
 * @author
 * @version 2.26
 */
@Name("companySettingAction")
@Scope(ScopeType.CONVERSATION)
@Restrict("#{s:hasPermission('COMPANYSETTING','VIEW')}")
public class CompanySettingAction extends GenericAction<CompanyConfiguration> {

    @In
    private CompanyConfigurationService companyConfigurationService;

    @Factory(value = "companySetting", scope = ScopeType.STATELESS)
    @Restrict("#{s:hasPermission('COMPANYSETTING','VIEW')}")
    public CompanyConfiguration initCompanyConfiguration() {
        return getInstance();
    }

    @Create
    @Begin(ifOutcome = Outcome.SUCCESS, flushMode = FlushModeType.MANUAL)
    public void loadCompanySettings() {
        Conversation.instance().changeFlushMode(FlushModeType.MANUAL);
        CompanyConfiguration companyConfiguration = null;
        try {
            companyConfiguration = companyConfigurationService.findCompanyConfiguration();
        } catch (CompanyConfigurationNotFoundException e) {
        }
        if (companyConfiguration != null) {
            setOp(OP_UPDATE);
            setInstance(companyConfiguration);
        }
    }

    @Restrict("#{s:hasPermission('COMPANYSETTING','UPDATE')}")
    public String saveChanges() {
        if (isManaged()) {
            return update();
        } else {
            String outcome = create();
            if (Outcome.SUCCESS.equals(outcome)) {
                refreshInstance();
                setOp(OP_UPDATE);
            }
            return outcome;
        }

    }

    public void clearExchangeRateBalanceCostCenter() {
        getInstance().setExchangeRateBalanceCostCenter(null);
    }

    public void assignCostCenter(CostCenter costCenter) {
        getInstance().setExchangeRateBalanceCostCenter(costCenter);
    }

    public void cleanDefaultAccountancyUser() {
        getInstance().setDefaultAccountancyUser(null);
    }

    public void assignDefaultAccountancyUser(FinanceUser financeUser) {
        getInstance().setDefaultAccountancyUser(financeUser);
    }

    public void cleanDefaultTreasuryUser() {
        getInstance().setDefaultTreasuryUser(null);
    }

    public void assignDefaultTreasuryUser(FinanceUser financeUser) {
        getInstance().setDefaultTreasuryUser(financeUser);
    }

    public void cleanDefaultPayableUser() {
        getInstance().setDefaultPayableFinanceUser(null);
    }

    public void assignDefaultPayableUser(FinanceUser financeUser) {
        getInstance().setDefaultPayableFinanceUser(financeUser);
    }

    public void assignDepositInTransitForeignCurrencyAccount(CashAccount cashAccount) {
        try {
            cashAccount = getService().findById(CashAccount.class, cashAccount.getId());
        } catch (EntryNotFoundException e) {
            entryNotFoundLog();
        }
        getInstance().setDepositInTransitForeignCurrencyAccount(cashAccount);
    }

    public void assignDepositInTransitNationalCurrencyAccount(CashAccount cashAccount) {
        try {
            cashAccount = getService().findById(CashAccount.class, cashAccount.getId());
        } catch (EntryNotFoundException e) {
            entryNotFoundLog();
        }
        getInstance().setDepositInTransitNationalCurrencyAccount(cashAccount);
    }

    public void clearDepositInTransitForeignCurrencyAccount() {
        getInstance().setDepositInTransitForeignCurrencyAccount(null);
    }

    public void clearDepositInTransitNationalCurrencyAccount() {
        getInstance().setDepositInTransitNationalCurrencyAccount(null);
    }

}
