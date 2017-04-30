package com.encens.khipus.action.contacts;

import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.model.contacts.Country;
import com.encens.khipus.model.contacts.Department;
import com.encens.khipus.model.contacts.Zone;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;


/**
 * Actions for Zone
 *
 * @author:
 */

@Name("zoneAction")
@Scope(ScopeType.CONVERSATION)
public class ZoneAction extends GenericAction<Zone> {

    private Country country;
    private Department department;
    private boolean getCountryOnce = true;
    private boolean getDepartmentOnce = true;

    @Factory(value = "zone", scope = ScopeType.STATELESS)
    @Restrict("#{s:hasPermission('ZONE','VIEW')}")
    public Zone initZone() {
        return getInstance();
    }

    @Override
    protected String getDisplayNameProperty() {
        return "name";
    }

    public Country getCountry() {
        if (isManaged() && getCountryOnce) {
            country = getInstance().getCity().getDepartment().getCountry();
            getCountryOnce = false;
        }
        return country;
    }

    public void setCountry(Country country) {
        this.country = country;
    }

    public Department getDepartment() {
        if (isManaged() && getDepartmentOnce) {
            department = getInstance().getCity().getDepartment();
            getDepartmentOnce = false;
        }
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }
}
