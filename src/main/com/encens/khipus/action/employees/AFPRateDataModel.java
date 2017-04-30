package com.encens.khipus.action.employees;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.employees.AFPRate;
import com.encens.khipus.model.employees.AFPRateType;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.log.Log;

import java.util.Arrays;
import java.util.List;

/**
 * AFP rate data model
 *
 * @author
 * @version 2.26
 */
@Name("afpRateDataModel")
@Scope(ScopeType.PAGE)
@Restrict("#{s:hasPermission('AFPRATE','VIEW')}")
public class AFPRateDataModel extends QueryDataModel<Long, AFPRate> {

    @Logger
    private Log log;

    private static final String[] RESTRICTIONS = {
            "afpRate.rate = #{afpRateDataModel.criteria.rate}",
            "afpRate.afpRateType = #{afpRateDataModel.criteria.afpRateType}"
    };

    @Override
    public String getEjbql() {
        return "select afpRate from AFPRate afpRate";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }

    @Override
    public void clear() {
        super.clear();
    }

    @Factory(value = "afpRateType", scope = ScopeType.STATELESS)
    public AFPRateType[] getAfpRateType() {
        return AFPRateType.values();
    }
}
