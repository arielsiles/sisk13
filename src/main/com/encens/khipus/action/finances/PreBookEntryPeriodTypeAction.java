package com.encens.khipus.action.finances;

import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.model.finances.PreBookEntryPeriodType;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

/**
 * Actions for Pre book entry period type
 *
 * @author:
 */

@Name("preBookEntryPeriodTypeAction")
@Scope(ScopeType.CONVERSATION)
public class PreBookEntryPeriodTypeAction extends GenericAction<PreBookEntryPeriodType> {


    @Factory(value = "preBookEntryPeriodType", scope = ScopeType.STATELESS)
    public PreBookEntryPeriodType initPreBookEntryPeriodType() {
        return getInstance();
    }

    @Override
    protected String getDisplayNameProperty() {
        return "name";
    }
}
