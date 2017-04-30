package com.encens.khipus.service.finances;

import com.encens.khipus.exception.ConcurrencyException;
import com.encens.khipus.exception.EntryDuplicatedException;
import com.encens.khipus.exception.finances.CompanyConfigurationNotFoundException;
import com.encens.khipus.framework.service.GenericService;
import com.encens.khipus.model.admin.BusinessUnit;
import com.encens.khipus.model.finances.AccountingMovement;
import com.encens.khipus.model.finances.PayableDocument;
import com.encens.khipus.util.finances.PayableDocumentSourceType;

import javax.ejb.Local;
import javax.ejb.TransactionAttribute;
import java.util.List;

import static javax.ejb.TransactionAttributeType.REQUIRES_NEW;

/**
 * @author
 * @version 3.2.9
 */
@Local
public interface PayableDocumentService extends GenericService {
    @SuppressWarnings("UnnecessaryUnboxing")
    @TransactionAttribute(REQUIRES_NEW)
    PayableDocument convertToPayableDocument(PayableDocument payableDocument,
                                             PayableDocumentSourceType converterType,
                                             BusinessUnit businessUnit,
                                             AccountingMovement itemForPayableDocument,
                                             List<AccountingMovement> itemsForPayments) throws EntryDuplicatedException, ConcurrencyException, CompanyConfigurationNotFoundException;

    @TransactionAttribute(REQUIRES_NEW)
    String nextPayableDocumentNumberForVoucher(PayableDocumentSourceType sourceType);

    PayableDocument findByTransactionNumber(String transactionNumber);
}
