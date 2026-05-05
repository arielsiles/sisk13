package com.encens.khipus.service.warehouse;

import com.encens.khipus.action.warehouse.reconciliation.dto.ReconciliationDetail;
import com.encens.khipus.action.warehouse.reconciliation.dto.ReconciliationDiff;
import com.encens.khipus.action.warehouse.reconciliation.dto.ReconciliationStep;
import com.encens.khipus.exception.warehouse.MultipleWarehouseException;
import com.encens.khipus.exception.warehouse.NegativeFinalBalanceException;
import com.encens.khipus.exception.warehouse.NoInitialInventoryException;
import com.encens.khipus.framework.service.GenericServiceBean;
import com.encens.khipus.model.warehouse.InventoryAdjustment;
import com.encens.khipus.util.BigDecimalUtil;
import com.encens.khipus.util.Constants;
import com.encens.khipus.util.DateUtils;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.OptimisticLockException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 * Implementacion de la pantalla "Almacenes > Configuracion > Actualizar Inventario".
 *
 * - listDifferences: compara saldos calculados (provistos por el caller) vs inv_inventario.
 * - reconstructArticle: replay cronologico desde el primer inv_inicio aplicando promedio
 *   ponderado, hasta endDate. Retorna trace + valores reconstruidos + alertas.
 * - applyAdjustment: actualiza inv_inventario, inv_inventario_detalle, inv_articulos
 *   con check de version optimista, y graba auditoria en inv_ajuste_saldo.
 *
 * Lee inv_movdet.costounitario y inv_movdet.monto tal cual fueron grabados por el flujo
 * normal de aprobacion de vales (ya vienen netos del 87% en compras con factura).
 */
@Stateless
@Name("inventoryReconciliationService")
@AutoCreate
public class InventoryReconciliationServiceBean extends GenericServiceBean
        implements InventoryReconciliationService {

    @In(value = "#{entityManager}")
    private EntityManager em;

    /** Tolerancia para considerar dos saldos iguales (ruido de redondeo). */
    private static final BigDecimal EPSILON = new BigDecimal("0.001");

    /** Escala interna de calculo para el costo promedio. */
    private static final int COST_SCALE = 6;

    // -----------------------------------------------------------------
    // 1) listDifferences
    // -----------------------------------------------------------------

    @Override
    @SuppressWarnings("unchecked")
    public List<ReconciliationDiff> listDifferences(String warehouseCode,
                                                    Map<String, BigDecimal> calculatedSaldos) {
        if (warehouseCode == null || calculatedSaldos == null) {
            return Collections.emptyList();
        }

        // Cargar todos los articulos del almacen + su saldo_uni actual
        List<Object[]> rows = em.createNativeQuery(
                "SELECT a.cod_art, a.descri, COALESCE(i.saldo_uni, 0) " +
                "FROM inv_articulos a " +
                "LEFT JOIN inv_inventario i " +
                "  ON i.cod_art = a.cod_art AND i.cod_alm = a.cod_alm AND i.no_cia = a.no_cia " +
                "WHERE a.cod_alm = :alm AND a.no_cia = :noCia")
                .setParameter("alm", warehouseCode)
                .setParameter("noCia", Constants.defaultCompanyNumber)
                .getResultList();

        List<ReconciliationDiff> diffs = new ArrayList<ReconciliationDiff>();
        for (Object[] r : rows) {
            String code = (String) r[0];
            String name = (String) r[1];
            BigDecimal current = (BigDecimal) r[2];
            BigDecimal calculated = calculatedSaldos.get(code);
            if (calculated == null) calculated = BigDecimal.ZERO;

            BigDecimal diff = calculated.subtract(current);
            if (diff.abs().compareTo(EPSILON) > 0) {
                diffs.add(new ReconciliationDiff(code, name, calculated, current));
            }
        }

        // Ordenar por |diff| desc
        Collections.sort(diffs, new Comparator<ReconciliationDiff>() {
            public int compare(ReconciliationDiff a, ReconciliationDiff b) {
                return b.getDifference().abs().compareTo(a.getDifference().abs());
            }
        });

        return diffs;
    }

    // -----------------------------------------------------------------
    // 2) reconstructArticle
    // -----------------------------------------------------------------

    @Override
    @SuppressWarnings("unchecked")
    public ReconciliationDetail reconstructArticle(String productItemCode,
                                                   String warehouseCode,
                                                   Date startDate,
                                                   Date endDate,
                                                   BigDecimal reportQuantity)
            throws MultipleWarehouseException, NoInitialInventoryException {

        // 1. Validar multi-almacen
        List<String> warehouses = em.createNativeQuery(
                "SELECT DISTINCT cod_alm FROM inv_inventario " +
                "WHERE cod_art = :art AND no_cia = :noCia")
                .setParameter("art", productItemCode)
                .setParameter("noCia", Constants.defaultCompanyNumber)
                .getResultList();
        if (warehouses != null && warehouses.size() > 1) {
            throw new MultipleWarehouseException(productItemCode, warehouses);
        }

        // 2. Cargar primer inv_inicio (gestion mas antigua)
        // Nota: usamos setMaxResults(1) en lugar de LIMIT en el SQL porque
        // Hibernate appendea internamente "LIMIT 2" en getSingleResult() de queries
        // nativas, lo que choca con un LIMIT explicito en el SQL.
        @SuppressWarnings("unchecked")
        List<Object[]> iniRows = em.createNativeQuery(
                "SELECT cantidad, costo_uni, gestion FROM inv_inicio " +
                "WHERE alm = :alm AND cod_art = :art AND no_cia = :noCia " +
                "ORDER BY gestion ASC")
                .setParameter("alm", warehouseCode)
                .setParameter("art", productItemCode)
                .setParameter("noCia", Constants.defaultCompanyNumber)
                .setMaxResults(1)
                .getResultList();
        if (iniRows.isEmpty()) {
            throw new NoInitialInventoryException(productItemCode, warehouseCode);
        }
        Object[] inicio = iniRows.get(0);

        BigDecimal qty = nullToZero((BigDecimal) inicio[0]);
        BigDecimal costoUni = nullToZero((BigDecimal) inicio[1]);
        String gestionInicial = (String) inicio[2];
        BigDecimal totalMon = qty.multiply(costoUni);

        Date fechaIni = DateUtils.firstDayOfYear(Integer.valueOf(gestionInicial));

        // 3. Movimientos APR ordenados cronologicamente.
        //    LEFT JOIN agregado a sf_tmpdet para traer el valor contable (debe+haber)
        //    asociado al articulo en el asiento del vale (vale.idtmpenc -> sf_tmpdet).
        @SuppressWarnings("unchecked")
        List<Object[]> movs = em.createNativeQuery(
                "SELECT v.fecha, md.tipo_mov, md.cantidad, md.costounitario, md.monto, " +
                "       v.no_trans, md.id_inv_movdet, " +
                "       COALESCE(td.acc_amount, 0) AS acc_amount " +
                "FROM inv_movdet md " +
                "JOIN inv_vales  v ON v.no_trans = md.no_trans AND v.no_cia = md.no_cia " +
                "LEFT JOIN ( " +
                "    SELECT id_tmpenc, cod_art, " +
                "           SUM(COALESCE(debe,0) + COALESCE(haber,0)) AS acc_amount " +
                "    FROM sf_tmpdet " +
                "    WHERE cod_art = :art " +
                "    GROUP BY id_tmpenc, cod_art " +
                ") td ON td.id_tmpenc = v.idtmpenc AND td.cod_art = md.cod_art " +
                "WHERE md.no_cia = :noCia AND md.cod_alm = :alm AND md.cod_art = :art " +
                "  AND v.estado = 'APR' " +
                "  AND v.fecha BETWEEN :ini AND :fin " +
                "ORDER BY v.fecha ASC, md.id_inv_movdet ASC")
                .setParameter("noCia", Constants.defaultCompanyNumber)
                .setParameter("alm", warehouseCode)
                .setParameter("art", productItemCode)
                .setParameter("ini", fechaIni)
                .setParameter("fin", endDate)
                .getResultList();

        // 4. Caminata cronologica (promedio ponderado movil)
        List<ReconciliationStep> trace = new ArrayList<ReconciliationStep>();
        boolean hasNegatives = false;

        // Paso 0: inv_inicio como punto de partida
        ReconciliationStep ini = new ReconciliationStep();
        ini.setDate(fechaIni);
        ini.setReference("INV_INICIO[" + gestionInicial + "]");
        ini.setType(ReconciliationStep.StepType.INV_INICIO);
        ini.setQuantity(qty);
        ini.setMovementUnitCost(costoUni);
        ini.setRunningQuantity(qty);
        ini.setRunningUnitCost(costoUni);
        ini.setRunningTotalValue(totalMon);
        trace.add(ini);

        for (Object[] mov : movs) {
            Date fecha = (Date) mov[0];
            String tipo = (String) mov[1];
            BigDecimal cantidad = nullToZero((BigDecimal) mov[2]);
            BigDecimal costoMov = nullToZero((BigDecimal) mov[3]);
            BigDecimal montoMov = nullToZero((BigDecimal) mov[4]);
            String noTrans = (String) mov[5];
            BigDecimal accAmount = nullToZero((BigDecimal) mov[7]);

            ReconciliationStep step = new ReconciliationStep();
            step.setDate(fecha);
            step.setReference(noTrans);
            step.setQuantity(cantidad);
            step.setAccountingAmount(accAmount);

            if ("E".equals(tipo)) {
                step.setType(ReconciliationStep.StepType.ENTRADA);
                step.setMovementUnitCost(costoMov);
                qty = qty.add(cantidad);
                totalMon = totalMon.add(montoMov);
                if (qty.compareTo(BigDecimal.ZERO) > 0) {
                    costoUni = totalMon.divide(qty, COST_SCALE, RoundingMode.HALF_UP);
                }
            } else { // S
                step.setType(ReconciliationStep.StepType.SALIDA);
                qty = qty.subtract(cantidad);
                totalMon = totalMon.subtract(cantidad.multiply(costoUni));
                // costoUni no cambia
            }

            if (qty.compareTo(BigDecimal.ZERO) < 0) {
                step.setNegativeFlag(true);
                hasNegatives = true;
            }

            step.setRunningQuantity(qty);
            step.setRunningUnitCost(costoUni);
            step.setRunningTotalValue(totalMon);
            trace.add(step);
        }

        // 5. Armar el DTO de resultado
        ReconciliationDetail detail = new ReconciliationDetail();
        detail.setProductItemCode(productItemCode);
        detail.setWarehouseCode(warehouseCode);
        detail.setProductItemName(loadProductItemName(productItemCode));
        detail.setWarehouseName(loadWarehouseName(warehouseCode));
        detail.setTrace(trace);
        detail.setReconstructedQuantity(qty);
        detail.setReconstructedUnitCost(costoUni);
        detail.setReconstructedMonetaryBalance(totalMon);
        detail.setReportQuantity(reportQuantity);
        detail.setHasIntermediateNegatives(hasNegatives);
        detail.setBlockedByNegativeFinal(qty.compareTo(BigDecimal.ZERO) < 0);
        detail.setReportMismatch(reportQuantity != null
                && qty.subtract(reportQuantity).abs().compareTo(EPSILON) > 0);

        // Cargar valores actuales y versiones para detectar race en applyAdjustment
        loadCurrentValues(detail);

        return detail;
    }

    // -----------------------------------------------------------------
    // 3) applyAdjustment
    // -----------------------------------------------------------------

    @Override
    public void applyAdjustment(ReconciliationDetail detail,
                                Date calcStartDate,
                                Date calcEndDate,
                                String reason,
                                String userNumber)
            throws MultipleWarehouseException, NegativeFinalBalanceException {

        if (detail == null) {
            throw new IllegalArgumentException("detail is null");
        }
        if (reason == null || reason.trim().isEmpty()) {
            throw new IllegalArgumentException("reason is required");
        }
        if (detail.isBlockedByNegativeFinal()) {
            throw new NegativeFinalBalanceException(detail.getProductItemCode(),
                    detail.getReconstructedQuantity());
        }

        // Re-validar multi-almacen
        @SuppressWarnings("unchecked")
        List<String> warehouses = em.createNativeQuery(
                "SELECT DISTINCT cod_alm FROM inv_inventario " +
                "WHERE cod_art = :art AND no_cia = :noCia")
                .setParameter("art", detail.getProductItemCode())
                .setParameter("noCia", Constants.defaultCompanyNumber)
                .getResultList();
        if (warehouses != null && warehouses.size() > 1) {
            throw new MultipleWarehouseException(detail.getProductItemCode(), warehouses);
        }

        BigDecimal newQty = detail.getReconstructedQuantity();
        BigDecimal newCost = detail.getReconstructedUnitCost();
        BigDecimal newMon = detail.getReconstructedMonetaryBalance();

        // 1) inv_inventario con check de version
        int rows = em.createNativeQuery(
                "UPDATE inv_inventario SET saldo_uni = :q, version = version + 1 " +
                "WHERE no_cia = :noCia AND cod_alm = :alm AND cod_art = :art " +
                "  AND version = :v")
                .setParameter("q", newQty)
                .setParameter("noCia", Constants.defaultCompanyNumber)
                .setParameter("alm", detail.getWarehouseCode())
                .setParameter("art", detail.getProductItemCode())
                .setParameter("v", detail.getInventoryVersion())
                .executeUpdate();
        if (rows == 0) {
            throw new OptimisticLockException(
                    "inv_inventario fue modificado por otro proceso. Refresque y reintente.");
        }

        // 2) inv_inventario_detalle con check de version
        rows = em.createNativeQuery(
                "UPDATE inv_inventario_detalle SET cantidad = :q, version = version + 1 " +
                "WHERE no_cia = :noCia AND cod_alm = :alm AND cod_art = :art " +
                "  AND version = :v")
                .setParameter("q", newQty)
                .setParameter("noCia", Constants.defaultCompanyNumber)
                .setParameter("alm", detail.getWarehouseCode())
                .setParameter("art", detail.getProductItemCode())
                .setParameter("v", detail.getInventoryDetailVersion())
                .executeUpdate();
        if (rows == 0) {
            throw new OptimisticLockException(
                    "inv_inventario_detalle fue modificado por otro proceso. Refresque y reintente.");
        }

        // 3) inv_articulos con check de version
        rows = em.createNativeQuery(
                "UPDATE inv_articulos SET costo_uni = :c, saldo_mon = :m, version = version + 1 " +
                "WHERE no_cia = :noCia AND cod_art = :art AND version = :v")
                .setParameter("c", newCost)
                .setParameter("m", newMon)
                .setParameter("noCia", Constants.defaultCompanyNumber)
                .setParameter("art", detail.getProductItemCode())
                .setParameter("v", detail.getProductItemVersion())
                .executeUpdate();
        if (rows == 0) {
            throw new OptimisticLockException(
                    "inv_articulos fue modificado por otro proceso. Refresque y reintente.");
        }

        // 4) Auditoria
        InventoryAdjustment adj = new InventoryAdjustment();
        adj.setCompanyNumber(Constants.defaultCompanyNumber);
        adj.setWarehouseCode(detail.getWarehouseCode());
        adj.setProductItemCode(detail.getProductItemCode());
        adj.setAdjustmentDate(new Date());
        adj.setCalcStartDate(calcStartDate);
        adj.setCalcEndDate(calcEndDate);
        adj.setPreviousQuantity(detail.getCurrentQuantity());
        adj.setNewQuantity(newQty);
        adj.setPreviousUnitCost(detail.getCurrentUnitCost());
        adj.setNewUnitCost(newCost);
        adj.setPreviousMonetaryBalance(detail.getCurrentMonetaryBalance());
        adj.setNewMonetaryBalance(newMon);
        adj.setReason(reason.trim());
        adj.setUserNumber(userNumber);
        em.persist(adj);
        em.flush();
    }

    // -----------------------------------------------------------------
    // Helpers privados
    // -----------------------------------------------------------------

    private void loadCurrentValues(ReconciliationDetail detail) {
        Object[] inv = singleRowOrNull(
                "SELECT saldo_uni, version FROM inv_inventario " +
                "WHERE no_cia = :noCia AND cod_alm = :alm AND cod_art = :art",
                detail.getWarehouseCode(), detail.getProductItemCode());
        if (inv != null) {
            detail.setCurrentQuantity(nullToZero((BigDecimal) inv[0]));
            detail.setInventoryVersion(((Number) inv[1]).longValue());
        }

        Object[] det = singleRowOrNull(
                "SELECT cantidad, version FROM inv_inventario_detalle " +
                "WHERE no_cia = :noCia AND cod_alm = :alm AND cod_art = :art",
                detail.getWarehouseCode(), detail.getProductItemCode());
        if (det != null) {
            detail.setInventoryDetailVersion(((Number) det[1]).longValue());
        }

        try {
            Object[] art = (Object[]) em.createNativeQuery(
                    "SELECT costo_uni, saldo_mon, version FROM inv_articulos " +
                    "WHERE no_cia = :noCia AND cod_art = :art")
                    .setParameter("noCia", Constants.defaultCompanyNumber)
                    .setParameter("art", detail.getProductItemCode())
                    .getSingleResult();
            detail.setCurrentUnitCost(nullToZero((BigDecimal) art[0]));
            detail.setCurrentMonetaryBalance(nullToZero((BigDecimal) art[1]));
            detail.setProductItemVersion(((Number) art[2]).longValue());
        } catch (NoResultException ignored) {
        }
    }

    private Object[] singleRowOrNull(String sql, String alm, String art) {
        try {
            return (Object[]) em.createNativeQuery(sql)
                    .setParameter("noCia", Constants.defaultCompanyNumber)
                    .setParameter("alm", alm)
                    .setParameter("art", art)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    private String loadProductItemName(String productItemCode) {
        try {
            return (String) em.createNativeQuery(
                    "SELECT descri FROM inv_articulos " +
                    "WHERE no_cia = :noCia AND cod_art = :art")
                    .setParameter("noCia", Constants.defaultCompanyNumber)
                    .setParameter("art", productItemCode)
                    .getSingleResult();
        } catch (NoResultException e) {
            return productItemCode;
        }
    }

    private String loadWarehouseName(String warehouseCode) {
        try {
            return (String) em.createNativeQuery(
                    "SELECT descri FROM inv_almacenes " +
                    "WHERE no_cia = :noCia AND cod_alm = :alm")
                    .setParameter("noCia", Constants.defaultCompanyNumber)
                    .setParameter("alm", warehouseCode)
                    .getSingleResult();
        } catch (NoResultException e) {
            return warehouseCode;
        }
    }

    private static BigDecimal nullToZero(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }
}
