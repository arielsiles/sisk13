package com.encens.khipus.action.customers;

import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.model.customers.Client;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

/**
 * @author
 * @version 2.2
 */

@Name("clientAction")
@Scope(ScopeType.CONVERSATION)
public class ClientAction extends GenericAction<Client> {

    private String clientName;

    @Factory(value = "client", scope = ScopeType.STATELESS)
    public Client initClient() {
        return getInstance();
    }

    public Client getClient() {
        return getInstance();
    }

    @Override
    protected String getDisplayNameProperty() {
        return "fullName";
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }



}
