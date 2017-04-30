package com.encens.khipus.action.production;

import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.model.production.IndirectCosts;
import com.encens.khipus.model.production.PeriodIndirectCost;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

/**
 * Created with IntelliJ IDEA.
 * User: Diego
 * Date: 10/09/14
 * Time: 13:59
 * To change this template use File | Settings | File Templates.
 */
@Name("periodIndirectCostAction")
@Scope(ScopeType.CONVERSATION)
public class PeriodIndirectCostAction extends GenericAction<PeriodIndirectCost> {

    @Factory(value = "periodIndirectCost", scope = ScopeType.STATELESS)
    public PeriodIndirectCost initIndirectCosts() {
        return getInstance();
    }
}
