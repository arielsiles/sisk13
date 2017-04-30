package com.encens.khipus.action.employees;

import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.model.employees.ContractMode;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

/**
 * Actions for ContractMode
 *
 * @author
 */

@Name("contractModeAction")
@Scope(ScopeType.CONVERSATION)
public class ContractModeAction extends GenericAction<ContractMode> {
    @Factory(value = "contractMode", scope = ScopeType.STATELESS)
    public ContractMode initContractMode() {
        return getInstance();
    }

    @Override
    protected String getDisplayNameProperty() {
        return "name";
    }
}