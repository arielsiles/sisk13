package com.encens.khipus.action.contacts;

import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.model.contacts.Country;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;

/**
 * @author:
 */
@Name("countryAction")
@Scope(ScopeType.CONVERSATION)
public class CountryAction extends GenericAction<Country> {

    @Factory(value = "country", scope = ScopeType.STATELESS)
    @Restrict("#{s:hasPermission('COUNTRY','VIEW')}")
    public Country initCountry() {
        return getInstance();
    }

    @Override
    protected String getDisplayNameProperty() {
        return "name";
    }
}
