package com.encens.khipus.action.employees;

import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.framework.action.Outcome;
import com.encens.khipus.model.customers.Client;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;

import javax.persistence.EntityManager;

/**
 * Client action class
 *
 * @author
 * @version 1.0
 */
@Name("clientAction")
@Scope(ScopeType.CONVERSATION)
public class ClientAction extends GenericAction<Client> {

    @Factory(value = "client", scope = ScopeType.STATELESS)
    public Client initUser() {
        return getInstance();
    }

    @In("#{entityManager}")
    private EntityManager em;


    @Override
    @Begin(ifOutcome = Outcome.SUCCESS, flushMode = FlushModeType.MANUAL)
    public String select(Client instance) {
        String outCome = super.select(instance);
        return outCome;
    }


}