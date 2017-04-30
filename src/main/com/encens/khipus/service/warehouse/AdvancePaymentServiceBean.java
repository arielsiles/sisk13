package com.encens.khipus.service.warehouse;

import com.encens.khipus.exception.ConcurrencyException;
import com.encens.khipus.exception.EntryDuplicatedException;
import com.encens.khipus.exception.finances.CompanyConfigurationNotFoundException;
import com.encens.khipus.exception.finances.RotatoryFundReceivableResidueException;
import com.encens.khipus.exception.purchase.*;
import com.encens.khipus.exception.warehouse.AdvancePaymentAmountException;
import com.encens.khipus.exception.warehouse.AdvancePaymentStateException;
import com.encens.khipus.framework.service.GenericServiceBean;
import com.encens.khipus.interceptor.FinancesUser;
import com.encens.khipus.model.admin.User;
import com.encens.khipus.model.finances.FinancesCurrencyType;
import com.encens.khipus.model.finances.PurchaseOrderPaymentKind;
import com.encens.khipus.model.finances.RotatoryFund;
import com.encens.khipus.model.purchases.PurchaseOrder;
import com.encens.khipus.model.purchases.PurchaseOrderPayment;
import com.encens.khipus.model.purchases.PurchaseOrderPaymentState;
import com.encens.khipus.model.purchases.PurchaseOrderPaymentType;
import com.encens.khipus.service.finances.RotatoryFundCollectionService;
import com.encens.khipus.service.purchases.PurchaseOrderService;
import com.encens.khipus.util.BigDecimalUtil;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.persistence.EntityManager;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static javax.ejb.TransactionAttributeType.REQUIRES_NEW;

/**
 * @author
 * @version 2.24
 */

@FinancesUser
@Stateless
@Name("advancePaymentService")
@AutoCreate
public class AdvancePaymentServiceBean extends GenericServiceBean implements AdvancePaymentService {

    @In(value = "#{listEntityManager}")
    private EntityManager listEm;

    @In
    private PurchaseOrderService purchaseOrderService;

    @In
    private WarehousePurchaseOrderService warehousePurchaseOrderService;

    @In
    private WarehouseAccountEntryService warehouseAccountEntryService;

    @In
    private RotatoryFundCollectionService rotatoryFundCollectionService;

    @In
    private User currentUser;

    public void approveAdvancePayment(PurchaseOrderPayment entity)
            throws AdvancePaymentStateException,
            PurchaseOrderNullifiedException,
            CompanyConfigurationNotFoundException,
            PurchaseOrderLiquidatedException,
            RotatoryFundNullifiedException,
            RotatoryFundLiquidatedException,
            CollectionSumExceedsRotatoryFundAmountException,
            RotatoryFundConcurrencyException,
            RotatoryFundReceivableResidueException {

        if (PurchaseOrderPaymentType.PAYMENT_ROTATORY_FUND.equals(entity.getPaymentType())) {
            RotatoryFund rotatoryFund = listEm.find(RotatoryFund.class, entity.getRotatoryFund().getId());
            if (entity.getSourceAmount().compareTo(rotatoryFund.getReceivableResidue()) > 0) {
                throw new RotatoryFundReceivableResidueException("The rotatory fund's residue isn't enough to pay");
            }
        }
        updateAdvancePaymentState(entity, PurchaseOrderPaymentState.APPROVED);
        warehouseAccountEntryService.createAdvancePaymentAccountEntry(entity);
        if (PurchaseOrderPaymentType.PAYMENT_ROTATORY_FUND.equals(entity.getPaymentType())) {
            try {
                rotatoryFundCollectionService.generateCollectionForPurchaseOrderPayment(entity);
            } catch (com.encens.khipus.exception.finances.RotatoryFundNullifiedException e) {
                throw new com.encens.khipus.exception.purchase.RotatoryFundNullifiedException(e);
            } catch (com.encens.khipus.exception.finances.RotatoryFundLiquidatedException e) {
                throw new com.encens.khipus.exception.purchase.RotatoryFundLiquidatedException(e);
            } catch (com.encens.khipus.exception.finances.CollectionSumExceedsRotatoryFundAmountException e) {
                throw new com.encens.khipus.exception.purchase.CollectionSumExceedsRotatoryFundAmountException(e);
            } catch (ConcurrencyException e) {
                throw new com.encens.khipus.exception.purchase.RotatoryFundConcurrencyException(e);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        purchaseOrderService.updateCurrentPaymentStatus(entity.getPurchaseOrder());
    }

    public void nullifyAdvancePayment(PurchaseOrderPayment entity)
            throws AdvancePaymentStateException,
            PurchaseOrderLiquidatedException,
            PurchaseOrderNullifiedException {
        updateAdvancePaymentState(entity, PurchaseOrderPaymentState.NULLIFIED);
    }

    @TransactionAttribute(REQUIRES_NEW)
    public void updateAdvancePayment(PurchaseOrderPayment purchaseOrderPayment)
            throws PurchaseOrderNullifiedException,
            PurchaseOrderLiquidatedException,
            AdvancePaymentAmountException,
            ConcurrencyException,
            AdvancePaymentStateException,
            RotatoryFundReceivableResidueException {

        if (isPurchaseOrderFinalized(purchaseOrderPayment.getPurchaseOrder().getId())
                && isAdvancePaymentPending(purchaseOrderPayment.getId()) && isValidPayAmount(purchaseOrderPayment)) {
            if (PurchaseOrderPaymentType.PAYMENT_BANK_ACCOUNT.equals(purchaseOrderPayment.getPaymentType())) {
                purchaseOrderPayment.setBeneficiaryType(null);
                purchaseOrderPayment.setBeneficiaryName(null);
            }

            //by default bankAmount and payAmount are equal values
            purchaseOrderPayment.setSourceAmount(purchaseOrderPayment.getPayAmount());

            //if advancePayment use a exchange currency its necessary update bankAmount
            if (purchaseOrderPayment.useExchangeCurrency()) {
                if (FinancesCurrencyType.D.equals(purchaseOrderPayment.getSourceCurrency())
                        && FinancesCurrencyType.P.equals(purchaseOrderPayment.getPayCurrency())) {
                    purchaseOrderPayment.setSourceAmount(purchaseOrderPayment.changePayAmountToExchangeCurrency());
                }

                if (FinancesCurrencyType.P.equals(purchaseOrderPayment.getSourceCurrency())
                        && FinancesCurrencyType.D.equals(purchaseOrderPayment.getPayCurrency())) {
                    purchaseOrderPayment.setSourceAmount(purchaseOrderPayment.changePayAmountToLocalCurrency());
                }
            } else {
                purchaseOrderPayment.setExchangeRate(BigDecimal.ONE);
            }

            if (PurchaseOrderPaymentType.PAYMENT_ROTATORY_FUND.equals(purchaseOrderPayment.getPaymentType())) {
                RotatoryFund rotatoryFund = listEm.find(RotatoryFund.class, purchaseOrderPayment.getRotatoryFund().getId());
                if (purchaseOrderPayment.getSourceAmount().compareTo(rotatoryFund.getReceivableResidue()) > 0) {
                    throw new RotatoryFundReceivableResidueException("The rotatory fund's residue isn't enough to pay");
                }
            }

            try {
                super.update(purchaseOrderPayment);
            } catch (EntryDuplicatedException e) {
                throw new RuntimeException("Unexpected error was happen", e);
            }
        }

    }

    public void createAdvancePayment(PurchaseOrderPayment purchaseOrderPayment)
            throws PurchaseOrderNullifiedException,
            AdvancePaymentAmountException,
            PurchaseOrderLiquidatedException {

        if (isPurchaseOrderFinalized(purchaseOrderPayment.getPurchaseOrder().getId())) {
            persistAdvancePayment(purchaseOrderPayment);
        }
    }

    public void persistAdvancePayment(PurchaseOrderPayment purchaseOrderPayment) throws AdvancePaymentAmountException {
        isValidPayAmount(purchaseOrderPayment);

        purchaseOrderPayment.setState(PurchaseOrderPaymentState.PENDING);

        if (purchaseOrderPayment.getCreationDate() == null) {
            purchaseOrderPayment.setCreationDate(new Date());
        }
        if (purchaseOrderPayment.getRegisterEmployee() == null) {
            purchaseOrderPayment.setRegisterEmployee(currentUser);
        }

        //by default bankAmount and payAmount are equal values
        purchaseOrderPayment.setSourceAmount(purchaseOrderPayment.getPayAmount());

        //if advancePayment use a exchange currency its necessary update bankAmount
        if (purchaseOrderPayment.useExchangeCurrency()) {
            if (FinancesCurrencyType.D.equals(purchaseOrderPayment.getSourceCurrency())
                    && FinancesCurrencyType.P.equals(purchaseOrderPayment.getPayCurrency())) {
                purchaseOrderPayment.setSourceAmount(purchaseOrderPayment.changePayAmountToExchangeCurrency());
            }

            if (FinancesCurrencyType.P.equals(purchaseOrderPayment.getSourceCurrency())
                    && FinancesCurrencyType.D.equals(purchaseOrderPayment.getPayCurrency())) {
                purchaseOrderPayment.setSourceAmount(purchaseOrderPayment.changePayAmountToLocalCurrency());
            }
        } else {
            purchaseOrderPayment.setExchangeRate(BigDecimal.ONE);
        }

        Date accountingEntryDefaultDate = purchaseOrderPayment.getAccountingEntryDefaultDate();
        String accountingEntryDefaultUserNumber = purchaseOrderPayment.getAccountingEntryDefaultUserNumber();
        Date approvalDate = purchaseOrderPayment.getApprovalDate();
        User approvedByEmployee = purchaseOrderPayment.getApprovedByEmployee();
        try {
            super.create(purchaseOrderPayment);
        } catch (EntryDuplicatedException e) {
            throw new RuntimeException("Unexpected error was happen", e);
        }
        purchaseOrderPayment.setAccountingEntryDefaultDate(accountingEntryDefaultDate);
        purchaseOrderPayment.setAccountingEntryDefaultUserNumber(accountingEntryDefaultUserNumber);
        purchaseOrderPayment.setApprovalDate(approvalDate);
        purchaseOrderPayment.setApprovedByEmployee(approvedByEmployee);
    }


    @SuppressWarnings(value = "unchecked")
    public BigDecimal sumAllPaymentAmounts(PurchaseOrder purchaseOrder) {
        List<PurchaseOrderPaymentState> states = Arrays.asList(PurchaseOrderPaymentState.APPROVED, PurchaseOrderPaymentState.PENDING);

        List<PurchaseOrderPayment> purchaseOrderPayments = listEm
                .createNamedQuery("PurchaseOrderPayment.findByState")
                .setParameter("purchaseOrder", purchaseOrder)
                .setParameter("states", states).getResultList();

        BigDecimal totalAdvancePayment = BigDecimal.ZERO;

        for (PurchaseOrderPayment purchaseOrderPayment : purchaseOrderPayments) {
            BigDecimal payAmount = purchaseOrderPayment.getPayAmount();

            if (purchaseOrderPayment.useExchangeCurrency() && FinancesCurrencyType.D.equals(purchaseOrderPayment.getPayCurrency())) {
                payAmount = purchaseOrderPayment.changePayAmountToLocalCurrency();
            }

            totalAdvancePayment = BigDecimalUtil.sum(totalAdvancePayment, payAmount);
        }

        return totalAdvancePayment;
    }

    @SuppressWarnings(value = "unchecked")
    public BigDecimal sumAllPaymentAmountsByKind(PurchaseOrder purchaseOrder, PurchaseOrderPaymentKind purchaseOrderPaymentKind) {
        List<PurchaseOrderPaymentState> states = Arrays.asList(PurchaseOrderPaymentState.APPROVED, PurchaseOrderPaymentState.PENDING);

        List<PurchaseOrderPayment> purchaseOrderPayments = listEm
                .createNamedQuery("PurchaseOrderPayment.findByStateAndKind")
                .setParameter("purchaseOrder", purchaseOrder)
                .setParameter("states", states)
                .setParameter("purchaseOrderPaymentKind", purchaseOrderPaymentKind).getResultList();

        BigDecimal totalAdvancePayment = BigDecimal.ZERO;

        for (PurchaseOrderPayment purchaseOrderPayment : purchaseOrderPayments) {
            BigDecimal payAmount = purchaseOrderPayment.getPayAmount();

            if (purchaseOrderPayment.useExchangeCurrency() && FinancesCurrencyType.D.equals(purchaseOrderPayment.getPayCurrency())) {
                payAmount = purchaseOrderPayment.changePayAmountToLocalCurrency();
            }

            totalAdvancePayment = BigDecimalUtil.sum(totalAdvancePayment, payAmount);
        }

        return totalAdvancePayment;
    }

    @SuppressWarnings(value = "unchecked")
    public BigDecimal sumAllPaymentAmountsByKindPurchaseOrder(PurchaseOrder purchaseOrder, PurchaseOrderPaymentKind purchaseOrderPaymentKind) {
        List<PurchaseOrderPaymentState> states = Arrays.asList(PurchaseOrderPaymentState.APPROVED, PurchaseOrderPaymentState.PENDING);

        List<PurchaseOrderPayment> purchaseOrderPayments = listEm
                .createNamedQuery("PurchaseOrderPayment.findByStateAndKindPurchaseOrder")
                .setParameter("purchaseOrder", purchaseOrder)
                .setParameter("states", states)
                .setParameter("purchaseOrderPaymentKind", purchaseOrderPaymentKind).getResultList();

        BigDecimal totalAdvancePayment = BigDecimal.ZERO;

        for (PurchaseOrderPayment purchaseOrderPayment : purchaseOrderPayments) {
            BigDecimal payAmount = purchaseOrderPayment.getPayAmount();

            if (purchaseOrderPayment.useExchangeCurrency() && FinancesCurrencyType.D.equals(purchaseOrderPayment.getPayCurrency())) {
                payAmount = purchaseOrderPayment.changePayAmountToLocalCurrency();
            }

            totalAdvancePayment = BigDecimalUtil.sum(totalAdvancePayment, payAmount);
        }

        return totalAdvancePayment;
    }

    @SuppressWarnings(value = "unchecked")
    public BigDecimal sumAllAdvancePaymentAmountsButCurrent(PurchaseOrderPayment purchaseOrderPayment) {
        PurchaseOrder purchaseOrder = purchaseOrderPayment.getPurchaseOrder();
        List<PurchaseOrderPaymentState> states = Arrays.asList(PurchaseOrderPaymentState.APPROVED, PurchaseOrderPaymentState.PENDING);
        BigDecimal totalAdvancePayment = BigDecimal.ZERO;

        PurchaseOrderPayment oldPurchaseOrderPayment = findAdvancePaymentInDataBase(purchaseOrder.getId());
        BigDecimal oldPayAmount = BigDecimal.ZERO;
        if (oldPurchaseOrderPayment != null) {
            oldPayAmount = oldPurchaseOrderPayment.getPayAmount();
            if (oldPurchaseOrderPayment.useExchangeCurrency() && FinancesCurrencyType.D.equals(oldPurchaseOrderPayment.getPayCurrency())) {
                oldPayAmount = oldPurchaseOrderPayment.changePayAmountToLocalCurrency();
            }
        }

        return BigDecimalUtil.subtract(
                BigDecimalUtil.sum(sumAllAdvancePaymentAmountsByState(purchaseOrder, PurchaseOrderPaymentState.PENDING),
                        sumAllAdvancePaymentAmountsByState(purchaseOrder, PurchaseOrderPaymentState.APPROVED)),
                oldPayAmount);
    }

    @SuppressWarnings(value = "unchecked")
    public BigDecimal sumAllAdvancePaymentAmountsByApprovedState(PurchaseOrder purchaseOrder) {
        return sumAllAdvancePaymentAmountsByState(purchaseOrder, PurchaseOrderPaymentState.APPROVED);
    }

    @SuppressWarnings(value = "unchecked")
    public BigDecimal sumAllAdvancePaymentAmountsByState(PurchaseOrder purchaseOrder, PurchaseOrderPaymentState purchaseOrderPaymentState) {
        List<PurchaseOrderPaymentState> states = Arrays.asList(purchaseOrderPaymentState);
        List<PurchaseOrderPayment> purchaseOrderPayments = listEm
                .createNamedQuery("PurchaseOrderPayment.findByState")
                .setParameter("purchaseOrder", purchaseOrder)
                .setParameter("states", states).getResultList();
        BigDecimal totalAdvancePayment = BigDecimal.ZERO;
        for (PurchaseOrderPayment purchaseOrderPayment : purchaseOrderPayments) {
            BigDecimal payAmount = purchaseOrderPayment.getPayAmount();

            if (purchaseOrderPayment.useExchangeCurrency() && FinancesCurrencyType.D.equals(purchaseOrderPayment.getPayCurrency())) {
                payAmount = purchaseOrderPayment.changePayAmountToLocalCurrency();
            }
            totalAdvancePayment = BigDecimalUtil.sum(totalAdvancePayment, payAmount);
        }

        return totalAdvancePayment;
    }

    private void updateAdvancePaymentState(PurchaseOrderPayment entity, PurchaseOrderPaymentState state)
            throws AdvancePaymentStateException,
            PurchaseOrderLiquidatedException,
            PurchaseOrderNullifiedException {

        if (isPurchaseOrderFinalized(entity.getPurchaseOrder().getId())
                && isAdvancePaymentPending(entity.getId())) {

            PurchaseOrderPayment purchaseOrderPayment = getEntityManager().find(PurchaseOrderPayment.class, entity.getId());
            getEntityManager().refresh(purchaseOrderPayment);
            purchaseOrderPayment.setState(state);
            purchaseOrderPayment.setApprovalDate(new Date());
            purchaseOrderPayment.setApprovedByEmployee(currentUser);
            if (!getEntityManager().contains(purchaseOrderPayment)) {
                getEntityManager().merge(purchaseOrderPayment);
            }
            getEntityManager().flush();
        }
    }

    private boolean isPurchaseOrderFinalized(Long purchaseOrderId)
            throws PurchaseOrderNullifiedException,
            PurchaseOrderLiquidatedException {
        PurchaseOrder purchaseOrder = getEntityManager().find(PurchaseOrder.class, purchaseOrderId);

        if (warehousePurchaseOrderService.isPurchaseOrderNullified(purchaseOrder)) {
            getEntityManager().refresh(purchaseOrder);
            throw new PurchaseOrderNullifiedException("The purchase order was already nullified, and cannot be changed");
        }
        if (warehousePurchaseOrderService.isPurchaseOrderLiquidated(purchaseOrder)) {
            getEntityManager().refresh(purchaseOrder);
            throw new PurchaseOrderLiquidatedException("The purchase order was already liquidated, and cannot be changed");
        }

        return true;
    }

    private boolean isAdvancePaymentPending(Long advancePaymentId) throws AdvancePaymentStateException {
        PurchaseOrderPayment dbPurchaseOrderPayment = findAdvancePaymentInDataBase(advancePaymentId);

        if (PurchaseOrderPaymentState.APPROVED.equals(dbPurchaseOrderPayment.getState())) {
            refreshAdvancePayment(advancePaymentId);
            throw new AdvancePaymentStateException(PurchaseOrderPaymentState.APPROVED);
        }

        if (PurchaseOrderPaymentState.NULLIFIED.equals(dbPurchaseOrderPayment.getState())) {
            refreshAdvancePayment(advancePaymentId);
            throw new AdvancePaymentStateException(PurchaseOrderPaymentState.NULLIFIED);
        }

        return PurchaseOrderPaymentState.PENDING.equals(dbPurchaseOrderPayment.getState());
    }

    private boolean isValidPayAmount(PurchaseOrderPayment purchaseOrderPayment) throws AdvancePaymentAmountException {

        BigDecimal payAmount = purchaseOrderPayment.getPayAmount();
        if (purchaseOrderPayment.useExchangeCurrency() && FinancesCurrencyType.D.equals(purchaseOrderPayment.getPayCurrency())) {
            payAmount = purchaseOrderPayment.changePayAmountToLocalCurrency();
        }

        PurchaseOrder dbPurchaseOrder = listEm.find(PurchaseOrder.class, purchaseOrderPayment.getPurchaseOrder().getId());

        BigDecimal totalAmountPurchaseOrder = dbPurchaseOrder.getTotalAmount();

        BigDecimal amount = sumAllAdvancePaymentAmountsButCurrent(purchaseOrderPayment);

        BigDecimal limit = BigDecimalUtil.subtract(totalAmountPurchaseOrder, amount);

        log.debug("purchase order Total amount: " + totalAmountPurchaseOrder);
        log.debug("pay amount: " + payAmount);
        log.debug("summation of all advanced payments: " + amount);
        log.debug("limit amount: " + limit);
        if (limit.compareTo(payAmount) < 0) {
            throw new AdvancePaymentAmountException(limit);
        }

        return true;
    }


    private PurchaseOrderPayment findAdvancePaymentInDataBase(Long id) {
        return listEm.find(PurchaseOrderPayment.class, id);
    }

    private void refreshAdvancePayment(Long id) {
        PurchaseOrderPayment purchaseOrderPayment =
                getEntityManager().find(PurchaseOrderPayment.class, id);
        getEntityManager().refresh(purchaseOrderPayment);
    }
}
