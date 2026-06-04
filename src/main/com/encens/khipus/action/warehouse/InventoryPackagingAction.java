package com.encens.khipus.action.warehouse;

import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.framework.action.Outcome;
import com.encens.khipus.model.warehouse.DispatchCatalogState;
import com.encens.khipus.model.warehouse.InventoryPackaging;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.international.StatusMessage;

import javax.persistence.EntityManager;

/**
 * Action CRUD del catalogo de Tipos de Bolsa/Envase.
 * Hereda create/update/delete/select/cancel de GenericAction.
 */
@Name("inventoryPackagingAction")
@Scope(ScopeType.CONVERSATION)
public class InventoryPackagingAction extends GenericAction<InventoryPackaging> {

    @In(value = "#{entityManager}")
    private EntityManager em;

    @Factory(value = "inventoryPackaging", scope = ScopeType.STATELESS)
    public InventoryPackaging initInventoryPackaging() {
        return getInstance();
    }

    @Factory(value = "inventoryPackagingStateList")
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
        InventoryPackaging p = getInstance();
        if (p.getName() == null || p.getName().trim().isEmpty()
                || p.getCapacityLabel() == null || p.getCapacityLabel().trim().isEmpty()) {
            return true; // @NotNull lo cubre
        }
        Long count;
        if (currentId == null) {
            count = (Long) em.createNamedQuery("InventoryPackaging.countByName")
                    .setParameter("name", p.getName().trim())
                    .setParameter("capacityLabel", p.getCapacityLabel().trim())
                    .getSingleResult();
        } else {
            count = (Long) em.createNamedQuery("InventoryPackaging.countByNameAndDifferent")
                    .setParameter("name", p.getName().trim())
                    .setParameter("capacityLabel", p.getCapacityLabel().trim())
                    .setParameter("id", currentId)
                    .getSingleResult();
        }
        if (count != null && count > 0) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "InventoryPackaging.error.duplicated", p.getFullName());
            return false;
        }
        return true;
    }

    @Override
    protected String getDisplayNameProperty() {
        return "name";
    }
}
