package com.encens.khipus.service.warehouse;

import com.encens.khipus.framework.service.ExtendedGenericServiceBean;
import com.encens.khipus.model.production.MetaProduct;
import com.encens.khipus.model.warehouse.*;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;
import java.util.List;

import static com.encens.khipus.model.warehouse.ProductionTransferLogState.RECEIVED;

@Name("productionTransferLogService")
@Stateless
@AutoCreate
public class ProductionTransferLogServiceBean extends ExtendedGenericServiceBean implements ProductionTransferLogService {

    @Override
    public void prepareForReceiving(ProductionTransferLog transfer) {
        for(IncomingProductionOrder po : transfer.getIncomingProductionOrderList()) {
            if (po.getFinishedGoodsInventory() == null) {
                po.setFinishedGoodsInventory(new FinishedGoodsInventory());
            }
        }
    }

    @Override
    protected Object preUpdate(Object entity) {
        ProductionTransferLog transfer = (ProductionTransferLog)entity;
        checkRejectedProductionOrders(transfer);
        removeFakeFinishedGoodsInventories(transfer);
        return entity;
    }

    private void checkRejectedProductionOrders(ProductionTransferLog transfer) {
        if (transfer.getState() != RECEIVED) {
            return;
        }

        for(IncomingProductionOrder incoming : transfer.getIncomingProductionOrderList()) {
            if (incoming.getFinishedGoodsInventory().getWarehouseSlot() == null) {
                incoming.setFinishedGoodsInventory(null);
            }
        }
    }

    private void removeFakeFinishedGoodsInventories(ProductionTransferLog transfer) {
        if (transfer.getState() == RECEIVED) {
            return;
        }

        for(IncomingProductionOrder po : transfer.getIncomingProductionOrderList()) {
            if (po.getFinishedGoodsInventory() != null && po.getFinishedGoodsInventory().getId() == null) {
                po.setFinishedGoodsInventory(null);
            }
        }
    }

    @Override
    public List<WarehouseSlot> findWarehouseSlots(MetaProduct product) {
        return getEntityManager().createNamedQuery("WarehouseSlot.findByMetaProduct")
                                 .setParameter("metaProduct", product)
                                 .getResultList();
    }
}
