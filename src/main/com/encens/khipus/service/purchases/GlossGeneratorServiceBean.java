package com.encens.khipus.service.purchases;

import com.encens.khipus.framework.service.GenericServiceBean;
import com.encens.khipus.model.employees.Month;
import com.encens.khipus.model.purchases.PurchaseDocument;
import com.encens.khipus.model.purchases.PurchaseOrder;
import com.encens.khipus.model.purchases.PurchaseOrderType;
import com.encens.khipus.util.MessageUtils;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;

/**
 * @author
 * @version 2.24
 */
@Stateless
@Name("glossGeneratorService")
@AutoCreate
public class GlossGeneratorServiceBean extends GenericServiceBean implements GlossGeneratorService {
    public String generatePurchaseOrderGloss(PurchaseOrder purchaseOrder, String module, String acronym) {
        return generatePurchaseOrderGloss(purchaseOrder, module, acronym, true);
    }

    public String generatePurchaseOrderGloss(PurchaseOrder purchaseOrder, String module, String acronym, boolean findPurchaseOrder) {
        if (findPurchaseOrder) {
            purchaseOrder = getEntityManager().find(PurchaseOrder.class, purchaseOrder.getId());
        }
        String obs = purchaseOrder.getGloss();
        String executorUnitName = purchaseOrder.getExecutorUnit().getOrganization().getName();
        String costCenterName = purchaseOrder.getCostCenter().getDescription();
        String petitionerName = MessageUtils.getMessage("PurchaseOrder.petitionerMessage", purchaseOrder.getPetitionerJobContract().getContract().getEmployee().getFullName());
        Month month = purchaseOrder.getConsumeMonth() != null ? purchaseOrder.getConsumeMonth() : Month.getMonth(purchaseOrder.getDate());
        String monthName = MessageUtils.getMessage(month.getResourceKey());
        String purchaseOrderNumber = purchaseOrder.getOrderNumber();
        String providerName = MessageUtils.getMessage("PurchaseOrder.providerMessage", purchaseOrder.getProvider().getEntity().getAcronym());

        String gloss = (obs + ", "
                + executorUnitName + ", "
                + costCenterName + ", "
                + petitionerName + ", "
                + providerName + ", "
                + monthName + ", "
                + acronym + " " + purchaseOrderNumber + ", "
                + module).toUpperCase();

        log.debug("Generated gloss: " + gloss);
        return gloss;
    }


    public String generatePurchaseDocumentGloss(PurchaseOrder purchaseOrder, PurchaseDocument purchaseDocument, boolean findPurchaseOrder) {
        String module, acronym;

        if (PurchaseOrderType.WAREHOUSE.equals(purchaseOrder.getOrderType())) {
            module = MessageUtils.getMessage("WarehousePurchaseOrder.warehouses");
            acronym = MessageUtils.getMessage("WarehousePurchaseOrder.orderNumberAcronym");
        } else {
            module = MessageUtils.getMessage("FixedAssetPurchaseOrder.fixedAssets");
            acronym = MessageUtils.getMessage("FixedAssetPurchaseOrder.orderNumberAcronym");
        }

        String invoiceNumberMessage = MessageUtils.getMessage(purchaseDocument.isAdjustmentDocument() ? "PurchaseDocument.adjustmentNumberMessage" : "PurchaseDocument.invoiceNumberMessage", purchaseDocument.getNumber());
        String glossMessage = MessageUtils.getMessage("PurchaseDocument.glossMessage", generatePurchaseOrderGloss(purchaseOrder, module, acronym, findPurchaseOrder));

        return (invoiceNumberMessage + ", " + glossMessage).toUpperCase();
    }
}
