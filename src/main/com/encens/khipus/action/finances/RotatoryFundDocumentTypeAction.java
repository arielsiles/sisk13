package com.encens.khipus.action.finances;

import com.encens.khipus.exception.EntryDuplicatedException;
import com.encens.khipus.exception.EntryNotFoundException;
import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.model.finances.CashAccount;
import com.encens.khipus.model.finances.FinanceUser;
import com.encens.khipus.model.finances.RotatoryFundDocumentType;
import com.encens.khipus.model.finances.RotatoryFundType;
import com.encens.khipus.service.finances.RotatoryFundDocumentTypeService;
import com.encens.khipus.util.ValidatorUtil;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;
import org.jboss.seam.annotations.security.Restrict;

/**
 * @author
 * @version 2.30
 */
@Name("rotatoryFundDocumentTypeAction")
@Scope(ScopeType.CONVERSATION)
public class RotatoryFundDocumentTypeAction extends GenericAction<RotatoryFundDocumentType> {

    @In
    private RotatoryFundDocumentTypeService rotatoryFundDocumentTypeService;

    @Factory(value = "rotatoryFundDocumentType", scope = ScopeType.STATELESS)
    public RotatoryFundDocumentType init() {
        return getInstance();
    }

    @Override
    public RotatoryFundDocumentType createInstance() {
        RotatoryFundDocumentType result = super.createInstance();
        result.setActive(true);
        return result;
    }

    @Override
    protected String getDisplayNameMessage() {
        return getInstance().getFullName();
    }

    @Override
    @Begin(ifOutcome = com.encens.khipus.framework.action.Outcome.SUCCESS, flushMode = FlushModeType.MANUAL)
    public String select(RotatoryFundDocumentType instance) {
        try {
            setOp(OP_UPDATE);
            //Ensure the instance exists in the database, find it
            setInstance(rotatoryFundDocumentTypeService.load(instance));
            return com.encens.khipus.framework.action.Outcome.SUCCESS;

        } catch (EntryNotFoundException e) {
            addNotFoundMessage();
            return com.encens.khipus.framework.action.Outcome.FAIL;
        }
    }

    @Override
    @End
    @Restrict("#{s:hasPermission('ROTATORYFUNDDOCUMENTTYPE','CREATE')}")
    public String create() {
        try {
            rotatoryFundDocumentTypeService.createDocumentType(getInstance());
            addCreatedMessage();
            return com.encens.khipus.framework.action.Outcome.SUCCESS;
        } catch (EntryDuplicatedException e) {
            addDuplicatedMessage();
            return com.encens.khipus.framework.action.Outcome.FAIL;
        }
    }

    @Override
    @Restrict("#{s:hasPermission('ROTATORYFUNDDOCUMENTTYPE','CREATE')}")
    public void createAndNew() {
        try {
            rotatoryFundDocumentTypeService.createDocumentType(getInstance());
            addCreatedMessage();
            createInstance();
        } catch (EntryDuplicatedException e) {
            addDuplicatedMessage();
        }
    }

    @Override
    @End
    @Restrict("#{s:hasPermission('ROTATORYFUNDDOCUMENTTYPE','UPDATE')}")
    public String update() {
        String outcome = super.update();
        if (ValidatorUtil.isBlankOrNull(outcome)) {
            outcome = com.encens.khipus.framework.action.Outcome.FAIL;
        }
        return outcome;
    }

    @Override
    @End
    @Restrict("#{s:hasPermission('ROTATORYFUNDDOCUMENTTYPE','DELETE')}")
    public String delete() {
        return super.delete();
    }

    public boolean isOtherReceivables() {
        return null != getInstance().getRotatoryFundType() && getInstance().getRotatoryFundType().equals(RotatoryFundType.OTHER_RECEIVABLES);
    }

    public void assignNationalCashAccount(CashAccount cashAccount) {
        try {
            cashAccount = getService().findById(CashAccount.class, cashAccount.getId());
        } catch (EntryNotFoundException e) {
            entryNotFoundLog();
        }
        getInstance().setNationalCashAccount(cashAccount);
    }

    public void assignForeignCashAccount(CashAccount cashAccount) {
        try {
            cashAccount = getService().findById(CashAccount.class, cashAccount.getId());
        } catch (EntryNotFoundException e) {
            entryNotFoundLog();
        }
        getInstance().setForeignCashAccount(cashAccount);
    }

    public void assignAdjustmentNationalCashAccount(CashAccount cashAccount) {
        try {
            cashAccount = getService().findById(CashAccount.class, cashAccount.getId());
        } catch (EntryNotFoundException e) {
            entryNotFoundLog();
        }
        getInstance().setAdjustmentNationalCashAccount(cashAccount);
    }

    public void assignAdjustmentForeignCashAccount(CashAccount cashAccount) {
        try {
            cashAccount = getService().findById(CashAccount.class, cashAccount.getId());
        } catch (EntryNotFoundException e) {
            entryNotFoundLog();
        }
        getInstance().setAdjustmentForeignCashAccount(cashAccount);
    }

    public void clearNationalCashAccount() {
        getInstance().setNationalCashAccount(null);
    }

    public void clearForeignCashAccount() {
        getInstance().setForeignCashAccount(null);
    }

    public void clearAdjustmentNationalCashAccount() {
        getInstance().setAdjustmentNationalCashAccount(null);
    }

    public void clearAdjustmentForeignCashAccount() {
        getInstance().setAdjustmentForeignCashAccount(null);
    }

    public void cleanFinanceUser() {
        getInstance().setFinanceUser(null);
    }

    public void assignFinanceUser(FinanceUser financeUser) {
        getInstance().setFinanceUser(financeUser);
    }
}