package com.encens.khipus.service.purchases;

import com.encens.khipus.framework.service.GenericService;
import com.encens.khipus.model.purchases.PurchaseOrder;

import javax.ejb.Local;

/**
 * @author
 * @version 2.25
 */
@Local
public interface PurchaseOrderNumberGeneratorService extends GenericService {
    String generatePurchaseOrderNumber(PurchaseOrder purchaseOrder);
}
