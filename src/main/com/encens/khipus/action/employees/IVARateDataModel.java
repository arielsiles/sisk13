package com.encens.khipus.action.employees;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.employees.IVARate;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.log.Log;

import java.util.Arrays;
import java.util.List;

/**
 * IVA rate data model
 *
 * @author
 * @version 2.26
 */
@Name("ivaRateDataModel")
@Scope(ScopeType.PAGE)
@Restrict("#{s:hasPermission('IVARATE','VIEW')}")
public class IVARateDataModel extends QueryDataModel<Long, IVARate> {

    @Logger
    private Log log;

    private static final String[] RESTRICTIONS = {
            "ivaRate.rate = #{ivaRateDataModel.criteria.rate}"
    };

    @Override
    public String getEjbql() {
        return "select ivaRate from IVARate ivaRate";
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
