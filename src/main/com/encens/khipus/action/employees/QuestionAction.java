package com.encens.khipus.action.employees;

import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.model.employees.Question;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;
import org.jboss.seam.annotations.security.Restrict;

/**
 * Encens Team
 *
 * @author
 * @version : QuestionAction, 21-10-2009 04:36:32 PM
 */
@Name("questionAction")
@Scope(ScopeType.CONVERSATION)
public class QuestionAction extends GenericAction<Question> {

    @Factory(value = "question", scope = ScopeType.STATELESS)
    @Restrict("#{s:hasPermission('POLLFORMQUESTION','VIEW')}")
    public Question initSection() {
        return getInstance();
    }

    @Begin(nested = true, flushMode = FlushModeType.MANUAL)
    public String assignQuestion() {
        return com.encens.khipus.framework.action.Outcome.SUCCESS;
    }

    @Override
    @Begin(nested = true, flushMode = FlushModeType.MANUAL)
    @Restrict("#{s:hasPermission('POLLFORMQUESTION','VIEW')}")
    public String select(Question instance) {
        return super.select(instance);
    }

    @Override
    @End(beforeRedirect = true)
    @Restrict("#{s:hasPermission('POLLFORMQUESTION','CREATE')}")
    public String create() {
        return super.create();
    }

    @Override
    @Restrict("#{s:hasPermission('POLLFORMQUESTION','CREATE')}")
    public void createAndNew() {
        super.createAndNew();
    }

    @Override
    @End(beforeRedirect = true)
    @Restrict("#{s:hasPermission('POLLFORMQUESTION','UPDATE')}")
    public String update() {
        return super.update();
    }

    @Override
    @End(beforeRedirect = true)
    @Restrict("#{s:hasPermission('POLLFORMQUESTION','DELETE')}")
    public String delete() {
        return super.delete();
    }

    @Override
    @End(beforeRedirect = true)
    public String cancel() {
        return super.cancel();
    }

    @Override
    protected String getDisplayNameProperty() {
        return "content";
    }
}
