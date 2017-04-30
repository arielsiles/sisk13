package com.encens.khipus.service.finances;

import com.encens.khipus.framework.service.GenericService;
import com.encens.khipus.model.finances.FinanceDocument;
import com.encens.khipus.model.finances.Voucher;

import javax.ejb.Local;

/**
 * @author
 * @version 2.24
 */

@Local
public interface PaymentRemakeHelperService extends GenericService {
    FinanceDocument getFinanceDocument(String companyNumber, String transactionNumber);

    Voucher getVoucher(String transactionNumber);

    Boolean isStoredInAccountingMovementDetail(String companyNumber, String transactionNumber);

    void nullifyPaymentVoucher(String transactionNumber);

    String getOldDocumentNumber(String companyNumber, String transactionNumber);

    void updateDocumentNumberInAccountEntry(String transactionNumber, String documentNumber);
}
