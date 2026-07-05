package com.encens.khipus.action.sales;

import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.model.sales.CommercialCostCenter;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;

/**
 * Commercial cost center catalog action.
 *
 * @author
 * @version 1.0
 */
@Name("commercialCostCenterAction")
@Scope(ScopeType.CONVERSATION)
public class CommercialCostCenterAction extends GenericAction<CommercialCostCenter> {

    @Factory(value = "commercialCostCenter", scope = ScopeType.STATELESS)
    @Restrict("#{s:hasPermission('COMMERCIALCOSTCENTER','VIEW')}")
    public CommercialCostCenter initCommercialCostCenter() {
        return getInstance();
    }

    @Override
    public String getDisplayNameProperty() {
        return "description";
    }
}
