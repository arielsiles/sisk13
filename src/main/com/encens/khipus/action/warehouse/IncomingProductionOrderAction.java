package com.encens.khipus.action.warehouse;

import com.encens.khipus.exception.EntryDuplicatedException;
import com.encens.khipus.exception.EntryNotFoundException;
import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.framework.action.Outcome;
import com.encens.khipus.model.production.ProductionOrder;
import com.encens.khipus.model.warehouse.FinishedGoodsInventory;
import com.encens.khipus.model.warehouse.FinishedGoodsWarehouse;
import com.encens.khipus.model.warehouse.IncomingProductionOrder;
import com.encens.khipus.model.warehouse.WarehouseSlot;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;

@Name("incomingProductionOrderAction")
@Scope(ScopeType.CONVERSATION)
public class IncomingProductionOrderAction extends GenericAction<IncomingProductionOrder> {

    private ProductionOrder productionOrder;
    private FinishedGoodsWarehouse warehouse;

    public boolean getManaged() { return false; }

    @Factory(value = "incomingProductionOrder", scope = ScopeType.STATELESS)
    public IncomingProductionOrder initIncomigProductionOrder() {
        return getInstance();
    }

    @Override
    public IncomingProductionOrder createInstance() {
        IncomingProductionOrder incoming = super.createInstance();
        incoming.setFinishedGoodsInventory(new FinishedGoodsInventory());
        return incoming;
    }

    @Override
    protected String getDisplayNameProperty() {
        return "productionOrderCode";
    }

    @Begin(flushMode = FlushModeType.MANUAL, ifOutcome = Outcome.SUCCESS)
    public String select(ProductionOrder productionOrder) {
        try {
            this.productionOrder = getService().findById(ProductionOrder.class, productionOrder.getId());
            //getInstance().setProductionOrderCode(this.productionOrder.getCode());
            return Outcome.SUCCESS;
        } catch (EntryNotFoundException e) {
            e.printStackTrace();
            return Outcome.REDISPLAY;
        }
    }

    @End
    public String save() {
        prepareInstanceForSaving();
        return makeSave();
    }

    private void prepareInstanceForSaving() {
        WarehouseSlot slot = new WarehouseSlot();
        slot.setMetaProduct(productionOrder.getProductComposition().getProcessedProduct());
        slot.setFinishedGoodsWarehouse(warehouse);

        for(WarehouseSlot s : warehouse.getWarehouseSlotList()) {
            if (s.getMetaProduct().getId().equals(productionOrder.getProductComposition().getProcessedProduct().getId())) {
                slot = s;
            }
        }

        getInstance().getFinishedGoodsInventory().setWarehouseSlot(slot);
    }

    private String makeSave() {
        try {
            genericService.create(getInstance());
            return Outcome.SUCCESS;
        } catch (EntryDuplicatedException e) {
            log.error(e);
            e.printStackTrace();
            addDuplicatedMessage();
            return Outcome.REDISPLAY;
        }
    }

    @End
    public String cancel() {
        return Outcome.SUCCESS;
    }

    public ProductionOrder getProductionOrder() {
        return productionOrder;
    }

    public FinishedGoodsWarehouse getWarehouse() {
        return warehouse;
    }

    public void setWarehouse(FinishedGoodsWarehouse warehouse) {
        this.warehouse = warehouse;
    }
}
