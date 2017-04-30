package com.encens.khipus.action.contacts;

import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.model.contacts.MaritalStatus;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;

/**
 * Actions for Marital status
 *
 * @author:
 */

@Name("maritalStatusAction")
@Scope(ScopeType.CONVERSATION)
public class MaritalStatusAction extends GenericAction<MaritalStatus> {

    @Factory(value = "maritalStatus", scope = ScopeType.STATELESS)
    @Restrict("#{s:hasPermission('MARITALSTATUS','VIEW')}")
    public MaritalStatus initMaritalStatus() {
        return getInstance();
    }

    @Override
    protected String getDisplayNameProperty() {
        return "name";
    }
}
