package com.encens.khipus.action.finances;

import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.model.finances.DosageType;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

/**
 * Actions for Dosage type
 *
 * @author:
 */

@Name("dosageTypeAction")
@Scope(ScopeType.CONVERSATION)
public class DosageTypeAction extends GenericAction<DosageType> {

    @Factory(value = "dosageType", scope = ScopeType.STATELESS)
    public DosageType initDosageType() {
        return getInstance();
    }

    @Override
    protected String getDisplayNameProperty() {
        return "name";
    }

}
