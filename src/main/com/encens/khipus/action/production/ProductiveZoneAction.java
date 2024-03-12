package com.encens.khipus.action.production;

import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.model.contacts.Department;
import com.encens.khipus.model.production.ProductiveZone;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

@Name("productiveZoneAction")
@Scope(ScopeType.CONVERSATION)
public class ProductiveZoneAction extends GenericAction<ProductiveZone> {

    private Department department;

    //TODO change the name initContinent
    @Factory(value = "productiveZone", scope = ScopeType.STATELESS)
    public ProductiveZone initContinent() {
        return getInstance();
    }

    @Override
    protected String getDisplayNameProperty() {
        return "name";
    }

    public Department getDepartment() {
        if (isManaged()) {
            department = getInstance().getCity().getDepartment();
        }
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }
}
