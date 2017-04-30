package com.encens.khipus.action.warehouse;

import com.encens.khipus.action.common.FunctionAction;
import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.framework.action.Outcome;
import com.encens.khipus.model.finances.CashAccount;
import com.encens.khipus.model.warehouse.Group;
import com.encens.khipus.service.common.SequenceGeneratorService;
import com.encens.khipus.service.finances.CashAccountService;
import com.encens.khipus.service.warehouse.WarehouseCatalogService;
import com.encens.khipus.util.Constants;
import com.encens.khipus.util.MessageUtils;
import com.encens.khipus.util.ValidatorUtil;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.international.StatusMessage;

/**
 * @author
 * @version 2.0
 */

@Name("groupAction")
@Scope(ScopeType.CONVERSATION)
public class GroupAction extends GenericAction<Group> {

    @In(create = true)
    private FunctionAction functionAction;

    @In
    private WarehouseCatalogService warehouseCatalogService;

    @In
    private CashAccountService cashAccountService;

    @In
    private SequenceGeneratorService sequenceGeneratorService;

    @Create
    public void atCreateTime() {
        if (!isManaged()) {
            assignCode();
        }
    }

    private void assignCode() {
        getInstance().getId().setGroupCode(String.valueOf(sequenceGeneratorService.findNextSequenceValue(Constants.WAREHOUSE_GROUP_SEQUENCE)));
    }

    private void updateCode() {
        getInstance().getId().setGroupCode(String.valueOf(sequenceGeneratorService.nextValue(Constants.WAREHOUSE_GROUP_SEQUENCE)));
    }

    @Override
    @Restrict("#{s:hasPermission('GROUP','CREATE')}")
    @End
    public String create() {
        String validationOutcome = validate(getInstance().getInventoryAccount());
        if (!Outcome.SUCCESS.equals(validationOutcome)) {
            return validationOutcome;
        }
        updateCode();
        return super.create();
    }

    @Override
    @Restrict("#{s:hasPermission('GROUP','CREATE')}")
    public void createAndNew() {
        String validationOutcome = validate(getInstance().getInventoryAccount());
        if (Outcome.SUCCESS.equals(validationOutcome)) {
            updateCode();
            super.createAndNew();
            if (!functionAction.getHasSeverityErrorMessages()) {
                atCreateTime();
            }
        }
    }

    @Override
    @Restrict("#{s:hasPermission('GROUP','UPDATE')}")
    @End
    public String update() {
        String validationOutcome = validate(getInstance().getInventoryAccount());
        if (!Outcome.SUCCESS.equals(validationOutcome)) {
            return validationOutcome;
        }

        return super.update();
    }

    @Override
    @Restrict("#{s:hasPermission('GROUP','DELETE')}")
    @End
    public String delete() {
        return super.delete();
    }

    @Factory(value = "warehouseGroup", scope = ScopeType.STATELESS)
    @Restrict("#{s:hasPermission('GROUP','VIEW')}")
    public Group initGroup() {
        return getInstance();
    }

    @Override
    protected String getDisplayNameProperty() {
        return "name";
    }

    public void clearInventoryAccount() {
        getInstance().setInventoryAccount(null);
    }

    public void assignInventoryAccount(CashAccount cashAccount) {
        getInstance().setInventoryAccount(cashAccount.getAccountCode());
    }

    private String validate(String accountCode) {

        if (!cashAccountService.existsAccount(accountCode)) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "Common.error.notFound",
                    accountCode);
            return Outcome.REDISPLAY;
        }

        if (ValidatorUtil.isBlankOrNull(accountCode)) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "Common.required", MessageUtils.getMessage("Group.inventoryAccount"));
            return Outcome.REDISPLAY;
        }

        if (!isManaged() && !warehouseCatalogService.validateGroupCode(getInstance().getId().getGroupCode())) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "Common.error.duplicatedCode", getInstance().getId().getGroupCode());
            assignCode();
            return Outcome.REDISPLAY;
        }

        return Outcome.SUCCESS;
    }

    @Override
    protected void addDuplicatedMessage() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                "Warehouse.common.message.duplicated", getInstance().getId().getGroupCode());
    }
}
