package com.encens.khipus.action.fixedassets;

import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.model.fixedassets.FixedAssetLocation;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;

/**
 * @author
 * @version 2.28.2
 */
@Name("fixedAssetLocationAction")
@Scope(ScopeType.CONVERSATION)
public class FixedAssetLocationAction extends GenericAction<FixedAssetLocation> {

    @Factory(value = "fixedAssetLocation", scope = ScopeType.STATELESS)
    @Restrict("#{s:hasPermission('FIXEDASSETLOCATION','VIEW')}")
    public FixedAssetLocation initFixedAssetLocation() {
        return getInstance();
    }

    @Override
    protected String getDisplayNameProperty() {
        return "name";
    }
}
