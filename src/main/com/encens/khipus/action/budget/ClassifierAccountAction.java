package com.encens.khipus.action.budget;

import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.framework.action.Outcome;
import com.encens.khipus.model.budget.Classifier;
import com.encens.khipus.model.budget.ClassifierAccount;
import com.encens.khipus.model.finances.CashAccount;
import com.encens.khipus.util.MessageUtils;
import com.encens.khipus.util.ValidatorUtil;
import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.international.StatusMessage;

/**
 * ClassifierAccountAction
 *
 * @author
 * @version 2.1
 */
@Name("classifierAccountAction")
@Scope(ScopeType.CONVERSATION)
public class ClassifierAccountAction extends GenericAction<ClassifierAccount> {

    private String currentView;
    private String burdenView = "BurdenView";
    private String accountingItemView = "AccountingItemView";

    @Factory(value = "classifierAccount", scope = ScopeType.STATELESS)
    @Restrict("#{s:hasPermission('CLASSIFIERACCOUNT','VIEW')}")
    public ClassifierAccount initClassifier() {
        return getInstance();
    }

    @Override
    protected String getDisplayNameProperty() {
        return "accountCode";
    }

    public void assignCashAccount(CashAccount cashAccount) {
        log.info("Assigning cash account code...... " + cashAccount.getAccountCode());
        getInstance().setAccountCode(cashAccount.getAccountCode());
    }

    public void clearCashAccount() {
        getInstance().setAccountCode(null);
    }

    public void setDefaultValues() {
        if (isFromBurdenView()) {
            getInstance().setClassifier((Classifier) Component.getInstance("burden"));
        } else if (isFromAccountingItem()) {
            getInstance().setClassifier((Classifier) Component.getInstance("accountingItem"));
        }
    }

    public String getCurrentView() {
        return currentView;
    }

    public void setCurrentView(String currentView) {
        this.currentView = currentView;
    }

    public Boolean isFromBurdenView() {
        return burdenView.equals(getCurrentView());
    }

    public Boolean isFromAccountingItem() {
        return accountingItemView.equals(getCurrentView());
    }

    public String getOutcome(String currentOutCome) {
        return Outcome.SUCCESS.equals(currentOutCome) || Outcome.CANCEL.equals(currentOutCome) ? getCurrentView() : currentOutCome;
    }


    @Begin(nested = true)
    @Restrict("#{s:hasPermission('CLASSIFIERACCOUNT','UPDATE')}")
    public String newAssignToBurden() {
        setCurrentView(burdenView);
        return Outcome.SUCCESS;
    }

    @Begin(nested = true)
    @Restrict("#{s:hasPermission('CLASSIFIERACCOUNT','UPDATE')}")
    public String newAssignToAccountingItem() {
        setCurrentView(accountingItemView);
        return Outcome.SUCCESS;
    }

    @Begin(nested = true, flushMode = FlushModeType.MANUAL)
    @Restrict("#{s:hasPermission('CLASSIFIERACCOUNT','VIEW')}")
    public String selectAssignToBurden(ClassifierAccount instance) {
        setCurrentView(burdenView);
        return super.select(instance);
    }

    @Begin(nested = true, flushMode = FlushModeType.MANUAL)
    @Restrict("#{s:hasPermission('CLASSIFIERACCOUNT','VIEW')}")
    public String selectAssignToAccountingItem(ClassifierAccount instance) {
        setCurrentView(accountingItemView);
        return super.select(instance);
    }

    @Override
    @End(beforeRedirect = true)
    @Restrict("#{s:hasPermission('CLASSIFIERACCOUNT','CREATE')}")
    public String create() {
        if (ValidatorUtil.isBlankOrNull(getInstance().getAccountCode())) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR, "Common.required", MessageUtils.getMessage("ClassifierAccount.accountCode"));
            return Outcome.REDISPLAY;
        }
        setDefaultValues();
        return getOutcome(super.create());
    }

    @Override
    @Restrict("#{s:hasPermission('CLASSIFIERACCOUNT','CREATE')}")
    public void createAndNew() {
        if (ValidatorUtil.isBlankOrNull(getInstance().getAccountCode())) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR, "Common.required", MessageUtils.getMessage("ClassifierAccount.accountCode"));
        } else {
            setDefaultValues();
            super.createAndNew();
        }
    }

    @Override
    @End(beforeRedirect = true)
    @Restrict("#{s:hasPermission('CLASSIFIERACCOUNT','UPDATE')}")
    public String update() {
        if (ValidatorUtil.isBlankOrNull(getInstance().getAccountCode())) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR, "Common.required", MessageUtils.getMessage("ClassifierAccount.accountCode"));
            return Outcome.REDISPLAY;
        }
        setDefaultValues();
        return getOutcome(super.update());
    }

    @Override
    @End(beforeRedirect = true)
    @Restrict("#{s:hasPermission('CLASSIFIERACCOUNT','DELETE')}")
    public String delete() {
        return getOutcome(super.delete());
    }

    @Override
    @End(beforeRedirect = true)
    public String cancel() {
        return getOutcome(super.cancel());
    }
}
