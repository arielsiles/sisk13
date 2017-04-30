package com.encens.khipus.action.fixedassets;

import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.model.fixedassets.FixedAssetMovement;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

/**
 * Actions for FixedAssetMovementAction
 *
 * @author
 * @version 2.0
 */

@Name("fixedAssetMovementAction")
@Scope(ScopeType.CONVERSATION)
public class FixedAssetMovementAction extends GenericAction<FixedAssetMovement> {

    @Factory(value = "fixedAssetMovement", scope = ScopeType.STATELESS)
    public FixedAssetMovement initFixedAssetMovement() {
        return getInstance();
    }

    @Override
    protected String getDisplayNameProperty() {
        return "cause";
    }
}