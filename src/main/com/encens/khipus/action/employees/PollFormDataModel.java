package com.encens.khipus.action.employees;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.employees.PollForm;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;

import java.util.Arrays;
import java.util.List;

/**
 * Encens Team
 *
 * @author
 * @version : PollFormDataModel, 21-10-2009 03:09:07 PM
 */
@Name("pollFormDataModel")
@Scope(ScopeType.PAGE)
@Restrict("#{s:hasPermission('HUMANRESOURCESPOLL','VIEW')}")
public class PollFormDataModel extends QueryDataModel<Long, PollForm> {

    private static final String[] RESTRICTIONS = {
            "lower(pollForm.code) like concat(lower(#{pollFormDataModel.criteria.code}), '%')",
            "lower(pollForm.title) like concat('%', concat(lower(#{pollFormDataModel.criteria.title}), '%'))"};

    @Create
    public void init() {
        sortProperty = "pollForm.code";
    }

    @Override
    public String getEjbql() {
        return "select pollForm from PollForm pollForm";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }
}
