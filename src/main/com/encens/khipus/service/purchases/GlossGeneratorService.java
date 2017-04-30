package com.encens.khipus.service.purchases;

import com.encens.khipus.framework.service.GenericService;
import com.encens.khipus.model.purchases.PurchaseDocument;
import com.encens.khipus.model.purchases.PurchaseOrder;

import javax.ejb.Local;

/**
 * @author
 * @version 2.24
 */
@Local
public interface GlossGeneratorService extends GenericService {
    String generatePurchaseOrderGloss(PurchaseOrder purchaseOrder, String module, String acronym);

    String generatePurchaseDocumentGloss(PurchaseOrder purchaseOrder, PurchaseDocument purchaseDocument, boolean findPurchaseOrder);
}
