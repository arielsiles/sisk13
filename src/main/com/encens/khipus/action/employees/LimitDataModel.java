package com.encens.khipus.action.employees;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.employees.Limit;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;

import java.util.Arrays;
import java.util.List;

/**
 * Data model for Limit
 *
 * @author
 */

@Name("limitDataModel")
@Scope(ScopeType.PAGE)
@Restrict("#{s:hasPermission('LIMIT','VIEW')}")
public class LimitDataModel extends QueryDataModel<Long, Limit> {
    private static final String[] RESTRICTIONS =
            {"limit.afterInit = #{limitDataModel.criteria.afterInit}"};

    @Create
    public void init() {
        sortProperty = "limit.afterInit";
    }

    @Override
    public String getEjbql() {
        return "select limit from Limit limit";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }
}