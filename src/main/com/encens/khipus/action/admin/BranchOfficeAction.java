package com.encens.khipus.action.admin;

import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.framework.action.Outcome;
import com.encens.khipus.model.customers.BranchOffice;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;


/**
 * Created by IntelliJ IDEA.
 * User: admin
 *
 * @version 2.8
 */
@Name("branchOfficeAction")
@Scope(ScopeType.CONVERSATION)
public class BranchOfficeAction extends GenericAction<BranchOffice> {


    @Factory(value = "branchOffice", scope = ScopeType.STATELESS)
    public BranchOffice initBusinessUnit() {
        return getInstance();
    }


    @Override
    @Begin(ifOutcome = Outcome.SUCCESS, flushMode = FlushModeType.MANUAL)
    public String select(BranchOffice instance) {
        String outCome = super.select(instance);
        return outCome;
    }

}