package com.encens.khipus.action.warehouse;

import com.encens.khipus.action.SessionUser;
import com.encens.khipus.exception.EntryNotFoundException;
import com.encens.khipus.exception.warehouse.ProductItemNotFoundException;
import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.framework.action.Outcome;
import com.encens.khipus.framework.service.GenericService;
import com.encens.khipus.model.employees.Charge;
import com.encens.khipus.model.employees.Employee;
import com.encens.khipus.model.finances.CostCenter;
import com.encens.khipus.model.finances.JobContract;
import com.encens.khipus.model.warehouse.*;
import com.encens.khipus.service.employees.JobContractService;
import com.encens.khipus.service.warehouse.InventoryService;
import com.encens.khipus.service.warehouse.WarehouseService;
import com.encens.khipus.util.BigDecimalUtil;
import com.encens.khipus.util.FormatUtils;
import com.encens.khipus.util.MessageUtils;
import com.encens.khipus.util.warehouse.InventoryMessage;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.international.StatusMessage;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author
 * @version 3.0
 */
@Name("warehouseVoucherGeneralAction")

public class WarehouseVoucherGeneralAction extends GenericAction<WarehouseVoucher> {
    @In
    protected WarehouseService warehouseService;

    @In
    private JobContractService jobContractService;

    @In
    private SessionUser sessionUser;

    @In
    private InventoryService inventoryService;

    private static final Integer SCALE = 6;

    protected WarehouseVoucher warehouseVoucher = new WarehouseVoucher();

    protected InventoryMovement inventoryMovement = new InventoryMovement();

    protected MovementDetail movementDetail = new MovementDetail();

    // this map stores the MovementDetails that are under the minimal stock and the unitaryBalance of the Inventory
    protected Map<MovementDetail, BigDecimal> movementDetailUnderMinimalStockMap = new HashMap<MovementDetail, BigDecimal>();
    // this map stores the MovementDetails that are over the maximum stock and the unitaryBalance of the Inventory
    protected Map<MovementDetail, BigDecimal> movementDetailOverMaximumStockMap = new HashMap<MovementDetail, BigDecimal>();
    // this list stores the MovementDetails that should not show warnings
    protected List<MovementDetail> movementDetailWithoutWarnings = new ArrayList<MovementDetail>();

    @Override
    protected GenericService getService() {
        return warehouseService;
    }


    public List<String> getElementsToReReder() {
        List<String> list = new ArrayList<String>();
        if (warehouseVoucher.isExecutorUnitTransfer()) {
            list.add("executorUnitCodeField");
            list.add("warehousePanel");
        } else {
            list.add("executorUnitCodeField");
            list.add("warehousePanel");
            list.add("targetWarehousePanel");
        }
        return list;
    }

    public List<String> getElementsToReRenderForPetitionerJobContractChange() {
        List<String> list = new ArrayList<String>();
        list.add("petitionerJobContractField");
        list.add("jobContractDiv");
        list.add("costCenterCodeField");
        list.add("executorUnitField");
        list.addAll(getElementsToReReder());

        for (String s : list) {
            log.debug("item " + s);
        }
        return list;
    }

    private Employee readEmployee(Long employeeId) {
        try {
            return genericService.findById(Employee.class, employeeId);
        } catch (EntryNotFoundException e) {
            log.debug("Cannot find Employee entity for id=" + employeeId);
        }

        return null;
    }

    protected String validateExecutorUnitTransferenceVoucher() {
        String validationOutcome = Outcome.SUCCESS;

        if (warehouseVoucher.isExecutorUnitTransfer()) {

            String sourceExecutorUnitCode = warehouseVoucher.getExecutorUnit().getExecutorUnitCode();
            String targetExecutorUnitCode = warehouseVoucher.getTargetExecutorUnit().getExecutorUnitCode();

            if (sourceExecutorUnitCode.equals(targetExecutorUnitCode)) {
                facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                        "WarehouseVoucher.error.fieldSelectedtwice",
                        MessageUtils.getMessage("WarehouseVoucher.sourceExecutorUnitCode"),
                        MessageUtils.getMessage("WarehouseVoucher.targetExecutorUnitCode"));
                validationOutcome = Outcome.REDISPLAY;
            }

            if (!isValidCostCenter(warehouseVoucher.getCostCenter(), "WarehouseVoucher.sourceCostCenterCode")) {
                validationOutcome = Outcome.REDISPLAY;
            }

            if (!isValidCostCenter(warehouseVoucher.getTargetCostCenter(), "WarehouseVoucher.targetCostCenterCode")) {
                validationOutcome = Outcome.REDISPLAY;
            }

            if (!isValidWarehouse(warehouseVoucher.getWarehouse(), "WarehouseVoucher.sourceWarehouse")) {
                validationOutcome = Outcome.REDISPLAY;
            }

            if (!isValidWarehouse(warehouseVoucher.getTargetWarehouse(), "WarehouseVoucher.targetWarehouse")) {
                validationOutcome = Outcome.REDISPLAY;
            }
        }
        return validationOutcome;
    }


    protected String validateTransferenceVoucher() {
        String validationOutcome = Outcome.SUCCESS;

        if (warehouseVoucher.isTransfer()) {

            if (!isValidCostCenter(warehouseVoucher.getCostCenter(), "WarehouseVoucher.costCenter")) {
                validationOutcome = Outcome.REDISPLAY;
            }

            if (null != warehouseVoucher.getWarehouse() && null != warehouseVoucher.getTargetWarehouse()) {
                if (warehouseVoucher.getWarehouse().getId().equals(warehouseVoucher.getTargetWarehouse().getId())) {
                    facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                            "WarehouseVoucher.error.fieldSelectedtwice",
                            MessageUtils.getMessage("WarehouseVoucher.sourceWarehouse"),
                            MessageUtils.getMessage("WarehouseVoucher.targetWarehouse"));
                    validationOutcome = Outcome.REDISPLAY;
                }
            } else {
                if (!isValidWarehouse(warehouseVoucher.getWarehouse(), "WarehouseVoucher.sourceWarehouse")) {
                    validationOutcome = Outcome.REDISPLAY;
                }

                if (!isValidWarehouse(warehouseVoucher.getTargetWarehouse(), "WarehouseVoucher.targetWarehouse")) {
                    validationOutcome = Outcome.REDISPLAY;
                }
            }
        }

        return validationOutcome;
    }

    protected String validateReceptionVoucher() {
        String validationOutcome = Outcome.SUCCESS;

        if (warehouseVoucher.isInput() || warehouseVoucher.isReception() || warehouseVoucher.isDevolution()) {
            if (!isValidCostCenter(warehouseVoucher.getCostCenter(), "WarehouseVoucher.costCenter")) {
                validationOutcome = Outcome.REDISPLAY;
            }

            if (!isValidWarehouse(warehouseVoucher.getWarehouse(), "WarehouseVoucher.warehouse")) {
                validationOutcome = Outcome.REDISPLAY;
            }
        }

        return validationOutcome;
    }

    protected String validateConsumptionVoucher() {
        String validationOutcome = Outcome.SUCCESS;

        if (warehouseVoucher.isConsumption() || warehouseVoucher.isOutput()) {
            if (!isValidCostCenter(warehouseVoucher.getCostCenter(), "WarehouseVoucher.costCenter")) {
                validationOutcome = Outcome.REDISPLAY;
            }

            if (!isValidWarehouse(warehouseVoucher.getWarehouse(), "WarehouseVoucher.warehouse")) {
                validationOutcome = Outcome.REDISPLAY;
            }

            if (null == warehouseVoucher.getResponsible()) {
                facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                        "Common.required", MessageUtils.getMessage("WarehouseVoucher.responsible"));
                validationOutcome = Outcome.REDISPLAY;
            }
        }

        return validationOutcome;
    }

    protected String validateOutputVoucher() {
        String validationOutcome = Outcome.SUCCESS;

        if (!isValidPetitioner(warehouseVoucher.getPetitionerJobContract(), "WarehouseVoucher.petitioner")) {
            validationOutcome = Outcome.REDISPLAY;
        }

        return validationOutcome;
    }

    private boolean isValidCostCenter(CostCenter costCenter, String resourceKey) {
        if (null == costCenter) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "Common.required", MessageUtils.getMessage(resourceKey));
            return false;
        }

        return true;
    }

    private boolean isValidWarehouse(Warehouse warehouse, String resourceKey) {
        if (null == warehouse) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "Common.required", MessageUtils.getMessage(resourceKey));
            return false;
        }

        return true;
    }


    private boolean isValidPetitioner(JobContract jobContract, String resourceKey) {
        if (null == jobContract) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "Common.required", MessageUtils.getMessage(resourceKey));
            return false;
        }

        return true;
    }

    private boolean isValidCharge(Charge charge, String resourceKey) {
        if (null == charge) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "Common.required", MessageUtils.getMessage(resourceKey));
            return false;
        }

        return true;
    }


    public void loadPetitionerJobContractValues() {
        if (warehouseVoucher.getPetitionerJobContract() != null) {
            warehouseVoucher.setPetitionerJobContract(jobContractService.load(warehouseVoucher.getPetitionerJobContract()));
            warehouseVoucher.setExecutorUnit(warehouseVoucher.getPetitionerJobContract().getJob().getOrganizationalUnit().getBusinessUnit());
            warehouseVoucher.setCostCenter(warehouseVoucher.getPetitionerJobContract().getJob().getOrganizationalUnit().getCostCenter());
        }
        clearWarehouses();
    }

    public void clearPetitionerJobContract() {
        warehouseVoucher.setPetitionerJobContract(null);
        warehouseVoucher.setExecutorUnit(null);
        warehouseVoucher.setCostCenter(null);
        clearWarehouses();
    }

    public void buildValidateQuantityMappings(MovementDetail movementDetail) throws ProductItemNotFoundException {
        BigDecimal requiredQuantity = movementDetail.getQuantity();
        if (null != requiredQuantity) {
            ProductItem productItem = null;
            try {
                productItem = getService().findById(ProductItem.class, movementDetail.getProductItem().getId(), true);
            } catch (EntryNotFoundException e) {
                throw new ProductItemNotFoundException(productItem);
            }
            Warehouse warehouse = movementDetail.getWarehouse();
            BigDecimal minimalStock = productItem.getMinimalStock();
            BigDecimal maximumStock = productItem.getMaximumStock();
            BigDecimal unitaryBalance = inventoryService.findUnitaryBalanceByProductItemAndArticle(warehouse.getId(), productItem.getId());
            BigDecimal totalQuantity = movementDetail.getMovementType().equals(MovementDetailType.E) ?
                    BigDecimalUtil.sum(unitaryBalance, requiredQuantity, SCALE) :
                    BigDecimalUtil.subtract(unitaryBalance, requiredQuantity, SCALE);
            // by default does not show warning until is verified
            boolean showWarning = false;

            if (null != minimalStock) {
                // minimalStock is not null
                int minimalComparison = totalQuantity.compareTo(minimalStock);
                if (minimalComparison < 0) {
                    // if under minimalStock
                    this.movementDetailUnderMinimalStockMap.put(movementDetail, unitaryBalance);
                    showWarning = true;
                }
            }
            if (null != maximumStock) {
                // maximumStock is not null
                int maximumComparison = totalQuantity.compareTo(maximumStock);
                if (maximumComparison > 0) {
                    // if over maximumStock
                    this.movementDetailOverMaximumStockMap.put(movementDetail, unitaryBalance);
                    showWarning = true;
                }
            }
            if (!showWarning) {
                movementDetailWithoutWarnings.add(movementDetail);
            }
        }
    }

    /**
     * Shows the warnings attribute according to the Maps and List mappings
     */
    public void showMovementDetailWarningMessages() {
        for (Map.Entry<MovementDetail, BigDecimal> movementDetailBigDecimalEntry : movementDetailUnderMinimalStockMap.entrySet()) {
            MovementDetail movementDetail = movementDetailBigDecimalEntry.getKey();
            // if under minimal Stock
            facesMessages.addFromResourceBundle(StatusMessage.Severity.WARN,
                    "MovementDetail.warning.underMinimalStock",
                    movementDetail.getMovementType().equals(MovementDetailType.E) ? messages.get("Common.math.sum") : messages.get("Common.math.subtraction"),
                    FormatUtils.formatNumber(movementDetailBigDecimalEntry.getValue(), messages.get("patterns.decimal6FNumber"), sessionUser.getLocale()),
                    FormatUtils.formatNumber(movementDetail.getQuantity(), messages.get("patterns.decimal6FNumber"), sessionUser.getLocale()),
                    movementDetail.getProductItem().getFullName(),
                    FormatUtils.formatNumber(movementDetail.getProductItem().getMinimalStock(), messages.get("patterns.decimal6FNumber"), sessionUser.getLocale()),
                    movementDetail.getWarehouse().getFullName());
        }
        for (Map.Entry<MovementDetail, BigDecimal> movementDetailBigDecimalEntry : movementDetailOverMaximumStockMap.entrySet()) {
            MovementDetail movementDetail = movementDetailBigDecimalEntry.getKey();
            // if over maximumStock
            facesMessages.addFromResourceBundle(StatusMessage.Severity.WARN,
                    "MovementDetail.warning.overMaximumStock",
                    movementDetail.getMovementType().equals(MovementDetailType.E) ? messages.get("Common.math.sum") : messages.get("Common.math.subtraction"),
                    FormatUtils.formatNumber(movementDetailBigDecimalEntry.getValue(), messages.get("patterns.decimal6FNumber"), sessionUser.getLocale()),
                    FormatUtils.formatNumber(movementDetail.getQuantity(), messages.get("patterns.decimal6FNumber"), sessionUser.getLocale()),
                    movementDetail.getProductItem().getFullName(),
                    FormatUtils.formatNumber(movementDetail.getProductItem().getMaximumStock(), messages.get("patterns.decimal6FNumber"), sessionUser.getLocale()),
                    movementDetail.getWarehouse().getFullName());
        }
    }


    /**
     * Cleans the movementDetail validation mappings
     */
    public void resetValidateQuantityMappings() {
        movementDetailUnderMinimalStockMap = new HashMap<MovementDetail, BigDecimal>();
        movementDetailOverMaximumStockMap = new HashMap<MovementDetail, BigDecimal>();
        movementDetailWithoutWarnings = new ArrayList<MovementDetail>();
    }


    /* state helpers */
    public boolean isTransference() {
        return warehouseVoucher.isTransfer();
    }

    public boolean isExecutorUnitTransference() {
        return warehouseVoucher.isExecutorUnitTransfer();
    }

    public boolean isReception() {
        return warehouseVoucher.isReception();
    }

    public boolean isConsumption() {
        return warehouseVoucher.isConsumption();
    }

    public boolean isOutput() {
        return warehouseVoucher.isOutput();
    }

    public boolean isExecutorUnitFieldSelected() {
        return null != warehouseVoucher.getExecutorUnit();
    }

    public boolean isTargetExecutorUnitFieldSelected() {
        return null != warehouseVoucher.getTargetExecutorUnit();
    }

    public boolean isEnabledResponsibleField() {
        return warehouseVoucher.isConsumption() || warehouseVoucher.isOutput();
    }

    /* getters and setters */
    public WarehouseVoucher getWarehouseVoucher() {
        return warehouseVoucher;
    }

    public void setWarehouseVoucher(WarehouseVoucher warehouseVoucher) {
        this.warehouseVoucher = warehouseVoucher;
    }

    public InventoryMovement getInventoryMovement() {
        return inventoryMovement;
    }

    public void setInventoryMovement(InventoryMovement inventoryMovement) {
        this.inventoryMovement = inventoryMovement;
    }

    public MovementDetail getMovementDetail() {
        return movementDetail;
    }

    public void setMovementDetail(MovementDetail movementDetail) {
        this.movementDetail = movementDetail;
    }

    public String getCostCenterFullName() {
        return warehouseVoucher.getCostCenter() != null ? warehouseVoucher.getCostCenter().getFullName() : null;
    }

    public void assignCostCenter(CostCenter costCenter) {
        warehouseVoucher.setCostCenter(costCenter);
    }

    public void clearCostCenter() {
        warehouseVoucher.setCostCenter(null);
    }

    public String getTargetCostCenterFullName() {
        return null != warehouseVoucher.getTargetCostCenter() ? warehouseVoucher.getTargetCostCenter().getFullName() : null;
    }

    public void assignTargetCostCenter(CostCenter costCenter) {
        warehouseVoucher.setTargetCostCenter(costCenter);
    }

    public void clearTargetCostCenter() {
        warehouseVoucher.setTargetCostCenter(null);
    }

    public void assignWarehouse(Warehouse warehouse) {
        warehouseVoucher.setWarehouse(warehouse);
        assignResponsible();
    }

    public void assignResponsible() {
        if (null != warehouseVoucher.getWarehouse()) {
            Employee responsible = readEmployee(warehouseVoucher.getWarehouse().getResponsibleId());
            warehouseVoucher.setResponsible(responsible);
        }
    }

    public void clearWarehouse() {
        warehouseVoucher.setResponsible(null);
        warehouseVoucher.setWarehouse(null);
    }

    public void clearWarehouses() {
        clearWarehouse();
        if (warehouseVoucher.isTransfer()) {
            clearTargetWarehouse();
        }
    }

    public void assignTargetWarehouse(Warehouse warehouse) {
        warehouseVoucher.setTargetWarehouse(warehouse);
        assignTargetResponsible();
    }

    public void assignTargetResponsible() {
        if (null != warehouseVoucher.getTargetWarehouse()) {
            Employee responsible = readEmployee(warehouseVoucher.getTargetWarehouse().getResponsibleId());
            warehouseVoucher.setTargetResponsible(responsible);
        }
    }

    public void clearTargetWarehouse() {
        warehouseVoucher.setTargetResponsible(null);
        warehouseVoucher.setTargetWarehouse(null);
    }

    public void assignResponsible(Employee responsible) {
        warehouseVoucher.setResponsible(responsible);
    }

    public void cleanResponsible() {
        warehouseVoucher.setResponsible(null);
    }

    public Map<MovementDetail, BigDecimal> getMovementDetailUnderMinimalStockMap() {
        return movementDetailUnderMinimalStockMap;
    }

    public void setMovementDetailUnderMinimalStockMap(Map<MovementDetail, BigDecimal> movementDetailUnderMinimalStockMap) {
        this.movementDetailUnderMinimalStockMap = movementDetailUnderMinimalStockMap;
    }

    public Map<MovementDetail, BigDecimal> getMovementDetailOverMaximumStockMap() {
        return movementDetailOverMaximumStockMap;
    }

    public void setMovementDetailOverMaximumStockMap(Map<MovementDetail, BigDecimal> movementDetailOverMaximumStockMap) {
        this.movementDetailOverMaximumStockMap = movementDetailOverMaximumStockMap;
    }

    public List<MovementDetail> getMovementDetailWithoutWarnings() {
        return movementDetailWithoutWarnings;
    }

    public void setMovementDetailWithoutWarnings(List<MovementDetail> movementDetailWithoutWarnings) {
        this.movementDetailWithoutWarnings = movementDetailWithoutWarnings;
    }

    public void assignPetitionerJobContract(JobContract jobContract) {
        warehouseVoucher.setPetitionerJobContract(jobContract);
        loadPetitionerJobContractValues();
    }

    /*messages */
    public void addInventoryMessages(List<InventoryMessage> messages) {
        for (InventoryMessage message : messages) {
            if (message.isNotFound()) {
                facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                        "WarehouseVoucher.error.InventoryNotFound", message.getProductItem().getName());
                continue;
            }

            if (message.isNotEnough()) {
                facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                        "WarehouseVoucher.error.InventoryUnavailableProductItems", message.getProductItem().getName(),
                        message.getAvailableQuantity());
            }
        }
    }

    public void addProductItemNotFoundMessage(String productItemName) {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.WARN,
                "ProductItem.error.notFound", productItemName);
    }

    public void addWarehouseDocumentTypeErrorMessage() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO,
                "PurchaseOrder.error.warehouseDocumentTypeNotFound");
    }

    public void addWarehouseAccountCashNotFoundMessage() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                "WarehouseVoucher.error.WarehouseAccountCashNotFoundMessage");
    }
}
