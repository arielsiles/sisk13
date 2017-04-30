package com.encens.khipus.action.warehouse;

import com.encens.khipus.action.common.FunctionAction;
import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.framework.action.Outcome;
import com.encens.khipus.model.warehouse.Group;
import com.encens.khipus.model.warehouse.SubGroup;
import com.encens.khipus.model.warehouse.SubGroupState;
import com.encens.khipus.service.common.SequenceGeneratorService;
import com.encens.khipus.service.warehouse.WarehouseCatalogService;
import com.encens.khipus.util.Constants;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.international.StatusMessage;

/**
 * @author
 * @version 2.0
 */
@Name("subGroupAction")
@Scope(ScopeType.CONVERSATION)
public class SubGroupAction extends GenericAction<SubGroup> {

    @In(create = true)
    private FunctionAction functionAction;

    @In
    private GroupAction groupAction;

    @In
    private WarehouseCatalogService warehouseCatalogService;

    @In
    private SequenceGeneratorService sequenceGeneratorService;

    private void assignCode() {
        getInstance().getId().setSubGroupCode(String.valueOf(sequenceGeneratorService.findNextSequenceValue(Constants.WAREHOUSE_SUBGROUP_SEQUENCE + "_" + getInstance().getId().getGroupCode())));
    }

    private void updateCode() {
        getInstance().getId().setSubGroupCode(String.valueOf(sequenceGeneratorService.nextValue(Constants.WAREHOUSE_SUBGROUP_SEQUENCE + "_" + getInstance().getId().getGroupCode())));
    }

    @Factory(value = "subGroup", scope = ScopeType.STATELESS)
    @Restrict("#{s:hasPermission('SUBGROUP','VIEW')}")
    public SubGroup initSubGroup() {
        return getInstance();
    }

    @Factory(value = "subGroupStates", scope = ScopeType.STATELESS)
    public SubGroupState[] getSubGroupStates() {
        return SubGroupState.values();
    }

    @Begin(nested = true, flushMode = FlushModeType.MANUAL)
    public String addSubGroup() {
        Group group = groupAction.getInstance();
        getInstance().getId().setGroupCode(group.getGroupCode());
        assignCode();
        getInstance().setState(SubGroupState.VIG);
        return Outcome.SUCCESS;
    }

    @Override
    @Begin(nested = true, flushMode = FlushModeType.MANUAL)
    @Restrict("#{s:hasPermission('SUBGROUP','VIEW')}")
    public String select(SubGroup instance) {
        return super.select(instance);
    }

    @Override
    @Restrict("#{s:hasPermission('SUBGROUP','CREATE')}")
    public String create() {
        if (!validate()) {
            return Outcome.REDISPLAY;
        }
        updateCode();
        String outcome = super.create();
        closeConversation(outcome);
        return outcome;
    }

    @Override
    @Restrict("#{s:hasPermission('SUBGROUP','CREATE')}")
    public void createAndNew() {
        if (validate()) {
            updateCode();
            super.createAndNew();
            if (!functionAction.getHasSeverityErrorMessages()) {
                addSubGroup();
            }
        }
    }

    @Override
    @Restrict("#{s:hasPermission('SUBGROUP','UPDATE')}")
    public String update() {
        String outcome = super.update();
        closeConversation(outcome);

        return outcome;
    }

    @Override
    @Restrict("#{s:hasPermission('SUBGROUP','DELETE')}")
    public String delete() {
        String outcome = super.delete();
        closeConversation(outcome);

        return outcome;
    }

    @Override
    @End(beforeRedirect = true)
    public String cancel() {
        return super.cancel();
    }

    @Override
    protected String getDisplayNameProperty() {
        return "name";
    }

    @Override
    protected void addDuplicatedMessage() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                "Warehouse.common.message.duplicated", getInstance().getId().getSubGroupCode());
    }

    @Override
    public SubGroup createInstance() {
        SubGroup subGroup = super.createInstance();
        Group group = groupAction.getInstance();
        subGroup.getId().setGroupCode(group.getGroupCode());
        return subGroup;
    }

    private Boolean validate() {
        Boolean valid = true;
        if (!isManaged() && !warehouseCatalogService.validateSubGroupCode(getInstance().getId().getGroupCode(), getInstance().getId().getSubGroupCode())) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "Common.error.duplicatedCode", getInstance().getId().getSubGroupCode());
            assignCode();
            valid = false;
        }
        return valid;
    }

}
