package com.encens.khipus.action.employees;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.employees.PollCopy;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;

import java.util.Arrays;
import java.util.List;

/**
 * PollCopyForPollFormDataModel
 *
 * @author
 * @version 1.1.6
 */
@Name("pollCopyForPollFormDataModel")
@Scope(ScopeType.PAGE)
@Restrict("#{s:hasPermission('POLLCOPY','VIEW')}")
public class PollCopyForPollFormDataModel extends QueryDataModel<Long, PollCopy> {
    private static final String[] RESTRICTIONS = {"pollCopy.pollForm = #{pollForm}"};

    @Create
    public void init() {
        sortProperty = "pollCopy.revisionDate, pollCopy.revisionNumber";
    }

    @Override
    public String getEjbql() {
        return "select pollCopy from PollCopy pollCopy";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }
}

