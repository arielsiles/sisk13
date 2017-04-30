package com.encens.khipus.action.warehouse;

import com.encens.khipus.action.purchases.PurchaseDocumentAction;
import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.interceptor.BusinessUnitRestrict;
import com.encens.khipus.interceptor.BusinessUnitRestriction;
import com.encens.khipus.model.finances.CashAccount;
import com.encens.khipus.model.finances.CollectionDocumentType;
import com.encens.khipus.model.finances.FinancesEntity;
import com.encens.khipus.model.purchases.PurchaseDocument;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;
import org.jboss.seam.annotations.security.Restrict;

import java.util.List;

/**
 * @author
 * @version 2.25
 */
@Name("warehousePurchaseDocumentAction")
@Scope(ScopeType.CONVERSATION)
@BusinessUnitRestrict
public class WarehousePurchaseDocumentAction extends GenericAction<PurchaseDocument> {

    @In(create = true)
    private PurchaseDocumentAction purchaseDocumentAction;

    @In(value = "warehousePurchaseOrderAction")
    private WarehousePurchaseOrderAction warehousePurchaseOrderAction;

    @Create
    public void initialize() {
        purchaseDocumentAction.setPurchaseOrderAction(warehousePurchaseOrderAction);
    }

    public Boolean isPurchaseOrderPending() {
        return purchaseDocumentAction.isPurchaseOrderPending();
    }

    @Begin(nested = true, flushMode = FlushModeType.MANUAL)
    @Restrict("#{s:hasPermission('PURCHASEDOCUMENT','CREATE')}")
    @BusinessUnitRestriction(value = "#{warehousePurchaseOrderAction.instance}")
    public String addPurchaseDocument() {
        return purchaseDocumentAction.addPurchaseDocument();
    }

    @Override
    @Begin(nested = true, flushMode = FlushModeType.MANUAL)
    @Restrict("#{s:hasPermission('PURCHASEDOCUMENT','VIEW')}")
    @BusinessUnitRestriction(value = "#{warehousePurchaseOrderAction.instance}")
    public String select(PurchaseDocument purchaseDocument) {
        setOp(OP_UPDATE);
        return purchaseDocumentAction.select(purchaseDocument);
    }

    @Override
    @End(beforeRedirect = true)
    @Restrict("#{s:hasPermission('PURCHASEDOCUMENT','CREATE')}")
    @BusinessUnitRestriction(value = "#{warehousePurchaseOrderAction.instance}")
    public String create() {
        return purchaseDocumentAction.create();
    }

    @Override
    @End(beforeRedirect = true)
    @Restrict("#{s:hasPermission('PURCHASEDOCUMENT','UPDATE')}")
    @BusinessUnitRestriction(value = "#{warehousePurchaseOrderAction.instance}")
    public String update() {
        return purchaseDocumentAction.update();
    }

    @End(beforeRedirect = true)
    @Restrict("#{s:hasPermission('PURCHASEDOCUMENT','UPDATE')}")
    @BusinessUnitRestriction(value = "#{warehousePurchaseOrderAction.instance}")
    public String postApprovedUpdate() {
        return purchaseDocumentAction.postApprovedUpdate();
    }

    @Override
    public String delete() {
        return purchaseDocumentAction.delete();
    }

    @End(beforeRedirect = true)
    @Restrict("#{s:hasPermission('PURCHASEDOCUMENT','UPDATE')}")
    @BusinessUnitRestriction(value = "#{warehousePurchaseOrderAction.instance}")
    public String approve() {
        return purchaseDocumentAction.approve();
    }

    @End(beforeRedirect = true)
    @Restrict("#{s:hasPermission('PURCHASEDOCUMENT','UPDATE')}")
    @BusinessUnitRestriction(value = "#{warehousePurchaseOrderAction.instance}")
    public String nullify() {
        return purchaseDocumentAction.nullify();
    }

    @Override
    public boolean isManaged() {
        return purchaseDocumentAction.isManaged();
    }

    @Override
    @End(beforeRedirect = true)
    @BusinessUnitRestriction(value = "#{warehousePurchaseOrderAction.instance}")
    public String cancel() {
        return super.cancel();
    }

    public void updateExchangeRate() {
        purchaseDocumentAction.updateExchangeRate();
    }

    public void updateDocumentType() {
        purchaseDocumentAction.updateDocumentType();
    }

    public void assignFinancesEntity(FinancesEntity financesEntity) {
        purchaseDocumentAction.assignFinancesEntity(financesEntity);
    }

    public void clearFinancesEntity() {
        purchaseDocumentAction.clearFinancesEntity();
    }

    public List<CollectionDocumentType> getPurchaseDocumentTypeList() {
        return purchaseDocumentAction.getPurchaseDocumentTypeList();
    }

    public void assignCashAccountAdjustment(CashAccount cashAccount) {
        purchaseDocumentAction.assignCashAccountAdjustment(cashAccount);
    }

    public void clearCashAccountAdjustment() {
        purchaseDocumentAction.clearCashAccountAdjustment();
    }
}
