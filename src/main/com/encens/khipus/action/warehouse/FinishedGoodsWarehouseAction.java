package com.encens.khipus.action.warehouse;

import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.framework.action.Outcome;
import com.encens.khipus.framework.service.GenericService;
import com.encens.khipus.model.production.MetaProduct;
import com.encens.khipus.model.warehouse.FinishedGoodsWarehouse;
import com.encens.khipus.model.warehouse.WarehouseSlot;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;

import static org.jboss.seam.international.StatusMessage.Severity.ERROR;

@Name("finishedGoodsWarehouseAction")
@Scope(ScopeType.CONVERSATION)
public class FinishedGoodsWarehouseAction extends GenericAction<FinishedGoodsWarehouse> {

    private int auxiliarityKey = 0;

    @In("extendedGenericService")
    private GenericService extendedGenericService;

    @Override
    protected GenericService getService() {
        return extendedGenericService;
    }

    private int generateAuxiliarityKey() {
        return ++auxiliarityKey;
    }

    @Factory(value = "finishedGoodsWarehouse", scope = ScopeType.STATELESS)
    public FinishedGoodsWarehouse initFinishedGoodsWarehouse() {
        return getInstance();
    }

    @Override
    @Begin(ifOutcome = Outcome.SUCCESS, flushMode = FlushModeType.MANUAL)
    public String select(FinishedGoodsWarehouse instance) {
        String outcome = super.select(instance);
        if (outcome == Outcome.SUCCESS) {
            for(WarehouseSlot slot : getInstance().getWarehouseSlotList()) {
                slot.setAuxiliarityKey(generateAuxiliarityKey());
            }
        }
        return outcome;
    }

    @Override
    protected String getDisplayNameProperty() {
        return "name";
    }

    @Begin(flushMode = FlushModeType.MANUAL, ifOutcome = Outcome.SUCCESS)
    public String startCreate() {
        return Outcome.SUCCESS;
    }

    public void selectProduct(MetaProduct metaProduct) {
        try {
            metaProduct = getService().findById(MetaProduct.class, metaProduct.getId());

            WarehouseSlot warehouseSlot = new WarehouseSlot();
            warehouseSlot.setAuxiliarityKey(generateAuxiliarityKey());
            warehouseSlot.setMetaProduct(metaProduct);
            warehouseSlot.setFinishedGoodsWarehouse(getInstance());
            getInstance().getWarehouseSlotList().add(warehouseSlot);
        } catch (Exception ex) {
            recordAsUnexpected(ex);
        }
    }

    public void removeWarehouseSlot(WarehouseSlot warehouseSlot) {
        try {
            WarehouseSlot key = null;
            for (WarehouseSlot slot : getInstance().getWarehouseSlotList()) {
                if (slot.getAuxiliarityKey() == warehouseSlot.getAuxiliarityKey()) {
                    key = slot;
                }
            }
            getInstance().getWarehouseSlotList().remove(key);
        } catch (Exception ex) {
            recordAsUnexpected(ex);
        }
    }

    private void recordAsUnexpected(Exception ex) {
        log.error("Unexpected exception caught", ex);
        facesMessages.addFromResourceBundle(ERROR, "Common.globalError.description");
    }
}
