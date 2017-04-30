package com.encens.khipus.service.purchases;

import com.encens.khipus.exception.finances.CompanyConfigurationNotFoundException;
import com.encens.khipus.exception.purchase.PurchaseOrderNullifiedException;
import com.encens.khipus.exception.warehouse.AdvancePaymentAmountException;
import com.encens.khipus.exception.warehouse.AdvancePaymentStateException;
import com.encens.khipus.framework.service.GenericService;
import com.encens.khipus.model.purchases.PurchaseOrderPayment;

import javax.ejb.Local;
import java.util.List;

/**
 * @author
 * @version 3.2.10
 */
@Local
public interface PurchaseOrderRemakePaymentsService extends GenericService {
    List<PurchaseOrderPayment> remakePayments() throws AdvancePaymentAmountException, AdvancePaymentStateException, PurchaseOrderNullifiedException, CompanyConfigurationNotFoundException;
}
