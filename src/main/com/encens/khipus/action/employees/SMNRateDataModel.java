package com.encens.khipus.action.employees;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.employees.SMNRate;
import org.jboss.seam.ScopeType;
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
@Name("smnRateDataModel")
@Scope(ScopeType.PAGE)
@Restrict("#{s:hasPermission('SMNRATE','VIEW')}")
public class SMNRateDataModel extends QueryDataModel<Long, SMNRate> {

    @Logger
    private Log log;

    private static final String[] RESTRICTIONS = {
            "smnRate.rate = #{smnRateDataModel.criteria.rate}"
    };

    @Override
    public String getEjbql() {
        return "select smnRate from SMNRate smnRate";
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
