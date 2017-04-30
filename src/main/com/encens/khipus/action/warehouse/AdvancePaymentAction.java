package com.encens.khipus.action.warehouse;

import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.framework.action.Outcome;
import com.encens.khipus.interceptor.BusinessUnitRestrict;
import com.encens.khipus.interceptor.BusinessUnitRestriction;
import com.encens.khipus.model.finances.CashAccount;
import com.encens.khipus.model.finances.RotatoryFund;
import com.encens.khipus.model.purchases.PurchaseOrder;
import com.encens.khipus.model.purchases.PurchaseOrderPayment;
import com.encens.khipus.service.warehouse.WarehousePurchaseOrderService;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;
import org.jboss.seam.annotations.security.Restrict;

/**
 * @author
 * @version 2.24
 */
@Name("warehouseAdvancePaymentAction")
@Scope(ScopeType.CONVERSATION)
@BusinessUnitRestrict
public class AdvancePaymentAction extends GenericAction<PurchaseOrderPayment> {

    @In(create = true, value = "advancePaymentAction")
    private com.encens.khipus.action.purchases.AdvancePaymentAction advancePaymentAction;

    @In(value = "warehousePurchaseOrderAction")
    private WarehousePurchaseOrderAction warehousePurchaseOrderAction;

    @In(value = "warehousePurchaseOrderService")
    private WarehousePurchaseOrderService warehousePurchaseOrderService;

    @Create
    public void initialize() {
        advancePaymentAction.setPurchaseOrderService(warehousePurchaseOrderService);
        advancePaymentAction.setPurchaseOrderAction(warehousePurchaseOrderAction);
    }

    @Override
    public PurchaseOrderPayment getInstance() {
        return advancePaymentAction.getInstance();
    }

    @Override
    public String getOp() {
        return advancePaymentAction.getOp();
    }

    @Override
    public boolean isManaged() {
        return advancePaymentAction.isManaged();
    }

    public void paymentTypeChanged() {
        advancePaymentAction.paymentTypeChanged();
    }

    @BusinessUnitRestriction(value = "#{warehousePurchaseOrderAction.instance}")
    @Begin(nested = true, ifOutcome = Outcome.SUCCESS)
    @Restrict("#{s:hasPermission('ADVANCEPAYMENT','CREATE')}")
    public String addAdvancePayment() {
        warehousePurchaseOrderAction.enablePurchaseOrderPaymentTab();
        String outcome = advancePaymentAction.addAdvancePayment();

        if (Outcome.SUCCESS.equals(outcome)) {
            setDefaultDescription(getInstance().getPurchaseOrder());
        }

        return outcome;
    }


    @Override
    @End(beforeRedirect = true)
    @BusinessUnitRestriction(value = "#{warehousePurchaseOrderAction.instance}")
    public String cancel() {
        return advancePaymentAction.cancel();
    }

    @Override
    @Begin(nested = true, flushMode = FlushModeType.MANUAL)
    @BusinessUnitRestriction(value = "#{warehousePurchaseOrderAction.instance}")
    @Restrict("#{s:hasPermission('ADVANCEPAYMENT','VIEW')}")
    public String select(PurchaseOrderPayment instance) {
        warehousePurchaseOrderAction.enablePurchaseOrderPaymentTab();
        return advancePaymentAction.select(instance);
    }

    @Begin(nested = true, flushMode = FlushModeType.MANUAL)
    @BusinessUnitRestriction(value = "#{warehousePurchaseOrderAction.instance}")
    @Restrict("#{s:hasPermission('REMAKEPURCHASEORDERPAYMENT','VIEW')}")
    public String selectToRemake(PurchaseOrderPayment instance) {
        return advancePaymentAction.selectToRemake(instance);
    }

    @End(beforeRedirect = true)
    @BusinessUnitRestriction(value = "#{warehousePurchaseOrderAction.instance}")
    @Restrict("#{s:hasPermission('REMAKEPURCHASEORDERPAYMENT','VIEW')}")
    public String remake() {
        return advancePaymentAction.remake();
    }

    @Override
    @BusinessUnitRestriction(value = "#{warehousePurchaseOrderAction.instance}")
    @Restrict("#{s:hasPermission('ADVANCEPAYMENT','CREATE')}")
    public String create() {
        return advancePaymentAction.create();
    }

    @Override
    @End(beforeRedirect = true)
    @BusinessUnitRestriction(value = "#{warehousePurchaseOrderAction.instance}")
    @Restrict("#{s:hasPermission('ADVANCEPAYMENT','UPDATE')}")
    public String update() {
        return advancePaymentAction.update();
    }

    @End(beforeRedirect = true)
    @BusinessUnitRestriction(value = "#{warehousePurchaseOrderAction.instance}")
    @Restrict("#{s:hasPermission('APPROVEADVANCEPAYMENT','VIEW')}")
    public String approve() {
        return advancePaymentAction.approve();
    }

    @End(beforeRedirect = true)
    @BusinessUnitRestriction(value = "#{warehousePurchaseOrderAction.instance}")
    @Restrict("#{s:hasPermission('NULLIFYADVANCEPAYMENT','VIEW')}")
    public String nullify() {
        return advancePaymentAction.nullify();
    }

    public boolean isEnableBeneficiaryFields() {
        return advancePaymentAction.isEnableBeneficiaryFields();
    }

    public void assignCashBoxCashAccount(CashAccount cashAccount) {
        advancePaymentAction.assignCashBoxCashAccount(cashAccount);
    }

    public void clearCashBoxCashAccount() {
        advancePaymentAction.clearCashBoxCashAccount();
    }

    public void assignRotatoryFund(RotatoryFund rotatoryFund) {
        advancePaymentAction.assignRotatoryFund(rotatoryFund);
    }

    public void clearRotatoryFund() {
        advancePaymentAction.clearRotatoryFund();
    }

    public boolean isEnableExchangeRateField() {
        return advancePaymentAction.isEnableExchangeRateField();
    }

    public boolean isEnableBankAccount() {
        return advancePaymentAction.isEnableBankAccount();
    }

    public boolean isCheckPayment() {
        return advancePaymentAction.isCheckPayment();
    }

    public boolean isBankPayment() {
        return advancePaymentAction.isBankPayment();
    }


    public boolean isCashBoxPayment() {
        return advancePaymentAction.isCashBoxPayment();
    }

    public boolean isRotatoryFundPayment() {
        return advancePaymentAction.isRotatoryFundPayment();
    }

    public boolean isApproved() {
        return advancePaymentAction.isApproved();
    }

    public boolean isNullified() {
        return advancePaymentAction.isNullified();
    }

    public boolean isPending() {
        return advancePaymentAction.isPending();
    }

    public void accountChanged() {
        advancePaymentAction.accountChanged();
    }

    public void setDefaultDescription(PurchaseOrder purchaseOrder) {
        advancePaymentAction.setDefaultDescription(purchaseOrder);
    }

    public Boolean checkIsEnabledToRemake(PurchaseOrderPayment payment) {
        return advancePaymentAction.checkIsEnabledToRemake(payment);
    }

    public boolean isRemake() {
        return advancePaymentAction.isRemake();
    }

    public PurchaseOrderPayment getInstanceToRemake() {
        return advancePaymentAction.getInstanceToRemake();
    }

    public void setInstanceToRemake(PurchaseOrderPayment instanceToRemake) {
        advancePaymentAction.setInstanceToRemake(instanceToRemake);
    }

    public String getOldDocumentNumber() {
        return advancePaymentAction.getOldDocumentNumber();
    }

    public void setOldDocumentNumber(String oldDocumentNumber) {
        advancePaymentAction.setOldDocumentNumber(oldDocumentNumber);
    }

    public boolean isUseOldDocumentNumber() {
        return advancePaymentAction.isUseOldDocumentNumber();
    }

    public void setUseOldDocumentNumber(boolean useOldDocumentNumber) {
        advancePaymentAction.setUseOldDocumentNumber(useOldDocumentNumber);
    }

    public boolean isPurchaseOrderLiquidated() {
        return advancePaymentAction.isPurchaseOrderLiquidated();
    }
}
