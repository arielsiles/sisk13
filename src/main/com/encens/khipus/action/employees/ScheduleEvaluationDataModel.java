package com.encens.khipus.action.employees;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.employees.ScheduleEvaluation;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;

import java.util.Arrays;
import java.util.List;

/**
 * ScheduleEvaluationDataModel
 *
 * @author
 * @version 2.24
 */
@Name("scheduleEvaluationDataModel")
@Scope(ScopeType.PAGE)
@Restrict("#{s:hasPermission('SCHEDULEEVALUATION','VIEW')}")
public class ScheduleEvaluationDataModel extends QueryDataModel<Long, ScheduleEvaluation> {

    private static final String[] RESTRICTIONS = {
            "lower(scheduleEvaluation.name) like concat('%',concat(lower(#{scheduleEvaluationDataModel.criteria.name}), '%'))",
            "cycle = #{scheduleEvaluationDataModel.criteria.cycle}"
    };

    @Create
    public void init() {
        sortProperty = "scheduleEvaluation.name";
    }

    @Override
    public String getEjbql() {
        return "select scheduleEvaluation from ScheduleEvaluation scheduleEvaluation" +
                " left join fetch scheduleEvaluation.cycle cycle";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }

    @Override
    public void search() {
        super.search();
    }
}
