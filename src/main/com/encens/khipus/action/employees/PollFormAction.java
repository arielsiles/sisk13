package com.encens.khipus.action.employees;

import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.model.employees.FieldRestrictionType;
import com.encens.khipus.model.employees.PollForm;
import com.encens.khipus.model.employees.PollFormGrouppingType;
import com.encens.khipus.util.employees.PollFormUtil;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;

/**
 * Encens Team
 *
 * @author
 * @version : PollFormAction, 21-10-2009 02:21:52 PM
 */
@Name("pollFormAction")
@Scope(ScopeType.CONVERSATION)
public class PollFormAction extends GenericAction<PollForm> {

    @Factory(value = "pollForm", scope = ScopeType.STATELESS)
    @Restrict("#{s:hasPermission('HUMANRESOURCESPOLL','VIEW')}")
    public PollForm initPollForm() {
        return getInstance();
    }

    @Factory(value = "fieldRestrictionTypeEnum")
    public FieldRestrictionType[] getFieldRestrictionType() {
        return FieldRestrictionType.values();
    }

    @Factory(value = "pollFormGrouppingTypeEnum")
    public PollFormGrouppingType[] getPollFormGrouppingType() {
        return PollFormGrouppingType.values();
    }

    public Boolean isVisible(FieldRestrictionType type) {
        return PollFormUtil.isVisible(type);
    }

    public Boolean isRequired(FieldRestrictionType type) {
        return PollFormUtil.isRequired(type);
    }

    public Boolean isFacultyGrouppingType(PollFormGrouppingType pollFormGrouppingType) {
        return PollFormUtil.isFacultyGrouppingType(pollFormGrouppingType);
    }

    public Boolean isCareerGrouppingType(PollFormGrouppingType pollFormGrouppingType) {
        return PollFormUtil.isCareerGrouppingType(pollFormGrouppingType);
    }

    public Boolean isSubjectGrouppingType(PollFormGrouppingType pollFormGrouppingType) {
        return PollFormUtil.isSubjectGrouppingType(pollFormGrouppingType);
    }

    @Override
    protected String getDisplayNameProperty() {
        return "code";
    }
}
