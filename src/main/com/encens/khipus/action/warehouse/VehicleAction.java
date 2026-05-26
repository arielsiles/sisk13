package com.encens.khipus.action.warehouse;

import com.encens.khipus.exception.EntryDuplicatedException;
import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.framework.action.Outcome;
import com.encens.khipus.model.warehouse.DispatchCatalogState;
import com.encens.khipus.model.warehouse.Driver;
import com.encens.khipus.model.warehouse.Vehicle;
import com.encens.khipus.model.warehouse.WarehouseVoucherDispatch;
import com.encens.khipus.util.ELEvaluator;
import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Begin;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.FlushModeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.core.Manager;
import org.jboss.seam.international.StatusMessage;

import javax.persistence.EntityManager;
import java.util.ArrayList;

/**
 * Action CRUD del catalogo de Vehiculos de Transporte.
 */
@Name("vehicleAction")
@Scope(ScopeType.CONVERSATION)
public class VehicleAction extends GenericAction<Vehicle> {

    @In(value = "#{entityManager}")
    private EntityManager em;

    @In(create = true)
    private ELEvaluator elEvaluator;

    private String postCreateAction;

    @Factory(value = "vehicle", scope = ScopeType.STATELESS)
    public Vehicle initVehicle() {
        return getInstance();
    }

    @Factory(value = "vehicleStateList")
    public DispatchCatalogState[] getStateList() {
        return DispatchCatalogState.values();
    }

    @Override
    public String create() {
        if (!validateUniqueness(null)) {
            return Outcome.REDISPLAY;
        }
        return super.create();
    }

    @Override
    public String update() {
        if (!validateUniqueness(getInstance().getId())) {
            return Outcome.REDISPLAY;
        }
        return super.update();
    }

    private boolean validateUniqueness(Long currentId) {
        Vehicle v = getInstance();
        if (v.getPlate() == null || v.getPlate().trim().isEmpty()) {
            return true;
        }
        Long count;
        if (currentId == null) {
            count = (Long) em.createNamedQuery("Vehicle.countByPlate")
                    .setParameter("plate", v.getPlate().trim())
                    .getSingleResult();
        } else {
            count = (Long) em.createNamedQuery("Vehicle.countByPlateAndDifferent")
                    .setParameter("plate", v.getPlate().trim())
                    .setParameter("id", currentId)
                    .getSingleResult();
        }
        if (count != null && count > 0) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "Vehicle.error.plateDuplicated", v.getPlate());
            return false;
        }
        return true;
    }

    /* =========================================================
     * Creacion inline desde un modalPanel (patron FinanceProviderAction).
     * ========================================================= */

    @Begin(nested = true, flushMode = FlushModeType.MANUAL)
    public void newInstanceInModalPanel() {
        setOp(OP_CREATE);
        createInstance();
    }

    public void createInline() {
        if (!validateUniqueness(null)) {
            return;
        }
        try {
            getService().create(getInstance());
            addCreatedMessage();
            // Auto-asociacion al conductor del despacho activo (si existe).
            // Se hace DESPUES del persist, cuando getInstance().getId() ya
            // tiene el id generado. El listener pre-action no podia hacerlo
            // porque el id aun era null en ese momento.
            autoAssociateToCurrentDispatchDriver();
            if (postCreateAction != null && !postCreateAction.trim().isEmpty()) {
                elEvaluator.evaluateMethodBinding(postCreateAction);
            }
            Manager.instance().endConversation(true);
        } catch (EntryDuplicatedException e) {
            addDuplicatedMessage();
        }
    }

    /**
     * Si el modal de creacion fue abierto desde un formulario de despacho
     * con un conductor ya seleccionado, asocia el nuevo vehiculo a ese
     * conductor para mantener coherente la relacion M:N. Si no hay
     * dispatchVoucherAction en contexto, o no hay conductor seleccionado,
     * no hace nada.
     */
    private void autoAssociateToCurrentDispatchDriver() {
        Long vehicleId = getInstance().getId();
        if (vehicleId == null) return;
        DispatchVoucherAction dva = (DispatchVoucherAction)
                Component.getInstance("dispatchVoucherAction", false);
        if (dva == null) return;
        WarehouseVoucherDispatch d = dva.getInstance();
        if (d == null) return;
        Driver drv = d.getDriver();
        if (drv == null || drv.getId() == null) return;

        Driver managedDriver = em.find(Driver.class, drv.getId());
        Vehicle managedVehicle = em.find(Vehicle.class, vehicleId);
        if (managedDriver == null || managedVehicle == null) return;

        if (managedDriver.getVehicles() == null) {
            managedDriver.setVehicles(new ArrayList<Vehicle>());
        }
        for (Vehicle existing : managedDriver.getVehicles()) {
            if (existing.getId() != null
                    && existing.getId().equals(managedVehicle.getId())) {
                return; // ya asociado
            }
        }
        managedDriver.getVehicles().add(managedVehicle);
        em.merge(managedDriver);
        d.setDriver(managedDriver);
    }

    public String getPostCreateAction() {
        return postCreateAction;
    }

    public void setPostCreateAction(String postCreateAction) {
        this.postCreateAction = postCreateAction;
    }

    @Override
    protected String getDisplayNameProperty() {
        return "plate";
    }
}
