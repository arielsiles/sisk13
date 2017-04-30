package com.encens.khipus.action.contacts;

import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.model.contacts.Department;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;

/**
 * @author
 */
@Name("departmentAction")
@Scope(ScopeType.CONVERSATION)
public class DepartmentAction extends GenericAction<Department> {

    @Factory(value = "department", scope = ScopeType.STATELESS)
    @Restrict("#{s:hasPermission('DEPARTMENT','VIEW')}")
    public Department initDepartment() {
        return getInstance();
    }

    @Override
    protected String getDisplayNameProperty() {
        return "name";
    }
}
