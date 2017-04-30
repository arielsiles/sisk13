package com.encens.khipus.service.purchases;

import com.encens.khipus.exception.ConcurrencyException;
import com.encens.khipus.exception.EntryDuplicatedException;
import com.encens.khipus.exception.finances.CompanyConfigurationNotFoundException;
import com.encens.khipus.exception.finances.DuplicatedFinanceAccountingDocumentException;
import com.encens.khipus.exception.purchase.PurchaseDocumentAmountException;
import com.encens.khipus.exception.purchase.PurchaseDocumentException;
import com.encens.khipus.exception.purchase.PurchaseDocumentNotFoundException;
import com.encens.khipus.exception.purchase.PurchaseDocumentStateException;
import com.encens.khipus.framework.service.GenericServiceBean;
import com.encens.khipus.model.finances.CollectionDocumentType;
import com.encens.khipus.model.purchases.PurchaseDocument;
import com.encens.khipus.model.purchases.PurchaseDocumentState;
import com.encens.khipus.model.purchases.PurchaseOrder;
import com.encens.khipus.model.purchases.PurchaseOrderState;
import com.encens.khipus.service.common.SequenceGeneratorService;
import com.encens.khipus.service.finances.FinanceAccountingDocumentService;
import com.encens.khipus.service.finances.FinancesPkGeneratorService;
import com.encens.khipus.util.BigDecimalUtil;
import com.encens.khipus.util.Constants;
import com.encens.khipus.util.purchases.PurchaseDocumentUtil;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.persistence.EntityManager;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static javax.ejb.TransactionAttributeType.REQUIRES_NEW;

/**
 * @author
 * @version 2.25
 */
@Stateless
@Name("purchaseDocumentService")
@AutoCreate
public class PurchaseDocumentServiceBean extends GenericServiceBean implements PurchaseDocumentService {
    @In(value = "#{listEntityManager}")
    private EntityManager eventEm;

    @In
    private FinanceAccountingDocumentService financeAccountingDocumentService;

    @In
    private FinancesPkGeneratorService financesPkGeneratorService;

    @In
    private SequenceGeneratorService sequenceGeneratorService;

    public void createDocument(PurchaseDocument document) throws PurchaseDocumentException, PurchaseDocumentAmountException {
        validatePurchaseDocumentAmount(document, false);

        if (document.isInvoiceDocument()) {
            processInvoiceDocument(document);
        }

        if (document.isReceiptDocument()) {
            processReceiptDocument(document);
        }
        if (document.isAdjustmentDocument() && document.getNumber().endsWith("-")) {
            document.setNumber(document.getNumber() + sequenceGeneratorService.nextValue(Constants.PURCHASEDOCUMENT_ADJUSTMENT_CODE_SEQUENCE));
        }

        getEntityManager().persist(document);
        getEntityManager().flush();
    }

    public void createDocumentSimple(PurchaseDocument document) throws PurchaseDocumentException, PurchaseDocumentAmountException {

        //validatePurchaseDocumentAmount(document, false);

        if (document.isInvoiceDocument()) {
            processInvoiceDocument(document);
        }

        if (document.isReceiptDocument()) {
            processReceiptDocument(document);
        }
        if (document.isAdjustmentDocument() && document.getNumber().endsWith("-")) {
            document.setNumber(document.getNumber() + sequenceGeneratorService.nextValue(Constants.PURCHASEDOCUMENT_ADJUSTMENT_CODE_SEQUENCE));
        }

        document.setState(PurchaseDocumentState.PENDING);
        getEntityManager().persist(document);
        getEntityManager().flush();
    }

    public PurchaseDocument readDocument(Long id) throws PurchaseDocumentNotFoundException {
        if (null == getPurchaseDocumentFromDatabase(id)) {
            throw new PurchaseDocumentNotFoundException();
        }

        PurchaseDocument document = getEntityManager().find(PurchaseDocument.class, id);
        getEntityManager().refresh(document);
        return document;
    }

    @TransactionAttribute(REQUIRES_NEW)
    public void updateDocument(PurchaseDocument document)
            throws PurchaseDocumentStateException,
            PurchaseDocumentAmountException,
            PurchaseDocumentException,
            ConcurrencyException, PurchaseDocumentNotFoundException {
        if (null == getPurchaseDocumentFromDatabase(document.getId())) {
            throw new PurchaseDocumentNotFoundException();
        }

        validatePurchaseDocumentState(document);

        validatePurchaseDocumentAmount(document, true);
        if (document.isInvoiceDocument()) {
            processInvoiceDocument(document);
        }

        if (document.isReceiptDocument()) {
            processReceiptDocument(document);
        }

        try {
            super.update(document);
        } catch (EntryDuplicatedException e) {
            throw new RuntimeException("Unexpected error was happen. ", e);
        }
    }

    public void approveDocument(PurchaseDocument document) throws PurchaseDocumentStateException,
            PurchaseDocumentNotFoundException,
            DuplicatedFinanceAccountingDocumentException,
            CompanyConfigurationNotFoundException,
            PurchaseDocumentAmountException {
        if (null == getPurchaseDocumentFromDatabase(document.getId())) {
            throw new PurchaseDocumentNotFoundException();
        }
        financeAccountingDocumentService.validatePK(document);

        validatePurchaseDocumentState(document);
        validatePurchaseDocumentAmount(document, false);


        document.setTransactionNumber(financesPkGeneratorService.getNextPK());
        document.setState(PurchaseDocumentState.APPROVED);
        getEntityManager().merge(document);

        if (document.isInvoiceDocument()) {
            financeAccountingDocumentService.createFinanceAccountingDocument(document);
            financeAccountingDocumentService.createAccountingVoucher(document);
        } else if (document.isAdjustmentDocument()) {
            financeAccountingDocumentService.createAccountingVoucher(document);
        }

        getEntityManager().flush();
    }

    public void approveDocumentSimple(PurchaseDocument document) throws PurchaseDocumentStateException,
            PurchaseDocumentNotFoundException,
            DuplicatedFinanceAccountingDocumentException,
            CompanyConfigurationNotFoundException,
            PurchaseDocumentAmountException {
        if (null == getPurchaseDocumentFromDatabase(document.getId())) {
            throw new PurchaseDocumentNotFoundException();
        }
        financeAccountingDocumentService.validatePK(document);

        validatePurchaseDocumentState(document);
        //validatePurchaseDocumentAmount(document, false);


        document.setTransactionNumber(financesPkGeneratorService.getNextPK());
        document.setState(PurchaseDocumentState.APPROVED);
        getEntityManager().merge(document);

        if (document.isInvoiceDocument()) {
            financeAccountingDocumentService.createFinanceAccountingDocument(document);
            //financeAccountingDocumentService.createAccountingVoucher(document);
        }/* else if (document.isAdjustmentDocument()) {
            financeAccountingDocumentService.createAccountingVoucher(document);
        }*/

        getEntityManager().flush();
    }

    public void nullifyDocument(PurchaseDocument document) throws PurchaseDocumentNotFoundException,
            PurchaseDocumentStateException {
        if (null == getPurchaseDocumentFromDatabase(document.getId())) {
            throw new PurchaseDocumentNotFoundException();
        }

        validatePurchaseDocumentState(document);

        document.setState(PurchaseDocumentState.NULLIFIED);

        getEntityManager().merge(document);
        getEntityManager().flush();
    }

    public Long countDistinctByPurchaseOrder(PurchaseOrder purchaseOrder, CollectionDocumentType type) {
        List<CollectionDocumentType> typeEnumList = new ArrayList<CollectionDocumentType>();
        typeEnumList.add(type);
        typeEnumList.add(CollectionDocumentType.ADJUSTMENT);
        return (Long) eventEm.createNamedQuery("PurchaseDocument.countDistinctByPurchaseOrder")
                .setParameter("purchaseOrderId", purchaseOrder.getId())
                .setParameter("typeEnumList", typeEnumList)
                .setParameter("state", PurchaseDocumentState.NULLIFIED)
                .getSingleResult();
    }

    @SuppressWarnings(value = "unchecked")
    public List<PurchaseDocument> getPendingPurchaseDocuments(PurchaseOrder purchaseOrder) {
        List<PurchaseDocument> result = eventEm.createNamedQuery("PurchaseDocument.findByState")
                .setParameter("state", PurchaseDocumentState.PENDING)
                .setParameter("purchaseOrderId", purchaseOrder.getId())
                .getResultList();

        if (null != result) {
            return result;
        }

        return new ArrayList<PurchaseDocument>();
    }

    public Long countPendingPurchaseDocuments(PurchaseOrder purchaseOrder) {
        return (Long) eventEm.createNamedQuery("PurchaseDocument.countByState")
                .setParameter("state", PurchaseDocumentState.PENDING)
                .setParameter("purchaseOrderId", purchaseOrder.getId())
                .getSingleResult();
    }

    private void processReceiptDocument(PurchaseDocument document) {
        document.setNetAmount(document.getAmount());
        document.setAuthorizationNumber(null);
        document.setControlCode(null);
        document.setExempt(null);
        document.setIce(null);
        document.setIva(null);
        document.setNit(null);
    }


    private void processInvoiceDocument(PurchaseDocument document) throws PurchaseDocumentException {
        if (null == document.getExempt()) {
            document.setExempt(BigDecimal.ZERO);
        }

        if (null == document.getIce()) {
            document.setIce(BigDecimal.ZERO);
        }

        validateInvoiceDocument(document);

        BigDecimal netAmount = PurchaseDocumentUtil.i.calculateNETAmount(document.getAmount(),
                document.getExempt(),
                document.getIce());

        document.setNetAmount(netAmount);
        document.setIva(PurchaseDocumentUtil.i.calculateIVAAmount(netAmount));
    }

    private void validateInvoiceDocument(PurchaseDocument document) throws PurchaseDocumentException {
        List<PurchaseDocumentException.ErrorType> errors = new ArrayList<PurchaseDocumentException.ErrorType>();
        if (BigDecimalUtil.isNegative(document.getIce())) {
            errors.add(PurchaseDocumentException.ErrorType.ICE_NEGATIVE_VALUE);
        }

        if (!PurchaseDocumentUtil.i.isValidICE(document.getAmount(), document.getIce())) {
            errors.add(PurchaseDocumentException.ErrorType.ICE_GREATER_THAN_AMOUNT);
        }

        if (BigDecimalUtil.isNegative(document.getExempt())) {
            errors.add(PurchaseDocumentException.ErrorType.EXEMPT_NEGATIVE_VALUE);
        }

        if (!PurchaseDocumentUtil.i.isValidExempt(document.getAmount(), document.getExempt())) {
            errors.add(PurchaseDocumentException.ErrorType.EXEMPT_GREATER_THAN_AMOUNT);
        }

        if (!PurchaseDocumentUtil.i.canCalculateNETAmount(document.getAmount(), document.getExempt(), document.getIce())) {
            errors.add(PurchaseDocumentException.ErrorType.SUM_ICE_EXEMPT_EXCEED_AMOUNT);
        }

        if (!errors.isEmpty()) {
            throw new PurchaseDocumentException(errors);
        }
    }


    private void validatePurchaseDocumentAmount(PurchaseDocument document,
                                                boolean isUpdate) throws PurchaseDocumentAmountException {
        BigDecimal totalAmount = sumApprovedPurchaseDocumentAmounts(document.getPurchaseOrder());


        if (isUpdate) {
            PurchaseDocument dbDocument = getPurchaseDocumentFromDatabase(document.getId());
            totalAmount = BigDecimalUtil.subtract(totalAmount, processDocumentAmount(dbDocument));
        }

        if (null == totalAmount) {
            totalAmount = BigDecimal.ZERO;
        }

        PurchaseOrder dbPurchaseOrder = getPurchaseOrderFromDatabase(document.getPurchaseOrderId());

        BigDecimal limit = BigDecimalUtil.subtract(dbPurchaseOrder.getTotalAmount(), totalAmount);

        totalAmount = BigDecimalUtil.sum(totalAmount, processDocumentAmount(document));

        if (totalAmount.compareTo(dbPurchaseOrder.getTotalAmount()) == 1) {
            throw new PurchaseDocumentAmountException(limit);
        }
    }

    private BigDecimal processDocumentAmount(PurchaseDocument document) {
        if (document.isInvoiceDocument()) {
            return document.getAmount();
        }

        if (document.isLocalCurrencyUsed()) {
            return document.getAmount();
        }

        return BigDecimalUtil.multiply(document.getAmount(), document.getExchangeRate());
    }

    private void validatePurchaseDocumentState(PurchaseDocument document) throws PurchaseDocumentStateException {
        PurchaseDocument dbPurchaseDocument = getPurchaseDocumentFromDatabase(document.getId());
        if (dbPurchaseDocument.isApproved()) {
            throw new PurchaseDocumentStateException(dbPurchaseDocument.getState());
        }

        if (dbPurchaseDocument.isNullified()) {
            throw new PurchaseDocumentStateException(dbPurchaseDocument.getState());
        }
    }

    private PurchaseDocument getPurchaseDocumentFromDatabase(Long id) {
        return eventEm.find(PurchaseDocument.class, id);
    }

    private PurchaseOrder getPurchaseOrderFromDatabase(Long id) {
        return eventEm.find(PurchaseOrder.class, id);
    }

    public BigDecimal sumApprovedPurchaseDocumentAmounts(PurchaseOrder purchaseOrder) {
        BigDecimal sumResult = (BigDecimal) eventEm.createNamedQuery("PurchaseDocument.sumAmountsByPurchaseOrderAndState")
                .setParameter("purchaseOrderId", purchaseOrder.getId())
                .setParameter("state", PurchaseDocumentState.APPROVED)
                .getSingleResult();
        return sumResult != null ? sumResult : BigDecimal.ZERO;
    }

    public BigDecimal getPurchaseDocumentOpenAmount(PurchaseOrder purchaseOrder) {
        if (!PurchaseOrderState.PEN.equals(purchaseOrder.getState()) &&
                !PurchaseOrderState.ANL.equals(purchaseOrder.getState())) {
            return BigDecimalUtil.subtract(purchaseOrder.getTotalAmount(), sumApprovedPurchaseDocumentAmounts(purchaseOrder));
        }
        return null;
    }

}
