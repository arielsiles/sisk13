package com.encens.khipus.action.employees;

import com.encens.khipus.exception.EntryDuplicatedException;
import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.framework.action.Outcome;
import com.encens.khipus.model.employees.DismissalCause;
import com.encens.khipus.service.employees.DismissalCauseService;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;
import org.jboss.seam.annotations.security.Restrict;

/**
 * @author
 * @version 3.5
 */
@Name("dismissalCauseAction")
@Scope(ScopeType.CONVERSATION)
public class DismissalCauseAction extends GenericAction<DismissalCause> {

    @In
    private DismissalCauseService dismissalCauseService;

    @Factory(value = "dismissalCause", scope = ScopeType.STATELESS)
    public DismissalCause init() {
        return getInstance();
    }

    @Override
    public DismissalCause createInstance() {
        DismissalCause result = super.createInstance();
        result.setActive(true);
        return result;
    }

    @Override
    protected String getDisplayNameMessage() {
        return getInstance().getFullName();
    }

    @Override
    @End
    @Restrict("#{s:hasPermission('DISMISSALCAUSE','CREATE')}")
    public String create() {
        try {
            dismissalCauseService.createDismissalCause(getInstance());
            addCreatedMessage();
            return Outcome.SUCCESS;
        } catch (EntryDuplicatedException e) {
            addDuplicatedMessage();
            return Outcome.FAIL;
        }
    }

    @Override
    @Restrict("#{s:hasPermission('DISMISSALCAUSE','CREATE')}")
    public void createAndNew() {
        try {
            dismissalCauseService.createDismissalCause(getInstance());
            addCreatedMessage();
            createInstance();
        } catch (EntryDuplicatedException e) {
            addDuplicatedMessage();
        }
    }

    @Override
    @End
    @Restrict("#{s:hasPermission('DISMISSALCAUSE','UPDATE')}")
    public String update() {
        return super.update();
    }

    @Override
    @End
    @Restrict("#{s:hasPermission('DISMISSALCAUSE','DELETE')}")
    public String delete() {
        return super.delete();
    }
}