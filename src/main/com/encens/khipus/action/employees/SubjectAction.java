package com.encens.khipus.action.employees;

import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.model.employees.Subject;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

/**
 * Actions for Subject
 *
 * @author
 */

@Name("subjectAction")
@Scope(ScopeType.CONVERSATION)
public class SubjectAction extends GenericAction<Subject> {

    @Factory(value = "subject", scope = ScopeType.STATELESS)
    public Subject initSubject() {
        return getInstance();
    }

    @Override
    protected String getDisplayNameProperty() {
        return "name";
    }
}