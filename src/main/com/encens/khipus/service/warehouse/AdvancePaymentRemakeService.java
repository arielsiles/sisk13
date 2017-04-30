package com.encens.khipus.service.warehouse;

import com.encens.khipus.exception.finances.CompanyConfigurationNotFoundException;
import com.encens.khipus.exception.purchase.PurchaseOrderNullifiedException;
import com.encens.khipus.exception.warehouse.AdvancePaymentAmountException;
import com.encens.khipus.exception.warehouse.AdvancePaymentStateException;
import com.encens.khipus.framework.service.GenericService;
import com.encens.khipus.model.purchases.PurchaseOrderPayment;

import javax.ejb.Local;

/**
 * @author
 * @version 2.24
 */

@Local
public interface AdvancePaymentRemakeService extends GenericService {
    Boolean isEnabledToRemake(PurchaseOrderPayment payment);

    PurchaseOrderPayment readToRemake(PurchaseOrderPayment sourcePayment);

    void remake(PurchaseOrderPayment sourcePayment,
                PurchaseOrderPayment remakePayment,
                Boolean useOldDocumentNumber)
            throws PurchaseOrderNullifiedException,
            AdvancePaymentStateException,
            AdvancePaymentAmountException,
            CompanyConfigurationNotFoundException;
}
