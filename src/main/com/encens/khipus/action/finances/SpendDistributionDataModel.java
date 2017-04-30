package com.encens.khipus.action.finances;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.finances.SpendDistribution;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;

import java.util.Arrays;
import java.util.List;

/**
 * @author
 * @version 2.21
 */

@Name("spendDistributionDataModel")
@Scope(ScopeType.PAGE)
@Restrict("#{s:hasPermission('SPENDDISTRIBUTION','VIEW')}")
public class SpendDistributionDataModel extends QueryDataModel<Long, SpendDistribution> {
    private static final String[] RESTRICTIONS = {
            /* since this is a nested conversation that allows to view both entities in the same form
            * we select the instance which evolves the other*/
            "spendDistribution.rotatoryFund = #{rotatoryFund}"
    };

    @Create
    public void init() {
        sortProperty = "spendDistribution.amount";
    }

    @Override
    public String getEjbql() {
        return "select spendDistribution from SpendDistribution spendDistribution";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }
}