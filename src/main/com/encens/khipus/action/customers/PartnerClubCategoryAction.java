package com.encens.khipus.action.customers;

import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.model.customers.PartnerClubCategory;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;

/**
 * @author:
 */
@Name("partnerClubCategoryAction")
@Scope(ScopeType.CONVERSATION)
public class PartnerClubCategoryAction extends GenericAction<PartnerClubCategory> {

    @Factory(value = "partnerClubCategory", scope = ScopeType.STATELESS)
    @Restrict("#{s:hasPermission('PARTNERCLUBCATEGORY','VIEW')}")
    public PartnerClubCategory initPartnerClubCategory() {
        return getInstance();
    }

    @Override
    protected String getDisplayNameProperty() {
        return "name";
    }
}
