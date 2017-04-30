package com.encens.khipus.action.finances;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.finances.RotatoryFundCollectionSpendDistribution;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;

import java.util.Arrays;
import java.util.List;

/**
 * @author
 * @version 2.22
 */

@Name("rotatoryFundCollectionSpendDistributionDataModel")
@Scope(ScopeType.PAGE)
@Restrict("#{s:hasPermission('ROTATORYFUNDCOLLECTIONSPENDDISTRIBUTION','VIEW')}")
public class RotatoryFundCollectionSpendDistributionDataModel extends QueryDataModel<Long, RotatoryFundCollectionSpendDistribution> {
    private static final String[] RESTRICTIONS = {
            /* since this is a nested conversation that allows to view both entities in the same form
            * we select the instance which evolves the other*/
            "rotatoryFundCollectionSpendDistribution.rotatoryFundCollection= #{rotatoryFundCollection}"
    };

    @Create
    public void init() {
        sortProperty = "rotatoryFundCollectionSpendDistribution.amount";
    }

    @Override
    public String getEjbql() {
        return "select rotatoryFundCollectionSpendDistribution from RotatoryFundCollectionSpendDistribution rotatoryFundCollectionSpendDistribution";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }
}