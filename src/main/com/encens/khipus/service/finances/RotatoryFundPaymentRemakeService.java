package com.encens.khipus.service.finances;

import com.encens.khipus.exception.finances.*;
import com.encens.khipus.framework.service.GenericService;
import com.encens.khipus.model.finances.RotatoryFundPayment;

import javax.ejb.Local;

/**
 * @author
 * @version 2.24
 */
@Local
public interface RotatoryFundPaymentRemakeService extends GenericService {
    Boolean isEnabledToRemake(RotatoryFundPayment payment);

    String getOldDocumentNumber(RotatoryFundPayment payment);

    RotatoryFundPayment readToRemake(RotatoryFundPayment sourcePayment);

    void remake(RotatoryFundPayment sourcePayment, RotatoryFundPayment remakePayment, Boolean useOldDocumentNumber)
            throws RotatoryFundNullifiedException,
            RotatoryFundPaymentAnnulledException,
            RotatoryFundLiquidatedException,
            RotatoryFundPaymentNotFoundException,
            CompanyConfigurationNotFoundException,
            PaymentSumExceedsRotatoryFundAmountException;
}
