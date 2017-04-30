package com.encens.khipus.action.finances;

import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.model.finances.CostCenterGroup;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;

/**
 * CostCenterGroup action class
 *
 * @author
 * @version 1.2.1
 */
@Name("costCenterGroupAction")
@Scope(ScopeType.CONVERSATION)
public class CostCenterGroupAction extends GenericAction<CostCenterGroup> {

    @Factory(value = "costCenterGroup", scope = ScopeType.STATELESS)
    @Restrict("#{s:hasPermission('COSTCENTERGROUP','VIEW')}")
    public CostCenterGroup initCostCenterGroup() {
        return getInstance();
    }

    @Override
    public String getDisplayNameProperty() {
        return "description";
    }
}