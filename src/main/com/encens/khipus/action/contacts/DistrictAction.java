package com.encens.khipus.action.contacts;

import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.model.contacts.City;
import com.encens.khipus.model.contacts.Country;
import com.encens.khipus.model.contacts.Department;
import com.encens.khipus.model.contacts.District;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;


/**
 * Actions for District
 *
 * @author:
 */

@Name("districtAction")
@Scope(ScopeType.CONVERSATION)
public class DistrictAction extends GenericAction<District> {

    private Country country;
    private Department department;
    private City city;
    private boolean getCountryOnce = true;
    private boolean getDepartmentOnce = true;
    private boolean getCityOnce = true;

    @Factory(value = "district", scope = ScopeType.STATELESS)
    @Restrict("#{s:hasPermission('DISTRICT','VIEW')}")
    public District initDistrict() {
        return getInstance();
    }

    @Override
    protected String getDisplayNameProperty() {
        return "name";
    }

    public Country getCountry() {
        if (isManaged() && getCountryOnce) {
            country = getInstance().getZone().getCity().getDepartment().getCountry();
            getCountryOnce = false;
        }
        return country;
    }

    public void setCountry(Country country) {
        this.country = country;
    }

    public Department getDepartment() {
        if (isManaged() && getDepartmentOnce) {
            department = getInstance().getZone().getCity().getDepartment();
            getDepartmentOnce = false;
        }
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    public City getCity() {
        if (isManaged() && getCityOnce) {
            city = getInstance().getZone().getCity();
            getCityOnce = false;
        }
        return city;
    }

    public void setCity(City city) {
        this.city = city;
    }
}
