package com.encens.khipus.action.contacts;

import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.model.contacts.*;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;

/**
 * Actions for Street
 *
 * @author:
 */

@Name("streetAction")
@Scope(ScopeType.CONVERSATION)
public class StreetAction extends GenericAction<Street> {

    private Country country;
    private Department department;
    private City city;
    private Zone zone;
    private boolean getCountryOnce = true;
    private boolean getDepartmentOnce = true;
    private boolean getCityOnce = true;
    private boolean getZoneOnce = true;

    @Factory(value = "street", scope = ScopeType.STATELESS)
    @Restrict("#{s:hasPermission('STREET','VIEW')}")
    public Street initStreet() {
        return getInstance();
    }

    @Override
    protected String getDisplayNameProperty() {
        return "name";
    }

    public Country getCountry() {
        if (isManaged() && getCountryOnce) {
            country = getInstance().getDistrict().getZone().getCity().getDepartment().getCountry();
            getCountryOnce = false;
        }
        return country;
    }

    public void setCountry(Country country) {
        this.country = country;
    }

    public Department getDepartment() {
        if (isManaged() && getDepartmentOnce) {
            department = getInstance().getDistrict().getZone().getCity().getDepartment();
            getDepartmentOnce = false;
        }
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    public City getCity() {
        if (isManaged() && getCityOnce) {
            city = getInstance().getDistrict().getZone().getCity();
            getCityOnce = false;
        }
        return city;
    }

    public void setCity(City city) {
        this.city = city;
    }

    public Zone getZone() {
        if (isManaged() && getZoneOnce) {
            zone = getInstance().getDistrict().getZone();
            getZoneOnce = false;
        }
        return zone;
    }

    public void setZone(Zone zone) {
        this.zone = zone;
    }
}
