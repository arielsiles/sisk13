package com.encens.khipus.action.employees;

import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.model.employees.Creditor;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

/**
 * Creditor action class
 *
 * @author Ariel Siles Encinas
 * @version 1.0
 */
@Name("creditorAction")
@Scope(ScopeType.CONVERSATION)
public class CreditorAction extends GenericAction<Creditor> {

    @Factory(value = "creditor", scope = ScopeType.STATELESS)
    public Creditor initCreditor() {
        return getInstance();
    }

    @Override
    public String getDisplayNameProperty() {
        return "name";
    }

}