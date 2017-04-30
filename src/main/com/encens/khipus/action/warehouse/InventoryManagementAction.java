package com.encens.khipus.action.warehouse;

import com.encens.khipus.exception.warehouse.InventoryUnitaryBalanceException;
import com.encens.khipus.framework.action.Outcome;
import com.encens.khipus.model.admin.BusinessUnit;
import com.encens.khipus.model.finances.CostCenter;
import com.encens.khipus.model.warehouse.InventoryDetail;
import com.encens.khipus.model.warehouse.ProductItem;
import com.encens.khipus.model.warehouse.Warehouse;
import com.encens.khipus.service.warehouse.InventoryManagementService;
import com.encens.khipus.util.MessageUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * @author
 * @version 2.2
 */

@Name("inventoryManagementAction")
@Scope(ScopeType.CONVERSATION)
public class InventoryManagementAction implements Serializable {
    @In
    private FacesMessages facesMessages;

    @In
    private InventoryManagementService inventoryManagementService;

    private BusinessUnit executorUnit;

    private Warehouse warehouse;

    private ProductItem productItem;

    private InventoryDetail inventoryDetail;

    private CostCenter targetCostCenter;

    private BigDecimal quantity;

    private String description;

    private List<InventoryDetail> availableInventoryDetails;


    public Warehouse getWarehouse() {
        return warehouse;
    }

    public void setWarehouse(Warehouse warehouse) {
        this.warehouse = warehouse;
    }

    public ProductItem getProductItem() {
        return productItem;
    }

    public void setProductItem(ProductItem productItem) {
        this.productItem = productItem;
    }

    public List<InventoryDetail> getAvailableInventoryDetails() {
        return availableInventoryDetails;
    }

    public void setAvailableInventoryDetails(List<InventoryDetail> availableInventoryDetails) {
        this.availableInventoryDetails = availableInventoryDetails;
    }

    public InventoryDetail getInventoryDetail() {
        return inventoryDetail;
    }

    public void setInventoryDetail(InventoryDetail inventoryDetail) {
        this.inventoryDetail = inventoryDetail;
    }

    public CostCenter getTargetCostCenter() {
        return targetCostCenter;
    }

    public void setTargetCostCenter(CostCenter targetCostCenterCode) {
        this.targetCostCenter = targetCostCenterCode;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void cleanMainOptions() {
        setWarehouse(null);
        setProductItem(null);
        setAvailableInventoryDetails(null);
    }

    public void assignProductItem(ProductItem productItem) {
        this.productItem = productItem;
    }

    public void cleanProductItemField() {
        this.productItem = null;
    }

    public void assignWarehouse(Warehouse warehouse) {
        this.warehouse = warehouse;
    }

    public void cleanWarehouseField() {
        this.warehouse = null;
    }

    public void assignTargetCostCenter(CostCenter costCenter) {
        setTargetCostCenter(costCenter);
    }

    public void cleanTargetCostCenter() {
        setTargetCostCenter(null);
    }

    public boolean isExistsInventoryDetails() {
        if (null != executorUnit &&
                null != warehouse &&
                null != productItem) {
            setAvailableInventoryDetails(inventoryManagementService.getAvailableInventoryDetails(
                    warehouse.getId().getCompanyNumber(),
                    executorUnit,
                    warehouse.getId().getWarehouseCode(),
                    productItem.getId().getProductItemCode()
            ));
        }

        return null != availableInventoryDetails && !availableInventoryDetails.isEmpty();
    }

    public boolean isExecutorUnitFieldSelected() {
        return null != this.executorUnit;
    }

    public String buildInventoryDetailLabel(InventoryDetail inventoryDetail) {
        return MessageUtils.getMessage("InventoryManagement.inventoryDetailLabel",
                inventoryDetail.getCostCenter().getFullName(), inventoryDetail.getQuantity());
    }

    @Restrict("#{s:hasPermission('INVENTORYMANAGEMENT','CREATE')}")
    public String create() {
        String validationOutcome = fieldValidations();
        if (!Outcome.SUCCESS.equals(validationOutcome)) {
            return validationOutcome;
        }
        try {
            inventoryManagementService.createInventoryDetail(inventoryDetail.getId(),
                    targetCostCenter, quantity, description);
        } catch (InventoryUnitaryBalanceException e) {
            addInventoryUnitaryBalanceErrorMessage(e.getAvailableUnitaryBalance(), e.getProductItem());
            return Outcome.REDISPLAY;
        }

        addSuccessFullMessage();
        return Outcome.SUCCESS;
    }

    public String cancel() {
        return Outcome.CANCEL;
    }

    public BusinessUnit getExecutorUnit() {
        return executorUnit;
    }

    public void setExecutorUnit(BusinessUnit executorUnit) {
        this.executorUnit = executorUnit;
    }

    private String fieldValidations() {
        String validationOutcome = Outcome.SUCCESS;

        if (null == warehouse) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "Common.required", MessageUtils.getMessage("InventoryManagement.warehouse"));
            validationOutcome = Outcome.REDISPLAY;
        }

        if (null == productItem) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "Common.required", MessageUtils.getMessage("InventoryManagement.productItem"));
            validationOutcome = Outcome.REDISPLAY;
        }

        if (null != inventoryDetail && null != targetCostCenter) {
            String sourceCostCenterCode = inventoryDetail.getCostCenterCode();
            String targetCostCenterCode = targetCostCenter.getId().getCode();

            if (sourceCostCenterCode.equals(targetCostCenterCode)) {
                facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                        "Common.error.fieldSelectedtwice",
                        MessageUtils.getMessage("InventoryManagement.sourceCostCenterCode"),
                        MessageUtils.getMessage("InventoryManagement.targetCostCenterCode"));
                validationOutcome = Outcome.REDISPLAY;
            }
        } else {
            if (null == inventoryDetail) {
                facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                        "Common.required", MessageUtils.getMessage("InventoryManagement.sourceCostCenterCode"));
                validationOutcome = Outcome.REDISPLAY;
            }

            if (null == targetCostCenter) {
                facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                        "Common.required", MessageUtils.getMessage("InventoryManagement.targetCostCenterCode"));
                validationOutcome = Outcome.REDISPLAY;
            }
        }

        return validationOutcome;
    }

    private void addInventoryUnitaryBalanceErrorMessage(BigDecimal availableUnitaryBalance,
                                                        ProductItem productItem) {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                "inventoryManagement.error.notEnoughUnitaryBalance",
                productItem.getName(),
                availableUnitaryBalance);
    }

    private void addSuccessFullMessage() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO,
                "inventoryManagement.success",
                quantity,
                productItem.getName(),
                warehouse.getName(),
                targetCostCenter.getFullName());
    }
}
