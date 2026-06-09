package com.encens.khipus.service.warehouse;

import com.encens.khipus.exception.ConcurrencyException;
import com.encens.khipus.exception.ReferentialIntegrityException;
import com.encens.khipus.exception.finances.CompanyConfigurationNotFoundException;
import com.encens.khipus.exception.finances.FinancesCurrencyNotFoundException;
import com.encens.khipus.exception.finances.FinancesExchangeRateNotFoundException;
import com.encens.khipus.exception.warehouse.InventoryException;
import com.encens.khipus.exception.warehouse.InventoryProductItemNotFoundException;
import com.encens.khipus.exception.warehouse.InventoryUnitaryBalanceException;
import com.encens.khipus.exception.warehouse.ProductItemAmountException;
import com.encens.khipus.exception.warehouse.ProductItemNotFoundException;
import com.encens.khipus.exception.warehouse.ReverseNotAllowedException;
import com.encens.khipus.exception.warehouse.WarehouseAccountCashNotFoundException;
import com.encens.khipus.exception.warehouse.WarehouseVoucherApprovedException;
import com.encens.khipus.exception.warehouse.WarehouseVoucherEmptyException;
import com.encens.khipus.exception.warehouse.WarehouseVoucherNotFoundException;
import com.encens.khipus.util.ValidatorUtil;
import com.encens.khipus.model.warehouse.DispatchState;
import com.encens.khipus.model.warehouse.DispatchStockImpact;
import com.encens.khipus.model.warehouse.DocumentTypePK;
import com.encens.khipus.model.warehouse.InventoryMovement;
import com.encens.khipus.model.warehouse.InventoryPackaging;
import com.encens.khipus.model.warehouse.MovementDetail;
import com.encens.khipus.model.warehouse.MovementDetailType;
import com.encens.khipus.model.warehouse.ProductItem;
import com.encens.khipus.model.warehouse.WarehouseDocumentType;
import com.encens.khipus.model.warehouse.WarehouseVoucher;
import com.encens.khipus.model.warehouse.WarehouseVoucherDispatch;
import com.encens.khipus.model.warehouse.WarehouseVoucherDispatchDetail;
import com.encens.khipus.model.warehouse.WarehouseVoucherDispatchEnvelope;
import com.encens.khipus.model.warehouse.WarehouseVoucherState;
import com.encens.khipus.model.warehouse.WarehouseVoucherType;
import com.encens.khipus.service.common.SequenceGeneratorService;
import com.encens.khipus.service.finances.FinancesUserService;
import com.encens.khipus.util.BigDecimalUtil;
import com.encens.khipus.util.MessageUtils;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * Implementacion del servicio CRUD del Vale de Despacho (BORRADOR).
 */
@Stateless
@Name("dispatchVoucherService")
@AutoCreate
public class DispatchVoucherServiceBean implements DispatchVoucherService {

    public static final String DISPATCH_ORDER_NUMBER_SEQUENCE = "DISPATCH_ORDER_NUMBER";
    public static final String DISPATCH_DOCUMENT_CODE = "DSP";

    @In(value = "#{entityManager}")
    private EntityManager em;

    @In
    private FinancesUserService financesUserService;

    @In
    private SequenceGeneratorService sequenceGeneratorService;

    @In
    private InventoryService inventoryService;

    @In
    private WarehouseService warehouseService;

    @In
    private ApprovalWarehouseVoucherService approvalWarehouseVoucherService;

    @In
    private ReverseWarehouseVoucherService reverseWarehouseVoucherService;

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public WarehouseVoucherDispatch saveDraft(WarehouseVoucherDispatch dispatch) {
        if (dispatch.getState() == null) {
            dispatch.setState(DispatchState.BORRADOR);
        }
        if (dispatch.getState() != DispatchState.BORRADOR) {
            throw new IllegalStateException(
                    "Solo se puede guardar un despacho en estado BORRADOR (estado actual: "
                            + dispatch.getState() + ")");
        }
        String userCode = financesUserService.getFinancesUserCode();
        Date now = new Date();
        dispatch.setCreatedBy(userCode);
        dispatch.setCreatedDate(now);
        dispatch.setUpdatedBy(userCode);
        dispatch.setUpdatedDate(now);

        // El @CompanyListener pone company; el campo no_cia se autollena en
        // los setters de Warehouse/CostCenter/Provider. Si llega null aqui,
        // significa que el form no se completo aun - dejamos persistir y
        // que la BD valide cuando corresponda en aprobacion.

        linkDetails(dispatch);
        em.persist(dispatch);
        em.flush();
        return dispatch;
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public WarehouseVoucherDispatch updateDraft(WarehouseVoucherDispatch dispatch) {
        if (dispatch.getState() != DispatchState.BORRADOR) {
            throw new IllegalStateException(
                    "Solo se puede editar un despacho en estado BORRADOR (estado actual: "
                            + dispatch.getState() + ")");
        }
        dispatch.setUpdatedBy(financesUserService.getFinancesUserCode());
        dispatch.setUpdatedDate(new Date());

        linkDetails(dispatch);
        WarehouseVoucherDispatch merged = em.merge(dispatch);
        em.flush();
        return merged;
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public void deleteDraft(WarehouseVoucherDispatch dispatch) {
        if (dispatch.getState() != DispatchState.BORRADOR) {
            throw new IllegalStateException(
                    "Solo se puede eliminar un despacho en estado BORRADOR (estado actual: "
                            + dispatch.getState() + ")");
        }
        WarehouseVoucherDispatch managed = em.find(WarehouseVoucherDispatch.class, dispatch.getId());
        if (managed != null) {
            em.remove(managed);
            em.flush();
        }
    }

    @Override
    public WarehouseVoucherDispatch findById(Long id) {
        return em.find(WarehouseVoucherDispatch.class, id);
    }

    /**
     * Asegura que cada detalle tenga la referencia inversa al despacho.
     * Necesario porque JPA 1.0 no tiene mappedBy bidireccional auto-sync.
     */
    private void linkDetails(WarehouseVoucherDispatch dispatch) {
        if (dispatch.getDetails() == null) {
            return;
        }
        for (WarehouseVoucherDispatchDetail detail : dispatch.getDetails()) {
            detail.setDispatch(dispatch);
        }
    }

    /* =============================================================
     * Aprobacion - Paso 2: preview de impacto en inventario
     * ============================================================= */

    @Override
    public List<DispatchStockImpact> calculateInventoryImpact(WarehouseVoucherDispatch dispatch) {
        List<DispatchStockImpact> impact = new ArrayList<DispatchStockImpact>();
        if (dispatch == null || dispatch.getWarehouse() == null
                || dispatch.getDetails() == null) {
            return impact;
        }
        for (WarehouseVoucherDispatchDetail line : dispatch.getDetails()) {
            ProductItem item = line.getProductItem();
            if (item == null || line.getQuantity() == null) {
                continue;
            }
            BigDecimal current = inventoryService
                    .findUnitaryBalanceByProductItemAndArticle(
                            dispatch.getWarehouse().getId(), item.getId());
            if (current == null) {
                current = BigDecimal.ZERO;
            }
            BigDecimal required = line.getQuantity();
            BigDecimal result = BigDecimalUtil.subtract(current, required, 6);
            BigDecimal unitCost = item.getUnitCost() != null
                    ? item.getUnitCost() : BigDecimal.ZERO;
            BigDecimal amount = BigDecimalUtil.multiply(required, unitCost, 6);
            impact.add(new DispatchStockImpact(item, line.getMeasureUnit(),
                    current, required, result, unitCost, amount));
        }
        return impact;
    }

    /* =============================================================
     * Aprobacion definitiva del despacho
     * ============================================================= */

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public WarehouseVoucherDispatch approve(WarehouseVoucherDispatch dispatch)
            throws InventoryException,
                   WarehouseVoucherApprovedException,
                   WarehouseVoucherNotFoundException,
                   WarehouseVoucherEmptyException,
                   ProductItemAmountException,
                   InventoryUnitaryBalanceException,
                   InventoryProductItemNotFoundException,
                   CompanyConfigurationNotFoundException,
                   FinancesCurrencyNotFoundException,
                   FinancesExchangeRateNotFoundException,
                   ConcurrencyException,
                   ReferentialIntegrityException,
                   ProductItemNotFoundException,
                   WarehouseAccountCashNotFoundException {

        // 1) Re-validacion de estado y precondiciones minimas
        if (dispatch.getState() != DispatchState.BORRADOR) {
            throw new IllegalStateException(
                    "Solo se puede aprobar un despacho en estado BORRADOR (actual: "
                            + dispatch.getState() + ")");
        }
        if (dispatch.getDetails() == null || dispatch.getDetails().isEmpty()) {
            throw new IllegalStateException(
                    "El despacho debe tener al menos una linea para ser aprobado.");
        }
        if (dispatch.getWarehouse() == null
                || dispatch.getCostCenter() == null
                || dispatch.getExecutorUnit() == null
                || dispatch.getResponsible() == null
                || dispatch.getClient() == null
                || dispatch.getTransportCompany() == null) {
            throw new IllegalStateException(
                    "Faltan campos obligatorios para aprobar el despacho.");
        }

        // 2) Asignar N de orden de entrega si no tiene
        if (dispatch.getDeliveryOrderNumber() == null) {
            long next = sequenceGeneratorService.nextValue(DISPATCH_ORDER_NUMBER_SEQUENCE);
            dispatch.setDeliveryOrderNumber(next);
        }

        // 3) Buscar DocumentType DSP (semilla en query_v6.0.79)
        DocumentTypePK docPk = new DocumentTypePK(
                dispatch.getCompanyNumber(), DISPATCH_DOCUMENT_CODE);
        WarehouseDocumentType docType = em.find(WarehouseDocumentType.class, docPk);
        if (docType == null) {
            // Fallback: cualquier tipo de salida disponible
            docType = warehouseService.findWarehouseDocumentType(WarehouseVoucherType.S);
        }

        // 4) Construir WarehouseVoucher de egreso
        WarehouseVoucher vale = new WarehouseVoucher();
        vale.setDate(dispatch.getDispatchDate());
        vale.setExecutorUnit(dispatch.getExecutorUnit());
        vale.setCostCenter(dispatch.getCostCenter());
        vale.setWarehouse(dispatch.getWarehouse());
        vale.setResponsible(dispatch.getResponsible());
        vale.setState(WarehouseVoucherState.PEN);
        vale.setDocumentType(docType);

        // 5) InventoryMovement
        InventoryMovement inv = new InventoryMovement();
        inv.setCreationDate(new Date());
        inv.setMovementDate(dispatch.getDispatchDate());
        String desc = MessageUtils.getMessage("WarehouseDispatch.gloss.movement",
                dispatch.getDeliveryOrderNumber(),
                dispatch.getClient() != null ? dispatch.getClient().getFullName() : "",
                dispatch.getSalesLotCode());
        inv.setDescription(desc);

        // 6) MovementDetail (tipo S = output)
        List<MovementDetail> details = new ArrayList<MovementDetail>();
        for (WarehouseVoucherDispatchDetail line : dispatch.getDetails()) {
            ProductItem item = line.getProductItem();
            BigDecimal unitCost = item.getUnitCost() != null
                    ? item.getUnitCost() : BigDecimal.ZERO;
            BigDecimal amount = BigDecimalUtil.multiply(line.getQuantity(), unitCost, 6);

            MovementDetail md = new MovementDetail();
            md.setProductItem(item);
            md.setProductItemCode(item.getId().getProductItemCode());
            md.setMeasureUnit(line.getMeasureUnit());
            md.setMeasureCode(line.getMeasureUnitCode());
            md.setQuantity(line.getQuantity());
            md.setUnitCost(unitCost);
            md.setAmount(amount);
            md.setUnitPurchasePrice(unitCost);
            md.setPurchasePrice(amount);
            md.setMovementType(MovementDetailType.S);
            md.setWarehouse(dispatch.getWarehouse());
            md.setCashAccount(item.getCashAccount());
            details.add(md);

            // Snapshot en la linea del despacho
            line.setUnitCost(unitCost);
            line.setAmount(amount);
        }

        // 7) Guardar y aprobar el vale (descuento de stock + asiento contable)
        HashMap<MovementDetail, BigDecimal> under = new HashMap<MovementDetail, BigDecimal>();
        HashMap<MovementDetail, BigDecimal> over = new HashMap<MovementDetail, BigDecimal>();
        List<MovementDetail> withoutWarnings = new ArrayList<MovementDetail>();

        warehouseService.saveWarehouseVoucher(vale, inv, details, under, over, withoutWarnings);
        approvalWarehouseVoucherService.approveWarehouseVoucher(
                vale.getId(), buildGloss(dispatch), under, over, withoutWarnings);

        // 8) Enlazar vale al despacho y persistir
        dispatch.setWarehouseVoucher(vale);
        dispatch.setState(DispatchState.APROBADO);
        dispatch.setUpdatedBy(financesUserService.getFinancesUserCode());
        dispatch.setUpdatedDate(new Date());

        // 9) Generar envases por cada detalle (Eager). Cada bolsa fisica del
        //    despacho se materializa como una fila en inv_valedespacho_envase
        //    con codigo, detalle textual y peso editables hasta que el
        //    despacho pase a FINALIZADO.
        generateEnvelopes(dispatch);

        WarehouseVoucherDispatch merged = em.merge(dispatch);
        em.flush();

        return merged;
    }

    /**
     * Materializa una fila por bolsa fisica en cada detalle del despacho.
     * Numero de filas por detalle = detail.bagsCount (autoritativo). El
     * correlativo arranca en detail.bagsFromNumber (si null, en 1).
     * codigo_identificacion = salesLotCode + "/" + lpad(correlativo, 3, '0').
     */
    private void generateEnvelopes(WarehouseVoucherDispatch dispatch) {
        if (dispatch.getDetails() == null) {
            return;
        }
        String lot = dispatch.getSalesLotCode() != null ? dispatch.getSalesLotCode() : "";
        String userCode = financesUserService.getFinancesUserCode();
        Date now = new Date();

        for (WarehouseVoucherDispatchDetail det : dispatch.getDetails()) {
            Integer count = det.getBagsCount();
            if (count == null || count <= 0) {
                continue;
            }
            int from = det.getBagsFromNumber() != null ? det.getBagsFromNumber() : 1;
            InventoryPackaging pkg = det.getPackaging();
            String defaultDetail = pkg != null ? pkg.getDefaultEnvelopeDetail() : null;
            java.math.BigDecimal defaultWeight = pkg != null ? pkg.getUnitCapacityKg() : null;

            if (det.getEnvelopes() == null) {
                det.setEnvelopes(new ArrayList<WarehouseVoucherDispatchEnvelope>());
            }
            // Idempotencia: si el detalle ya tiene envases (ej. re-aprobacion
            // tras anulacion), no regeneramos.
            if (!det.getEnvelopes().isEmpty()) {
                continue;
            }
            for (int i = 0; i < count; i++) {
                int correlative = from + i;
                WarehouseVoucherDispatchEnvelope env = new WarehouseVoucherDispatchEnvelope();
                env.setDispatch(dispatch);
                env.setDetail(det);
                env.setCorrelativeNumber(correlative);
                env.setIdentificationCode(lot + "/" + String.format("%03d", correlative));
                env.setEnvelopeDetail(defaultDetail);
                env.setNetWeightApproxKg(defaultWeight);
                env.setCreatedBy(userCode);
                env.setCreatedDate(now);
                env.setUpdatedBy(userCode);
                env.setUpdatedDate(now);
                det.getEnvelopes().add(env);
            }
        }
    }

    private String[] buildGloss(WarehouseVoucherDispatch d) {
        String clientName = d.getClient() != null ? d.getClient().getFullName() : "";
        String message = MessageUtils.getMessage("WarehouseDispatch.gloss.format",
                d.getDeliveryOrderNumber(),
                clientName,
                d.getSalesLotCode());
        // El servicio existente approveWarehouseVoucher accede a gloss[1]
        // internamente al armar lineas del asiento (gloss[0]=outgoing,
        // gloss[1]=incoming en flujos de transferencia; para salida simple
        // ambos slots se rellenan con el mismo mensaje para evitar
        // ArrayIndexOutOfBoundsException). Mismo patron que
        // WarehouseVoucherUpdateAction.getGlossMessage().
        String[] gloss = new String[2];
        gloss[0] = message;
        gloss[1] = message;
        return gloss;
    }

    /* =============================================================
     * Anulacion del despacho aprobado
     * ============================================================= */

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public WarehouseVoucherDispatch annul(WarehouseVoucherDispatch dispatch, String reason)
            throws ReverseNotAllowedException,
                   WarehouseVoucherNotFoundException,
                   InventoryUnitaryBalanceException,
                   InventoryProductItemNotFoundException {

        if (dispatch.getState() != DispatchState.APROBADO) {
            throw new IllegalStateException(
                    "Solo se puede anular un despacho APROBADO (actual: "
                            + dispatch.getState() + ")");
        }
        if (dispatch.getWarehouseVoucher() == null
                || dispatch.getWarehouseVoucher().getId() == null) {
            throw new IllegalStateException(
                    "El despacho no tiene un WarehouseVoucher enlazado; no se puede anular.");
        }
        if (ValidatorUtil.isBlankOrNull(reason)) {
            throw new IllegalArgumentException(
                    "El motivo de anulacion es obligatorio.");
        }

        String userCode = financesUserService.getFinancesUserCode();

        // Delegar la reversion al servicio existente. skipValidation=true
        // porque la validacion de elegibilidad ya la hicimos aqui (estado
        // APROBADO + vale enlazado); ademas el vale fue generado desde este
        // flujo y no pertenece al flujo independiente de vales.
        reverseWarehouseVoucherService.reverseWarehouseVoucher(
                dispatch.getWarehouseVoucher().getId(),
                reason,
                userCode,
                true);

        // Auditoria de anulacion en el despacho + cambio de estado
        Date now = new Date();
        dispatch.setState(DispatchState.ANULADO);
        dispatch.setAnnulReason(reason.trim());
        dispatch.setAnnulUser(userCode);
        dispatch.setAnnulDate(now);
        dispatch.setUpdatedBy(userCode);
        dispatch.setUpdatedDate(now);

        WarehouseVoucherDispatch merged = em.merge(dispatch);
        em.flush();
        return merged;
    }

    /* =============================================================
     * Finalizar / reverso (APR <-> FIN)
     * ============================================================= */

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public WarehouseVoucherDispatch finalizeDispatch(WarehouseVoucherDispatch dispatch) {
        if (dispatch.getState() != DispatchState.APROBADO) {
            throw new IllegalStateException(
                    "Solo se puede finalizar un despacho APROBADO (actual: "
                            + dispatch.getState() + ")");
        }
        String userCode = financesUserService.getFinancesUserCode();
        dispatch.setState(DispatchState.FINALIZADO);
        dispatch.setUpdatedBy(userCode);
        dispatch.setUpdatedDate(new Date());
        WarehouseVoucherDispatch merged = em.merge(dispatch);
        em.flush();
        return merged;
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public WarehouseVoucherDispatch unfinalizeDispatch(WarehouseVoucherDispatch dispatch) {
        if (dispatch.getState() != DispatchState.FINALIZADO) {
            throw new IllegalStateException(
                    "Solo se puede desfinalizar un despacho FINALIZADO (actual: "
                            + dispatch.getState() + ")");
        }
        String userCode = financesUserService.getFinancesUserCode();
        dispatch.setState(DispatchState.APROBADO);
        dispatch.setUpdatedBy(userCode);
        dispatch.setUpdatedDate(new Date());
        WarehouseVoucherDispatch merged = em.merge(dispatch);
        em.flush();
        return merged;
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public WarehouseVoucherDispatch updateEnvelopes(WarehouseVoucherDispatch dispatch) {
        if (dispatch.getState() != DispatchState.APROBADO) {
            throw new IllegalStateException(
                    "Solo se pueden editar envases de un despacho APROBADO (actual: "
                            + dispatch.getState() + ")");
        }
        String userCode = financesUserService.getFinancesUserCode();
        Date now = new Date();
        if (dispatch.getDetails() != null) {
            for (WarehouseVoucherDispatchDetail det : dispatch.getDetails()) {
                if (det.getEnvelopes() == null) continue;
                for (WarehouseVoucherDispatchEnvelope env : det.getEnvelopes()) {
                    env.setUpdatedBy(userCode);
                    env.setUpdatedDate(now);
                }
            }
        }
        dispatch.setUpdatedBy(userCode);
        dispatch.setUpdatedDate(now);
        WarehouseVoucherDispatch merged = em.merge(dispatch);
        em.flush();
        return merged;
    }
}
