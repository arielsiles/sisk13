package com.encens.khipus.action.employees;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.employees.CNSRate;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.log.Log;

import java.util.Arrays;
import java.util.List;

/**
 * CNS rate data model
 *
 * @author
 * @version 2.26
 */
@Name("cnsRateDataModel")
@Scope(ScopeType.PAGE)
@Restrict("#{s:hasPermission('CNSRATE','VIEW')}")
public class CNSRateDataModel extends QueryDataModel<Long, CNSRate> {

    @Logger
    private Log log;

    private static final String[] RESTRICTIONS = {
            "cnsRate.rate = #{cnsRateDataModel.criteria.rate}"
    };

    @Override
    public String getEjbql() {
        return "select cnsRate from CNSRate cnsRate";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }

    @Override
    public void clear() {
        super.clear();
    }
}
