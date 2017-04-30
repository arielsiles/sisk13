package com.encens.khipus.action.production;

import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.model.production.MeasureUnitProduction;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

/**
 * Created with IntelliJ IDEA.
 * User: david
 * Date: 6/5/13
 * Time: 12:35 PM
 * To change this template use File | Settings | File Templates.
 */
@Name("measureUnitProductionAction")
@Scope(ScopeType.CONVERSATION)
public class MeasureUnitProductionAction extends GenericAction<MeasureUnitProduction> {

    @Factory(value = "measureUnitProduction", scope = ScopeType.STATELESS)
    public MeasureUnitProduction initMeasureUnitProduction() {
        return getInstance();
    }

    @Override
    protected String getDisplayNameProperty() {
        return "name";
    }
}
