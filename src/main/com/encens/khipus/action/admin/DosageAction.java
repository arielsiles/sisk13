package com.encens.khipus.action.admin;

import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.framework.action.Outcome;
import com.encens.khipus.model.customers.BranchOffice;
import com.encens.khipus.model.customers.Dosage;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;


/**
 * Created by IntelliJ IDEA.
 * User: admin
 *
 * @version 2.8
 */
@Name("dosageAction")
@Scope(ScopeType.CONVERSATION)
public class DosageAction extends GenericAction<Dosage> {


    @Factory(value = "dosage", scope = ScopeType.STATELESS)
    public Dosage initBusinessUnit() {
        return getInstance();
    }


    @Override
    @Begin(ifOutcome = Outcome.SUCCESS, flushMode = FlushModeType.MANUAL)
    public String select(Dosage instance) {
        String outCome = super.select(instance);
        return outCome;
    }

}