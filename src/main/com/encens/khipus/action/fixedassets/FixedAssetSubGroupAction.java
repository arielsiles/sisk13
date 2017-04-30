package com.encens.khipus.action.fixedassets;

import com.encens.khipus.exception.EntryNotFoundException;
import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.framework.action.Outcome;
import com.encens.khipus.model.finances.CashAccount;
import com.encens.khipus.model.fixedassets.FixedAssetGroup;
import com.encens.khipus.model.fixedassets.FixedAssetSubGroup;
import com.encens.khipus.service.common.SequenceGeneratorService;
import com.encens.khipus.service.fixedassets.FixedAssetService;
import com.encens.khipus.util.Constants;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.international.StatusMessage;

/**
 * Actions for FixedAssetSubGroupAction
 *
 * @author
 * @version 2.0
 */

@Name("fixedAssetSubGroupAction")
@Scope(ScopeType.CONVERSATION)
public class FixedAssetSubGroupAction extends GenericAction<FixedAssetSubGroup> {

    @In
    private FixedAssetService fixedAssetService;
    @In
    private SequenceGeneratorService sequenceGeneratorService;

    public void atCreateTime() {
        if (!isManaged()) {
            assignCode();
        }
    }

    private void assignCode() {
        if (getInstance().getId().getFixedAssetGroupCode() != null) {
            getInstance().getId().setFixedAssetSubGroupCode(String.valueOf(sequenceGeneratorService.findNextSequenceValue(Constants.FIXEDASSET_SUBGROUP_SEQUENCE + "_" + getInstance().getId().getFixedAssetGroupCode())));
        } else {
            getInstance().getId().setFixedAssetSubGroupCode(null);
        }
    }

    private void updateCode() {
        getInstance().getId().setFixedAssetSubGroupCode(String.valueOf(sequenceGeneratorService.nextValue(Constants.FIXEDASSET_SUBGROUP_SEQUENCE + "_" + getInstance().getId().getFixedAssetGroupCode())));
    }

    public void updateCurrentFixedAssetCode() {
        atCreateTime();
    }

    @Factory(value = "fixedAssetSubGroup", scope = ScopeType.STATELESS)
    @Restrict("#{s:hasPermission('FIXEDASSETSUBGROUP','VIEW')}")
    public FixedAssetSubGroup initFixedAssetSubGroup() {
        FixedAssetSubGroup fixedAssetSubGroup = getInstance();
        fixedAssetSubGroup.setFixedAssetNumber("0");
        return fixedAssetSubGroup;
    }

    @Override
    @Restrict("#{s:hasPermission('FIXEDASSETSUBGROUP','VIEW')}")
    @Begin(ifOutcome = Outcome.SUCCESS, flushMode = FlushModeType.MANUAL)
    public String select(FixedAssetSubGroup instance) {
        return super.select(instance);
    }

    @Override
    @Restrict("#{s:hasPermission('FIXEDASSETSUBGROUP','CREATE')}")
    @End
    public String create() {
        if (!validate()) {
            return Outcome.REDISPLAY;
        }
        updateCode();
        return super.create();
    }

    @Override
    @Restrict("#{s:hasPermission('FIXEDASSETSUBGROUP','CREATE')}")
    public void createAndNew() {
        if (validate()) {
            updateCode();
            super.createAndNew();
        }
    }

    @Override
    @Restrict("#{s:hasPermission('FIXEDASSETSUBGROUP','UPDATE')}")
    @End
    public String update() {
        return super.update();
    }

    @Override
    @Restrict("#{s:hasPermission('FIXEDASSETSUBGROUP','DELETE')}")
    @End
    public String delete() {
        return super.delete();
    }

    @Override
    protected String getDisplayNameProperty() {
        return "description";
    }

    public void assignOriginalValueCashAccount(CashAccount cashAccount) {
        try {
            cashAccount = getService().findById(CashAccount.class, cashAccount.getId());
            getInstance().setOriginalValueCashAccount(cashAccount);
        } catch (EntryNotFoundException e) {
            entryNotFoundErrorLog(e);
        }

    }

    public void clearOriginalValueCashAccount() {
        getInstance().setOriginalValueCashAccount(null);
    }

    public void assignImprovementCashAccount(CashAccount cashAccount) {
        try {
            cashAccount = getService().findById(CashAccount.class, cashAccount.getId());
            getInstance().setImprovementCashAccount(cashAccount);
        } catch (EntryNotFoundException e) {
            entryNotFoundErrorLog(e);
        }
    }

    public void clearImprovementCashAccount() {
        getInstance().setImprovementCashAccount(null);
    }

    public void assignAccumulatedDepreciationCashAccount(CashAccount cashAccount) {
        try {
            cashAccount = getService().findById(CashAccount.class, cashAccount.getId());
        } catch (EntryNotFoundException e) {
            entryNotFoundErrorLog(e);
        }

        getInstance().setAccumulatedDepreciationCashAccount(cashAccount);
    }

    public void clearAccumulatedDepreciationCashAccount() {
        getInstance().setAccumulatedDepreciationCashAccount(null);
    }

    public void assignWarehouseCashAccount(CashAccount cashAccount) {
        try {
            cashAccount = getService().findById(CashAccount.class, cashAccount.getId());
        } catch (EntryNotFoundException e) {
            entryNotFoundErrorLog(e);
        }

        getInstance().setWarehouseCashAccount(cashAccount);
    }

    public void clearWarehouseCashAccount() {

        getInstance().setWarehouseCashAccount(null);
    }

    public void assignExpenseCashAccount(CashAccount cashAccount) {
        try {
            cashAccount = getService().findById(CashAccount.class, cashAccount.getId());
        } catch (EntryNotFoundException e) {
            entryNotFoundErrorLog(e);
        }

        getInstance().setExpenseCashAccount(cashAccount);
    }

    public void clearExpenseCashAccount() {
        getInstance().setExpenseCashAccount(null);
    }

    public void assignFixedAssetGroup(FixedAssetGroup fixedAssetGroup) {
        getInstance().setFixedAssetGroup(fixedAssetGroup);
        updateCurrentFixedAssetCode();
    }

    public void clearFixedAssetGroup() {
        getInstance().setFixedAssetGroup(null);
        updateCurrentFixedAssetCode();
    }

    private Boolean validate() {
        Boolean valid = true;
        if (!isManaged() && !fixedAssetService.validateSubGroupCode(getInstance().getId().getFixedAssetGroupCode(), getInstance().getId().getFixedAssetSubGroupCode())) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "Common.error.duplicatedCode", getInstance().getId().getFixedAssetSubGroupCode());
            assignCode();
            valid = false;
        }

        if (getInstance().getOriginalValueCashAccount() == null) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR, "FixedAssetSubGroup.error.originalValueCashAccountRequired");
            valid = false;
        }

        if (getInstance().getAccumulatedDepreciationCashAccount() == null) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR, "FixedAssetSubGroup.error.accumulatedDepreciationCashAccountRequired");
            valid = false;
        }

        if (getInstance().getWarehouseCashAccount() == null) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR, "FixedAssetSubGroup.error.warehouseCashAccountRequired");
            valid = false;
        }

        if (getInstance().getExpenseCashAccount() == null) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR, "FixedAssetSubGroup.error.expenseCashAccountRequired");
            valid = false;
        }

        return valid;
    }
}