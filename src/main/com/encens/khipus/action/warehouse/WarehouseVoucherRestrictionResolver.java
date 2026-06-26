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

import javax.faces.context.FacesContext;
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
     * AISLAMIENTO POR PANTALLA: la restriccion de almacen/articulo/tipo-doc solo
     * debe filtrar en las pantallas de VALE. Los modelos de datos
     * (WarehouseSearchDataModel, ProductItemByWarehouseDataModel) son COMPARTIDOS
     * con despacho, produccion y reportes; para no afectarlos, los getters que
     * usan esos modelos via EL devuelven null cuando la pantalla activa NO es de
     * vale. La validacion de servidor usa las listas "raw" (sin este gate), por
     * lo que siempre enforce al guardar un vale.
     *
     * Pantallas de vale (donde SI aplica el filtro):
     *  - warehouseVoucherCreate.xhtml  (crear vale)
     *  - warehouseVoucherUpdate.xhtml  (editar vale)
     *  - movementDetail.xhtml          (anadir detalle desde la edicion del vale)
     */
    private boolean isVoucherScreen() {
        try {
            FacesContext ctx = FacesContext.getCurrentInstance();
            if (ctx == null || ctx.getViewRoot() == null) {
                return false;
            }
            String viewId = ctx.getViewRoot().getViewId();
            if (viewId == null) {
                return false;
            }
            return viewId.contains("warehouseVoucherCreate")
                    || viewId.contains("warehouseVoucherUpdate")
                    || viewId.contains("movementDetail");
        } catch (Exception e) {
            // fail-open: si no se puede determinar la pantalla, NO se restringe.
            return false;
        }
    }

    /* ---- Listas "raw" (basadas solo en la config, sin gate de pantalla) ---- */

    private List<String> rawAllowedDocumentCodes() {
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

    private List<Warehouse> rawAllowedWarehouses() {
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

    private List<ProductItem> rawAllowedProductItems() {
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

    /* ---- Getters EL (para los modelos COMPARTIDOS): gated por pantalla de vale.
           Fuera de las pantallas de vale devuelven null => sin restriccion (despacho,
           produccion, reportes quedan intactos). ---- */

    public List<String> getAllowedDocumentCodes() {
        return isVoucherScreen() ? rawAllowedDocumentCodes() : null;
    }

    public List<Warehouse> getAllowedWarehouses() {
        return isVoucherScreen() ? rawAllowedWarehouses() : null;
    }

    public List<ProductItem> getAllowedProductItems() {
        return isVoucherScreen() ? rawAllowedProductItems() : null;
    }

    /* ---- Validaciones de servidor (refuerzo anti-bypass): usan las listas raw,
           por lo que SIEMPRE enforce al guardar un vale, sin depender del viewId. ---- */

    public boolean isDocumentTypeAllowed(WarehouseDocumentType documentType) {
        List<String> codes = rawAllowedDocumentCodes();
        if (codes == null) {
            return true;
        }
        return documentType != null
                && documentType.getId() != null
                && codes.contains(documentType.getId().getDocumentCode());
    }

    public boolean isWarehouseAllowed(Warehouse warehouse) {
        List<Warehouse> allowed = rawAllowedWarehouses();
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
        List<ProductItem> allowed = rawAllowedProductItems();
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
