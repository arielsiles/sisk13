package com.encens.khipus.service.finances;

import com.encens.khipus.exception.ConcurrencyException;
import com.encens.khipus.exception.EntryDuplicatedException;
import com.encens.khipus.exception.finances.CompanyConfigurationNotFoundException;
import com.encens.khipus.framework.service.GenericServiceBean;
import com.encens.khipus.interceptor.FinancesUser;
import com.encens.khipus.model.admin.BusinessUnit;
import com.encens.khipus.model.finances.*;
import com.encens.khipus.model.purchases.PurchaseOrderPayment;
import com.encens.khipus.model.purchases.PurchaseOrderPaymentType;
import com.encens.khipus.service.common.SequenceGeneratorService;
import com.encens.khipus.service.fixedassets.CompanyConfigurationService;
import com.encens.khipus.util.BigDecimalUtil;
import com.encens.khipus.util.Constants;
import com.encens.khipus.util.MessageUtils;
import com.encens.khipus.util.ValidatorUtil;
import com.encens.khipus.util.finances.PayableDocumentSourceType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.persistence.NoResultException;
import java.math.BigDecimal;
import java.util.List;

import static javax.ejb.TransactionAttributeType.REQUIRES_NEW;

/**
 * @author
 * @version 3.2.9
 */
@Name("payableDocumentService")
@Stateless
@FinancesUser
@AutoCreate
public class PayableDocumentServiceBean extends GenericServiceBean implements PayableDocumentService {

    @In
    private SequenceGeneratorService sequenceGeneratorService;
    @In
    private FinancesPkGeneratorService financesPkGeneratorService;
    @In
    private CompanyConfigurationService companyConfigurationService;

    private PayableDocument findByAccountingMovement(AccountingMovement accountingMovement) {
        List<PayableDocument> payableDocumentList = getEntityManager()
                .createNamedQuery("PayableDocument.findByAccountingMovement")
                .setParameter("accountingMovement", accountingMovement)
                .getResultList();

        PayableDocument result = null;

        if (!ValidatorUtil.isEmptyOrNull(payableDocumentList)) {
            result = payableDocumentList.get(0);
        }

        return result;
    }

    private String getAccountingMovementTransactionNumber(AccountingMovement accountingMovement) {
        return (String) getEntityManager().createNamedQuery("AccountingMovementDetail.findTransactionNumber")
                .setParameter("accountingMovement", accountingMovement).getSingleResult();
    }

    private List<FinanceDocument> getFinancesDocumentByAccountingMovement(AccountingMovement accountingMovement) {
        return getEntityManager()
                .createNamedQuery("FinanceDocument.findByAccountingMovement")
                .setParameter("accountingMovement", accountingMovement)
                .getResultList();
    }

    private List<Object[]> getPurchaseOrderCashBoxPaymentByAccountingMovement(AccountingMovement accountingMovement) {
        return getEntityManager()
                .createNamedQuery("AccountingMovementDetail.findByPurchaseOrderPaymentType")
                .setParameter("accountingMovement", accountingMovement)
                .setParameter("paymentType", PurchaseOrderPaymentType.PAYMENT_CASHBOX)
                .getResultList();
    }

    private BigDecimal sumFromPayableRelatedDocument(String transactionNumber) {
        BigDecimal result = (BigDecimal) getEntityManager().createNamedQuery("PayableRelatedDocument.sumByTransactionNumber")
                .setParameter("transactionNumber", transactionNumber)
                .getSingleResult();

        return result == null ? BigDecimal.ZERO : result;
    }

    private FinanceDocumentMovement findMovementByFinancesDocument(FinanceDocument financeDocument, FinanceDocumentState state) {
        FinanceDocumentMovement financeDocumentMovement = null;
        List<FinanceDocumentMovement> financeDocumentMovementList = getEntityManager().createNamedQuery("FinanceDocumentMovement.findMovementByFinancesDocumentState")
                .setParameter("companyNumber", financeDocument.getCompanyNumber())
                .setParameter("transactionNumber", financeDocument.getTransactionNumber())
                .setParameter("state", state).getResultList();

        if (!ValidatorUtil.isEmptyOrNull(financeDocumentMovementList)) {
            financeDocumentMovement = financeDocumentMovementList.get(0);
        }

        return financeDocumentMovement;
    }

    private BigDecimal getMovementDetailAmount(AccountingMovement itemForPayableDocument, String transactionNumber) {
        return (BigDecimal) getEntityManager()
                .createNamedQuery("AccountingMovementDetail.sumByMovementDetail")
                .setParameter("accountingMovement", itemForPayableDocument)
                .setParameter("type", FinanceMovementType.D)
                .setParameter("transactionNumber", transactionNumber)
                .getSingleResult();
    }

    @TransactionAttribute(REQUIRES_NEW)
    public String nextPayableDocumentNumberForVoucher(PayableDocumentSourceType sourceType) {
        return Constants.PAYABLES_VOUCHER_FORM
                + MessageUtils.getMessage(sourceType.getAcronymResourceKey())
                + Constants.HYPHEN_SEPARATOR
                + sequenceGeneratorService.forceNextValue(Constants.PAYABLEDOCUMENT_VOUCHER_DOCUMENTNUMBER_CODE_SEQUENCE + Constants.UNDERSCORE_SEPARATOR + sourceType);
    }

    private String nextPayableDocumentType(PayableDocumentSourceType sourceType) {
        return "REG" + MessageUtils.getMessage(sourceType.getAcronymResourceKey())
                + Constants.HYPHEN_SEPARATOR
                + sequenceGeneratorService.nextValue(Constants.PAYABLEDOCUMENT_DOCUMENTNUMBER_CODE_SEQUENCE + Constants.UNDERSCORE_SEPARATOR + sourceType);
    }

    private String nextPayableDocumentType(PayableDocumentType payableDocumentType) {
        return "REG" + payableDocumentType.getDocumentType()
                + Constants.HYPHEN_SEPARATOR
                + sequenceGeneratorService.nextValue(Constants.PAYABLEDOCUMENT_DOCUMENTNUMBER_CODE_SEQUENCE + Constants.UNDERSCORE_SEPARATOR + payableDocumentType.getDocumentType()
        );
    }

    @SuppressWarnings("UnnecessaryUnboxing")
    @TransactionAttribute(REQUIRES_NEW)
    public PayableDocument convertToPayableDocument(PayableDocument mainDocument,
                                                    PayableDocumentSourceType sourceType,
                                                    BusinessUnit businessUnit,
                                                    AccountingMovement itemForPayableDocument,
                                                    List<AccountingMovement> itemsForPayments)
            throws EntryDuplicatedException
            , ConcurrencyException
            , CompanyConfigurationNotFoundException {

        PayableDocumentType cashBoxDocumentType = null;
        PayableDocument currentPayableDocument = findByAccountingMovement(itemForPayableDocument);
        String companyNumber = itemForPayableDocument.getCompanyNumber();
        if (currentPayableDocument == null) {
            String transactionNumber = getAccountingMovementTransactionNumber(itemForPayableDocument);
            BigDecimal payableAmount = getMovementDetailAmount(itemForPayableDocument, transactionNumber);
            mainDocument.getId().setCompanyNumber(companyNumber);
            mainDocument.getId().setTransactionNumber(transactionNumber);
            mainDocument.setTransactionNumber(transactionNumber);
            mainDocument.setAmount(payableAmount);
            mainDocument.setResidue(payableAmount);
            mainDocument.setDocumentNumber(nextPayableDocumentType(sourceType));
            mainDocument.setBeneficiary(businessUnit.getOrganization().getName());
            mainDocument.setCurrencyCode(FinancesCurrencyType.P.name());
            mainDocument.setExchangeRate(BigDecimal.ONE);
            mainDocument.setState(PayableDocumentState.APR);
            mainDocument.setExpirationDate(itemForPayableDocument.getRecordDate());
            mainDocument.setRegistrationDate(itemForPayableDocument.getCreateDate());
            mainDocument.setMovementDate(itemForPayableDocument.getRecordDate());
            mainDocument.setAccountingMovement(itemForPayableDocument);
            mainDocument.setRegistrationPending(false);
            mainDocument.setRegularizeVoucherResidue(BigDecimal.ZERO);
            mainDocument.setAllowUpdate(false);
            create(mainDocument);
            PayableDocumentMovement payableDocumentMovement = new PayableDocumentMovement();
            payableDocumentMovement.getId().setCompanyNumber(companyNumber);
            payableDocumentMovement.getId().setTransactionNumber(transactionNumber);
            payableDocumentMovement.getId().setState(mainDocument.getState().name());
            payableDocumentMovement.setAccountingMovement(itemForPayableDocument);
            payableDocumentMovement.setCreatedOnDate(itemForPayableDocument.getCreateDate());
            payableDocumentMovement.setMovementDate(itemForPayableDocument.getRecordDate());
            payableDocumentMovement.setMovementType(mainDocument.getDocumentType().getMovementType());
            payableDocumentMovement.setState(mainDocument.getState());
            payableDocumentMovement.setProvider(mainDocument.getProvider());
            payableDocumentMovement.setDescription(itemForPayableDocument.getGloss());
            payableDocumentMovement.setUserNumber(itemForPayableDocument.getUserNumber());
            create(payableDocumentMovement);
            itemForPayableDocument.setSourceModule(Constants.PAYABLES_VOUCHER_FORM);
            update(itemForPayableDocument);

        } else {
            mainDocument = currentPayableDocument;
        }

        for (AccountingMovement payment : itemsForPayments) {
            List<FinanceDocument> financeDocumentList = getFinancesDocumentByAccountingMovement(payment);
            if (!ValidatorUtil.isEmptyOrNull(financeDocumentList)) {
                for (FinanceDocument financeDocument : financeDocumentList) {
                    if (!isDocumentRelated(financeDocument.getTransactionNumber(), mainDocument)) {
                        PayableDocument paymentDocument = createAsPayableDocument(mainDocument, financeDocument);
                        createDocumentsRelationship(mainDocument, paymentDocument);
                    }
                }
            } else if (PayableDocumentSourceType.PURCHASE_ORDER.equals(sourceType)) {
                List<Object[]> purchaseOrderCashBoxPaymentList = getPurchaseOrderCashBoxPaymentByAccountingMovement(payment);
                if (!ValidatorUtil.isEmptyOrNull(purchaseOrderCashBoxPaymentList)) {
                    for (Object[] purchasePayment : purchaseOrderCashBoxPaymentList) {
                        AccountingMovement cashBoxPaymentAccountingMovement = (AccountingMovement) purchasePayment[0];
                        PurchaseOrderPayment purchaseOrderPayment = (PurchaseOrderPayment) purchasePayment[1];
                        String cashBoxPaymentTransactionNumber = getAccountingMovementTransactionNumber(cashBoxPaymentAccountingMovement);
                        if (!isDocumentRelated(cashBoxPaymentTransactionNumber, mainDocument)) {
                            if (cashBoxDocumentType == null) {
                                cashBoxDocumentType = companyConfigurationService.findDefaultPayableCashBoxDocumentType();
                            }
                            PayableDocument paymentDocument = createAsPayableDocument(mainDocument, cashBoxPaymentAccountingMovement, purchaseOrderPayment, cashBoxDocumentType);
                            createDocumentsRelationship(mainDocument, paymentDocument);
                        }
                    }
                }
            }
        }

        return mainDocument;
    }

    private void createDocumentsRelationship(PayableDocument mainDocument, PayableDocument paymentDocument) throws EntryDuplicatedException {
        String conciliationNumber = financesPkGeneratorService.executeFunction(FinancesPkGeneratorService.NativeFunction.CONCILIATION_NUMBER);

        PayableRelatedDocument relationshipForPayment = new PayableRelatedDocument(
                paymentDocument.getId().getCompanyNumber(),
                paymentDocument.getId().getTransactionNumber(),
                conciliationNumber,
                paymentDocument.getExchangeRate(),
                paymentDocument.getAmount()
        );
        create(relationshipForPayment);

        PayableRelatedDocument relationshipForPayableDocument = new PayableRelatedDocument(
                mainDocument.getCompanyNumber(),
                mainDocument.getId().getTransactionNumber(),
                conciliationNumber,
                BigDecimal.ONE,
                BigDecimalUtil.multiply(paymentDocument.getAmount(), paymentDocument.getExchangeRate())
        );
        create(relationshipForPayableDocument);
    }

    private PayableDocument createAsPayableDocument(PayableDocument mainDocument, FinanceDocument financeDocument) throws EntryDuplicatedException {
        PayableDocument payableDocument = findByAccountingMovement(financeDocument.getAccountingMovement());
        if (null == payableDocument) {
            PayableDocumentState payableDocumentState = toPayableDocumentState(financeDocument.getState());
            PayableDocument newDocument = new PayableDocument();
            newDocument.getId().setCompanyNumber(financeDocument.getId().getCompanyNumber());
            newDocument.getId().setTransactionNumber(financeDocument.getId().getTransactionNumber());
            newDocument.setEntityCode(mainDocument.getEntityCode());
            newDocument.setProviderCode(mainDocument.getProviderCode());
            newDocument.setDocumentTypeCode(financeDocument.getDocumentTypeCode());
            newDocument.setDocumentNumber(financeDocument.getDocumentNumber());
            newDocument.setMovementDate(financeDocument.getDate());
            newDocument.setAmount(financeDocument.getAmount());
            newDocument.setResidue(financeDocument.getAmount());
            newDocument.setCurrencyCode(financeDocument.getCurrency().name());
            newDocument.setExchangeRate(financeDocument.getExchangeRate());
            newDocument.setState(payableDocumentState);
            newDocument.setPayableAccountCode(financeDocument.getBankAccount().getAccountingAccountCode());
            newDocument.setAccountingMovement(financeDocument.getAccountingMovement());
            newDocument.setAllowUpdate(false);
            newDocument.setRegularizeVoucherResidue(BigDecimal.ZERO);
            newDocument.setBeneficiary(financeDocument.getBeneficiaryName());
            create(newDocument);

            FinanceDocumentState standardFinanceDocumentState = toStandardFinanceDocumentState(financeDocument.getState());
            FinanceDocumentMovement financeMovement = findMovementByFinancesDocument(financeDocument, standardFinanceDocumentState);
            PayableDocumentMovement newMovement = new PayableDocumentMovement();
            newMovement.getId().setCompanyNumber(financeMovement.getId().getCompanyNumber());
            newMovement.getId().setTransactionNumber(financeMovement.getId().getTransactionNumber());
            newMovement.getId().setState(payableDocumentState.name());
            newMovement.setProviderCode(mainDocument.getProviderCode());
            newMovement.setMovementDate(financeMovement.getDate());
            newMovement.setCreatedOnDate(financeMovement.getCreatedOnDate());
            newMovement.setMovementType(FinanceMovementType.D);
            newMovement.setDescription(financeMovement.getDescription());
            newMovement.setUserNumber(financeMovement.getUserNumber());
            newMovement.setAccountingMovement(financeMovement.getAccountingMovement());
            create(newMovement);
            payableDocument = newDocument;
        }
        return payableDocument;
    }

    private PayableDocument createAsPayableDocument(PayableDocument mainDocument,
                                                    AccountingMovement cashBoxAccountingMovement,
                                                    PurchaseOrderPayment purchaseOrderPayment,
                                                    PayableDocumentType cashBoxDocumentType) throws EntryDuplicatedException, ConcurrencyException {
        PayableDocument payableDocument = findByAccountingMovement(cashBoxAccountingMovement);
        if (ValidatorUtil.isBlankOrNull(purchaseOrderPayment.getTransactionNumber())) {
            purchaseOrderPayment.setTransactionNumber(financesPkGeneratorService.executeFunction(FinancesPkGeneratorService.NativeFunction.TRANSACTION_NUMBER));
            update(purchaseOrderPayment);
        }
        if (null == payableDocument) {
            PayableDocumentState payableDocumentState = PayableDocumentState.APR;
            PayableDocument newDocument = new PayableDocument();
            newDocument.getId().setCompanyNumber(cashBoxAccountingMovement.getCompanyNumber());
            newDocument.getId().setTransactionNumber(purchaseOrderPayment.getTransactionNumber());
            newDocument.setEntityCode(mainDocument.getEntityCode());
            newDocument.setProviderCode(mainDocument.getProviderCode());
            newDocument.setDocumentTypeCode(cashBoxDocumentType.getDocumentType());
            //todo this is the same behavior in Finances system
            newDocument.setDocumentNumber(mainDocument.getDocumentNumber());

            newDocument.setMovementDate(cashBoxAccountingMovement.getRecordDate());
            newDocument.setAmount(purchaseOrderPayment.getPayAmount());
            newDocument.setResidue(purchaseOrderPayment.getPayAmount());
            newDocument.setCurrencyCode(purchaseOrderPayment.getPayCurrency().name());
            newDocument.setExchangeRate(FinancesCurrencyType.P.equals(purchaseOrderPayment.getPayCurrency()) ? BigDecimal.ONE : purchaseOrderPayment.getExchangeRate());
            newDocument.setState(payableDocumentState);
            newDocument.setPayableAccountCode(purchaseOrderPayment.getCashBoxCashAccount().getAccountCode());
            newDocument.setAccountingMovement(cashBoxAccountingMovement);
            newDocument.setAllowUpdate(false);
            newDocument.setRegularizeVoucherResidue(BigDecimal.ZERO);
            newDocument.setBeneficiary(purchaseOrderPayment.getBeneficiaryName());
            create(newDocument);

            PayableDocumentMovement newMovement = new PayableDocumentMovement();
            newMovement.getId().setCompanyNumber(cashBoxAccountingMovement.getCompanyNumber());
            newMovement.getId().setTransactionNumber(purchaseOrderPayment.getTransactionNumber());
            newMovement.getId().setState(payableDocumentState.name());
            newMovement.setProviderCode(mainDocument.getProviderCode());
            newMovement.setMovementDate(cashBoxAccountingMovement.getRecordDate());
            newMovement.setCreatedOnDate(cashBoxAccountingMovement.getCreateDate());
            newMovement.setMovementType(FinanceMovementType.D);
            newMovement.setDescription(cashBoxAccountingMovement.getGloss());
            newMovement.setUserNumber(cashBoxAccountingMovement.getUserNumber());
            newMovement.setAccountingMovement(cashBoxAccountingMovement);
            create(newMovement);

            payableDocument = newDocument;
        }
        return payableDocument;
    }

    private Boolean isDocumentRelated(String transactionNumber, PayableDocument payableDocument) {
        Long counter = 0l;
        List<String> conciliationNumberList = getEntityManager()
                .createNamedQuery("PayableRelatedDocument.findConciliationNumber")
                .setParameter("transactionNumber", transactionNumber)
                .getResultList();
        if (!ValidatorUtil.isEmptyOrNull(conciliationNumberList)) {
            counter = (Long) getEntityManager()
                    .createNamedQuery("PayableRelatedDocument.countByConciliationAndTransactionNumber")
                    .setParameter("conciliationNumberList", conciliationNumberList)
                    .setParameter("transactionNumber", payableDocument.getTransactionNumber())
                    .getSingleResult();
        }
        return counter != null && counter > 0;
    }

    public static PayableDocumentState toPayableDocumentState(FinanceDocumentState state) {
        return FinanceDocumentState.ANL.equals(state) ? PayableDocumentState.ANL : PayableDocumentState.APR;
    }

    public static FinanceDocumentState toStandardFinanceDocumentState(FinanceDocumentState state) {
        return FinanceDocumentState.ANL.equals(state) ? FinanceDocumentState.ANL : FinanceDocumentState.APR;
    }

    public PayableDocument findByTransactionNumber(String transactionNumber) {
        PayableDocument payableDocument = null;
        try {
            payableDocument = (PayableDocument) getEntityManager().createNamedQuery("PayableDocument.findByTransactionNumber")
                    .setParameter("transactionNumber", transactionNumber)
                    .setFirstResult(0)
                    .setMaxResults(1).getSingleResult();
        } catch (NoResultException ignored) {
        }
        return payableDocument;
    }

}
