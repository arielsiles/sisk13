package com.encens.khipus.service.purchases;

import com.encens.khipus.exception.finances.CompanyConfigurationNotFoundException;
import com.encens.khipus.exception.purchase.PurchaseOrderNullifiedException;
import com.encens.khipus.exception.warehouse.AdvancePaymentAmountException;
import com.encens.khipus.exception.warehouse.AdvancePaymentStateException;
import com.encens.khipus.framework.service.GenericServiceBean;
import com.encens.khipus.interceptor.FinancesUser;
import com.encens.khipus.model.purchases.PurchaseOrderPayment;
import com.encens.khipus.model.purchases.PurchaseOrderPaymentState;
import com.encens.khipus.model.purchases.PurchaseOrderPaymentType;
import com.encens.khipus.model.purchases.PurchaseOrderState;
import com.encens.khipus.service.fixedassets.CompanyConfigurationService;
import com.encens.khipus.service.warehouse.AdvancePaymentRemakeService;
import com.encens.khipus.util.DateUtils;
import com.encens.khipus.util.ValidatorUtil;
import org.jboss.ejb3.annotation.TransactionTimeout;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * @author
 * @version 3.2.10
 */
@Name("purchaseOrderRemakePaymentsService")
@Stateless
@AutoCreate
@FinancesUser
public class PurchaseOrderRemakePaymentsServiceBean extends GenericServiceBean implements PurchaseOrderRemakePaymentsService {

    @In
    private AdvancePaymentRemakeService advancePaymentRemakeService;
    @In
    private PurchaseOrderService purchaseOrderService;
    @In
    private CompanyConfigurationService companyConfigurationService;

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    @TransactionTimeout(3600)
    public List<PurchaseOrderPayment> remakePayments() throws AdvancePaymentAmountException, AdvancePaymentStateException, PurchaseOrderNullifiedException, CompanyConfigurationNotFoundException {
        List<PurchaseOrderState> purchaseOrderStateList = Arrays.asList(PurchaseOrderState.FIN, PurchaseOrderState.LIQ);
        List<PurchaseOrderPaymentState> purchaseOrderPaymentStateList = Arrays.asList(PurchaseOrderPaymentState.APPROVED);
        List<PurchaseOrderPaymentType> purchaseOrderPaymentTypeList = Arrays.asList(
                PurchaseOrderPaymentType.PAYMENT_BANK_ACCOUNT,
                PurchaseOrderPaymentType.PAYMENT_WITH_CHECK,
                PurchaseOrderPaymentType.PAYMENT_CASHBOX);
        List<PurchaseOrderPayment> processedPaymentList = new ArrayList<PurchaseOrderPayment>(0);
        List<PurchaseOrderPayment> purchaseOrderPaymentList = purchaseOrderService.findByStatesAndPaymentType(
                purchaseOrderStateList,
                purchaseOrderPaymentStateList,
                purchaseOrderPaymentTypeList);
        if (!ValidatorUtil.isEmptyOrNull(purchaseOrderPaymentList)) {
            for (PurchaseOrderPayment purchaseOrderPayment : purchaseOrderPaymentList) {
                if (advancePaymentRemakeService.isEnabledToRemake(purchaseOrderPayment)) {
                    processedPaymentList.add(purchaseOrderPayment);
                    PurchaseOrderPayment remakePayment = advancePaymentRemakeService.readToRemake(purchaseOrderPayment);
                    remakePayment.setCreationDate(purchaseOrderPayment.getCreationDate());
                    remakePayment.setRegisterEmployee(purchaseOrderPayment.getRegisterEmployee());
                    remakePayment.setApprovalDate(purchaseOrderPayment.getApprovalDate());
                    remakePayment.setApprovedByEmployee(purchaseOrderPayment.getApprovedByEmployee());
                    // set default values
                    Date lastDayForConsumeMonthAndYear = DateUtils.lastDayOfMonth(
                            purchaseOrderPayment.getPurchaseOrder().getConsumeMonth().getValue(),
                            companyConfigurationService.findDefaultPurchaseOrderRemakeYear()
                    );
                    remakePayment.setAccountingEntryDefaultDate(lastDayForConsumeMonthAndYear);
                    remakePayment.setAccountingEntryDefaultUserNumber(companyConfigurationService.findDefaultPurchaseOrderRemakePaymentUserNumber());
                    advancePaymentRemakeService.remake(purchaseOrderPayment, remakePayment, false);
                }
            }
        }
        return processedPaymentList;
    }
}
