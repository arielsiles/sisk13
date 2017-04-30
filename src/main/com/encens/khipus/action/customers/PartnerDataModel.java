package com.encens.khipus.action.customers;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.customers.Credit;
import com.encens.khipus.model.customers.Partner;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;

import java.util.Arrays;
import java.util.List;

/**
 * Data model for Credit
 *
 * @author:
 */

@Name("partnerDataModel")
@Scope(ScopeType.PAGE)
public class PartnerDataModel extends QueryDataModel<Long, Partner> {

    private static final String[] RESTRICTIONS = {
            "lower(partner.idNumber) like concat(lower(#{partnerDataModel.criteria.idNumber}), '%')",
            "lower(partner.firstName) like concat(lower(#{partnerDataModel.criteria.firstName}), '%')"};

    @Override
    public String getEjbql() {
        return "select partner from Partner partner";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }
}
