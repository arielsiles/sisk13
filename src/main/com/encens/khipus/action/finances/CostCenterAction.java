package com.encens.khipus.action.finances;

import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.model.finances.CostCenter;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;

/**
 * CostCenter action class
 *
 * @author
 * @version 1.2.1
 */
@Name("costCenterAction")
@Scope(ScopeType.CONVERSATION)
public class CostCenterAction extends GenericAction<CostCenter> {

    @Factory(value = "costCenter", scope = ScopeType.STATELESS)
    @Restrict("#{s:hasPermission('COSTCENTER','VIEW')}")
    public CostCenter initCostCenter() {
        return getInstance();
    }

    @Override
    public String getDisplayNameProperty() {
        return "description";
    }
}