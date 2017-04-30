package com.encens.khipus.action.employees;

import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.model.employees.SubjectGroup;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

/**
 * Actions for SubjectGroup
 *
 * @author
 */

@Name("subjectGroupAction")
@Scope(ScopeType.CONVERSATION)
public class SubjectGroupAction extends GenericAction<SubjectGroup> {

    @Factory(value = "subjectGroup", scope = ScopeType.STATELESS)
    public SubjectGroup initSubjectGroup() {
        return getInstance();
    }

    @Override
    protected String getDisplayNameProperty() {
        return "name";
    }
}