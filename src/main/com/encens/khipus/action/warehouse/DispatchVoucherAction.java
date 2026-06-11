package com.encens.khipus.action.warehouse;

import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.framework.action.Outcome;
import com.encens.khipus.model.admin.BusinessUnit;
import com.encens.khipus.model.customers.Client;
import com.encens.khipus.model.employees.Employee;
import com.encens.khipus.model.finances.CostCenter;
import com.encens.khipus.model.finances.JobContract;
import com.encens.khipus.model.finances.MeasureUnit;
import com.encens.khipus.model.finances.MeasureUnitPk;
import com.encens.khipus.model.finances.Provider;
import com.encens.khipus.model.warehouse.DispatchPlace;
import com.encens.khipus.model.warehouse.DispatchState;
import com.encens.khipus.model.warehouse.DispatchStockImpact;
import com.encens.khipus.model.warehouse.Driver;
import com.encens.khipus.model.warehouse.InventoryPackaging;
import com.encens.khipus.model.warehouse.ProductItem;
import com.encens.khipus.model.warehouse.ProductItemPK;
import com.encens.khipus.model.warehouse.Vehicle;
import com.encens.khipus.model.warehouse.Warehouse;
import com.encens.khipus.model.warehouse.WarehouseVoucherDispatch;
import com.encens.khipus.model.warehouse.WarehouseVoucherDispatchDetail;
import com.encens.khipus.model.xproduction.ProductionGroup;
import com.encens.khipus.service.warehouse.DispatchVoucherService;
import com.encens.khipus.service.warehouse.WarehouseCatalogService;
import com.encens.khipus.util.BigDecimalUtil;
import com.encens.khipus.util.MessageUtils;
import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.international.StatusMessage;

import javax.persistence.EntityManager;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Action Seam para creacion y edicion de Vales de Despacho en estado
 * BORRADOR. Las transiciones a APROBADO (con doble confirmacion) y a
 * ANULADO se agregaran en fases 5 y 6.
 */
@Name("dispatchVoucherAction")
@Scope(ScopeType.CONVERSATION)
public class DispatchVoucherAction extends GenericAction<WarehouseVoucherDispatch> {

    @In
    private DispatchVoucherService dispatchVoucherService;

    @In
    private WarehouseCatalogService warehouseCatalogService;

    @In(value = "#{entityManager}")
    private EntityManager em;

    // Conjunto de IDs de productos ya agregados, para evitar duplicados al
    // agregar desde el popup.
    private Set<ProductItemPK> selectedProductItemIds = new HashSet<ProductItemPK>();

    // Estado del flujo de aprobacion en doble confirmacion.
    private boolean approvalStep1Visible = false;
    private boolean approvalStep2Visible = false;
    private List<DispatchStockImpact> approvalImpact = new ArrayList<DispatchStockImpact>();

    // Motivo de anulacion (capturado en el modal de Anular)
    private String annulReason;

    /* =========================================================
     * Factories
     * ========================================================= */

    @Factory(value = "dispatchVoucher", scope = ScopeType.STATELESS)
    public WarehouseVoucherDispatch initDispatchVoucher() {
        WarehouseVoucherDispatch d = getInstance();
        if (d.getDispatchDate() == null) {
            d.setDispatchDate(new Date());
        }
        if (d.getState() == null) {
            d.setState(DispatchState.BORRADOR);
        }
        return d;
    }

    @SuppressWarnings("unchecked")
    @Factory(value = "dispatchOriginPlaceList", scope = ScopeType.STATELESS)
    public List<DispatchPlace> getOriginPlaces() {
        return em.createNamedQuery("DispatchPlace.findByKind")
                .setParameter("kind",
                        com.encens.khipus.model.warehouse.DispatchPlaceKind.ORIGEN)
                .getResultList();
    }

    @SuppressWarnings("unchecked")
    @Factory(value = "dispatchDestinationPlaceList", scope = ScopeType.STATELESS)
    public List<DispatchPlace> getDestinationPlaces() {
        return em.createNamedQuery("DispatchPlace.findByKind")
                .setParameter("kind",
                        com.encens.khipus.model.warehouse.DispatchPlaceKind.DESTINO)
                .getResultList();
    }

    @SuppressWarnings("unchecked")
    @Factory(value = "dispatchApprovedRouteList", scope = ScopeType.STATELESS)
    public List<com.encens.khipus.model.warehouse.DispatchRoute> getApprovedRoutes() {
        return em.createNamedQuery("DispatchRoute.findApproved").getResultList();
    }

    /**
     * Lista de descripciones tecnicas APROBADAS para el producto de la linea
     * dada. Usada por el dropdown "Descripcion (Hoja de Ruta)" del detalle.
     * Si la linea no tiene producto aun, retorna lista vacia.
     */
    @SuppressWarnings("unchecked")
    public List<com.encens.khipus.model.warehouse.ProductDescription>
            getApprovedDescriptions(com.encens.khipus.model.warehouse.WarehouseVoucherDispatchDetail detail) {
        if (detail == null || detail.getProductItemCode() == null) {
            return java.util.Collections.emptyList();
        }
        return em.createNamedQuery("ProductDescription.findApprovedByProduct")
                .setParameter("companyNumber", detail.getProductItemCompanyNumber())
                .setParameter("productItemCode", detail.getProductItemCode())
                .getResultList();
    }

    @SuppressWarnings("unchecked")
    @Factory(value = "dispatchProductionGroupList", scope = ScopeType.STATELESS)
    public List<ProductionGroup> getProductionGroups() {
        return em.createQuery(
                "select g from ProductionGroup g order by g.code")
                .getResultList();
    }

    @SuppressWarnings("unchecked")
    @Factory(value = "dispatchDriverList", scope = ScopeType.STATELESS)
    public List<Driver> getDriverList() {
        return em.createNamedQuery("Driver.findAllActive").getResultList();
    }

    @SuppressWarnings("unchecked")
    @Factory(value = "dispatchPackagingList", scope = ScopeType.STATELESS)
    public List<InventoryPackaging> getPackagingList() {
        return em.createNamedQuery("InventoryPackaging.findAllActive").getResultList();
    }

    /**
     * Lista de vehiculos validos en el contexto del despacho actual. Si hay
     * conductor seleccionado, devuelve los vehiculos asociados a el; caso
     * contrario, devuelve todos los vehiculos activos (placeholder Fase 9.A;
     * la restriccion estricta por conductor se aplicara en Fase 9.D).
     */
    @SuppressWarnings("unchecked")
    @Factory(value = "dispatchVehicleList", scope = ScopeType.STATELESS)
    public List<Vehicle> getVehicleList() {
        WarehouseVoucherDispatch d = getInstance();
        if (d != null && d.getDriver() != null && d.getDriver().getId() != null) {
            return em.createNamedQuery("Vehicle.findByDriver")
                    .setParameter("driverId", d.getDriver().getId())
                    .getResultList();
        }
        return em.createNamedQuery("Vehicle.findAllActive").getResultList();
    }

    /* =========================================================
     * CRUD (BORRADOR)
     * ========================================================= */

    @Override
    public String create() {
        if (!validateDraft()) {
            return Outcome.REDISPLAY;
        }
        WarehouseVoucherDispatch d = getInstance();
        dispatchVoucherService.saveDraft(d);
        addCreatedMessage();
        return Outcome.SUCCESS;
    }

    @Override
    public String update() {
        if (!validateDraft()) {
            return Outcome.REDISPLAY;
        }
        WarehouseVoucherDispatch d = getInstance();
        WarehouseVoucherDispatch merged = dispatchVoucherService.updateDraft(d);
        setInstance(merged);
        addUpdatedMessage();
        // Permanecer en el formulario (solo Cancelar/Finalizar navegan al listado).
        return Outcome.REDISPLAY;
    }

    @Override
    public String delete() {
        try {
            dispatchVoucherService.deleteDraft(getInstance());
            addDeletedMessage();
            return Outcome.SUCCESS;
        } catch (IllegalStateException e) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "WarehouseDispatch.delete.error.notDraft");
            return Outcome.REDISPLAY;
        }
    }

    /* =========================================================
     * Validaciones (V01..V08) - estado BORRADOR
     * ========================================================= */

    public boolean validateDraft() {
        WarehouseVoucherDispatch d = getInstance();
        boolean ok = true;

        // V03 - peso bruto > tara (los required del XHTML cubren los null)
        if (d.getTareWeightKg() != null && d.getGrossWeightKg() != null
                && d.getGrossWeightKg().compareTo(d.getTareWeightKg()) <= 0) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "WarehouseDispatch.error.weightInvalid");
            ok = false;
        }

        // V02 - hora fin > hora inicio
        if (d.getLoadingStartTime() != null && d.getLoadingEndTime() != null
                && !d.getLoadingEndTime().after(d.getLoadingStartTime())) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "WarehouseDispatch.error.timeRangeInvalid");
            ok = false;
        }

        // V04 - bolsa hasta >= bolsa desde por DETALLE (la numeracion ahora vive
        // por linea, no en la cabecera). Validacion suave: si ambos estan
        // definidos en un detalle, hasta debe ser >= desde.
        if (d.getDetails() != null) {
            for (WarehouseVoucherDispatchDetail det : d.getDetails()) {
                if (det.getBagsFromNumber() != null && det.getBagsToNumber() != null
                        && det.getBagsToNumber() < det.getBagsFromNumber()) {
                    facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                            "WarehouseDispatch.error.bagsRangeInvalid");
                    ok = false;
                    break;
                }
            }
        }

        // V05 - al menos un detalle
        if (d.getDetails() == null || d.getDetails().isEmpty()) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "WarehouseDispatch.error.detailsRequired");
            ok = false;
        } else {
            // V08 - cantidad > 0 por linea
            for (WarehouseVoucherDispatchDetail det : d.getDetails()) {
                if (det.getQuantity() == null
                        || det.getQuantity().compareTo(BigDecimal.ZERO) <= 0) {
                    facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                            "Common.required",
                            MessageUtils.getMessage("WarehouseDispatch.detail.quantity"));
                    ok = false;
                    break;
                }
            }
        }

        // V09 (suave, NO bloqueante) - consistencia bolsas x capacidad vs peso
        // de la linea. Solo aplica si la linea tiene tipo de envase con capacidad
        // configurada y cantidad de bolsas. Tolerancia: el peso de una bolsa.
        warnPackagingConsistency(d);

        return ok;
    }

    /**
     * Advertencia no bloqueante: por cada linea con tipo de envase (capacidad
     * por bolsa en Kg) y cantidad de bolsas, verifica que bolsas * capacidad
     * coincida aproximadamente con el peso de la linea (cantidad, en Kg).
     * Tolera una desviacion menor al peso de una bolsa.
     */
    private void warnPackagingConsistency(WarehouseVoucherDispatch d) {
        if (d.getDetails() == null) {
            return;
        }
        for (WarehouseVoucherDispatchDetail det : d.getDetails()) {
            InventoryPackaging pkg = det.getPackaging();
            if (pkg == null || pkg.getUnitCapacityKg() == null
                    || det.getBagsCount() == null || det.getQuantity() == null) {
                continue;
            }
            if (pkg.getUnitCapacityKg().compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }
            BigDecimal expected = pkg.getUnitCapacityKg()
                    .multiply(new BigDecimal(det.getBagsCount()));
            BigDecimal diff = expected.subtract(det.getQuantity()).abs();
            if (diff.compareTo(pkg.getUnitCapacityKg()) > 0) {
                facesMessages.addFromResourceBundle(StatusMessage.Severity.WARN,
                        "WarehouseDispatch.detail.packagingMismatch",
                        det.getProductItem() != null ? det.getProductItem().getName() : "",
                        det.getBagsCount(), pkg.getCapacityLabel(),
                        expected, det.getQuantity());
            }
        }
    }

    /* =========================================================
     * Recalculos en linea (peso neto, total de bolsas)
     * ========================================================= */

    public void recalculateNetWeight() {
        WarehouseVoucherDispatch d = getInstance();
        if (d.getGrossWeightKg() != null && d.getTareWeightKg() != null) {
            d.setNetWeightKg(BigDecimalUtil.subtract(
                    d.getGrossWeightKg(), d.getTareWeightKg(), 3));
        } else {
            d.setNetWeightKg(null);
        }
    }

    /**
     * Mantenido por compatibilidad con el XHTML existente (onchange de
     * bolsa_desde/hasta del detalle dispara este metodo). El total de bolsas
     * del despacho ahora es derivado de la suma por detalle (sin set explicito).
     * Se conserva el metodo para que los eventos AJAX existentes no fallen.
     */
    public void recalculateBagCount() {
        // no-op: dispatch.bagCount se calcula como suma de detail.bagsCount
    }

    /* =========================================================
     * Gestion del detalle (productos terminados)
     * ========================================================= */

    public void addProductItems(List<ProductItem> productItems) {
        if (productItems == null) {
            return;
        }
        WarehouseVoucherDispatch d = getInstance();
        if (d.getDetails() == null) {
            d.setDetails(new ArrayList<WarehouseVoucherDispatchDetail>());
        }
        rebuildSelectedIds();
        for (ProductItem item : productItems) {
            if (selectedProductItemIds.contains(item.getId())) {
                continue;
            }
            MeasureUnit measureUnit = findMeasureUnit(item);
            if (measureUnit == null) {
                continue;
            }
            WarehouseVoucherDispatchDetail detail = new WarehouseVoucherDispatchDetail();
            detail.setDispatch(d);
            detail.setProductItem(item);
            detail.setMeasureUnit(measureUnit);
            detail.setUnitCost(item.getUnitCost() != null ? item.getUnitCost() : BigDecimal.ZERO);
            detail.setAmount(BigDecimal.ZERO);
            d.getDetails().add(detail);
            selectedProductItemIds.add(item.getId());
        }
    }

    public void removeDetail(WarehouseVoucherDispatchDetail detail) {
        WarehouseVoucherDispatch d = getInstance();
        if (d.getDetails() != null) {
            d.getDetails().remove(detail);
        }
        if (detail.getProductItem() != null) {
            selectedProductItemIds.remove(detail.getProductItem().getId());
        }
    }

    private void rebuildSelectedIds() {
        selectedProductItemIds.clear();
        WarehouseVoucherDispatch d = getInstance();
        if (d.getDetails() != null) {
            for (WarehouseVoucherDispatchDetail det : d.getDetails()) {
                if (det.getProductItem() != null) {
                    selectedProductItemIds.add(det.getProductItem().getId());
                }
            }
        }
    }

    private MeasureUnit findMeasureUnit(ProductItem item) {
        if (item == null || item.getId() == null) {
            return null;
        }
        MeasureUnitPk pk = new MeasureUnitPk(
                item.getId().getCompanyNumber(),
                item.getUsageMeasureCode());
        return warehouseCatalogService.findWarehouseCatalog(MeasureUnit.class, pk);
    }

    /* =========================================================
     * Asignaciones desde popups (callbacks de select/quickSearch)
     * ========================================================= */

    public void assignWarehouse(Warehouse warehouse) {
        if (warehouse == null || warehouse.getId() == null) {
            return;
        }
        WarehouseVoucherDispatch d = getInstance();

        // El warehouse que llega del popup/dataModel viene de un EM ya
        // cerrado. Re-fetch en MI EM para tener una entidad managed con
        // sesion activa y poder resolver lazy associations sin
        // LazyInitializationException.
        Warehouse fresh = em.find(Warehouse.class, warehouse.getId());
        if (fresh == null) {
            return;
        }
        d.setWarehouse(fresh);

        // Recuperamos la BusinessUnit completa: como fresh.executorUnit es
        // un proxy ligado a MI EM (sesion abierta), getId() funciona y
        // podemos re-fetch para tener una entidad totalmente cargada.
        BusinessUnit executorUnit = null;
        if (fresh.getExecutorUnit() != null) {
            try {
                Long buId = fresh.getExecutorUnit().getId();
                if (buId != null) {
                    executorUnit = warehouseCatalogService.findWarehouseCatalog(
                            BusinessUnit.class, buId);
                }
            } catch (Exception ignored) {
                // si no se encuentra, queda null
            }
        }
        d.setExecutorUnit(executorUnit);

        if (fresh.getResponsibleId() != null) {
            try {
                Employee responsible = warehouseCatalogService
                        .findWarehouseCatalog(Employee.class, fresh.getResponsibleId());
                d.setResponsible(responsible);
            } catch (Exception ignored) {
                // si no se encuentra el responsable, queda null
            }
        }

        // Al cambiar de almacen se limpia el detalle (los productos
        // dependen del almacen).
        d.setDetails(new ArrayList<WarehouseVoucherDispatchDetail>());
        selectedProductItemIds.clear();
    }

    public void clearWarehouse() {
        WarehouseVoucherDispatch d = getInstance();
        d.setWarehouse(null);
        d.setWarehouseCode(null);
        d.setResponsible(null);
        d.setDetails(new ArrayList<WarehouseVoucherDispatchDetail>());
        selectedProductItemIds.clear();
    }

    public void assignCostCenter(CostCenter costCenter) {
        getInstance().setCostCenter(costCenter);
    }

    public void clearCostCenter() {
        getInstance().setCostCenter(null);
        getInstance().setCostCenterCode(null);
    }

    public void assignClient(Client client) {
        getInstance().setClient(client);
    }

    public void clearClient() {
        getInstance().setClient(null);
    }

    public void assignTransportCompany(Provider provider) {
        getInstance().setTransportCompany(provider);
    }

    public void clearTransportCompany() {
        getInstance().setTransportCompany(null);
        getInstance().setTransportCompanyCode(null);
    }

    public void assignDeliverySeller(JobContract jobContract) {
        getInstance().setDeliverySeller(jobContract);
    }

    public void clearDeliverySeller() {
        getInstance().setDeliverySeller(null);
    }

    public void assignProductionTurn(ProductionGroup group) {
        getInstance().setProductionTurn(group);
    }

    public void assignOriginPlace(DispatchPlace place) {
        getInstance().setOriginPlace(place);
    }

    public void clearOriginPlace() {
        getInstance().setOriginPlace(null);
    }

    public void assignDestinationPlace(DispatchPlace place) {
        getInstance().setDestinationPlace(place);
    }

    public void clearDestinationPlace() {
        getInstance().setDestinationPlace(null);
    }

    public void assignDriver(Driver driver) {
        WarehouseVoucherDispatch d = getInstance();
        // Si cambia de conductor, el vehiculo previamente seleccionado podria
        // no estar asociado al nuevo conductor; lo limpiamos para que el
        // usuario reseleccione desde la lista filtrada.
        if (d.getDriver() != null && driver != null
                && d.getDriver().getId() != null
                && !d.getDriver().getId().equals(driver.getId())) {
            d.setVehicle(null);
        }
        d.setDriver(driver);
    }

    public void clearDriver() {
        WarehouseVoucherDispatch d = getInstance();
        d.setDriver(null);
        d.setVehicle(null);
    }

    public void assignVehicle(Vehicle vehicle) {
        getInstance().setVehicle(vehicle);
    }

    public void clearVehicle() {
        getInstance().setVehicle(null);
    }

    /**
     * Antes de abrir el modal de seleccion de vehiculo, aplica el filtro
     * por conductor seleccionado actualmente (si lo hay) en vehicleDataModel,
     * para que solo aparezcan los vehiculos asociados al conductor.
     * Si no hay conductor seleccionado, limpia el filtro (muestra todos).
     */
    public void prepareSelectVehicle() {
        Driver d = getInstance().getDriver();
        Long driverId = (d != null) ? d.getId() : null;
        VehicleDataModel m = (VehicleDataModel)
                Component.getInstance("vehicleDataModel", true);
        if (m != null) {
            m.setDriverIdFilter(driverId);
            m.search();
        }
    }

    /**
     * Callback invocado tras crear un Conductor desde el modal inline.
     *
     * <p>NOTA sobre el flujo: {@code <f:setPropertyActionListener>} invoca
     * este metodo via EL ANTES de que createInline persista la entidad, por
     * lo que {@code driver.getId()} puede ser null aqui. Solo seteamos la
     * referencia; persist luego mutara el mismo objeto Java para asignarle
     * el id, y el reRender posterior mostrara los datos persistidos.</p>
     */
    public void assignDriverAfterCreate(Driver driver) {
        if (driver == null) return;
        getInstance().setDriver(driver);
    }

    /**
     * Callback invocado tras crear un Vehiculo desde el modal inline.
     *
     * <p>Igual que con {@link #assignDriverAfterCreate}, el id puede ser
     * null en este punto. Solo seteamos la referencia; la auto-asociacion
     * del vehiculo al conductor seleccionado se hace en
     * {@code VehicleAction.createInline()} despues del persist, cuando el
     * id ya esta disponible.</p>
     */
    public void assignVehicleAfterCreate(Vehicle vehicle) {
        if (vehicle == null) return;
        getInstance().setVehicle(vehicle);
    }

    public void assignExecutorUnit(BusinessUnit executorUnit) {
        getInstance().setExecutorUnit(executorUnit);
    }

    public void assignResponsible(Employee responsible) {
        getInstance().setResponsible(responsible);
    }

    public void clearResponsible() {
        getInstance().setResponsible(null);
    }

    /**
     * Validacion estricta para aprobar: extiende validateDraft con la regla
     * de packaging requerido por cada detalle (necesario para generar el
     * default de la columna DETALLE DE ENVASE y del peso neto aprox al
     * crear los envases).
     */
    public boolean validateForApproval() {
        boolean ok = validateDraft();
        WarehouseVoucherDispatch d = getInstance();
        if (d.getDetails() != null) {
            for (WarehouseVoucherDispatchDetail det : d.getDetails()) {
                if (det.getPackaging() == null) {
                    facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                            "WarehouseDispatch.error.packagingRequired",
                            det.getProductItem() != null ? det.getProductItem().getName() : "");
                    ok = false;
                }
            }
        }
        return ok;
    }

    /* =========================================================
     * Aprobacion con doble confirmacion
     * ========================================================= */

    /**
     * Paso 0: el usuario hace click en "Aprobar Despacho".
     * Re-valida el borrador y abre el modal del Paso 1.
     */
    public void prepareApprove() {
        approvalStep1Visible = false;
        approvalStep2Visible = false;
        if (!validateForApproval()) {
            return;
        }
        approvalStep1Visible = true;
    }

    /**
     * Paso 1 -> Paso 2: el usuario confirmo los datos en el modal de revision.
     * Cierra el modal 1, calcula el impacto en inventario y abre el modal 2.
     */
    public void confirmApprovalStep1() {
        approvalStep1Visible = false;
        approvalImpact = dispatchVoucherService.calculateInventoryImpact(getInstance());
        // Si alguna linea no tiene stock suficiente, mostrar advertencia
        for (DispatchStockImpact i : approvalImpact) {
            if (!i.isSufficient()) {
                facesMessages.addFromResourceBundle(StatusMessage.Severity.WARN,
                        "WarehouseDispatch.error.insufficientStock",
                        i.getProductItem().getFullName(),
                        i.getCurrentStock(),
                        i.getRequiredQuantity());
            }
        }
        approvalStep2Visible = true;
    }

    /**
     * Paso 2 (confirmacion definitiva): invoca el servicio approve(),
     * que descuenta inventario y genera el asiento contable.
     * Retorna a la lista o redisplay segun el resultado.
     */
    public String confirmApprovalStep2() {
        approvalStep1Visible = false;
        approvalStep2Visible = false;
        try {
            WarehouseVoucherDispatch approved =
                    dispatchVoucherService.approve(getInstance());
            setInstance(approved);
            facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO,
                    "WarehouseDispatch.approve.success",
                    approved.getDeliveryOrderNumber());
            return Outcome.SUCCESS;
        } catch (Exception e) {
            log.error("Error aprobando despacho", e);
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "WarehouseDispatch.approve.error");
            return Outcome.REDISPLAY;
        }
    }

    /**
     * Cancela el modal de aprobacion en cualquiera de sus pasos.
     */
    public void cancelApproval() {
        approvalStep1Visible = false;
        approvalStep2Visible = false;
        approvalImpact = new ArrayList<DispatchStockImpact>();
    }

    /**
     * Retroceder desde el Paso 2 al Paso 1.
     */
    public void backToApprovalStep1() {
        approvalStep2Visible = false;
        approvalStep1Visible = true;
    }

    public boolean isApprovalStep1Visible() {
        return approvalStep1Visible;
    }

    public boolean isApprovalStep2Visible() {
        return approvalStep2Visible;
    }

    public List<DispatchStockImpact> getApprovalImpact() {
        return approvalImpact;
    }

    public boolean isApprovalImpactSufficient() {
        if (approvalImpact == null) {
            return false;
        }
        for (DispatchStockImpact i : approvalImpact) {
            if (!i.isSufficient()) {
                return false;
            }
        }
        return true;
    }

    /* =========================================================
     * Anulacion del despacho aprobado
     * ========================================================= */

    public void prepareAnnul() {
        annulReason = null;
    }

    public String confirmAnnul() {
        if (annulReason == null || annulReason.trim().isEmpty()) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "WarehouseDispatch.annul.reason.required");
            return Outcome.REDISPLAY;
        }
        try {
            WarehouseVoucherDispatch annulled =
                    dispatchVoucherService.annul(getInstance(), annulReason);
            setInstance(annulled);
            facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO,
                    "WarehouseDispatch.annul.success");
            return Outcome.SUCCESS;
        } catch (Exception e) {
            log.error("Error anulando despacho", e);
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "WarehouseDispatch.annul.error");
            return Outcome.REDISPLAY;
        }
    }

    public String getAnnulReason() {
        return annulReason;
    }

    public void setAnnulReason(String annulReason) {
        this.annulReason = annulReason;
    }

    /* =========================================================
     * Finalizacion / Reverso del despacho aprobado
     * ========================================================= */

    /**
     * Transicion APROBADO -> FINALIZADO. En FIN los envases quedan inmutables
     * y solo se imprime el reporte de Detalle de Envases Carguio.
     */
    public String finalizeDispatch() {
        try {
            WarehouseVoucherDispatch finalized =
                    dispatchVoucherService.finalizeDispatch(getInstance());
            setInstance(finalized);
            facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO,
                    "WarehouseDispatch.finalize.success");
            return Outcome.SUCCESS;
        } catch (IllegalStateException e) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "WarehouseDispatch.finalize.error.notApproved");
            return Outcome.REDISPLAY;
        } catch (Exception e) {
            log.error("Error finalizando despacho", e);
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "WarehouseDispatch.finalize.error");
            return Outcome.REDISPLAY;
        }
    }

    /**
     * Transicion FINALIZADO -> APROBADO. Re-habilita la edicion de los
     * envases generados al aprobar. Permanece en el formulario para que el
     * operador pueda continuar editando.
     */
    public String unfinalizeDispatch() {
        try {
            WarehouseVoucherDispatch reverted =
                    dispatchVoucherService.unfinalizeDispatch(getInstance());
            setInstance(reverted);
            facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO,
                    "WarehouseDispatch.unfinalize.success");
            return Outcome.REDISPLAY;
        } catch (IllegalStateException e) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "WarehouseDispatch.unfinalize.error.notFinalized");
            return Outcome.REDISPLAY;
        } catch (Exception e) {
            log.error("Error desfinalizando despacho", e);
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "WarehouseDispatch.unfinalize.error");
            return Outcome.REDISPLAY;
        }
    }

    /**
     * Guarda las ediciones de los envases (detalle textual y peso neto
     * aproximado por bolsa) realizadas desde la seccion inline del
     * formulario. Solo permitido en estado APROBADO. Permanece en el
     * formulario tras guardar (solo Cancelar/Finalizar navegan al listado).
     */
    public String saveEnvelopes() {
        try {
            WarehouseVoucherDispatch saved =
                    dispatchVoucherService.updateEnvelopes(getInstance());
            setInstance(saved);
            facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO,
                    "WarehouseDispatch.envelopes.save.success");
            return Outcome.REDISPLAY;
        } catch (IllegalStateException e) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "WarehouseDispatch.envelopes.save.error.notEditable");
            return Outcome.REDISPLAY;
        } catch (Exception e) {
            log.error("Error guardando envases", e);
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "WarehouseDispatch.envelopes.save.error");
            return Outcome.REDISPLAY;
        }
    }

    /* =========================================================
     * Helpers de estado para la vista
     * ========================================================= */

    public boolean isDraft() {
        WarehouseVoucherDispatch d = getInstance();
        return d != null && d.isDraft();
    }

    public boolean isApproved() {
        WarehouseVoucherDispatch d = getInstance();
        return d != null && d.isApproved();
    }

    public boolean isFinalized() {
        WarehouseVoucherDispatch d = getInstance();
        return d != null && d.isFinalized();
    }

    public boolean isAnnulled() {
        WarehouseVoucherDispatch d = getInstance();
        return d != null && d.isAnnulled();
    }

    /** True si el despacho tiene envases editables (estado APROBADO). */
    public boolean isEnvelopesEditable() {
        return isApproved();
    }

    /** True si el despacho tiene envases generados (APROBADO o FINALIZADO). */
    public boolean isHasEnvelopes() {
        WarehouseVoucherDispatch d = getInstance();
        return d != null && d.hasEnvelopes();
    }

    public boolean isWarehouseSelected() {
        WarehouseVoucherDispatch d = getInstance();
        return d != null && d.getWarehouse() != null;
    }

    @Override
    protected String getDisplayNameProperty() {
        // No tenemos un nombre directo; usamos el numero de orden de entrega
        // (que sera null hasta aprobar) o el lote de venta.
        return "salesLotCode";
    }
}
