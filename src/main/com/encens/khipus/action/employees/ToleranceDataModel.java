package com.encens.khipus.action.employees;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.employees.Tolerance;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;

import java.util.Arrays;
import java.util.List;

/**
 * Data model for Tolerance
 *
 * @author
 */

@Name("toleranceDataModel")
@Scope(ScopeType.PAGE)
@Restrict("#{s:hasPermission('TOLERANCE','VIEW')}")
public class ToleranceDataModel extends QueryDataModel<Long, Tolerance> {
    private static final String[] RESTRICTIONS =
            {"tolerance.afterInit = #{toleranceDataModel.criteria.afterInit}"};

    @Create
    public void init() {
        sortProperty = "tolerance.afterInit";
    }

    @Override
    public String getEjbql() {
        return "select tolerance from Tolerance tolerance";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }
}