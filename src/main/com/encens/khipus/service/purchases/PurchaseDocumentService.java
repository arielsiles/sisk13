package com.encens.khipus.service.purchases;

import com.encens.khipus.exception.ConcurrencyException;
import com.encens.khipus.exception.finances.CompanyConfigurationNotFoundException;
import com.encens.khipus.exception.finances.DuplicatedFinanceAccountingDocumentException;
import com.encens.khipus.exception.purchase.PurchaseDocumentAmountException;
import com.encens.khipus.exception.purchase.PurchaseDocumentException;
import com.encens.khipus.exception.purchase.PurchaseDocumentNotFoundException;
import com.encens.khipus.exception.purchase.PurchaseDocumentStateException;
import com.encens.khipus.framework.service.GenericService;
import com.encens.khipus.model.finances.CollectionDocumentType;
import com.encens.khipus.model.purchases.PurchaseDocument;
import com.encens.khipus.model.purchases.PurchaseOrder;

import javax.ejb.Local;
import java.math.BigDecimal;
import java.util.List;

/**
 * @author
 * @version 2.25
 */
@Local
public interface PurchaseDocumentService extends GenericService {
    void createDocument(PurchaseDocument document) throws PurchaseDocumentException, PurchaseDocumentAmountException;

    void createDocumentSimple(PurchaseDocument document) throws PurchaseDocumentException, PurchaseDocumentAmountException;

    PurchaseDocument readDocument(Long id) throws PurchaseDocumentNotFoundException;

    void updateDocument(PurchaseDocument document)
            throws PurchaseDocumentStateException,
            PurchaseDocumentAmountException,
            PurchaseDocumentException,
            ConcurrencyException, PurchaseDocumentNotFoundException;

    void approveDocument(PurchaseDocument document) throws PurchaseDocumentStateException,
            PurchaseDocumentNotFoundException, DuplicatedFinanceAccountingDocumentException, CompanyConfigurationNotFoundException, PurchaseDocumentAmountException;

    void approveDocumentSimple(PurchaseDocument document) throws PurchaseDocumentStateException,
            PurchaseDocumentNotFoundException, DuplicatedFinanceAccountingDocumentException, CompanyConfigurationNotFoundException, PurchaseDocumentAmountException;

    void nullifyDocument(PurchaseDocument document) throws PurchaseDocumentNotFoundException,
            PurchaseDocumentStateException;

    List<PurchaseDocument> getPendingPurchaseDocuments(PurchaseOrder purchaseOrder);

    Long countDistinctByPurchaseOrder(PurchaseOrder purchaseOrder, CollectionDocumentType type);

    BigDecimal getPurchaseDocumentOpenAmount(PurchaseOrder purchaseOrder);

    BigDecimal sumApprovedPurchaseDocumentAmounts(PurchaseOrder purchaseOrder);

    Long countPendingPurchaseDocuments(PurchaseOrder purchaseOrder);
}
