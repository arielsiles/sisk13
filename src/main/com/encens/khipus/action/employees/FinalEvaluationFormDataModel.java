package com.encens.khipus.action.employees;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.employees.FinalEvaluationForm;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;

import java.util.Arrays;
import java.util.List;

/**
 * FinalEvaluationFormDataModel
 *
 * @author
 * @version 2.8
 */
@Name("finalEvaluationFormDataModel")
@Scope(ScopeType.PAGE)
@Restrict("#{s:hasPermission('FINALEVALUATIONFORM','VIEW')}")
public class FinalEvaluationFormDataModel extends QueryDataModel<Long, FinalEvaluationForm> {

    private static final String[] RESTRICTIONS = {
            "lower(finalEvaluationForm.code) like concat(lower(#{finalEvaluationFormDataModel.criteria.code}), '%')",
            "lower(finalEvaluationForm.title) like concat('%', concat(lower(#{finalEvaluationFormDataModel.criteria.title}), '%'))",
            "lower(finalEvaluationForm.subtitle) like concat('%', concat(lower(#{finalEvaluationFormDataModel.criteria.subtitle}), '%'))",
            "finalEvaluationForm.cycle = #{finalEvaluationFormDataModel.criteria.cycle}",
            "finalEvaluationForm.type = #{finalEvaluationFormDataModel.criteria.type}"};

    @Create
    public void init() {
        sortProperty = "finalEvaluationForm.code";
    }

    @Override
    public String getEjbql() {
        return "select finalEvaluationForm " +
                "   from FinalEvaluationForm finalEvaluationForm " +
                "   left join fetch finalEvaluationForm.cycle cycle";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }
}
