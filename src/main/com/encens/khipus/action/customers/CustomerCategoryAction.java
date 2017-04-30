package com.encens.khipus.action.customers;

import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.model.customers.CustomerCategory;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;

/**
 * Actions for Customer category
 *
 * @author:
 */

@Name("customerCategoryAction")
@Scope(ScopeType.CONVERSATION)
public class CustomerCategoryAction extends GenericAction<CustomerCategory> {

    @Factory(value = "customerCategory", scope = ScopeType.STATELESS)
    @Restrict("#{s:hasPermission('CUSTOMERCATEGORY','VIEW')}")
    public CustomerCategory initCustomerCategory() {
        return getInstance();
    }

    @Override
    protected String getDisplayNameProperty() {
        return "name";
    }
}
