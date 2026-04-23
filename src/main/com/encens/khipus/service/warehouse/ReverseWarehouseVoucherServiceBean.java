package com.encens.khipus.service.warehouse;

import com.encens.khipus.exception.warehouse.InventoryProductItemNotFoundException;
import com.encens.khipus.exception.warehouse.InventoryUnitaryBalanceException;
import com.encens.khipus.exception.warehouse.ReverseNotAllowedException;
import com.encens.khipus.exception.warehouse.WarehouseVoucherNotFoundException;
import com.encens.khipus.framework.service.GenericServiceBean;
import com.encens.khipus.model.finances.Voucher;
import com.encens.khipus.model.warehouse.*;
import com.encens.khipus.service.finances.FinancesUserService;
import com.encens.khipus.util.Constants;
import com.encens.khipus.util.MessageUtils;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;
import java.util.Date;
import java.util.List;

/**
 * Implementacion del orquestador de anulacion de vales.
 *
 * Secuencia al anular un vale aprobado:
 *   1. Para cada MovementDetail del inventoryMovement aprobado:
 *        - reverseInventory      -> ajusta inv_inventario e inv_inventario_detalle
 *        - reverseProductItemCost -> ajusta unitCost/cu/investmentAmount/ct
 *        - reverseInventoryHistory-> ajusta inv_invmes del mes actual
 *   2. Marca los MovementDetail y el InventoryMovement como ANL.
 *   3. Si el vale tiene un asiento contable asociado, genera un contra-asiento
 *      simetrico (createReverseAccountEntry).
 *   4. Setea state=ANL y auditoria (fecha_anul/usuario_anul/motivo_anul) en
 *      el vale original.
 *
 * Para vales de transferencia entre almacenes (tipo T) no hay asiento que
 * revertir; solo se revierten las 2 lineas de movimiento (salida origen +
 * entrada destino). Para transferencias entre U.Ejec. hay 2 asientos
 * separados; ambos reciben contra-asiento.
 *
 * @version 6.0.70
 */
@Stateless
@Name("reverseWarehouseVoucherService")
@AutoCreate
public class ReverseWarehouseVoucherServiceBean extends GenericServiceBean implements ReverseWarehouseVoucherService {

    @In
    private ReverseInventoryService reverseInventoryService;

    @In
    private WarehouseAccountEntryService warehouseAccountEntryService;

    @In
    private MovementDetailService movementDetailService;

    @In
    private FinancesUserService financesUserService;

    @Override
    public void reverseWarehouseVoucher(WarehouseVoucherPK id,
                                        String reason,
                                        String userCode,
                                        boolean skipValidation)
            throws ReverseNotAllowedException,
                   WarehouseVoucherNotFoundException,
                   InventoryUnitaryBalanceException,
                   InventoryProductItemNotFoundException {

        WarehouseVoucher warehouseVoucher = getEntityManager().find(WarehouseVoucher.class, id);
        if (null == warehouseVoucher) {
            throw new WarehouseVoucherNotFoundException(
                    "Warehouse voucher not found for id=" + id);
        }
        getEntityManager().refresh(warehouseVoucher);

        if (!skipValidation) {
            validateReversibility(warehouseVoucher);
        } else {
            // Siempre bloqueamos si ya esta anulado, incluso si se salta el resto.
            if (warehouseVoucher.isNullified()) {
                throw new ReverseNotAllowedException(
                        MessageUtils.getMessage("WarehouseVoucher.reverse.alreadyNullified"));
            }
        }

        // 1) Revertir inventario por cada movement detail aprobado
        InventoryMovement approvedMovement = getApprovedMovement(warehouseVoucher);
        List<MovementDetail> details = approvedMovement != null
                ? movementDetailService.findDetailListByVoucher(warehouseVoucher)
                : java.util.Collections.<MovementDetail>emptyList();

        for (MovementDetail detail : details) {
            Warehouse warehouseForDetail = resolveWarehouseForDetail(warehouseVoucher, detail);
            reverseInventoryService.reverseInventory(warehouseVoucher, warehouseForDetail, detail);
            reverseInventoryService.reverseProductItemCost(warehouseVoucher, detail);
            reverseInventoryService.reverseInventoryHistory(detail);
        }

        // 2) Marcar movement details y approved movement como ANL
        for (MovementDetail detail : details) {
            detail.setState(WarehouseVoucherState.ANL);
            getEntityManager().merge(detail);
        }
        if (approvedMovement != null) {
            InventoryMovementPK anlPk = new InventoryMovementPK(
                    approvedMovement.getId().getCompanyNumber(),
                    approvedMovement.getId().getTransactionNumber(),
                    WarehouseVoucherState.ANL.name());
            // No existe aun la fila con estado ANL: cambiar el id es complejo con @EmbeddedId,
            // asi que simplemente actualizamos la descripcion como marcador.
            String desc = approvedMovement.getDescription() != null ? approvedMovement.getDescription() : "";
            String marker = Constants.ANNULMENT_REASON_OPEN + (reason != null ? reason : "") + Constants.ANNULMENT_REASON_CLOSE;
            if (!desc.startsWith(marker)) {
                approvedMovement.setDescription(marker + desc);
                getEntityManager().merge(approvedMovement);
            }
        }
        getEntityManager().flush();

        // 3) Contra-asiento si hay voucher asociado
        Voucher originalVoucher = warehouseVoucher.getVoucher();
        if (originalVoucher != null) {
            warehouseAccountEntryService.createReverseAccountEntry(warehouseVoucher, originalVoucher, reason);
        }

        // 4) Auditoria + estado ANL
        warehouseVoucher.setState(WarehouseVoucherState.ANL);
        warehouseVoucher.setNullifyDate(new Date());
        warehouseVoucher.setNullifyUser(
                userCode != null ? userCode : financesUserService.getFinancesUserCode());
        warehouseVoucher.setNullifyReason(reason);
        getEntityManager().merge(warehouseVoucher);
        getEntityManager().flush();
    }

    @Override
    public void validateReversibility(WarehouseVoucher warehouseVoucher)
            throws ReverseNotAllowedException {

        if (warehouseVoucher == null) {
            throw new ReverseNotAllowedException(
                    MessageUtils.getMessage("WarehouseVoucher.reverse.notAllowed"));
        }
        if (warehouseVoucher.isNullified()) {
            throw new ReverseNotAllowedException(
                    MessageUtils.getMessage("WarehouseVoucher.reverse.alreadyNullified"));
        }
        // Solo APR y PAR se pueden anular (PEN no afecto nada aun; ANL ya esta)
        WarehouseVoucherState state = warehouseVoucher.getState();
        if (!WarehouseVoucherState.APR.equals(state) && !WarehouseVoucherState.PAR.equals(state)) {
            throw new ReverseNotAllowedException(
                    MessageUtils.getMessage("WarehouseVoucher.reverse.notAllowed"));
        }
        // Vales con origen en otro modulo deben anularse desde allá
        if (warehouseVoucher.getOperation() != null) {
            throw new ReverseNotAllowedException(
                    MessageUtils.getMessage("WarehouseVoucher.reverse.hasOperation",
                            warehouseVoucher.getOperation().name()));
        }
        // Vales con OC asociada no se anulan directos (el flujo pasa por la OC)
        if (warehouseVoucher.hasPurchaseOrder()) {
            throw new ReverseNotAllowedException(
                    MessageUtils.getMessage("WarehouseVoucher.reverse.hasPurchaseOrder"));
        }
        // Vale de recepcion del almacen de acopio: flujo especial, bloqueado por ahora
        if (isMilkCollectionReception(warehouseVoucher)) {
            throw new ReverseNotAllowedException(
                    MessageUtils.getMessage("WarehouseVoucher.reverse.milkCollectionReception"));
        }
    }

    private boolean isMilkCollectionReception(WarehouseVoucher wv) {
        if (wv == null || wv.getWarehouse() == null || wv.getWarehouse().getId() == null) {
            return false;
        }
        boolean milkWarehouse = wv.getWarehouse().getId().equals(Constants.COD_WAREHUOSE_MILK_COLLECTED);
        boolean receptionDoc = wv.getDocumentType() != null
                && "RECEPCION".equals(wv.getDocumentType().getName());
        return milkWarehouse && receptionDoc;
    }

    @SuppressWarnings("unchecked")
    private InventoryMovement getApprovedMovement(WarehouseVoucher wv) {
        List<InventoryMovement> movements = getEntityManager()
                .createQuery("select im from InventoryMovement im"
                        + " where im.id.companyNumber =:companyNumber"
                        + " and im.id.transactionNumber =:transactionNumber"
                        + " and im.id.state =:state")
                .setParameter("companyNumber", wv.getId().getCompanyNumber())
                .setParameter("transactionNumber", wv.getId().getTransactionNumber())
                .setParameter("state", WarehouseVoucherState.APR.name())
                .getResultList();
        return movements.isEmpty() ? null : movements.get(0);
    }

    private Warehouse resolveWarehouseForDetail(WarehouseVoucher wv, MovementDetail detail) {
        // En vales de transferencia entre almacenes (tipo T), el MovementDetail
        // puede referenciar el almacen origen o destino directamente; si no lo
        // trae, usamos el del header.
        if (detail.getWarehouse() != null) {
            return detail.getWarehouse();
        }
        if (MovementDetailType.E.equals(detail.getMovementType())
                && wv.getTargetWarehouse() != null) {
            return wv.getTargetWarehouse();
        }
        return wv.getWarehouse();
    }
}
