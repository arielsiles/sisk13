package com.encens.khipus.action.customers;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.customers.DiscountPolicy;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;

import java.util.Arrays;
import java.util.List;

/**
 * Data model for Discount Policy
 *
 * @author:
 */

@Name("discountPolicyDataModel")
@Scope(ScopeType.PAGE)
@Restrict("#{s:hasPermission('DISCOUNTPOLICY','VIEW')}")
public class DiscountPolicyDataModel extends QueryDataModel<Long, DiscountPolicy> {

    private static final String[] RESTRICTIONS =
            {"lower(discountPolicy.name) like concat('%', concat(lower(#{discountPolicyDataModel.criteria.name}), '%'))"};

    @Create
    public void init() {
        sortProperty = "discountPolicy.name";
    }

    @Override
    public String getEjbql() {
        return "select discountPolicy from DiscountPolicy discountPolicy";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }
}
