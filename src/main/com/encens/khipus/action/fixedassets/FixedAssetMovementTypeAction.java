package com.encens.khipus.action.fixedassets;

import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.model.fixedassets.FixedAssetMovementType;
import com.encens.khipus.model.fixedassets.FixedAssetMovementTypeEnum;
import com.encens.khipus.model.fixedassets.FixedAssetMovementTypeState;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;

/**
 * Actions for FixedAssetMovementTypeAction
 *
 * @author
 * @version 2.0
 */

@Name("fixedAssetMovementTypeAction")
@Scope(ScopeType.CONVERSATION)
public class FixedAssetMovementTypeAction extends GenericAction<FixedAssetMovementType> {

    @Factory(value = "fixedAssetMovementType", scope = ScopeType.STATELESS)
    @Restrict("#{s:hasPermission('FIXEDASSETMOVEMENTTYPE','VIEW')}")
    public FixedAssetMovementType initFixedAssetMovementType() {
        return getInstance();
    }

    @Override
    protected String getDisplayNameProperty() {
        return "description";
    }

    @Factory(value = "fixedAssetMovementTypeStates", scope = ScopeType.STATELESS)
    public FixedAssetMovementTypeState[] getFixedAssetMovementTypeStates() {
        return FixedAssetMovementTypeState.values();
    }

    @Factory(value = "fixedAssetMovementTypeEnums", scope = ScopeType.STATELESS)
    public FixedAssetMovementTypeEnum[] getFixedAssetMovementTypeEnums() {
        return FixedAssetMovementTypeEnum.values();
    }
}