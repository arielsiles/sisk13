package com.encens.khipus.action.warehouse;

import com.encens.khipus.action.SessionUser;
import com.encens.khipus.model.warehouse.ProductItem;
import com.encens.khipus.model.warehouse.RestrictedConfigDocumentType;
import com.encens.khipus.model.warehouse.RestrictedConfigProductItem;
import com.encens.khipus.model.warehouse.RestrictedConfigWarehouse;
import com.encens.khipus.model.warehouse.RestrictedWarehouseVoucherConfig;
import com.encens.khipus.model.warehouse.Warehouse;
import com.encens.khipus.model.warehouse.WarehouseDocumentType;
import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import javax.persistence.EntityManager;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Resolutor de la restriccion de Vales de Almacen para el usuario logueado.
 *
 * Es el unico punto que decide si el usuario actual esta restringido y que
 * Tipos de Documento / Almacenes / Articulos puede ver al crear o editar un
 * vale (view/warehouse/warehouseVoucherCreate.xhtml y ...Update.xhtml).
 *
 * DISENO OPT-IN / NO-REGRESION:
 *  - Un usuario esta restringido solo si tiene una fila activa en
 *    inv_vale_restriccion (RestrictedWarehouseVoucherConfig).
 *  - Los getters de listas devuelven {@code null} cuando NO hay restriccion o
 *    cuando esa lista esta vacia (Interpretacion A: lista vacia = sin
 *    restriccion en esa dimension). El {@code null} hace que la restriccion EL
 *    correspondiente se omita (comportamiento estandar de Seam EntityQuery),
 *    de modo que los usuarios sin configuracion ven exactamente lo de siempre.
 *  - Cualquier error al resolver deja al usuario SIN restriccion (fail-open),
 *    para no romper la operativa en produccion.
 *
 * Scope EVENT: se carga una sola vez por request usando el mismo
 * listEntityManager (EVENT, con filtro de compania) que usan los DataModel,
 * por lo que las entidades devueltas en las clausulas {@code in (...)} estan
 * gestionadas por el contexto de persistencia vigente.
 */
@Name("warehouseVoucherRestrictionResolver")
@Scope(ScopeType.EVENT)
@AutoCreate
public class WarehouseVoucherRestrictionResolver implements Serializable {

    private static final long serialVersionUID = 1L;

    @In(value = "sessionUser", required = false)
    private SessionUser sessionUser;

    private boolean loaded = false;
    private RestrictedWarehouseVoucherConfig config = null;

    @SuppressWarnings("unchecked")
    private void ensureLoaded() {
        if (loaded) {
            return;
        }
        loaded = true;
        config = null;

        if (sessionUser == null || sessionUser.getUserId() == null) {
            return;
        }

        try {
            EntityManager em = (EntityManager) Component.getInstance("listEntityManager");
            if (em == null) {
                return;
            }
            List<RestrictedWarehouseVoucherConfig> result = em
                    .createNamedQuery("RestrictedWarehouseVoucherConfig.findActiveByUser")
                    .setParameter("userId", sessionUser.getUserId())
                    .getResultList();

            if (!result.isEmpty()) {
                RestrictedWarehouseVoucherConfig loadedConfig = result.get(0);
                // Inicializar las colecciones LAZY mientras el EM esta abierto.
                loadedConfig.getDocumentTypes().size();
                loadedConfig.getWarehouses().size();
                loadedConfig.getProductItems().size();
                config = loadedConfig;
            }
        } catch (Exception e) {
            // fail-open: ante cualquier problema NO se restringe.
            config = null;
        }
    }

    public boolean isRestricted() {
        ensureLoaded();
        return config != null;
    }

    /**
     * @return codigos de Tipo de Documento (cod_doc) permitidos, o {@code null}
     *         si no hay restriccion / la lista esta vacia.
     */
    public List<String> getAllowedDocumentCodes() {
        ensureLoaded();
        if (config == null) {
            return null;
        }
        List<String> codes = new ArrayList<String>();
        for (RestrictedConfigDocumentType item : config.getDocumentTypes()) {
            if (item.getDocumentType() != null && item.getDocumentType().getId() != null) {
                codes.add(item.getDocumentType().getId().getDocumentCode());
            }
        }
        return codes.isEmpty() ? null : codes;
    }

    /**
     * @return Almacenes permitidos, o {@code null} si no hay restriccion / la
     *         lista esta vacia.
     */
    public List<Warehouse> getAllowedWarehouses() {
        ensureLoaded();
        if (config == null) {
            return null;
        }
        List<Warehouse> warehouses = new ArrayList<Warehouse>();
        for (RestrictedConfigWarehouse item : config.getWarehouses()) {
            if (item.getWarehouse() != null) {
                warehouses.add(item.getWarehouse());
            }
        }
        return warehouses.isEmpty() ? null : warehouses;
    }

    /**
     * @return Articulos permitidos, o {@code null} si no hay restriccion / la
     *         lista esta vacia.
     */
    public List<ProductItem> getAllowedProductItems() {
        ensureLoaded();
        if (config == null) {
            return null;
        }
        List<ProductItem> productItems = new ArrayList<ProductItem>();
        for (RestrictedConfigProductItem item : config.getProductItems()) {
            if (item.getProductItem() != null) {
                productItems.add(item.getProductItem());
            }
        }
        return productItems.isEmpty() ? null : productItems;
    }

    /* ---- Validaciones de servidor (refuerzo anti-bypass) ---- */

    public boolean isDocumentTypeAllowed(WarehouseDocumentType documentType) {
        List<String> codes = getAllowedDocumentCodes();
        if (codes == null) {
            return true;
        }
        return documentType != null
                && documentType.getId() != null
                && codes.contains(documentType.getId().getDocumentCode());
    }

    public boolean isWarehouseAllowed(Warehouse warehouse) {
        List<Warehouse> allowed = getAllowedWarehouses();
        if (allowed == null || warehouse == null) {
            return true;
        }
        for (Warehouse item : allowed) {
            if (item.getId() != null && item.getId().equals(warehouse.getId())) {
                return true;
            }
        }
        return false;
    }

    public boolean isProductItemAllowed(ProductItem productItem) {
        List<ProductItem> allowed = getAllowedProductItems();
        if (allowed == null || productItem == null) {
            return true;
        }
        for (ProductItem item : allowed) {
            if (item.getId() != null && item.getId().equals(productItem.getId())) {
                return true;
            }
        }
        return false;
    }
}
