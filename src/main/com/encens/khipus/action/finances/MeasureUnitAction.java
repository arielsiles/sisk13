package com.encens.khipus.action.finances;

import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.model.finances.MeasureUnit;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;

/**
 * MeasureUnitAction
 *
 * @author
 * @version 2.0
 */
@Name("measureUnitAction")
@Scope(ScopeType.CONVERSATION)
public class MeasureUnitAction extends GenericAction<MeasureUnit> {

    @Factory(value = "measureUnit", scope = ScopeType.STATELESS)
    @Restrict("#{s:hasPermission('MEASUREUNIT','VIEW')}")
    public MeasureUnit initMeasureUnit() {
        return getInstance();
    }

    @Override
    protected String getDisplayNameProperty() {
        return "measureUnitCode";
    }
}
