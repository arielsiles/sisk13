package com.encens.khipus.service.purchases;

import com.encens.khipus.exception.ConcurrencyException;
import com.encens.khipus.exception.EntryDuplicatedException;
import com.encens.khipus.exception.EntryNotFoundException;
import com.encens.khipus.exception.purchase.*;
import com.encens.khipus.framework.service.GenericService;
import com.encens.khipus.model.purchases.*;

import javax.ejb.Local;
import java.math.BigDecimal;
import java.util.List;

/**
 * PurchaseOrderService
 *
 * @author
 * @version 2.0
 */
@Local
public interface PurchaseOrderService extends GenericService {
    PurchaseOrder findPurchaseOrder(Long id);

    void updatePurchaseOrder(PurchaseOrder entity)
            throws PurchaseOrderApprovedException,
            PurchaseOrderFinalizedException,
            ConcurrencyException, PurchaseOrderNullifiedException, PurchaseOrderLiquidatedException;

    void specialUpdatePurchaseOrder(PurchaseOrder purchaseOrder)
            throws EntryNotFoundException,
            ConcurrencyException,
            EntryDuplicatedException;

    Boolean isPurchaseOrderPending(PurchaseOrder instance);

    Boolean isPurchaseOrderApproved(PurchaseOrder instance);

    Boolean isPurchaseOrderNullified(PurchaseOrder instance);

    Boolean isPurchaseOrderFinalized(PurchaseOrder instance);

    Boolean isPurchaseOrderLiquidated(PurchaseOrder instance);

    Boolean containPurchaseOrderDetails(PurchaseOrder instance);

    boolean canChangePurchaseOrder(PurchaseOrder entity)
            throws PurchaseOrderApprovedException,
            PurchaseOrderFinalizedException, PurchaseOrderNullifiedException, PurchaseOrderLiquidatedException;

    void approvePurchaseOrder(PurchaseOrder entity)
            throws PurchaseOrderApprovedException,
            PurchaseOrderFinalizedException,
            PurchaseOrderDetailEmptyException,
            ConcurrencyException, PurchaseOrderNullifiedException, PurchaseOrderLiquidatedException;

    void nullifyPurchaseOrder(PurchaseOrder entity)
            throws PurchaseOrderFinalizedException,
            ConcurrencyException,
            PurchaseOrderNullifiedException,
            PurchaseOrderLiquidatedException;

    BigDecimal currentBalanceAmount(PurchaseOrder purchaseOrder);

    void updateCurrentPaymentStatus(PurchaseOrder entity);

    int updatePurchaseOrdersByCurrentValuesForBatchProcess();

    List<PurchaseOrderPayment> findByStatesAndPaymentType(
            List<PurchaseOrderState> purchaseOrderStateList
            , List<PurchaseOrderPaymentState> purchaseOrderPaymentStateList
            , List<PurchaseOrderPaymentType> purchaseOrderPaymentTypeList);
}
