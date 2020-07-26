package com.encens.khipus.action.customers;

import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.framework.action.Outcome;
import com.encens.khipus.model.customers.Client;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;

/**
 * @author
 * @version 2.2
 */

@Name("clientAction")
@Scope(ScopeType.CONVERSATION)
public class ClientAction extends GenericAction<Client> {

    private String clientName;
    private Boolean personFlag = Boolean.TRUE;

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

    @Override
    @Begin(ifOutcome = Outcome.SUCCESS, flushMode = FlushModeType.MANUAL)
    public String select(Client instance) {
        String outCome = super.select(instance);
        return outCome;
    }

    @End
    @Override
    public String create() {

        getInstance().setCommission(0.0);
        getInstance().setGuarantee(0.0);
        if (getInstance().getPersonFlag())
            getInstance().setPersonType("cliente");
        else
            getInstance().setPersonType("institucion");

        if (getInstance().getName() == null)
            getInstance().setName("");
        if (getInstance().getLastName() == null)
            getInstance().setLastName("");
        if (getInstance().getMaidenName() == null)
            getInstance().setMaidenName("");


        return super.create();

    }

    public void changePersonFlag(){
        System.out.println("------------> changePersonFlag: " + this.personFlag);
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }


    public Boolean getPersonFlag() {
        return personFlag;
    }

    public void setPersonFlag(Boolean personFlag) {
        this.personFlag = personFlag;
    }
}
