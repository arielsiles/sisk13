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
import com.encens.khipus.model.warehouse.ProductItem;
import com.encens.khipus.model.warehouse.ProductItemPK;
import com.encens.khipus.model.warehouse.Warehouse;
import com.encens.khipus.model.warehouse.WarehouseVoucherDispatch;
import com.encens.khipus.model.warehouse.WarehouseVoucherDispatchDetail;
import com.encens.khipus.model.xproduction.ProductionGroup;
import com.encens.khipus.service.warehouse.DispatchVoucherService;
import com.encens.khipus.service.warehouse.WarehouseCatalogService;
import com.encens.khipus.util.BigDecimalUtil;
import com.encens.khipus.util.MessageUtils;
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
    @Factory(value = "dispatchProductionGroupList", scope = ScopeType.STATELESS)
    public List<ProductionGroup> getProductionGroups() {
        return em.createQuery(
                "select g from ProductionGroup g order by g.code")
                .getResultList();
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
        return Outcome.SUCCESS;
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

        // V04 - bolsa hasta >= bolsa desde y cantidad coincide
        if (d.getBagsFromNumber() != null && d.getBagsToNumber() != null) {
            if (d.getBagsToNumber() < d.getBagsFromNumber()) {
                facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                        "WarehouseDispatch.error.bagsRangeInvalid");
                ok = false;
            } else if (d.getBagCount() != null) {
                int expected = d.getBagsToNumber() - d.getBagsFromNumber() + 1;
                if (!d.getBagCount().equals(expected)) {
                    facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                            "WarehouseDispatch.error.bagsMismatch",
                            d.getBagCount(), d.getBagsFromNumber(), d.getBagsToNumber(), expected);
                    ok = false;
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

        return ok;
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

    public void recalculateBagCount() {
        WarehouseVoucherDispatch d = getInstance();
        if (d.getBagsFromNumber() != null && d.getBagsToNumber() != null
                && d.getBagsToNumber() >= d.getBagsFromNumber()) {
            d.setBagCount(d.getBagsToNumber() - d.getBagsFromNumber() + 1);
        }
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
        WarehouseVoucherDispatch d = getInstance();
        d.setWarehouse(warehouse);
        if (warehouse != null) {
            // warehouse.getExecutorUnit() devuelve un proxy Hibernate lazy
            // que falla al renderizar si la sesion se cierra. Recuperamos
            // la BusinessUnit completa por id para tener una entidad
            // detachada utilizable en la vista.
            BusinessUnit executorUnit = null;
            if (warehouse.getExecutorUnit() != null
                    && warehouse.getExecutorUnit().getId() != null) {
                try {
                    executorUnit = warehouseCatalogService.findWarehouseCatalog(
                            BusinessUnit.class, warehouse.getExecutorUnit().getId());
                } catch (Exception ignored) {
                    // si no se encuentra, queda null
                }
            }
            d.setExecutorUnit(executorUnit);

            if (warehouse.getResponsibleId() != null) {
                try {
                    Employee responsible = warehouseCatalogService
                            .findWarehouseCatalog(Employee.class, warehouse.getResponsibleId());
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

    public void assignExecutorUnit(BusinessUnit executorUnit) {
        getInstance().setExecutorUnit(executorUnit);
    }

    public void assignResponsible(Employee responsible) {
        getInstance().setResponsible(responsible);
    }

    public void clearResponsible() {
        getInstance().setResponsible(null);
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
        if (!validateDraft()) {
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

    public boolean isAnnulled() {
        WarehouseVoucherDispatch d = getInstance();
        return d != null && d.isAnnulled();
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
