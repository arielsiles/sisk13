package com.encens.khipus.action.budget;

import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.framework.action.Outcome;
import com.encens.khipus.framework.service.GenericService;
import com.encens.khipus.model.budget.Classifier;
import com.encens.khipus.model.budget.ClassifierType;
import com.encens.khipus.model.finances.CashAccount;
import com.encens.khipus.service.budget.ClassifierService;
import com.encens.khipus.util.MessageUtils;
import com.encens.khipus.util.ValidatorUtil;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.international.StatusMessage;

/**
 * This class is a data model for burden list
 *
 * @author
 * @version 2.0
 */
@Name("accountingItemAction")
@Scope(ScopeType.CONVERSATION)
public class AccountingItemAction extends GenericAction<Classifier> {

    @In
    private ClassifierService classifierService;

    @Factory(value = "accountingItem", scope = ScopeType.STATELESS)
    @Restrict("#{s:hasPermission('CLASSIFIERS','VIEW')}")
    public Classifier initClassifier() {
        return getInstance();
    }

    @Override
    protected String getDisplayNameProperty() {
        return "name";
    }

    @Override
    protected GenericService getService() {
        return classifierService;
    }

    public void assignCashAccount(CashAccount cashAccount) {
        log.info("Assigning cash account code...... " + cashAccount.getAccountCode());
        getInstance().setAccountCode(cashAccount.getAccountCode());
    }

    public void clearCashAccount() {
        getInstance().setAccountCode(null);
    }

    @Override
    @End
    @Restrict("#{s:hasPermission('CLASSIFIERS','CREATE')}")
    public String create() {
        log.debug("Creating new AccountingItem .... in action......");
        if (ValidatorUtil.isBlankOrNull(getInstance().getAccountCode())) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR, "Common.required", MessageUtils.getMessage("ClassifierAccount.accountCode"));
            return Outcome.REDISPLAY;
        }
        getInstance().setType(ClassifierType.ACCOUNTING_ITEM);
        return super.create();
    }

    @Override
    @Restrict("#{s:hasPermission('CLASSIFIERS','CREATE')}")
    public void createAndNew() {
        if (ValidatorUtil.isBlankOrNull(getInstance().getAccountCode())) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR, "Common.required", MessageUtils.getMessage("ClassifierAccount.accountCode"));
        } else {
            getInstance().setType(ClassifierType.BURDEN);
            super.createAndNew();
        }
    }
}
