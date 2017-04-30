package com.encens.khipus.action.contacts;

import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.model.contacts.Salutation;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;

/**
 * Actions for Salutation
 *
 * @author:
 */

@Name("salutationAction")
@Scope(ScopeType.CONVERSATION)
public class SalutationAction extends GenericAction<Salutation> {

    @Factory(value = "salutation", scope = ScopeType.STATELESS)
    @Restrict("#{s:hasPermission('SALUTATION','VIEW')}")
    public Salutation initSalutation() {
        return getInstance();
    }

    @Override
    protected String getDisplayNameProperty() {
        return "name";
    }
}
