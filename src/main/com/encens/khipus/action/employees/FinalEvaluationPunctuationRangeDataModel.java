package com.encens.khipus.action.employees;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.employees.FinalEvaluationPunctuationRange;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;

import java.util.Arrays;
import java.util.List;

/**
 * FinalEvaluationPunctuationRangeDataModel
 *
 * @author
 * @version 2.8
 */
@Name("finalEvaluationPunctuationRangeDataModel")
@Scope(ScopeType.PAGE)
@Restrict("#{s:hasPermission('FEPUNCTUATIONRANGE','VIEW')}")
public class FinalEvaluationPunctuationRangeDataModel extends QueryDataModel<Long, FinalEvaluationPunctuationRange> {
    private static final String[] RESTRICTIONS =
            {"finalEvaluationPunctuationRange.finalEvaluationForm=#{finalEvaluationForm}"};

    @Create
    public void init() {
        sortProperty = "finalEvaluationPunctuationRange.position";
    }

    @Override
    public String getEjbql() {
        return "select finalEvaluationPunctuationRange " +
                " from FinalEvaluationPunctuationRange finalEvaluationPunctuationRange";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }
}
