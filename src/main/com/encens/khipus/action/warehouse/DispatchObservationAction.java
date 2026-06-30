package com.encens.khipus.action.warehouse;

import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.framework.action.Outcome;
import com.encens.khipus.model.warehouse.CatalogApprovalState;
import com.encens.khipus.model.warehouse.DispatchObservation;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.international.StatusMessage;

/**
 * CRUD del catalogo de Observaciones de Despacho.
 *
 * Ciclo de estados: BORRADOR -> APROBADO -> INACTIVO (sin vuelta atras).
 * APROBADO/INACTIVO son inmutables: actualizaciones/eliminaciones bloqueadas.
 */
@Name("dispatchObservationAction")
@Scope(ScopeType.CONVERSATION)
public class DispatchObservationAction extends GenericAction<DispatchObservation> {

    @Factory(value = "dispatchObservation", scope = ScopeType.STATELESS)
    public DispatchObservation initDispatchObservation() {
        return getInstance();
    }

    @Override
    public String create() {
        DispatchObservation o = getInstance();
        if (o.getState() == null) {
            o.setState(CatalogApprovalState.BORRADOR);
        }
        if (!validateNotEmpty()) {
            return Outcome.REDISPLAY;
        }
        return super.create();
    }

    @Override
    public String update() {
        DispatchObservation o = getInstance();
        if (!o.isEditable()) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.WARN,
                    "DispatchObservation.error.notEditable");
            return Outcome.REDISPLAY;
        }
        if (!validateNotEmpty()) {
            return Outcome.REDISPLAY;
        }
        return super.update();
    }

    @Override
    public String delete() {
        if (!getInstance().isEditable()) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.WARN,
                    "DispatchObservation.error.notDeletable");
            return Outcome.REDISPLAY;
        }
        return super.delete();
    }

    /**
     * Transicion BORRADOR -> APROBADO. Persiste el cambio inmediatamente.
     */
    public String approve() {
        DispatchObservation o = getInstance();
        if (o.getState() != CatalogApprovalState.BORRADOR) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.WARN,
                    "DispatchObservation.error.cannotApprove");
            return Outcome.REDISPLAY;
        }
        if (!validateNotEmpty()) {
            return Outcome.REDISPLAY;
        }
        o.setState(CatalogApprovalState.APROBADO);
        return super.update();
    }

    /**
     * Transicion APROBADO -> INACTIVO. Persiste el cambio inmediatamente.
     */
    public String inactivate() {
        DispatchObservation o = getInstance();
        if (o.getState() != CatalogApprovalState.APROBADO) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.WARN,
                    "DispatchObservation.error.cannotInactivate");
            return Outcome.REDISPLAY;
        }
        o.setState(CatalogApprovalState.INACTIVO);
        return super.update();
    }

    private boolean validateNotEmpty() {
        DispatchObservation o = getInstance();
        if (o.getName() == null || o.getName().trim().isEmpty()) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "DispatchObservation.error.nameRequired");
            return false;
        }
        if (o.getObservation() == null || o.getObservation().trim().isEmpty()) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "DispatchObservation.error.observationRequired");
            return false;
        }
        return true;
    }

    @Override
    protected String getDisplayNameProperty() {
        return "name";
    }
}
