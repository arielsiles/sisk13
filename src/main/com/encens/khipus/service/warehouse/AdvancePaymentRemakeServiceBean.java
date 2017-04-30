package com.encens.khipus.service.warehouse;

import com.encens.khipus.exception.finances.CompanyConfigurationNotFoundException;
import com.encens.khipus.exception.purchase.PurchaseOrderNullifiedException;
import com.encens.khipus.exception.warehouse.AdvancePaymentAmountException;
import com.encens.khipus.exception.warehouse.AdvancePaymentStateException;
import com.encens.khipus.framework.service.GenericServiceBean;
import com.encens.khipus.model.admin.User;
import com.encens.khipus.model.finances.FinanceDocument;
import com.encens.khipus.model.finances.Voucher;
import com.encens.khipus.model.purchases.PurchaseOrder;
import com.encens.khipus.model.purchases.PurchaseOrderPayment;
import com.encens.khipus.model.purchases.PurchaseOrderPaymentState;
import com.encens.khipus.service.finances.PaymentRemakeHelperService;
import com.encens.khipus.service.purchases.PurchaseOrderService;
import com.encens.khipus.util.ValidatorUtil;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import java.util.Date;

/**
 * @author
 * @version 2.24
 */
@Stateless
@Name("advancePaymentRemakeService")
@AutoCreate
public class AdvancePaymentRemakeServiceBean extends GenericServiceBean implements AdvancePaymentRemakeService {

    @In(value = "#{listEntityManager}")
    private EntityManager eventEm;

    @In
    private PaymentRemakeHelperService paymentRemakeHelperService;

    @In
    private WarehousePurchaseOrderService warehousePurchaseOrderService;

    @In
    private AdvancePaymentService advancePaymentService;

    @In
    private WarehouseAccountEntryService warehouseAccountEntryService;

    @In
    private PurchaseOrderService purchaseOrderService;

    @In
    private User currentUser;

    public Boolean isEnabledToRemake(PurchaseOrderPayment payment) {
        if ((payment.getPurchaseOrder().isFinalized() || payment.getPurchaseOrder().isLiquidated()) && payment.isApproved()
                && !ValidatorUtil.isBlankOrNull(payment.getTransactionNumber())) {
            if (payment.isCashboxPaymentType()) {
                return !paymentRemakeHelperService.isStoredInAccountingMovementDetail(
                        payment.getCompanyNumber(),
                        payment.getTransactionNumber());
            }

            if (payment.isBankAccountPaymentType() || payment.isCheckPaymentType()) {
                FinanceDocument financeDocument = paymentRemakeHelperService.getFinanceDocument(
                        payment.getCompanyNumber(),
                        payment.getTransactionNumber());

                if (null != financeDocument) {
                    return financeDocument.isNullified();
                } else {
                    Voucher voucher = paymentRemakeHelperService.getVoucher(payment.getTransactionNumber());
                    return null == voucher || voucher.isPending();
                }
            }
        }

        return false;
    }

    public PurchaseOrderPayment readToRemake(PurchaseOrderPayment sourcePayment) {
        sourcePayment = getEntityManager().find(PurchaseOrderPayment.class, sourcePayment.getId());
        if (isEnabledToRemake(sourcePayment)) {
            PurchaseOrderPayment newPayment = new PurchaseOrderPayment();
            newPayment.setBankAccount(sourcePayment.getBankAccount());
            newPayment.setBankAccountNumber(sourcePayment.getBankAccountNumber());
            newPayment.setBeneficiaryName(sourcePayment.getBeneficiaryName());
            newPayment.setBeneficiaryType(sourcePayment.getBeneficiaryType());
            newPayment.setCashBoxAccount(sourcePayment.getCashBoxAccount());
            newPayment.setCashBoxCashAccount(sourcePayment.getCashBoxCashAccount());
            newPayment.setCompanyNumber(sourcePayment.getCompanyNumber());
            newPayment.setCreationDate(sourcePayment.getCreationDate());
            newPayment.setDescription(sourcePayment.getDescription());
            newPayment.setExchangeRate(sourcePayment.getExchangeRate());
            newPayment.setPayAmount(sourcePayment.getPayAmount());
            newPayment.setPayCurrency(sourcePayment.getPayCurrency());
            newPayment.setPaymentType(sourcePayment.getPaymentType());
            newPayment.setPurchaseOrder(sourcePayment.getPurchaseOrder());
            newPayment.setPurchaseOrderPaymentKind(sourcePayment.getPurchaseOrderPaymentKind());
            newPayment.setSourceAmount(sourcePayment.getSourceAmount());
            newPayment.setSourceCurrency(sourcePayment.getSourceCurrency());
            newPayment.setState(sourcePayment.getState());

            return newPayment;
        }

        return null;
    }

    public void remake(PurchaseOrderPayment sourcePayment,
                       PurchaseOrderPayment remakePayment,
                       Boolean useOldDocumentNumber)
            throws PurchaseOrderNullifiedException,
            AdvancePaymentStateException,
            AdvancePaymentAmountException,
            CompanyConfigurationNotFoundException {
        validatePurchaseOrderToRemake(sourcePayment.getPurchaseOrder().getId());

        if (isPurchaseOrderPaymentNullified(sourcePayment.getId())) {
            throw new AdvancePaymentStateException(PurchaseOrderPaymentState.NULLIFIED);
        }

        if (purchaseOrderService.isPurchaseOrderLiquidated(sourcePayment.getPurchaseOrder())) {
            remakePayment.setPayCurrency(sourcePayment.getPayCurrency());
            remakePayment.setPayAmount(sourcePayment.getPayAmount());

        }

        nullifySourceAdvancePayment(sourcePayment);
        paymentRemakeHelperService.nullifyPaymentVoucher(sourcePayment.getTransactionNumber());

        advancePaymentService.persistAdvancePayment(remakePayment);

        approveRemakePayment(remakePayment);

        String oldDocumentNumber = paymentRemakeHelperService
                .getOldDocumentNumber(sourcePayment.getCompanyNumber(), sourcePayment.getTransactionNumber());

        if (null != oldDocumentNumber && useOldDocumentNumber) {
            paymentRemakeHelperService.updateDocumentNumberInAccountEntry(remakePayment.getTransactionNumber(),
                    oldDocumentNumber);
        }

        getEntityManager().flush();
    }

    private void validatePurchaseOrderToRemake(Long purchaseOrderId) throws PurchaseOrderNullifiedException {
        PurchaseOrder purchaseOrder = getEntityManager().find(PurchaseOrder.class, purchaseOrderId);

        if (warehousePurchaseOrderService.isPurchaseOrderNullified(purchaseOrder)) {
            getEntityManager().refresh(purchaseOrder);
            throw new PurchaseOrderNullifiedException("The purchase order was already nullified, and cannot be changed");
        }
    }

    private void nullifySourceAdvancePayment(PurchaseOrderPayment sourcePayment) {
        sourcePayment.setState(PurchaseOrderPaymentState.NULLIFIED);
        getEntityManager().merge(sourcePayment);
        getEntityManager().flush();
    }

    private void approveRemakePayment(PurchaseOrderPayment remakePayment)
            throws CompanyConfigurationNotFoundException {
        remakePayment.setState(PurchaseOrderPaymentState.APPROVED);
        if (remakePayment.getApprovalDate() == null) {
            remakePayment.setApprovalDate(new Date());
        }
        if (remakePayment.getApprovedByEmployee() == null) {
            remakePayment.setApprovedByEmployee(currentUser);
        }
        Date accountingEntryDefaultDate = remakePayment.getAccountingEntryDefaultDate();
        String accountingEntryDefaultUserNumber = remakePayment.getAccountingEntryDefaultUserNumber();
        getEntityManager().merge(remakePayment);
        getEntityManager().flush();
        remakePayment.setAccountingEntryDefaultDate(accountingEntryDefaultDate);
        remakePayment.setAccountingEntryDefaultUserNumber(accountingEntryDefaultUserNumber);

        warehouseAccountEntryService.createAdvancePaymentAccountEntry(remakePayment);
        purchaseOrderService.updateCurrentPaymentStatus(remakePayment.getPurchaseOrder());
    }

    private boolean isPurchaseOrderPaymentNullified(Long id) {
        PurchaseOrderPayment payment = eventEm.find(PurchaseOrderPayment.class, id);
        return payment.isNullified();
    }
}
