package com.encens.khipus.action.employees;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.employees.WeeklyWorkload;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;

import java.util.Arrays;
import java.util.List;

/**
 * @author
 * @version 6.1.0
 */
@Name("weeklyWorkloadDataModel")
@Scope(ScopeType.PAGE)
@Restrict("#{s:hasPermission('WEEKLYWORKLOAD','VIEW')}")
public class WeeklyWorkloadDataModel extends QueryDataModel<Long, WeeklyWorkload> {

    private static final String[] RESTRICTIONS = {
            "weeklyWorkload.gender = #{weeklyWorkloadDataModel.criteria.gender}"};

    @Create
    public void init() {
        sortProperty = "weeklyWorkload.gender";
    }

    @Override
    public String getEjbql() {
        return "select weeklyWorkload from WeeklyWorkload weeklyWorkload";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }
}
