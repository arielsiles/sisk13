package com.encens.khipus.service.purchases;

import com.encens.khipus.framework.service.GenericServiceBean;
import com.encens.khipus.model.purchases.PurchaseOrder;
import com.encens.khipus.service.common.SequenceGeneratorService;
import com.encens.khipus.util.Constants;
import com.encens.khipus.util.FormatUtils;
import com.encens.khipus.util.MessageUtils;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;

/**
 * @author
 * @version 2.25
 */
@Stateless
@Name("purchaseOrderNumberGeneratorService")
@AutoCreate
public class PurchaseOrderNumberGeneratorServiceBean extends GenericServiceBean implements PurchaseOrderNumberGeneratorService {

    @In
    private SequenceGeneratorService sequenceGeneratorService;

    public synchronized String generatePurchaseOrderNumber(PurchaseOrder purchaseOrder) {
        String sequenceName = FormatUtils.concatBySeparator("_",
                Constants.PURCHASEORDER_BY_BUSINESSUNIT,
                purchaseOrder.getExecutorUnit().getId(),
                purchaseOrder.getOrderType().toString());

        String purchaseOrderNumber = String.valueOf(sequenceGeneratorService.nextValue(sequenceName));

        purchaseOrderNumber = FormatUtils.concatBySeparator("-",
                purchaseOrder.getExecutorUnit().getAcronym(),
                MessageUtils.getMessage(purchaseOrder.getOrderType().getAcronym()),
                purchaseOrderNumber);

        log.debug("Generated purchase order number: " + purchaseOrderNumber);
        return purchaseOrderNumber;
    }
}
