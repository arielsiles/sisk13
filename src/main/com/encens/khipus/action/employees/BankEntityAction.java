package com.encens.khipus.action.employees;

import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.model.employees.BankEntity;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;

/**
 * Actions for BANK_ENTITY
 *
 * @author
 */

@Name("bankEntityAction")
@Scope(ScopeType.CONVERSATION)
public class BankEntityAction extends GenericAction<BankEntity> {

    @Factory(value = "bankEntity", scope = ScopeType.STATELESS)
    @Restrict("#{s:hasPermission('BANKENTITY','VIEW')}")
    public BankEntity initBankEntity() {
        return getInstance();
    }

    @Override
    protected String getDisplayNameProperty() {
        return "name";
    }
}