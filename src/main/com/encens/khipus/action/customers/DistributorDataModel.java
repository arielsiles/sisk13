package com.encens.khipus.action.customers;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.customers.Distributor;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import java.util.Arrays;
import java.util.List;

/**
 * Data model for Credit
 *
 * @author:
 */

@Name("distributorDataModel")
@Scope(ScopeType.PAGE)
public class DistributorDataModel extends QueryDataModel<Long, Distributor> {

    private static final String[] RESTRICTIONS = {
            "lower(distributor.firstName) like concat(lower(#{distributorDataModel.criteria.firstName}), '%')",
            "lower(distributor.lastName) like concat(lower(#{distributorDataModel.criteria.lastName}), '%')",
            "lower(distributor.maidenName) like concat(lower(#{distributorDataModel.criteria.maidenName}), '%')"
    };

    @Override
    public String getEjbql() {
        return "select distributor from Distributor distributor";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }
}
