package com.encens.khipus.action.warehouse.reconciliation;

import com.encens.khipus.action.warehouse.reconciliation.dto.ReconciliationDetail;
import com.encens.khipus.action.warehouse.reconciliation.dto.ReconciliationDiff;
import com.encens.khipus.action.warehouse.reports.ProductInventoryReportAction;
import com.encens.khipus.action.warehouse.reports.ProductInventoryReportAction.CollectionData;
import com.encens.khipus.exception.warehouse.MultipleWarehouseException;
import com.encens.khipus.exception.warehouse.NegativeFinalBalanceException;
import com.encens.khipus.exception.warehouse.NoInitialInventoryException;
import com.encens.khipus.model.admin.User;
import com.encens.khipus.model.warehouse.Warehouse;
import com.encens.khipus.service.warehouse.InventoryReconciliationService;
import com.encens.khipus.util.DateUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;
import org.jboss.seam.log.Log;
import org.jboss.seam.log.Logging;

import javax.persistence.OptimisticLockException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Action de la pantalla "Almacenes > Configuracion > Actualizar Inventario".
 *
 * Flujo:
 *   1) Seleccion almacen + fecha inicio + fecha fin + Buscar
 *      -> calcula saldos via ProductInventoryReportAction y arma lista de diferencias.
 *   2) Click "Ver detalle" en una fila
 *      -> reconstruye costo promedio cronologicamente desde el primer inv_inicio.
 *   3) Click "Aplicar ajuste" con motivo
 *      -> aplica los nuevos valores a inv_inventario, inv_inventario_detalle, inv_articulos.
 */
@Name("inventoryReconciliationAction")
@Scope(ScopeType.PAGE)
public class InventoryReconciliationAction {

    private final Log log = Logging.getLog(InventoryReconciliationAction.class);

    @In(create = true)
    private ProductInventoryReportAction productInventoryReportAction;

    @In
    private InventoryReconciliationService inventoryReconciliationService;

    @In
    private FacesMessages facesMessages;

    @In
    private User currentUser;

    /** Filtros de busqueda */
    private Warehouse warehouse;
    private Date startDate;
    private Date endDate;

    /** Resultado de la busqueda */
    private List<ReconciliationDiff> diffs = new ArrayList<ReconciliationDiff>();
    private boolean searched = false;

    /** Detalle del articulo seleccionado y su motivo */
    private ReconciliationDetail detail;
    private String reason;

    @Create
    public void init() {
        // Default: gestion actual hasta hoy
        Date today = new Date();
        startDate = DateUtils.firstDayOfYear(DateUtils.getCurrentYear(today));
        endDate = today;
    }

    // -----------------------------------------------------------------
    // Busqueda
    // -----------------------------------------------------------------

    public void search() {
        diffs.clear();
        detail = null;
        reason = null;
        searched = false;

        if (warehouse == null || startDate == null || endDate == null) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "InventoryReconciliation.search.missingFilters");
            return;
        }
        if (endDate.before(startDate)) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "InventoryReconciliation.search.invalidDateRange");
            return;
        }

        try {
            // 1) Reusar el calculo del reporte general
            productInventoryReportAction.setWarehouse(warehouse);
            productInventoryReportAction.setStartDate(startDate);
            productInventoryReportAction.setEndDate(endDate);
            productInventoryReportAction.setArticlesWithMovement(false);

            Collection<CollectionData> reportData =
                    productInventoryReportAction.calculateCollectionData2();

            // 2) Volcar en map cod_art -> saldo calculado
            Map<String, BigDecimal> calculatedSaldos = new HashMap<String, BigDecimal>();
            for (CollectionData cd : reportData) {
                calculatedSaldos.put(cd.getCode(), cd.getBalance());
            }

            // 3) Comparar con inv_inventario
            diffs = inventoryReconciliationService.listDifferences(
                    warehouse.getWarehouseCode(), calculatedSaldos);
            searched = true;

            if (diffs.isEmpty()) {
                facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO,
                        "InventoryReconciliation.search.noDifferences");
            }

        } catch (Exception e) {
            log.error("Error buscando diferencias", e);
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "InventoryReconciliation.search.error", e.getMessage());
        }
    }

    // -----------------------------------------------------------------
    // Detalle
    // -----------------------------------------------------------------

    public void selectArticle(ReconciliationDiff diff) {
        detail = null;
        reason = null;

        if (diff == null) return;

        try {
            detail = inventoryReconciliationService.reconstructArticle(
                    diff.getProductItemCode(),
                    warehouse.getWarehouseCode(),
                    startDate,
                    endDate,
                    diff.getCalculatedQuantity());

            // Avisos no bloqueantes
            if (detail.isReportMismatch()) {
                facesMessages.addFromResourceBundle(StatusMessage.Severity.WARN,
                        "InventoryReconciliation.detail.reportMismatch",
                        detail.getReconstructedQuantity(), detail.getReportQuantity());
            }
            if (detail.isHasIntermediateNegatives()) {
                facesMessages.addFromResourceBundle(StatusMessage.Severity.WARN,
                        "InventoryReconciliation.detail.intermediateNegatives");
            }
            if (detail.isBlockedByNegativeFinal()) {
                facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                        "InventoryReconciliation.detail.negativeFinal",
                        detail.getReconstructedQuantity());
            }

        } catch (MultipleWarehouseException e) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "InventoryReconciliation.detail.multipleWarehouses",
                    e.getWarehouseCodes().toString());
        } catch (NoInitialInventoryException e) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "InventoryReconciliation.detail.noInitialInventory");
        } catch (Exception e) {
            log.error("Error reconstruyendo articulo", e);
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "InventoryReconciliation.detail.error", e.getMessage());
        }
    }

    public void cancelDetail() {
        detail = null;
        reason = null;
    }

    // -----------------------------------------------------------------
    // Aplicar
    // -----------------------------------------------------------------

    public void confirmAdjustment() {
        if (detail == null) return;
        if (detail.isBlockedByNegativeFinal()) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "InventoryReconciliation.detail.negativeFinal",
                    detail.getReconstructedQuantity());
            return;
        }
        if (reason == null || reason.trim().isEmpty()) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "InventoryReconciliation.confirm.reasonRequired");
            return;
        }

        try {
            String userCode = currentUser != null ? currentUser.getFinancesCode() : null;
            inventoryReconciliationService.applyAdjustment(
                    detail, startDate, endDate, reason, userCode);

            facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO,
                    "InventoryReconciliation.confirm.success",
                    detail.getProductItemCode());

            // Cerrar modal y refrescar listado
            detail = null;
            reason = null;
            search();

        } catch (OptimisticLockException e) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "InventoryReconciliation.confirm.staleData");
        } catch (NegativeFinalBalanceException e) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "InventoryReconciliation.detail.negativeFinal", e.getFinalQuantity());
        } catch (MultipleWarehouseException e) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "InventoryReconciliation.detail.multipleWarehouses",
                    e.getWarehouseCodes().toString());
        } catch (Exception e) {
            log.error("Error aplicando ajuste", e);
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "InventoryReconciliation.confirm.error", e.getMessage());
        }
    }

    public void cleanWarehouse() {
        warehouse = null;
        diffs.clear();
        detail = null;
        searched = false;
    }

    // -----------------------------------------------------------------
    // Getters / setters
    // -----------------------------------------------------------------

    public Warehouse getWarehouse() { return warehouse; }
    public void setWarehouse(Warehouse warehouse) { this.warehouse = warehouse; }

    public Date getStartDate() { return startDate; }
    public void setStartDate(Date startDate) { this.startDate = startDate; }

    public Date getEndDate() { return endDate; }
    public void setEndDate(Date endDate) { this.endDate = endDate; }

    public List<ReconciliationDiff> getDiffs() { return diffs; }

    public boolean isSearched() { return searched; }

    public ReconciliationDetail getDetail() { return detail; }

    public boolean isShowDetail() { return detail != null; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
