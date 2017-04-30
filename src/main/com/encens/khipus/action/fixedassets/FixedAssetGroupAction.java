package com.encens.khipus.action.fixedassets;

import com.encens.khipus.action.common.FunctionAction;
import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.framework.action.Outcome;
import com.encens.khipus.model.fixedassets.FixedAssetGroup;
import com.encens.khipus.service.common.SequenceGeneratorService;
import com.encens.khipus.service.fixedassets.FixedAssetService;
import com.encens.khipus.util.Constants;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.international.StatusMessage;

/**
 * Actions for FixedAssetGroupAction
 *
 * @author
 * @version 2.0
 */

@Name("fixedAssetGroupAction")
@Scope(ScopeType.CONVERSATION)
public class FixedAssetGroupAction extends GenericAction<FixedAssetGroup> {

    @In(create = true)
    private FunctionAction functionAction;
    @In
    private FixedAssetService fixedAssetService;
    @In
    private SequenceGeneratorService sequenceGeneratorService;

    @Create
    public void atCreateTime() {
        if (!isManaged()) {
            assignCode();
        }
    }

    private void assignCode() {
        getInstance().getId().setGroupCode(String.valueOf(sequenceGeneratorService.findNextSequenceValue(Constants.FIXEDASSET_GROUP_SEQUENCE)));
    }

    private void updateCode() {
        getInstance().getId().setGroupCode(String.valueOf(sequenceGeneratorService.nextValue(Constants.FIXEDASSET_GROUP_SEQUENCE)));
    }

    @Factory(value = "fixedAssetGroup", scope = ScopeType.STATELESS)
    @Restrict("#{s:hasPermission('FIXEDASSETGROUP','VIEW')}")
    public FixedAssetGroup initFixedAssetGroup() {
        return getInstance();
    }

    @Override
    protected String getDisplayNameProperty() {
        return "description";
    }

    @Override
    @Restrict("#{s:hasPermission('FIXEDASSETGROUP','CREATE')}")
    @End
    public String create() {
        if (!validate()) {
            return Outcome.REDISPLAY;
        }
        updateCode();
        return super.create();
    }

    @Override
    @Restrict("#{s:hasPermission('FIXEDASSETGROUP','CREATE')}")
    public void createAndNew() {
        if (validate()) {
            updateCode();
            super.createAndNew();
            if (!functionAction.getHasSeverityErrorMessages()) {
                atCreateTime();
            }
        }
    }

    @Override
    @Restrict("#{s:hasPermission('FIXEDASSETGROUP','UPDATE')}")
    @End
    public String update() {
        return super.update();
    }

    @Override
    @Restrict("#{s:hasPermission('FIXEDASSETGROUP','DELETE')}")
    @End
    public String delete() {
        return super.delete();
    }

    private Boolean validate() {
        Boolean valid = true;
        if (!isManaged() && !fixedAssetService.validateGroupCode(getInstance().getId().getGroupCode())) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "Common.error.duplicatedCode", getInstance().getId().getGroupCode());
            assignCode();
            valid = false;
        }
        return valid;
    }
}