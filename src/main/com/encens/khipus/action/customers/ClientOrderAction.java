package com.encens.khipus.action.customers;

import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.model.customers.ClientOrder;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

/**
 * Actions for ClientOrder
 *
 * @author: Ariel Siles Encinas
 */

@Name("clientOrderAction")
@Scope(ScopeType.CONVERSATION)
public class ClientOrderAction extends GenericAction<ClientOrder> {

    @Factory(value = "clientOrder", scope = ScopeType.STATELESS)
    //@Restrict("#{s:hasPermission('CREDIT','VIEW')}")
    public ClientOrder initClientOrder() {
        return getInstance();
    }
}
