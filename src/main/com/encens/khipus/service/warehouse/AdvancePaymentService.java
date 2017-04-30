package com.encens.khipus.service.warehouse;

import com.encens.khipus.exception.ConcurrencyException;
import com.encens.khipus.exception.finances.CompanyConfigurationNotFoundException;
import com.encens.khipus.exception.finances.RotatoryFundReceivableResidueException;
import com.encens.khipus.exception.purchase.*;
import com.encens.khipus.exception.warehouse.AdvancePaymentAmountException;
import com.encens.khipus.exception.warehouse.AdvancePaymentStateException;
import com.encens.khipus.framework.service.GenericService;
import com.encens.khipus.model.finances.PurchaseOrderPaymentKind;
import com.encens.khipus.model.purchases.PurchaseOrder;
import com.encens.khipus.model.purchases.PurchaseOrderPayment;
import com.encens.khipus.model.purchases.PurchaseOrderPaymentState;

import javax.ejb.Local;
import java.math.BigDecimal;

/**
 * @author
 * @version 2.24
 */
@Local
public interface AdvancePaymentService extends GenericService {
    void createAdvancePayment(PurchaseOrderPayment entity)
            throws PurchaseOrderNullifiedException,
            AdvancePaymentAmountException,
            PurchaseOrderLiquidatedException;

    void persistAdvancePayment(PurchaseOrderPayment purchaseOrderPayment)
            throws AdvancePaymentAmountException;

    void updateAdvancePayment(PurchaseOrderPayment purchaseOrderPayment)
            throws PurchaseOrderNullifiedException,
            PurchaseOrderLiquidatedException,
            AdvancePaymentAmountException,
            ConcurrencyException, AdvancePaymentStateException, RotatoryFundReceivableResidueException;

    void approveAdvancePayment(PurchaseOrderPayment entity)
            throws AdvancePaymentStateException,
            PurchaseOrderNullifiedException,
            CompanyConfigurationNotFoundException,
            PurchaseOrderLiquidatedException,
            RotatoryFundNullifiedException,
            RotatoryFundLiquidatedException,
            CollectionSumExceedsRotatoryFundAmountException,
            RotatoryFundConcurrencyException, RotatoryFundReceivableResidueException;

    void nullifyAdvancePayment(PurchaseOrderPayment entity)
            throws AdvancePaymentStateException,
            PurchaseOrderLiquidatedException,
            PurchaseOrderNullifiedException;

    BigDecimal sumAllPaymentAmounts(PurchaseOrder purchaseOrder);

    @SuppressWarnings(value = "unchecked")
    BigDecimal sumAllAdvancePaymentAmountsByApprovedState(PurchaseOrder purchaseOrder);

    @SuppressWarnings(value = "unchecked")
    BigDecimal sumAllAdvancePaymentAmountsByState(PurchaseOrder purchaseOrder, PurchaseOrderPaymentState purchaseOrderPaymentState);

    @SuppressWarnings(value = "unchecked")
    BigDecimal sumAllAdvancePaymentAmountsButCurrent(PurchaseOrderPayment purchaseOrderPayment);

    @SuppressWarnings(value = "unchecked")
    BigDecimal sumAllPaymentAmountsByKind(PurchaseOrder purchaseOrder, PurchaseOrderPaymentKind purchaseOrderPaymentKind);

    @SuppressWarnings(value = "unchecked")
    BigDecimal sumAllPaymentAmountsByKindPurchaseOrder(PurchaseOrder purchaseOrder, PurchaseOrderPaymentKind purchaseOrderPaymentKind);
}
