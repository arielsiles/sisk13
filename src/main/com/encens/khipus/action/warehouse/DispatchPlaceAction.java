package com.encens.khipus.action.warehouse;

import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.model.warehouse.DispatchPlace;
import com.encens.khipus.model.warehouse.DispatchPlaceKind;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

/**
 * Action CRUD del catalogo Lugares de Despacho/Entrega.
 * Hereda create/update/delete/select/cancel de GenericAction.
 */
@Name("dispatchPlaceAction")
@Scope(ScopeType.CONVERSATION)
public class DispatchPlaceAction extends GenericAction<DispatchPlace> {

    @Factory(value = "dispatchPlace", scope = ScopeType.STATELESS)
    public DispatchPlace initDispatchPlace() {
        return getInstance();
    }

    @Factory("dispatchPlaceKindList")
    public DispatchPlaceKind[] getDispatchPlaceKindList() {
        return DispatchPlaceKind.values();
    }

    @Override
    protected String getDisplayNameProperty() {
        return "description";
    }
}
