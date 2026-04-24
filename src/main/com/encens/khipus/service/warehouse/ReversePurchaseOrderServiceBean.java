package com.encens.khipus.service.warehouse;

import com.encens.khipus.exception.warehouse.InventoryProductItemNotFoundException;
import com.encens.khipus.exception.warehouse.InventoryUnitaryBalanceException;
import com.encens.khipus.exception.warehouse.ReverseNotAllowedException;
import com.encens.khipus.exception.warehouse.WarehouseVoucherNotFoundException;
import com.encens.khipus.framework.service.GenericServiceBean;
import com.encens.khipus.model.finances.Voucher;
import com.encens.khipus.model.purchases.*;
import com.encens.khipus.model.warehouse.WarehouseVoucher;
import com.encens.khipus.service.finances.FinancesUserService;
import com.encens.khipus.util.MessageUtils;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;
import java.util.Date;
import java.util.List;

/**
 * Implementacion del orquestador de anulacion de OC.
 *
 * @version 6.0.70
 */
@Stateless
@Name("reversePurchaseOrderService")
@AutoCreate
public class ReversePurchaseOrderServiceBean extends GenericServiceBean implements ReversePurchaseOrderService {

    @In
    private ReverseWarehouseVoucherService reverseWarehouseVoucherService;

    @In
    private WarehouseAccountEntryService warehouseAccountEntryService;

    @In
    private WarehouseVoucherService warehouseVoucherService;

    @In
    private FinancesUserService financesUserService;

    @Override
    public void reversePurchaseOrder(PurchaseOrder purchaseOrder,
                                     String reason,
                                     String userCode)
            throws ReverseNotAllowedException,
                   InventoryUnitaryBalanceException,
                   InventoryProductItemNotFoundException {

        if (purchaseOrder == null) {
            throw new ReverseNotAllowedException(
                    MessageUtils.getMessage("WarehousePurchaseOrder.reverse.notAllowed"));
        }

        // Recargar desde DB para tener el estado actualizado y colecciones resolubles
        purchaseOrder = getEntityManager().find(PurchaseOrder.class, purchaseOrder.getId());
        getEntityManager().refresh(purchaseOrder);

        validateReversibility(purchaseOrder);

        PurchaseOrderState originalState = purchaseOrder.getState();
        String effectiveUser = userCode != null ? userCode : financesUserService.getFinancesUserCode();

        // --- FIN o LIQ: revertir vale de recepcion (inventario + contra-asiento IA)
        if (PurchaseOrderState.FIN.equals(originalState)
                || PurchaseOrderState.LIQ.equals(originalState)) {
            WarehouseVoucher warehouseVoucher =
                    warehouseVoucherService.findWarehouseVoucherByPurchaseOrder(purchaseOrder);
            if (warehouseVoucher != null) {
                try {
                    reverseWarehouseVoucherService.reverseWarehouseVoucher(
                            warehouseVoucher.getId(), reason, effectiveUser, true);
                } catch (WarehouseVoucherNotFoundException e) {
                    // No deberia pasar: acabamos de encontrarlo
                    throw new ReverseNotAllowedException(
                            MessageUtils.getMessage("WarehouseVoucher.error.notFound"), e);
                }
            }
        }

        // --- LIQ: anular asientos CP, pagos, anticipos y facturas
        if (PurchaseOrderState.LIQ.equals(originalState)) {
            annulPaymentsAndTheirVouchers(purchaseOrder, reason);
            nullifyPurchaseDocuments(purchaseOrder);
        }

        // --- Auditoria y cambio de estado
        purchaseOrder.setState(PurchaseOrderState.ANL);
        purchaseOrder.setNullifyDate(new Date());
        purchaseOrder.setNullifyUser(effectiveUser);
        purchaseOrder.setNullifyReason(reason);
        getEntityManager().merge(purchaseOrder);
        getEntityManager().flush();
    }

    @Override
    public void validateReversibility(PurchaseOrder purchaseOrder)
            throws ReverseNotAllowedException {
        if (purchaseOrder == null) {
            throw new ReverseNotAllowedException(
                    MessageUtils.getMessage("WarehousePurchaseOrder.reverse.notAllowed"));
        }
        if (PurchaseOrderState.ANL.equals(purchaseOrder.getState())) {
            throw new ReverseNotAllowedException(
                    MessageUtils.getMessage("WarehousePurchaseOrder.reverse.alreadyNullified"));
        }
        PurchaseOrderState state = purchaseOrder.getState();
        if (!PurchaseOrderState.APR.equals(state)
                && !PurchaseOrderState.FIN.equals(state)
                && !PurchaseOrderState.LIQ.equals(state)) {
            throw new ReverseNotAllowedException(
                    MessageUtils.getMessage("WarehousePurchaseOrder.reverse.notAllowed"));
        }
    }

    @SuppressWarnings("unchecked")
    private void annulPaymentsAndTheirVouchers(PurchaseOrder purchaseOrder, String reason) {
        List<PurchaseOrderPayment> payments = getEntityManager()
                .createNamedQuery("PurchaseOrderPayment.findByPurchaseOrder")
                .setParameter("purchaseOrder", purchaseOrder)
                .getResultList();

        for (PurchaseOrderPayment payment : payments) {
            if (PurchaseOrderPaymentState.NULLIFIED.equals(payment.getState())) {
                continue;
            }
            Voucher paymentVoucher = payment.getVoucher();
            if (paymentVoucher != null) {
                warehouseAccountEntryService.annulVoucher(paymentVoucher, reason);
            }
            payment.setState(PurchaseOrderPaymentState.NULLIFIED);
            getEntityManager().merge(payment);
        }
        getEntityManager().flush();
    }

    private void nullifyPurchaseDocuments(PurchaseOrder purchaseOrder) {
        List<PurchaseDocument> documents = purchaseOrder.getPurchaseDocumentList();
        if (documents == null) {
            return;
        }
        for (PurchaseDocument document : documents) {
            if (!PurchaseDocumentState.NULLIFIED.equals(document.getState())) {
                document.setState(PurchaseDocumentState.NULLIFIED);
                getEntityManager().merge(document);
            }
        }
        getEntityManager().flush();
    }
}
