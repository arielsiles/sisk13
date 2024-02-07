package com.encens.khipus.action.xproduction;

import com.encens.khipus.action.warehouse.WarehouseVoucherCreateAction;
import com.encens.khipus.action.warehouse.WarehouseVoucherUpdateAction;
import com.encens.khipus.exception.EntryNotFoundException;
import com.encens.khipus.exception.warehouse.InventoryException;
import com.encens.khipus.exception.warehouse.ProductItemNotFoundException;
import com.encens.khipus.exception.warehouse.WarehouseVoucherApprovedException;
import com.encens.khipus.exception.warehouse.WarehouseVoucherNotFoundException;
import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.framework.action.Outcome;
import com.encens.khipus.framework.service.GenericService;
import com.encens.khipus.model.employees.Employee;
import com.encens.khipus.model.finances.CostCenter;
import com.encens.khipus.model.xproduction.CollectionForm;
import com.encens.khipus.model.xproduction.CollectionFormState;
import com.encens.khipus.model.xproduction.CollectionRecord;
import com.encens.khipus.model.warehouse.*;
import com.encens.khipus.service.admin.BusinessUnitService;
import com.encens.khipus.service.finances.CostCenterService;
import com.encens.khipus.service.xproduction.CollectionFormService;
import com.encens.khipus.service.warehouse.MonthProcessService;
import com.encens.khipus.service.warehouse.ProductItemService;
import com.encens.khipus.service.warehouse.WarehousePurchaseOrderService;
import com.encens.khipus.service.warehouse.WarehouseService;
import com.encens.khipus.util.Constants;
import com.encens.khipus.util.DateUtils;
import com.encens.khipus.util.MessageUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;
import org.jboss.seam.international.StatusMessage;
import org.jboss.seam.log.Log;

import javax.faces.event.ActionEvent;
import java.math.BigDecimal;
import java.util.*;

import static org.jboss.seam.international.StatusMessage.Severity.ERROR;

@Name("collectionFormAction")
@Scope(ScopeType.CONVERSATION)
public class CollectionFormAction extends GenericAction<CollectionForm> {

    @In("CollectionFormService")
    private CollectionFormService collectionFormService;

    @In
    private BusinessUnitService businessUnitService;

    private WarehousePurchaseOrderService warehousePurchaseOrderService;

    @In
    private CostCenterService costCenterService;

    @In
    private WarehouseService warehouseService;

    @In
    private ProductItemService productItemService;

    @In(value = "monthProcessService")
    private MonthProcessService monthProcessService;

    @In(create = true)
    private WarehouseVoucherCreateAction warehouseVoucherCreateAction;

    @In(value = "warehouseVoucherUpdateAction")
    private WarehouseVoucherUpdateAction warehouseVoucherUpdateAction;


    @Logger
    private Log log;

    @Override
    protected GenericService getService() {
        return collectionFormService;
    }

    @Factory(value = "collectionForm", scope = ScopeType.STATELESS)
    public CollectionForm initCollectionForm() {
        return getInstance();
    }

    @Begin(ifOutcome = Outcome.SUCCESS, flushMode = FlushModeType.MANUAL)
    @Override
    public String select(CollectionForm instance) {

        try {
            collectionFormService.updateProductiveZone(getService().findById(getEntityClass(), getId(instance)));
            setOp(OP_UPDATE);
            //define the unmanaged instance as current instance
            this.setInstance(instance);
            //Ensure the instance exists in the database, find it
            setInstance(getService().findById(getEntityClass(), getId(instance)));
            return Outcome.SUCCESS;

        } catch (EntryNotFoundException e) {
            addNotFoundMessage();
            return Outcome.FAIL;
        }
    }

    @Override
    protected String getDisplayNameProperty() {
        return "date";
    }

    @Override
    public CollectionForm createInstance() {
        CollectionForm form = super.createInstance();

        try {
            collectionFormService.populateWithCollectionRecords(form);
        } catch (Exception ex) {
            recordUnexpectedException(ex);
        }
        return form;
    }

    private void recordUnexpectedException(Exception ex) {
        log.error("Exception caught", ex);
        facesMessages.addFromResourceBundle(ERROR, "Common.globalError.description");
    }

    public void recalculateTotalAmounts(ActionEvent e) {
        CollectionForm collectionForm = getInstance();
        log.info("recalculating totals");
        if (collectionForm.getDate() == null || collectionForm.getMetaProduct() == null) {
            log.info("Canceling calculus of totals {0}, {1}", collectionForm.getDate(), collectionForm.getMetaProduct());
            return;
        }

        try {
            collectionFormService.populateWithTotalsOfCollectedAmount(getInstance());
            collectionFormService.populateWithTotalsOfRejectedAmount(getInstance());
        } catch (Exception ex) {
            recordUnexpectedException(ex);
        }
    }

    @Begin(ifOutcome = Outcome.SUCCESS, flushMode = FlushModeType.MANUAL)
    public String startCreate() {
        return Outcome.SUCCESS;
    }

    public double getTotalWeightedAmount() {
        double total = 0.0;
        for (CollectionRecord record : getInstance().getCollectionRecordList()) {
            total += record.getWeightedAmount();
        }
        getInstance().setTotalWeighed(total);
        return total;
    }

    public double getTotalRejectedAmount() {
        double total = 0.0;
        for (CollectionRecord record : getInstance().getCollectionRecordList()) {
            total += record.getRejectedAmount();
        }
        return total;
    }

    public double getTotalReceivedAmount() {
        double total = 0.0;
        for (CollectionRecord record : getInstance().getCollectionRecordList()) {
            total += record.getReceivedAmount();
        }
        return total;
    }

    public String createVoucherApproved() throws InventoryException, ProductItemNotFoundException {

        if(this.getTotalWeightedAmount() == 0.0)
        {
            addTotalWeightedAmountIsZeroMessage();
            return Outcome.REDISPLAY;
        }
        Calendar dateColleted = Calendar.getInstance();
        dateColleted.setTime(getInstance().getDate());
        Calendar now = Calendar.getInstance();
        now.setTime(new Date());

        if(dateColleted.get(Calendar.MONTH) != now.get(Calendar.MONTH))
        {
            addOutsideGestionMessage();
            return Outcome.REDISPLAY;
        }

        WarehouseDocumentType warehouseDocumentType = collectionFormService.getFirstReceptionType();
        CostCenter publicCostCenter = costCenterService.findCostCenterByCode(Constants.DEFAULT_COST_CENTER_PRODUCTION);
        Warehouse warehouse = warehouseService.findWarehouseByCode(Constants.DEFAULT_CODE_WAREHOUSE_COLLECTION_MILK);

        Employee responsible = warehouse.getResponsible();
        String warehouseVoucherDescription = MessageUtils.getMessage("CollectionForm.warehouseVoucher.description") + DateUtils.format(getInstance().getDate(), MessageUtils.getMessage("patterns.date"));
        //Create the WarehouseVoucher
        WarehouseVoucher warehouseVoucher = new WarehouseVoucher();
        warehouseVoucher.setDocumentType(warehouseDocumentType);
        warehouseVoucher.setWarehouse(warehouse);
        warehouseVoucher.setState(WarehouseVoucherState.PEN);
        warehouseVoucher.setExecutorUnit(warehouse.getExecutorUnit());
        warehouseVoucher.setCostCenterCode(publicCostCenter.getId().getCode());
        warehouseVoucher.setResponsible(responsible);
        //warehouseVoucher.setDate(monthProcessService.getMothProcessDate(new Date()));
        warehouseVoucher.setDate(getInstance().getDate());

        InventoryMovement inventoryMovement = new InventoryMovement();
        inventoryMovement.setDescription(warehouseVoucherDescription);

        warehouseService.createWarehouseVoucher(warehouseVoucher, inventoryMovement, null, null, null, null);

        //Create the MovementDetails
        ProductItem productItem = productItemService.findProductItemByCode(Constants.ID_ART_RAW_MILK);
        BigDecimal quantity = new BigDecimal(getTotalWeightedAmount());
        BigDecimal unitCost = productItemService.findProductItemByCode(Constants.ID_ART_RAW_MILK).getUnitCost();

        MovementDetail movementDetailTemp = new MovementDetail();
        movementDetailTemp.setWarehouse(warehouse);
        movementDetailTemp.setProductItem(productItem);
        movementDetailTemp.setProductItemAccount(productItem.getProductItemAccount());
        movementDetailTemp.setQuantity(quantity);
        movementDetailTemp.setUnitCost(productItem.getUnitCost());
        movementDetailTemp.setAmount(quantity.multiply(unitCost));
        movementDetailTemp.setExecutorUnit(warehouse.getExecutorUnit());
        movementDetailTemp.setCostCenterCode(publicCostCenter.getId().getCode());
        movementDetailTemp.setMeasureUnit(productItem.getUsageMeasureUnit());

        inventoryMovement.getMovementDetailList().add(movementDetailTemp);
        movementDetailTemp.setInventoryMovement(inventoryMovement);


        Map<MovementDetail, BigDecimal> movementDetailUnderMinimalStockMap = new HashMap<MovementDetail, BigDecimal>();
        movementDetailUnderMinimalStockMap.put(movementDetailTemp, productItem.getMinimalStock());

        Map<MovementDetail, BigDecimal> movementDetailOverMaximumStockMap = new HashMap<MovementDetail, BigDecimal>();
        movementDetailOverMaximumStockMap.put(movementDetailTemp, productItem.getMaximumStock());

        List<MovementDetail> movementDetailWithoutWarnings = new ArrayList<MovementDetail>();
        movementDetailWithoutWarnings.add(movementDetailTemp);

        try {
            warehouseService.createMovementDetail(warehouseVoucher, movementDetailTemp, movementDetailUnderMinimalStockMap, movementDetailOverMaximumStockMap, movementDetailWithoutWarnings);
        } catch (WarehouseVoucherApprovedException e) {
            log.debug("This exception never happen because I just created a new WarehouseVoucher" +
                    " and his state is pending");
        } catch (WarehouseVoucherNotFoundException e) {
            log.debug("This exception never happen because I just created a new WarehouseVoucher");
        }

        try {
            warehouseVoucherUpdateAction.readWarehouseVoucher(warehouseVoucher.getId());
        } catch (Exception e) {
        }

        warehouseVoucherUpdateAction.approveFromCollection();
        getInstance().setState(CollectionFormState.APR);
        this.update();
        return Outcome.SUCCESS;
    }

    private void addOutsideGestionMessage() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,"CollectionForm.approve.OutsideGestion");
    }

    private void addTotalWeightedAmountIsZeroMessage() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,"CollectionForm.approve.TotalWeightedAmountIsZero");
    }

    public boolean isPending() {
        return CollectionFormState.PEN.equals(getInstance().getState());
    }

}
