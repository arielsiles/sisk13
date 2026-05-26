package com.encens.khipus.action.warehouse;

import com.encens.khipus.exception.EntryDuplicatedException;
import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.framework.action.Outcome;
import com.encens.khipus.model.warehouse.DispatchCatalogState;
import com.encens.khipus.model.warehouse.Driver;
import com.encens.khipus.model.warehouse.Vehicle;
import com.encens.khipus.util.ELEvaluator;
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
import java.util.List;

/**
 * Action CRUD del catalogo de Conductores. Permite ademas gestionar la
 * asociacion M:N con Vehiculos desde el mismo formulario.
 */
@Name("driverAction")
@Scope(ScopeType.CONVERSATION)
public class DriverAction extends GenericAction<Driver> {

    @In(value = "#{entityManager}")
    private EntityManager em;

    @In(create = true)
    private ELEvaluator elEvaluator;

    private String postCreateAction;

    @Factory(value = "driver", scope = ScopeType.STATELESS)
    public Driver initDriver() {
        return getInstance();
    }

    @Factory(value = "driverStateList")
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
        Driver d = getInstance();
        if (d.getLicense() == null || d.getLicense().trim().isEmpty()) {
            return true; // @NotNull lo cubre
        }
        Long count;
        if (currentId == null) {
            count = (Long) em.createNamedQuery("Driver.countByLicense")
                    .setParameter("license", d.getLicense().trim())
                    .getSingleResult();
        } else {
            count = (Long) em.createNamedQuery("Driver.countByLicenseAndDifferent")
                    .setParameter("license", d.getLicense().trim())
                    .setParameter("id", currentId)
                    .getSingleResult();
        }
        if (count != null && count > 0) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "Driver.error.licenseDuplicated", d.getLicense());
            return false;
        }
        return true;
    }

    /* =========================================================
     * Asociacion M:N con Vehiculos
     * ========================================================= */

    public void associateVehicle(Vehicle vehicle) {
        if (vehicle == null || vehicle.getId() == null) {
            return;
        }
        Driver d = getInstance();
        if (d.getVehicles() == null) {
            d.setVehicles(new ArrayList<Vehicle>());
        }
        // Evitar duplicados
        for (Vehicle v : d.getVehicles()) {
            if (v.getId() != null && v.getId().equals(vehicle.getId())) {
                return;
            }
        }
        // Re-fetch en EM activo para evitar LazyInit y entidades de EMs cerrados
        Vehicle fresh = em.find(Vehicle.class, vehicle.getId());
        if (fresh != null) {
            d.getVehicles().add(fresh);
        }
    }

    public void associateVehicles(List<Vehicle> vehicles) {
        if (vehicles == null) {
            return;
        }
        for (Vehicle v : vehicles) {
            associateVehicle(v);
        }
    }

    public void disassociateVehicle(Vehicle vehicle) {
        Driver d = getInstance();
        if (d.getVehicles() == null || vehicle == null || vehicle.getId() == null) {
            return;
        }
        Vehicle toRemove = null;
        for (Vehicle v : d.getVehicles()) {
            if (v.getId() != null && v.getId().equals(vehicle.getId())) {
                toRemove = v;
                break;
            }
        }
        if (toRemove != null) {
            d.getVehicles().remove(toRemove);
        }
    }

    /* =========================================================
     * Creacion inline desde un modalPanel (usado desde el formulario
     * de despacho via app:selectPopUp con renderedNewLink="true").
     * Sigue el patron de FinanceProviderAction.
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
            if (postCreateAction != null && !postCreateAction.trim().isEmpty()) {
                elEvaluator.evaluateMethodBinding(postCreateAction);
            }
            Manager.instance().endConversation(true);
        } catch (EntryDuplicatedException e) {
            addDuplicatedMessage();
        }
    }

    public String getPostCreateAction() {
        return postCreateAction;
    }

    public void setPostCreateAction(String postCreateAction) {
        this.postCreateAction = postCreateAction;
    }

    @Override
    protected String getDisplayNameProperty() {
        return "name";
    }
}
