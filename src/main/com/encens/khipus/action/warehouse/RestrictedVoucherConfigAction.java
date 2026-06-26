package com.encens.khipus.action.warehouse;

import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.framework.action.Outcome;
import com.encens.khipus.model.admin.User;
import com.encens.khipus.model.warehouse.ProductItem;
import com.encens.khipus.model.warehouse.RestrictedConfigDocumentType;
import com.encens.khipus.model.warehouse.RestrictedConfigProductItem;
import com.encens.khipus.model.warehouse.RestrictedConfigWarehouse;
import com.encens.khipus.model.warehouse.RestrictedWarehouseVoucherConfig;
import com.encens.khipus.model.warehouse.Warehouse;
import com.encens.khipus.model.warehouse.WarehouseDocumentType;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.international.StatusMessage;

import javax.persistence.EntityManager;
import java.util.List;

/**
 * Action CRUD de la pantalla de administracion de Restriccion de Vales por
 * usuario. Hereda create/update/delete/select/cancel de GenericAction.
 *
 * Permite definir, por usuario, que Tipos de Documento, Almacenes y Articulos
 * estaran disponibles al crear o editar Vales de Almacen.
 *
 * @see RestrictedWarehouseVoucherConfig
 * @see WarehouseVoucherRestrictionResolver
 */
@Name("restrictedVoucherConfigAction")
@Scope(ScopeType.CONVERSATION)
@Restrict("#{s:hasPermission('WAREHOUSEVOUCHERRESTRICTION','VIEW')}")
public class RestrictedVoucherConfigAction extends GenericAction<RestrictedWarehouseVoucherConfig> {

    @In
    private EntityManager entityManager;

    /* holders de seleccion de los combos "Anadir" */
    private WarehouseDocumentType selectedDocumentType;
    private Warehouse selectedWarehouse;

    @Factory(value = "restrictedVoucherConfig", scope = ScopeType.STATELESS)
    public RestrictedWarehouseVoucherConfig initInstance() {
        return getInstance();
    }

    /** Lista de usuarios para el combo de seleccion de la cabecera. */
    @Factory(value = "restrictedConfigUserList", scope = ScopeType.STATELESS)
    @SuppressWarnings("unchecked")
    public List<User> getUserList() {
        return entityManager.createQuery("select u from User u order by u.username").getResultList();
    }

    @Override
    public String create() {
        if (!validate()) {
            return Outcome.REDISPLAY;
        }
        return super.create();
    }

    @Override
    public String update() {
        if (!validate()) {
            return Outcome.REDISPLAY;
        }
        return super.update();
    }

    /**
     * Valida la cabecera: usuario obligatorio y unico (una sola configuracion
     * por usuario).
     */
    private boolean validate() {
        RestrictedWarehouseVoucherConfig config = getInstance();
        if (config.getUser() == null || config.getUser().getId() == null) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "RestrictedVoucherConfig.error.userRequired");
            return false;
        }

        Long count;
        if (config.getId() == null) {
            count = (Long) entityManager
                    .createNamedQuery("RestrictedWarehouseVoucherConfig.countByUser")
                    .setParameter("userId", config.getUser().getId())
                    .getSingleResult();
        } else {
            count = (Long) entityManager
                    .createNamedQuery("RestrictedWarehouseVoucherConfig.countByUserAndDifferent")
                    .setParameter("userId", config.getUser().getId())
                    .setParameter("id", config.getId())
                    .getSingleResult();
        }
        if (count != null && count > 0) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "RestrictedVoucherConfig.error.userDuplicated");
            return false;
        }
        return true;
    }

    /* ---- Tipos de Documento ---- */

    public void addSelectedDocumentType() {
        if (selectedDocumentType != null) {
            getInstance().addDocumentType(selectedDocumentType);
        }
        selectedDocumentType = null;
    }

    public void removeDocumentType(RestrictedConfigDocumentType item) {
        getInstance().removeDocumentType(item);
    }

    /* ---- Almacenes ---- */

    public void addSelectedWarehouse() {
        if (selectedWarehouse != null) {
            getInstance().addWarehouse(selectedWarehouse);
        }
        selectedWarehouse = null;
    }

    public void removeWarehouse(RestrictedConfigWarehouse item) {
        getInstance().removeWarehouse(item);
    }

    /* ---- Articulos ---- */

    public void addProductItems(List<ProductItem> productItems) {
        if (productItems != null) {
            for (ProductItem productItem : productItems) {
                getInstance().addProductItem(productItem);
            }
        }
    }

    public void removeProductItem(RestrictedConfigProductItem item) {
        getInstance().removeProductItem(item);
    }

    /* ---- getters / setters de los holders ---- */

    public WarehouseDocumentType getSelectedDocumentType() {
        return selectedDocumentType;
    }

    public void setSelectedDocumentType(WarehouseDocumentType selectedDocumentType) {
        this.selectedDocumentType = selectedDocumentType;
    }

    public Warehouse getSelectedWarehouse() {
        return selectedWarehouse;
    }

    public void setSelectedWarehouse(Warehouse selectedWarehouse) {
        this.selectedWarehouse = selectedWarehouse;
    }
}
