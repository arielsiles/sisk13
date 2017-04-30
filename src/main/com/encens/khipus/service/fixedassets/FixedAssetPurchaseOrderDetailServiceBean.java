package com.encens.khipus.service.fixedassets;

import com.encens.khipus.exception.ConcurrencyException;
import com.encens.khipus.exception.EntryDuplicatedException;
import com.encens.khipus.exception.ReferentialIntegrityException;
import com.encens.khipus.exception.purchase.*;
import com.encens.khipus.framework.service.GenericServiceBean;
import com.encens.khipus.model.fixedassets.Model;
import com.encens.khipus.model.fixedassets.PurchaseOrderDetailPart;
import com.encens.khipus.model.fixedassets.Trademark;
import com.encens.khipus.model.purchases.FixedAssetPurchaseOrderDetail;
import com.encens.khipus.model.purchases.PurchaseOrder;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * @author
 * @version 2.26
 */
@Stateless
@Name("fixedAssetPurchaseOrderDetailService")
@AutoCreate
public class FixedAssetPurchaseOrderDetailServiceBean extends GenericServiceBean implements FixedAssetPurchaseOrderDetailService {
    @In(value = "#{listEntityManager}")
    private EntityManager listEm;

    @In
    private TrademarkSynchronizeService trademarkSynchronizeService;

    @In
    private FixedAssetPurchaseOrderService fixedAssetPurchaseOrderService;

    @In
    private PurchaseOrderDetailPartService purchaseOrderDetailPartService;

    public void createFixedAssetPurchaseOrderDetail(FixedAssetPurchaseOrderDetail fixedAssetPurchaseOrderDetail,
                                                    PurchaseOrder purchaseOrder,
                                                    List<PurchaseOrderDetailPart> purchaseOrderDetailParts)
            throws PurchaseOrderApprovedException,
            PurchaseOrderFinalizedException,
            PurchaseOrderNullifiedException,
            ConcurrencyException,
            PurchaseOrderLiquidatedException {

        if (fixedAssetPurchaseOrderService.canChangePurchaseOrder(purchaseOrder)) {
            fixedAssetPurchaseOrderDetail.setPurchaseOrder(purchaseOrder);
            fixedAssetPurchaseOrderDetail.setDetailNumber(getNextDetailNumber(purchaseOrder));

            Trademark syncTrademark = trademarkSynchronizeService.synchronizeTrademark(
                    fixedAssetPurchaseOrderDetail.getTrademarkEntity(),
                    fixedAssetPurchaseOrderDetail.getTrademarkName());
            fixedAssetPurchaseOrderDetail.setTrademarkEntity(syncTrademark);
            if (null != syncTrademark) {
                fixedAssetPurchaseOrderDetail.setTrademark(syncTrademark.getName());
            } else {
                fixedAssetPurchaseOrderDetail.setTrademark(null);
            }

            Model syncModel = trademarkSynchronizeService.synchronizeModel(
                    fixedAssetPurchaseOrderDetail.getModelEntity(),
                    fixedAssetPurchaseOrderDetail.getModelName());
            fixedAssetPurchaseOrderDetail.setModelEntity(syncModel);
            if (null != syncModel) {
                fixedAssetPurchaseOrderDetail.setModel(syncModel.getName());
            } else {
                fixedAssetPurchaseOrderDetail.setModel(null);
            }

            try {
                super.create(fixedAssetPurchaseOrderDetail);
            } catch (EntryDuplicatedException e) {
                throw new RuntimeException("An Unexpected error has happened ", e);
            }

            getEntityManager().refresh(fixedAssetPurchaseOrderDetail);

            purchaseOrderDetailPartService.managePurchaseOrderDetailParts(fixedAssetPurchaseOrderDetail,
                    purchaseOrderDetailParts);
            try {
                fixedAssetPurchaseOrderService.updateFixedAssetPurchaseOrder(purchaseOrder, null);
            } catch (EntryDuplicatedException e) {
                log.error(e, "Un unexpected error has happened. Since the fixedAssetList is null this exception should never happen managing this list");
                ;
            }
        }
    }

    public FixedAssetPurchaseOrderDetail findFixedAssetPurchaseOrderDetail(Long id) throws PurchaseOrderDetailNotFoundException {
        findInDataBase(id);
        FixedAssetPurchaseOrderDetail fixedAssetPurchaseOrderDetail = getEntityManager().find(FixedAssetPurchaseOrderDetail.class, id);
        getEntityManager().refresh(fixedAssetPurchaseOrderDetail);

        fixedAssetPurchaseOrderDetail.putModelName();
        fixedAssetPurchaseOrderDetail.putTrademarkName();
        return fixedAssetPurchaseOrderDetail;
    }

    public void updatePurchaseOrder(FixedAssetPurchaseOrderDetail fixedAssetPurchaseOrderDetail,
                                    List<PurchaseOrderDetailPart> purchaseOrderDetailParts)
            throws PurchaseOrderFinalizedException,
            ConcurrencyException,
            PurchaseOrderDetailNotFoundException,
            PurchaseOrderApprovedException,
            PurchaseOrderNullifiedException,
            PurchaseOrderLiquidatedException {

        PurchaseOrder purchaseOrder = fixedAssetPurchaseOrderDetail.getPurchaseOrder();
        FixedAssetPurchaseOrderDetail databaseFixedAssetPurchaseOrderDetail = findInDataBase(fixedAssetPurchaseOrderDetail.getId());
        if (fixedAssetPurchaseOrderService.isPurchaseOrderFinalized(purchaseOrder)) {
            fixedAssetPurchaseOrderService.findPurchaseOrder(fixedAssetPurchaseOrderDetail.getId());
            throw new PurchaseOrderFinalizedException("The purchase order was already finalized, and cannot be changed");
        }

        Trademark syncTrademark = trademarkSynchronizeService.synchronizeTrademark(
                fixedAssetPurchaseOrderDetail.getTrademarkEntity(),
                fixedAssetPurchaseOrderDetail.getTrademarkName());
        fixedAssetPurchaseOrderDetail.setTrademarkEntity(syncTrademark);
        if (null != syncTrademark) {
            fixedAssetPurchaseOrderDetail.setTrademark(syncTrademark.getName());
        } else {
            fixedAssetPurchaseOrderDetail.setTrademark(null);
        }

        Model syncModel = trademarkSynchronizeService.synchronizeModel(
                fixedAssetPurchaseOrderDetail.getModelEntity(),
                fixedAssetPurchaseOrderDetail.getModelName());
        fixedAssetPurchaseOrderDetail.setModelEntity(syncModel);
        if (null != syncModel) {
            fixedAssetPurchaseOrderDetail.setModel(syncModel.getName());
        } else {
            fixedAssetPurchaseOrderDetail.setModel(null);
        }

        getEntityManager().merge(fixedAssetPurchaseOrderDetail);
        getEntityManager().flush();
        purchaseOrderDetailPartService.managePurchaseOrderDetailParts(fixedAssetPurchaseOrderDetail,
                purchaseOrderDetailParts);
        //update purchase order total amounts only if it is approved
        if (fixedAssetPurchaseOrderService.isPurchaseOrderApproved(purchaseOrder)) {
            purchaseOrder = fixedAssetPurchaseOrderService.updateTotalAmountFields(purchaseOrder);
            getEntityManager().merge(purchaseOrder);
            getEntityManager().flush();
            getEntityManager().refresh(purchaseOrder);
        }
        try {
            fixedAssetPurchaseOrderService.updateFixedAssetPurchaseOrder(purchaseOrder, null);
        } catch (EntryDuplicatedException e) {
            log.error(e, "Un unexpected error has happened. Since the fixedAssetList is null this exception should never happen managing this list");
        }
    }

    private BigDecimal calculateFixedAssetPurchaseOrderNetTotalAmount(PurchaseOrder purchaseOrder) {
        BigDecimal total = (BigDecimal) getEntityManager()
                .createNamedQuery("FixedAssetPurchaseOrderDetail.sumBsTotalAmounts")
                .setParameter("purchaseOrder", purchaseOrder)
                .getSingleResult();
        if (null == total) {
            total = BigDecimal.ZERO;
        }
        return total;
    }

    public void deletePurchaseOrder(FixedAssetPurchaseOrderDetail entity)
            throws PurchaseOrderFinalizedException,
            PurchaseOrderApprovedException,
            ReferentialIntegrityException,
            PurchaseOrderDetailNotFoundException,
            PurchaseOrderNullifiedException,
            ConcurrencyException,
            PurchaseOrderLiquidatedException {

        findInDataBase(entity.getId());

        PurchaseOrder purchaseOrder = entity.getPurchaseOrder();

        if (fixedAssetPurchaseOrderService.canChangePurchaseOrder(purchaseOrder)) {
            purchaseOrderDetailPartService.managePurchaseOrderDetailParts(entity,
                    new ArrayList<PurchaseOrderDetailPart>());
            try {
                super.delete(entity);
            } catch (ConcurrencyException e) {
                throw new PurchaseOrderDetailNotFoundException(e);
            }
            try {
                fixedAssetPurchaseOrderService.updateFixedAssetPurchaseOrder(purchaseOrder, null);
            } catch (EntryDuplicatedException e) {
                log.error(e, "Un unexpected error has happened. Since the fixedAssetList is null this exception should never happen managing this list");
                ;
            }
        }
    }


    @SuppressWarnings(value = "unchecked")
    public List<FixedAssetPurchaseOrderDetail> getFixedAssetPurchaseOrderDetailList(PurchaseOrder purchaseOrder) {
        return getEntityManager()
                .createNamedQuery("FixedAssetPurchaseOrderDetail.findByPurchaseOrder")
                .setParameter("purchaseOrder", purchaseOrder).getResultList();
    }

    public Boolean isPurchaseOrderDetailEmpty(PurchaseOrder purchaseOrder) {
        Long count = (Long) getEntityManager()
                .createNamedQuery("FixedAssetPurchaseOrderDetail.countByPurchaseOrder")
                .setParameter("purchaseOrder", purchaseOrder).getSingleResult();
        return count == null || count == 0;
    }

    private FixedAssetPurchaseOrderDetail findInDataBase(Long id) throws PurchaseOrderDetailNotFoundException {
        FixedAssetPurchaseOrderDetail fixedAssetPurchaseOrderDetail = listEm.find(FixedAssetPurchaseOrderDetail.class, id);
        if (null == fixedAssetPurchaseOrderDetail) {
            throw new PurchaseOrderDetailNotFoundException("Cannot find the FixedAssetPurchaseOrderDetail entity for id=" + id);
        }

        return fixedAssetPurchaseOrderDetail;
    }

    private Long getNextDetailNumber(PurchaseOrder purchaseOrder) {
        Long currentDetailNumber = (Long) getEntityManager()
                .createNamedQuery("FixedAssetPurchaseOrderDetail.maxByPurchaseOrder")
                .setParameter("purchaseOrder", purchaseOrder).getSingleResult();
        if (null == currentDetailNumber) {
            currentDetailNumber = (long) 1;
        } else {
            currentDetailNumber++;
        }
        return currentDetailNumber;
    }

}
