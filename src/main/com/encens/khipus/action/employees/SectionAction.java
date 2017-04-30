package com.encens.khipus.action.employees;

import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.model.employees.Section;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;
import org.jboss.seam.annotations.security.Restrict;

/**
 * Encens Team
 *
 * @author
 * @version : SectionAction, 21-10-2009 04:01:56 PM
 */
@Name("sectionAction")
@Scope(ScopeType.CONVERSATION)
public class SectionAction extends GenericAction<Section> {

    @In
    private PollFormAction pollFormAction;

    @Factory(value = "section", scope = ScopeType.STATELESS)
    @Restrict("#{s:hasPermission('POLLFORMSECTION','VIEW')}")
    public Section initSection() {
        return getInstance();
    }

    @Override
    @Begin(nested = true, flushMode = FlushModeType.MANUAL)
    @Restrict("#{s:hasPermission('POLLFORMSECTION','VIEW')}")
    public String select(Section instance) {
        return super.select(instance);
    }

    @Override
    @Restrict("#{s:hasPermission('POLLFORMSECTION','DELETE')}")
    public String delete() {
        return super.delete();
    }

    @Override
    @Restrict("#{s:hasPermission('POLLFORMSECTION','UPDATE')}")
    public String update() {
        return super.update();
    }

    @Override
    @Restrict("#{s:hasPermission('POLLFORMSECTION','CREATE')}")
    public String create() {
        getInstance().setPollForm(pollFormAction.getInstance());
        return super.create();
    }

    @Override
    @Restrict("#{s:hasPermission('POLLFORMSECTION','CREATE')}")
    public void createAndNew() {
        getInstance().setPollForm(pollFormAction.getInstance());
        super.createAndNew();
    }

    @Override
    @End(beforeRedirect = true)
    public String cancel() {
        return super.cancel();
    }

    @Begin(nested = true, flushMode = FlushModeType.MANUAL)
    @Restrict("#{s:hasPermission('POLLFORMSECTION','CREATE')}")
    public String assignSection() {
        return com.encens.khipus.framework.action.Outcome.SUCCESS;
    }

    @Override
    protected String getDisplayNameProperty() {
        return "title";
    }
}
