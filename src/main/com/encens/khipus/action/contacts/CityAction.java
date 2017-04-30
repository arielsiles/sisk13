package com.encens.khipus.action.contacts;

import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.model.contacts.City;
import com.encens.khipus.model.contacts.Country;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;

/**
 * Actions for City
 *
 * @author:
 */

@Name("cityAction")
@Scope(ScopeType.CONVERSATION)
public class CityAction extends GenericAction<City> {

    private Country country;
    private boolean getCountryOnce = true;

    @Factory(value = "city", scope = ScopeType.STATELESS)
    @Restrict("#{s:hasPermission('CITY','VIEW')}")
    public City initCity() {
        return getInstance();
    }

    @Override
    protected String getDisplayNameProperty() {
        return "name";
    }

    public Country getCountry() {
        if (isManaged() && getCountryOnce) {
            country = getInstance().getDepartment().getCountry();
            getCountryOnce = false;
        }
        return country;
    }

    public void setCountry(Country country) {
        this.country = country;
    }
}
