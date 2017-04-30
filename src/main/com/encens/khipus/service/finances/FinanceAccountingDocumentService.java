package com.encens.khipus.service.finances;

import com.encens.khipus.exception.finances.CompanyConfigurationNotFoundException;
import com.encens.khipus.exception.finances.DuplicatedFinanceAccountingDocumentException;
import com.encens.khipus.framework.service.GenericService;
import com.encens.khipus.model.employees.DischargeDocument;
import com.encens.khipus.model.finances.AccountingDocument;
import com.encens.khipus.model.finances.CollectionDocument;
import com.encens.khipus.model.finances.FinanceAccountingDocumentPk;
import com.encens.khipus.model.purchases.PurchaseDocument;
import com.encens.khipus.model.purchases.PurchaseOrder;

import javax.ejb.Local;
import java.util.List;

/**
 * @author
 * @version 2.26
 */
@Local
public interface FinanceAccountingDocumentService extends GenericService {

    void validatePK(FinanceAccountingDocumentPk id) throws DuplicatedFinanceAccountingDocumentException;

    void validatePK(PurchaseDocument document) throws DuplicatedFinanceAccountingDocumentException;

    void validatePK(DischargeDocument document) throws DuplicatedFinanceAccountingDocumentException;

    void createFinanceAccountingDocument(PurchaseDocument document);

    FinanceAccountingDocumentPk buildFinanceAccountingDocumentPk(PurchaseDocument document);

    void validatePK(CollectionDocument collectionDocument) throws DuplicatedFinanceAccountingDocumentException;

    void createFinanceAccountingDocument(AccountingDocument document);

    void createFinanceAccountingDocument(DischargeDocument document);

    void createAccountingVoucher(PurchaseDocument document)
            throws CompanyConfigurationNotFoundException;

    void createAccountingVoucherByPurchaseOrder(PurchaseOrder purchaseOrder) throws CompanyConfigurationNotFoundException;

    public List<PurchaseDocument> findByOrderVoucher(PurchaseOrder purchaseOrder) throws CompanyConfigurationNotFoundException;
}
