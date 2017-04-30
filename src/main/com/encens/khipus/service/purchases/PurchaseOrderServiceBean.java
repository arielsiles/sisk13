package com.encens.khipus.service.purchases;

import com.encens.khipus.exception.ConcurrencyException;
import com.encens.khipus.exception.EntryDuplicatedException;
import com.encens.khipus.exception.EntryNotFoundException;
import com.encens.khipus.exception.purchase.*;
import com.encens.khipus.framework.service.GenericServiceBean;
import com.encens.khipus.model.purchases.*;
import com.encens.khipus.service.warehouse.AdvancePaymentService;
import com.encens.khipus.util.BigDecimalUtil;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static javax.ejb.TransactionAttributeType.REQUIRES_NEW;

/**
 * PurchaseOrderServiceBean
 *
 * @author
 * @version 2.24
 */
@Name("purchaseOrderService")
@Stateless
@AutoCreate
public class PurchaseOrderServiceBean extends GenericServiceBean implements PurchaseOrderService {

    @In(value = "#{listEntityManager}")
    private EntityManager listEm;

    @In
    private AdvancePaymentService advancePaymentService;

    public PurchaseOrder findPurchaseOrder(Long id) {
        findInDataBase(id);
        PurchaseOrder purchaseOrder = getEntityManager().find(PurchaseOrder.class, id);
        getEntityManager().refresh(purchaseOrder);

        return purchaseOrder;
    }

    @Override
    public void create(Object entity) throws EntryDuplicatedException {
        PurchaseOrder purchaseOrder = (PurchaseOrder) entity;
        String orderNumber = String.valueOf(getNextOrderNumber());
        purchaseOrder.setOrderNumber(orderNumber);
        purchaseOrder.setDate(new Date());
        purchaseOrder.setState(PurchaseOrderState.PEN);
        purchaseOrder.setPaymentStatus(PurchaseOrderPaymentStatus.NO_PAYMENT);
        super.create(purchaseOrder);
    }

    @TransactionAttribute(REQUIRES_NEW)
    public void updatePurchaseOrder(PurchaseOrder entity)
            throws PurchaseOrderApprovedException,
            PurchaseOrderFinalizedException,
            ConcurrencyException,
            PurchaseOrderNullifiedException,
            PurchaseOrderLiquidatedException {
        if (canChangePurchaseOrder(entity)) {
            try {
                super.update(entity);
            } catch (EntryDuplicatedException e) {
                throw new RuntimeException("Unexpected error has happen", e);
            }
        }
    }

    @TransactionAttribute(REQUIRES_NEW)
    public void specialUpdatePurchaseOrder(PurchaseOrder purchaseOrder)
            throws EntryNotFoundException,
            ConcurrencyException,
            EntryDuplicatedException {
        PurchaseOrder currentInstance = super.findById(PurchaseOrder.class, purchaseOrder.getId());
        currentInstance.setInvoiceNumber(purchaseOrder.getInvoiceNumber());
        super.update(currentInstance);
    }

    public Boolean isPurchaseOrderPending(PurchaseOrder instance) {
        return isPurchaseOrderState(instance, PurchaseOrderState.PEN);
    }

    public Boolean isPurchaseOrderApproved(PurchaseOrder instance) {
        return isPurchaseOrderState(instance, PurchaseOrderState.APR);
    }

    public Boolean isPurchaseOrderNullified(PurchaseOrder instance) {
        return isPurchaseOrderState(instance, PurchaseOrderState.ANL);
    }

    public Boolean isPurchaseOrderFinalized(PurchaseOrder instance) {
        return isPurchaseOrderState(instance, PurchaseOrderState.FIN);
    }

    public Boolean isPurchaseOrderLiquidated(PurchaseOrder instance) {
        return isPurchaseOrderState(instance, PurchaseOrderState.LIQ);
    }

    @TransactionAttribute(REQUIRES_NEW)
    public void approvePurchaseOrder(PurchaseOrder entity)
            throws PurchaseOrderApprovedException,
            PurchaseOrderFinalizedException,
            PurchaseOrderDetailEmptyException,
            ConcurrencyException,
            PurchaseOrderNullifiedException,
            PurchaseOrderLiquidatedException {

        PurchaseOrder purchaseOrder = findInDataBase(entity.getId());
        if (canChangePurchaseOrder(purchaseOrder)) {
            if (isPurchaseOrderEmpty(entity)) {
                throw new PurchaseOrderDetailEmptyException("The purchase order detail cannot be empty");
            }

            entity.setState(PurchaseOrderState.APR);
            try {
                update(entity);
            } catch (EntryDuplicatedException e) {
                throw new RuntimeException("Unexpexted error was happen", e);
            }
        }
    }

    @TransactionAttribute(REQUIRES_NEW)
    public void nullifyPurchaseOrder(PurchaseOrder entity)
            throws PurchaseOrderFinalizedException,
            ConcurrencyException,
            PurchaseOrderNullifiedException,
            PurchaseOrderLiquidatedException {
        PurchaseOrder dbPurchaseOrder = findPurchaseOrder(entity.getId());

        if (isPurchaseOrderFinalized(dbPurchaseOrder)) {
            findPurchaseOrder(entity.getId());
            throw new PurchaseOrderFinalizedException("The purchase order was already finalized, and cannot be changed");
        }

        if (isPurchaseOrderNullified(dbPurchaseOrder)) {
            findPurchaseOrder(entity.getId());
            throw new PurchaseOrderNullifiedException("The purchase order was already nullified, and cannot be changed");
        }

        if (isPurchaseOrderLiquidated(dbPurchaseOrder)) {
            findPurchaseOrder(entity.getId());
            throw new PurchaseOrderLiquidatedException("The purchase order was already liquidated, and cannot be changed");
        }

        entity.setState(PurchaseOrderState.ANL);

        try {
            update(entity);
        } catch (EntryDuplicatedException e) {
            throw new RuntimeException("Unexpexted error was happen when nullify purchaseOrder", e);
        }
    }

    public boolean canChangePurchaseOrder(PurchaseOrder entity)
            throws PurchaseOrderApprovedException,
            PurchaseOrderFinalizedException, PurchaseOrderNullifiedException,
            PurchaseOrderLiquidatedException {

        if (isPurchaseOrderApproved(entity)) {
            findPurchaseOrder(entity.getId());
            throw new PurchaseOrderApprovedException("The purchase order was already approved, and cannot be changed");
        }

        if (isPurchaseOrderFinalized(entity)) {
            findPurchaseOrder(entity.getId());
            throw new PurchaseOrderFinalizedException("The purchase order was already finalized, and cannot be changed");
        }

        if (isPurchaseOrderNullified(entity)) {
            findPurchaseOrder(entity.getId());
            throw new PurchaseOrderNullifiedException("The purchase order was already nullified, and cannot be changed");
        }

        if (isPurchaseOrderLiquidated(entity)) {
            findPurchaseOrder(entity.getId());
            throw new PurchaseOrderLiquidatedException("The purchase order was already liquidated, and cannot be changed");
        }

        return PurchaseOrderState.PEN.equals(entity.getState());
    }

    public Boolean containPurchaseOrderDetails(PurchaseOrder instance) {
        return !isPurchaseOrderEmpty(instance);
    }

    protected PurchaseOrder findInDataBase(Long id) {
        PurchaseOrder purchaseOrder = listEm.find(PurchaseOrder.class, id);
        if (null == purchaseOrder) {
            throw new RuntimeException("Cannot find the PurchaseOrder entity for id=" + id);
        }

        return purchaseOrder;
    }

    protected Boolean isPurchaseOrderState(PurchaseOrder instance, PurchaseOrderState state) {
        PurchaseOrder purchaseOrder = findInDataBase(instance.getId());

        return null != purchaseOrder.getState() && state.equals(purchaseOrder.getState());
    }

    protected Boolean isPurchaseOrderEmpty(PurchaseOrder instance) {
        log.debug("Overwrite this method to verify if the PurchaseOrder entity contain details, " +
                "by default it always return true");
        return true;
    }

    protected Long getNextOrderNumber() {
        log.debug("This method should be overwritten in extended class by default it returns null value");
        return null;
    }

    @SuppressWarnings(value = "unchecked")
    protected boolean existsPendingAdvancePayments(PurchaseOrder entity) {
        List<PurchaseOrderPaymentState> states = Arrays.asList(PurchaseOrderPaymentState.PENDING);

        List<PurchaseOrderPayment> result = getEntityManager().createNamedQuery("PurchaseOrderPayment.findByState")
                .setParameter("purchaseOrder", entity)
                .setParameter("states", states).getResultList();
        return null != result && !result.isEmpty();
    }

    public BigDecimal currentBalanceAmount(PurchaseOrder purchaseOrder) {
        BigDecimal currentBalanceAmount = BigDecimal.ZERO;
//        Long countByStateAndPurchaseOrder = null;
//        try {
//            countByStateAndPurchaseOrder = (Long) listEm.createNamedQuery("PurchaseOrderPayment.countByStateAndPurchaseOrder")
//                    .setParameter("purchaseOrder", purchaseOrder)
//                    .setParameter("state", FixedAssetPaymentState.APPROVED)
//                    .getSingleResult();
//        } catch (NoResultException nre) {
//        }
//        if (countByStateAndPurchaseOrder == null || countByStateAndPurchaseOrder == 0) {
        BigDecimal sumAdvancePaymentAmount = advancePaymentService.sumAllAdvancePaymentAmountsByApprovedState(purchaseOrder);
        if (BigDecimalUtil.isZeroOrNull(sumAdvancePaymentAmount)) {
            sumAdvancePaymentAmount = BigDecimal.ZERO;
        }
        currentBalanceAmount = BigDecimalUtil.subtract(purchaseOrder.getTotalAmount(), sumAdvancePaymentAmount);
//        }
        return currentBalanceAmount;
    }

    public void updateCurrentPaymentStatus(PurchaseOrder purchaseOrder) {
        String logString = "\n*********************************************************************************************";
        logString += "\n purchaseOrder.getState()=" + purchaseOrder.getState();
        logString += "\n PurchaseOrderState.FIN.equals(purchaseOrder.getState())=" + PurchaseOrderState.FIN.equals(purchaseOrder.getState());
        if (PurchaseOrderState.FIN.equals(purchaseOrder.getState())) {
            BigDecimal currentBalanceAmount = currentBalanceAmount(purchaseOrder);
            logString += "\n currentBalanceAmount=" + currentBalanceAmount;
            purchaseOrder.setBalanceAmount(currentBalanceAmount);
            if (currentBalanceAmount.compareTo(BigDecimal.ZERO) > 0) {
                if (purchaseOrder.getTotalAmount().compareTo(currentBalanceAmount) == 0) {
                    purchaseOrder.setPaymentStatus(PurchaseOrderPaymentStatus.NO_PAYMENT);
                } else {
                    purchaseOrder.setPaymentStatus(PurchaseOrderPaymentStatus.PARTIAL_PAYMENT);
                }
            } else {
                purchaseOrder.setPaymentStatus(PurchaseOrderPaymentStatus.FULLY_PAID);
            }
        } else if (PurchaseOrderState.LIQ.equals(purchaseOrder.getState())) {
            purchaseOrder.setBalanceAmount(BigDecimal.ZERO);
            purchaseOrder.setPaymentStatus(PurchaseOrderPaymentStatus.FULLY_PAID);
        } else {
            purchaseOrder.setBalanceAmount(purchaseOrder.getTotalAmount());
            purchaseOrder.setPaymentStatus(PurchaseOrderPaymentStatus.NO_PAYMENT);
        }
        logString += "\n purchaseOrder.getBalanceAmount()=" + purchaseOrder.getBalanceAmount();
        logString += "\n purchaseOrder.getPaymentStatus()=" + purchaseOrder.getPaymentStatus();

        getEntityManager().merge(purchaseOrder);
        getEntityManager().flush();

        logString += "\n PurchaseOrder{" +
                "id=" + purchaseOrder.getId() +
                ", orderNumber=" + purchaseOrder.getOrderNumber() +
                ", orderType=" + purchaseOrder.getOrderType() +
                ", state=" + purchaseOrder.getState() +
                ", paymentStatus=" + purchaseOrder.getPaymentStatus() +
                ", balanceAmount=" + purchaseOrder.getBalanceAmount() +
                "}";

        log.debug(logString);
    }

    private String updateCurrentPaymentStatusForBatchProcess(Long purchaseOrderId) {
        PurchaseOrder purchaseOrder = listEm.find(PurchaseOrder.class, purchaseOrderId);

        String logString = "\n*********************************************************************************************";
        logString += "\n purchaseOrder.getState()=" + purchaseOrder.getState();
        logString += "\n PurchaseOrderState.FIN.equals(purchaseOrder.getState())=" + PurchaseOrderState.FIN.equals(purchaseOrder.getState());

        if (PurchaseOrderState.APR.equals(purchaseOrder.getState())) {
            BigDecimal currentBalanceAmount = currentBalanceAmount(purchaseOrder);
            logString += "\n currentBalanceAmount=" + currentBalanceAmount;
            purchaseOrder.setBalanceAmount(currentBalanceAmount);
            if (currentBalanceAmount.compareTo(BigDecimal.ZERO) > 0) {
                if (purchaseOrder.getTotalAmount().compareTo(currentBalanceAmount) == 0) {
                    purchaseOrder.setPaymentStatus(PurchaseOrderPaymentStatus.NO_PAYMENT);
                } else {
                    purchaseOrder.setPaymentStatus(PurchaseOrderPaymentStatus.PARTIAL_PAYMENT);
                }
            } else {
                purchaseOrder.setPaymentStatus(PurchaseOrderPaymentStatus.FULLY_PAID);
            }
            logString += "\n changeState to =" + PurchaseOrderState.FIN;
        } else if (PurchaseOrderState.FIN.equals(purchaseOrder.getState()) || PurchaseOrderState.LIQ.equals(purchaseOrder.getState())) {
            purchaseOrder.setBalanceAmount(BigDecimal.ZERO);
            purchaseOrder.setPaymentStatus(PurchaseOrderPaymentStatus.FULLY_PAID);
            logString += "\n changeState to =" + PurchaseOrderState.LIQ;
        } else {
            purchaseOrder.setBalanceAmount(purchaseOrder.getTotalAmount());
            purchaseOrder.setPaymentStatus(PurchaseOrderPaymentStatus.NO_PAYMENT);
        }

        logString += "\n purchaseOrder.getBalanceAmount()=" + purchaseOrder.getBalanceAmount();
        logString += "\n purchaseOrder.getPaymentStatus()=" + purchaseOrder.getPaymentStatus();
        listEm.merge(purchaseOrder);

        return logString += "\n PurchaseOrder{" +
                "id=" + purchaseOrder.getId() +
                ", orderNumber=" + purchaseOrder.getOrderNumber() +
                ", orderType=" + purchaseOrder.getOrderType() +
                ", state=" + purchaseOrder.getState() +
                ", paymentStatus=" + purchaseOrder.getPaymentStatus() +
                ", balanceAmount=" + purchaseOrder.getBalanceAmount() +
                "}";
    }

    @SuppressWarnings({"unchecked"})
    public int updatePurchaseOrdersByCurrentValuesForBatchProcess() {
        List<Long> purchaseOrderIdList = listEm.createNamedQuery("PurchaseOrder.findAll").getResultList();
        int rowsInProcess = purchaseOrderIdList.size();
        String result = "";
        for (Long purchaseOrderId : purchaseOrderIdList) {
            result += updateCurrentPaymentStatusForBatchProcess(purchaseOrderId);
        }
        log.info(result);
        return rowsInProcess;
    }

    public List<PurchaseOrderPayment> findByStatesAndPaymentType(
            List<PurchaseOrderState> purchaseOrderStateList
            , List<PurchaseOrderPaymentState> purchaseOrderPaymentStateList
            , List<PurchaseOrderPaymentType> purchaseOrderPaymentTypeList) {
        List<PurchaseOrderPayment> purchaseOrderPaymentList = new ArrayList<PurchaseOrderPayment>(0);
        try {
            purchaseOrderPaymentList = getEntityManager().createNamedQuery("PurchaseOrderPayment.findByStatesAndPaymentType")
                    .setParameter("purchaseOrderStateList", purchaseOrderStateList)
                    .setParameter("paymentStateList", purchaseOrderPaymentStateList)
                    .setParameter("paymentTypeList", purchaseOrderPaymentTypeList)
                    .getResultList();
        } catch (NoResultException ignored) {
        }
        return purchaseOrderPaymentList;
    }
}
