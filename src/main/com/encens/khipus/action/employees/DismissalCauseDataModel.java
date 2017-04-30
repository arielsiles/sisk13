package com.encens.khipus.action.employees;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.employees.DismissalCause;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import java.util.Arrays;
import java.util.List;

/**
 * @author
 * @version 3.5
 */
@Name("dismissalCauseDataModel")
@Scope(ScopeType.PAGE)
public class DismissalCauseDataModel extends QueryDataModel<Long, DismissalCause> {

    private static final String[] RESTRICTIONS = {
            "dismissalCause.code = #{dismissalCauseDataModel.criteria.code}",
            "dismissalCause.active = #{dismissalCauseDataModel.criteria.active}",
            "dismissalCause.payable = #{dismissalCauseDataModel.criteria.payable}",
            "lower(dismissalCause.description) like concat('%', concat(lower(#{dismissalCauseDataModel.criteria.description}),'%'))",
            "lower(dismissalCause.name) like concat('%',concat(lower(#{dismissalCauseDataModel.criteria.name}), '%'))"
    };

    @Create
    public void init() {
        sortProperty = "dismissalCause.code";
    }

    @Override
    public String getEjbql() {
        return "select dismissalCause from DismissalCause dismissalCause";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }
}