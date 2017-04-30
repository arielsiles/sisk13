package com.encens.khipus.action.employees;

import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.model.finances.OrganizationalLevel;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;

/**
 * Charge action class
 *
 * @author Ariel Siles Encinas
 * @version 1.0
 */
@Name("organizationalLevelAction")
@Scope(ScopeType.CONVERSATION)
public class OrganizationalLevelAction extends GenericAction<OrganizationalLevel> {

    @Factory(value = "organizationalLevel", scope = ScopeType.STATELESS)
    @Restrict("#{s:hasPermission('ORGANIZATIONALLEVEL','VIEW')}")
    public OrganizationalLevel initOrganizationalLevel() {
        return getInstance();
    }

    @Override
    public String getDisplayNameProperty() {
        return "name";
    }
}