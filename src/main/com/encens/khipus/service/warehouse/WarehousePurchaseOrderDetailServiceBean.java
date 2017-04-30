package com.encens.khipus.service.warehouse;

import com.encens.khipus.exception.ConcurrencyException;
import com.encens.khipus.exception.EntryDuplicatedException;
import com.encens.khipus.exception.ReferentialIntegrityException;
import com.encens.khipus.exception.purchase.*;
import com.encens.khipus.exception.warehouse.DiscountAmountException;
import com.encens.khipus.framework.service.GenericServiceBean;
import com.encens.khipus.interceptor.FinancesUser;
import com.encens.khipus.model.finances.Provide;
import com.encens.khipus.model.finances.Provider;
import com.encens.khipus.model.purchases.PurchaseOrder;
import com.encens.khipus.model.purchases.PurchaseOrderDetail;
import com.encens.khipus.model.warehouse.ProductItem;
import com.encens.khipus.model.warehouse.ProductItemByProviderHistory;
import com.encens.khipus.util.BigDecimalUtil;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static javax.ejb.TransactionAttributeType.REQUIRES_NEW;

/**
 * @author
 * @version 3.0
 */
@Stateless
@Name("warehousePurchaseOrderDetailService")
@FinancesUser
@AutoCreate
public class WarehousePurchaseOrderDetailServiceBean extends GenericServiceBean implements WarehousePurchaseOrderDetailService {
    @In(value = "#{listEntityManager}")
    private EntityManager listEm;

    @In
    private AdvancePaymentService advancePaymentService;

    @In
    private WarehousePurchaseOrderService warehousePurchaseOrderService;

    @In
    private ProvideService provideService;
    @In
    private ProductItemByProviderHistoryService productItemByProviderHistoryService;

    public void createPurchaseOrderDetail(PurchaseOrderDetail entity, BigDecimal unitPriceByProvider,
                                          Map<PurchaseOrderDetail, BigDecimal> purchaseOrderDetailUnderMinimalStockMap,
                                          Map<PurchaseOrderDetail, BigDecimal> purchaseOrderDetailOverMaximumStockMap,
                                          List<PurchaseOrderDetail> purchaseOrderDetailWithoutWarnings)
            throws PurchaseOrderApprovedException,
            PurchaseOrderFinalizedException,
            PurchaseOrderNullifiedException,
            ConcurrencyException,
            DiscountAmountException,
            DuplicatedPurchaseOrderDetailException,
            PurchaseOrderLiquidatedException {

        PurchaseOrder purchaseOrder = entity.getPurchaseOrder();
        PurchaseOrderDetail purchaseOrderDetail = (PurchaseOrderDetail) entity;
        if (!validateProductItemOnPurchaseOrder(entity)) {
            throw new DuplicatedPurchaseOrderDetailException("Duplicated product item(" + entity.getProductItemCode() + ") on pruchase order(" + purchaseOrder.getOrderNumber() + ")");
        }


        if (warehousePurchaseOrderService.canChangePurchaseOrder(purchaseOrder)) {
            Long detailNumber = getNextDetailNumber(purchaseOrder);

            BigDecimal totalAmount = BigDecimalUtil.multiply(entity.getRequestedQuantity(), unitPriceByProvider, 6);
            entity.setDetailNumber(detailNumber);
            entity.setUnitCost(unitPriceByProvider);
            entity.setTotalAmount(totalAmount);
            ProductItemByProviderHistory productItemByProviderHistory = productItemByProviderHistoryService.findLastUnitCostByProductItem(purchaseOrder.getProvider(), purchaseOrderDetail.getProductItem(), null);

            Provide dbProvide = provideService.findByProviderAndProductItem(purchaseOrder.getProvider(), purchaseOrderDetail.getProductItem(), listEm);
            Provide provide = provideService.findByProviderAndProductItem(purchaseOrder.getProvider(), purchaseOrderDetail.getProductItem(), null);
            if (purchaseOrderDetail.getUnitCost().compareTo(dbProvide.getGroupAmount()) != 0 && null == purchaseOrderDetail.getProductItemByProviderHistory()) {
                /*update group amount to the corresponding provide */
                provide.setGroupAmount(purchaseOrderDetail.getUnitCost());
                if (!getEntityManager().contains(provide)) {
                    getEntityManager().merge(provide);
                }

                productItemByProviderHistory = new ProductItemByProviderHistory();
                productItemByProviderHistory.setDate(new Date());
                productItemByProviderHistory.setProvide(provide);
                productItemByProviderHistory.setUnitCost(purchaseOrderDetail.getUnitCost());
                getEntityManager().persist(productItemByProviderHistory);
                getEntityManager().flush();
            }
            entity.setProductItemByProviderHistory(productItemByProviderHistory);
            try {
                // update detail warnings
                warehousePurchaseOrderService.fillPurchaseOrderDetail(entity,
                        purchaseOrderDetailUnderMinimalStockMap,
                        purchaseOrderDetailOverMaximumStockMap,
                        purchaseOrderDetailWithoutWarnings);
                super.create(entity);
            } catch (EntryDuplicatedException e) {
                throw new RuntimeException("Unexpected error was happen ", e);
            }
            getEntityManager().refresh(entity);
            warehousePurchaseOrderService.updateWarehousePurchaseOrder(purchaseOrder);
        }
    }

    public PurchaseOrderDetail findPurchaseOrderDetail(Long id) throws PurchaseOrderDetailNotFoundException {
        findInDataBase(id);
        PurchaseOrderDetail purchaseOrderDetail = getEntityManager().find(PurchaseOrderDetail.class, id);
        getEntityManager().refresh(purchaseOrderDetail);
        return purchaseOrderDetail;
    }

    @TransactionAttribute(REQUIRES_NEW)
    public void updatePurchaseOrderDetail(PurchaseOrderDetail entity,
                                          Map<PurchaseOrderDetail, BigDecimal> purchaseOrderDetailUnderMinimalStockMap,
                                          Map<PurchaseOrderDetail, BigDecimal> purchaseOrderDetailOverMaximumStockMap,
                                          List<PurchaseOrderDetail> purchaseOrderDetailWithoutWarnings)
            throws PurchaseOrderFinalizedException,
            ConcurrencyException,
            PurchaseOrderDetailNotFoundException,
            PurchaseOrderDetailTotalAmountException,
            PurchaseOrderApprovedException,
            PurchaseOrderNullifiedException,
            DiscountAmountException,
            DuplicatedPurchaseOrderDetailException,
            PurchaseOrderLiquidatedException {

        PurchaseOrder purchaseOrder = entity.getPurchaseOrder();

        if (!validateProductItemOnPurchaseOrder(entity)) {
            throw new DuplicatedPurchaseOrderDetailException("Duplicated product item(" + entity.getProductItemCode() + ") on pruchase order(" + purchaseOrder.getOrderNumber() + ")");
        }


        PurchaseOrderDetail dbPurchaseOrderDetail;
        try {
            dbPurchaseOrderDetail = findInDataBase(entity.getId());
        } catch (PurchaseOrderDetailNotFoundException e) {
            detach(entity);
            throw e;
        }

        if (warehousePurchaseOrderService.isPurchaseOrderFinalized(purchaseOrder)) {
            warehousePurchaseOrderService.findPurchaseOrder(entity.getId());
            throw new PurchaseOrderFinalizedException("The purchase order was already finalized, and cannot be changed");
        }

        BigDecimal totalAmount = BigDecimalUtil.multiply(entity.getRequestedQuantity(), entity.getUnitCost(), 6);

        if (warehousePurchaseOrderService.isPurchaseOrderApproved(purchaseOrder)) {
            isValidPurchaseOrderDetailTotalAmount(purchaseOrder,
                    totalAmount,
                    dbPurchaseOrderDetail.getTotalAmount());
        }

        if (dbPurchaseOrderDetail.getVersion() != entity.getVersion()) {
            throw new ConcurrencyException("The purchase order detail was updated by other user");
        }

        entity.setTotalAmount(totalAmount);
        // update detail warnings
        warehousePurchaseOrderService.fillPurchaseOrderDetail(entity, purchaseOrderDetailUnderMinimalStockMap,
                purchaseOrderDetailOverMaximumStockMap,
                purchaseOrderDetailWithoutWarnings);
        getEntityManager().merge(entity);
        getEntityManager().flush();
        getEntityManager().refresh(entity);

        Provide dbProvide = provideService.findByProviderAndProductItem(purchaseOrder.getProvider(), entity.getProductItem(), listEm);
        Provide provide = provideService.findByProviderAndProductItem(purchaseOrder.getProvider(), entity.getProductItem(), null);
        if (entity.getUnitCost().compareTo(dbProvide.getGroupAmount()) != 0) {
            /*update group amount to the corresponding provide */
            provide.setGroupAmount(entity.getUnitCost());
            if (!getEntityManager().contains(provide)) {
                getEntityManager().merge(provide);
            }

            ProductItemByProviderHistory productItemByProviderHistory = new ProductItemByProviderHistory();
            productItemByProviderHistory.setDate(new Date());
            productItemByProviderHistory.setProvide(provide);
            productItemByProviderHistory.setUnitCost(entity.getUnitCost());
            getEntityManager().persist(productItemByProviderHistory);
            getEntityManager().flush();
        }

        //update purchase order total amounts only if it is approved
        /* if (warehousePurchaseOrderService.isPurchaseOrderApproved(purchaseOrder)) {
            purchaseOrder = warehousePurchaseOrderService.updateTotalAmountFields(purchaseOrder);
            getEntityManager().merge(purchaseOrder);
            getEntityManager().flush();
            getEntityManager().refresh(purchaseOrder);
        }*/
        warehousePurchaseOrderService.updateWarehousePurchaseOrder(purchaseOrder);
    }

    public Provide findProvideElement(ProductItem productItem, Provider provider) {
        Provide provide = null;

        try {
            provide = (Provide) listEm.createNamedQuery("Provide.findByProviderAndProductItem")
                    .setParameter("productItem", productItem)
                    .setParameter("provider", provider).getSingleResult();
        } catch (NoResultException e) {
            log.debug("The provider=" + provider.getProviderCode() +
                    " does not provide anymore the productItem=" + productItem.getId().getProductItemCode());
        } catch (NonUniqueResultException e) {
            log.debug("This exception never happen because exists a constraint foreign key in provide entity");
        }

        return provide;
    }


    private boolean isValidPurchaseOrderDetailTotalAmount(PurchaseOrder purchaseOrder,
                                                          BigDecimal totalAmount,
                                                          BigDecimal oldTotalAmount)
            throws PurchaseOrderDetailTotalAmountException {
        PurchaseOrder dbPurchaseOrder = listEm.find(PurchaseOrder.class, purchaseOrder.getId());

        BigDecimal purchaseOrderTotalAmount = dbPurchaseOrder.getTotalAmount();

        BigDecimal partial = BigDecimalUtil.subtract(purchaseOrderTotalAmount, oldTotalAmount);

        BigDecimal newPurchaseOrderTotalAmount = BigDecimalUtil.sum(partial, totalAmount);

        BigDecimal advancePayments = advancePaymentService.sumAllPaymentAmounts(purchaseOrder);

        log.debug("New detail total amount : " + totalAmount);
        log.debug("Old detail total amount : " + oldTotalAmount);
        log.debug("The actual purchase order total amount : " + purchaseOrderTotalAmount);
        log.debug("The new purchase order total amount : " + newPurchaseOrderTotalAmount);
        log.debug("Tha all advance payments : " + advancePayments);

        boolean result = newPurchaseOrderTotalAmount.compareTo(advancePayments) >= 0;

        if (!result) {
            BigDecimal lowerLimit = BigDecimalUtil.subtract(advancePayments, partial);
            log.debug("The lower limit : " + lowerLimit);
            throw new PurchaseOrderDetailTotalAmountException(lowerLimit);
        }

        return result;
    }

    public void deletePurchaseOrderDetail(PurchaseOrderDetail entity)
            throws PurchaseOrderFinalizedException,
            PurchaseOrderApprovedException,
            ReferentialIntegrityException,
            PurchaseOrderDetailNotFoundException,
            PurchaseOrderNullifiedException,
            ConcurrencyException,
            DiscountAmountException,
            PurchaseOrderLiquidatedException {

        findInDataBase(entity.getId());

        PurchaseOrder purchaseOrder = entity.getPurchaseOrder();

        if (warehousePurchaseOrderService.canChangePurchaseOrder(purchaseOrder)) {
            try {
                super.delete(entity);
            } catch (ConcurrencyException e) {
                throw new PurchaseOrderDetailNotFoundException(e);
            }
        }
        warehousePurchaseOrderService.updateWarehousePurchaseOrder(purchaseOrder);
    }

    @SuppressWarnings(value = "unchecked")
    public List<PurchaseOrderDetail> getPurchaseOrderDetailList(PurchaseOrder purchaseOrder) {
        return getEntityManager()
                .createNamedQuery("PurchaseOrderDetail.findByPurchaseOrder")
                .setParameter("purchaseOrder", purchaseOrder).getResultList();
    }

    public Boolean isPurchaseOrderDetailEmpty(PurchaseOrder purchaseOrder) {
        Long count = (Long) getEntityManager()
                .createNamedQuery("PurchaseOrderDetail.countByPurchaseOrder")
                .setParameter("purchaseOrder", purchaseOrder).getSingleResult();
        return count == null || count == 0;
    }

    private PurchaseOrderDetail findInDataBase(Long id) throws PurchaseOrderDetailNotFoundException {
        PurchaseOrderDetail purchaseOrderDetail = listEm.find(PurchaseOrderDetail.class, id);
        if (null == purchaseOrderDetail) {
            throw new PurchaseOrderDetailNotFoundException("Cannot find the PurchaseOrderDetail entity for id=" + id);
        }

        return purchaseOrderDetail;
    }

    private Long getNextDetailNumber(PurchaseOrder purchaseOrder) {
        Long currentDetailNumber = (Long) getEntityManager()
                .createNamedQuery("PurchaseOrderDetail.maxByPurchaseOrder")
                .setParameter("purchaseOrder", purchaseOrder).getSingleResult();
        if (null == currentDetailNumber) {
            currentDetailNumber = (long) 1;
        } else {
            currentDetailNumber++;
        }
        return currentDetailNumber;
    }

    private Boolean validateProductItemOnPurchaseOrder(PurchaseOrderDetail purchaseOrderDetail) {
        Long counter = purchaseOrderDetail.getId() == null ?
                (Long) listEm.createNamedQuery("PurchaseOrderDetail.countByProductItemAndPurchaseOrder")
                        .setParameter("purchaseOrder", purchaseOrderDetail.getPurchaseOrder())
                        .setParameter("productItemCode", purchaseOrderDetail.getProductItemCode()).getSingleResult() :
                (Long) listEm.createNamedQuery("PurchaseOrderDetail.countByProductItemAndPurchaseOrderDetail")
                        .setParameter("detail", purchaseOrderDetail)
                        .setParameter("purchaseOrder", purchaseOrderDetail.getPurchaseOrder())
                        .setParameter("productItemCode", purchaseOrderDetail.getProductItemCode()).getSingleResult();
        return counter == null || counter == 0;
    }
}
