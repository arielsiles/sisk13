package com.encens.khipus.service.finances;

import com.encens.khipus.exception.ConcurrencyException;
import com.encens.khipus.exception.finances.*;
import com.encens.khipus.framework.service.GenericServiceBean;
import com.encens.khipus.model.admin.User;
import com.encens.khipus.model.finances.*;
import com.encens.khipus.util.BigDecimalUtil;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @author
 * @version 3.5.2.2
 */
@Stateless
@Name("rotatoryFundPaymentRemakeService")
@AutoCreate
public class RotatoryFundPaymentRemakeServiceBean extends GenericServiceBean implements RotatoryFundPaymentRemakeService {

    @In(value = "#{listEntityManager}")
    private EntityManager eventEm;

    @In
    private RotatoryFundService rotatoryFundService;

    @In
    private User currentUser;

    @In
    private RotatoryFundPaymentService rotatoryFundPaymentService;

    @In
    private PaymentRemakeHelperService paymentRemakeHelperService;

    public Boolean isEnabledToRemake(RotatoryFundPayment payment) {
        if (payment.getRotatoryFund().isLiquidated() || payment.getRotatoryFund().isNullified()) {
            return false;
        }

        if (payment.isApproved()) {
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

    public String getOldDocumentNumber(RotatoryFundPayment payment) {
        return paymentRemakeHelperService
                .getOldDocumentNumber(payment.getCompanyNumber(), payment.getTransactionNumber());
    }

    public RotatoryFundPayment readToRemake(RotatoryFundPayment sourcePayment) {
        sourcePayment = getEntityManager().find(RotatoryFundPayment.class, sourcePayment.getId());

        if (isEnabledToRemake(sourcePayment)) {
            RotatoryFundPayment newPayment = new RotatoryFundPayment();
            newPayment.setCompany(sourcePayment.getCompany());
            newPayment.setCompanyNumber(sourcePayment.getCompanyNumber());
            newPayment.setAnnulledByEmployee(sourcePayment.getAnnulledByEmployee());
            newPayment.setApprovedByEmployee(sourcePayment.getApprovedByEmployee());
            newPayment.setBankAccount(sourcePayment.getBankAccount());
            newPayment.setBeneficiaryName(sourcePayment.getBeneficiaryName());
            newPayment.setBeneficiaryType(sourcePayment.getBeneficiaryType());
            newPayment.setCheckDestination(sourcePayment.getCheckDestination());
            newPayment.setCashBoxCashAccount(sourcePayment.getCashBoxCashAccount());
            newPayment.setCreationDate(sourcePayment.getCreationDate());
            newPayment.setRegisterEmployee(currentUser);
            newPayment.setDescription(sourcePayment.getDescription());
            newPayment.setExchangeRate(sourcePayment.getExchangeRate());
            newPayment.setPaymentAmount(sourcePayment.getPaymentAmount());
            newPayment.setPaymentCurrency(sourcePayment.getPaymentCurrency());
            newPayment.setPaymentDate(sourcePayment.getPaymentDate());
            newPayment.setState(RotatoryFundPaymentState.PEN);

            newPayment.setRotatoryFundPaymentType(sourcePayment.getRotatoryFundPaymentType());
            newPayment.setRotatoryFund(sourcePayment.getRotatoryFund());

            return newPayment;
        }

        return null;
    }

    public void remake(RotatoryFundPayment sourcePayment, RotatoryFundPayment remakePayment, Boolean useOldDocumentNumber)
            throws RotatoryFundNullifiedException,
            RotatoryFundPaymentAnnulledException,
            RotatoryFundLiquidatedException,
            RotatoryFundPaymentNotFoundException,
            CompanyConfigurationNotFoundException,
            PaymentSumExceedsRotatoryFundAmountException {

        RotatoryFund rotatoryFund = sourcePayment.getRotatoryFund();
        if (rotatoryFundService.canCollectRotatoryFund(rotatoryFund)) {
            BigDecimal availableAmount = getAvailablePaymentAmountToRemake(sourcePayment);
            if (remakePayment.getPaymentAmount().compareTo(availableAmount) > 0) {
                throw new PaymentSumExceedsRotatoryFundAmountException("The remake payment amount cannot be exceed : "
                        + availableAmount);
            }

            sourcePayment.setReversionCause(remakePayment.getReversionCause());
            rotatoryFundPaymentService.annulRotatoryFundPaymentToRemake(sourcePayment);
            paymentRemakeHelperService.nullifyPaymentVoucher(sourcePayment.getTransactionNumber());

            rotatoryFundPaymentService.persistRotatoryFundPayment(remakePayment);

            //call to flush method because the approveRotatoryFundPayment logic verifies the nullified and the pendant payments.
            getEntityManager().flush();

            try {
                approveRotatoryFundPayment(remakePayment);

                String oldDocumentNumber = paymentRemakeHelperService
                        .getOldDocumentNumber(sourcePayment.getCompanyNumber(), sourcePayment.getTransactionNumber());

                if (null != oldDocumentNumber && useOldDocumentNumber) {
                    updateDocumentNumber(remakePayment, oldDocumentNumber);
                }
            } catch (ConcurrencyException e) {
                //this exception never comes up, because the created object is updated immediately
            } catch (RotatoryFundPaymentApprovedException e) {
                //this exception never comes up, because the created object are in pending state.
            }

            getEntityManager().flush();
        }
    }

    private void updateDocumentNumber(RotatoryFundPayment remakePayment, String documentNumber) {
        Voucher voucher = paymentRemakeHelperService.getVoucher(remakePayment.getTransactionNumber());
        voucher.setDocumentNumber(documentNumber);
    }

    private void approveRotatoryFundPayment(RotatoryFundPayment rotatoryFundPayment)
            throws RotatoryFundNullifiedException,
            ConcurrencyException,
            RotatoryFundLiquidatedException,
            PaymentSumExceedsRotatoryFundAmountException,
            RotatoryFundPaymentNotFoundException,
            RotatoryFundPaymentAnnulledException,
            RotatoryFundPaymentApprovedException,
            CompanyConfigurationNotFoundException {
        rotatoryFundPayment.setState(RotatoryFundPaymentState.APR);
        rotatoryFundPayment.setApprovedByEmployee(currentUser);
        rotatoryFundPayment.setApprovalDate(new Date());
        rotatoryFundPaymentService.updateRotatoryFund(rotatoryFundPayment, null);
        rotatoryFundService.computeRotatoryFundStatistics(rotatoryFundPayment.getRotatoryFund());
    }


    private BigDecimal getAvailablePaymentAmountToRemake(RotatoryFundPayment sourcePayment) {
        RotatoryFund rotatoryFund = sourcePayment.getRotatoryFund();
        BigDecimal totalAmount = sumAllPayments(rotatoryFund);

        BigDecimal partialAmount = BigDecimalUtil.subtract(totalAmount, sourcePayment.getPaymentAmount());

        return BigDecimalUtil.subtract(rotatoryFund.getAmount(), partialAmount);
    }

    private BigDecimal sumAllPayments(RotatoryFund rotatoryFund) {
        return BigDecimalUtil.sum(getApprovedPaymentSum(rotatoryFund), getPendantPaymentSum(rotatoryFund));
    }


    private BigDecimal getApprovedPaymentSum(RotatoryFund rotatoryFund) {
        BigDecimal queryResult = (BigDecimal) eventEm.createNamedQuery("RotatoryFundPayment.findSumByRotatoryFund")
                .setParameter("rotatoryFund", rotatoryFund)
                .setParameter("state", RotatoryFundPaymentState.APR)
                .getSingleResult();
        if (null == queryResult) {
            return BigDecimal.ZERO;
        }

        return queryResult;

    }

    private BigDecimal getPendantPaymentSum(RotatoryFund rotatoryFund) {
        BigDecimal queryResult = (BigDecimal) eventEm.createNamedQuery("RotatoryFundPayment.findSumByRotatoryFund")
                .setParameter("rotatoryFund", rotatoryFund)
                .setParameter("state", RotatoryFundPaymentState.PEN)
                .getSingleResult();
        if (null == queryResult) {
            return BigDecimal.ZERO;
        }

        return queryResult;
    }
}
