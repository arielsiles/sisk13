package com.encens.khipus.action.warehouse;

import com.encens.khipus.action.SessionUser;
import com.encens.khipus.action.common.FunctionAction;
import com.encens.khipus.exception.ConcurrencyException;
import com.encens.khipus.exception.EntryDuplicatedException;
import com.encens.khipus.exception.EntryNotFoundException;
import com.encens.khipus.exception.warehouse.ProductItemMinimalStockIsGreaterThanMaximumStockException;
import com.encens.khipus.exception.warehouse.ProductItemNotFoundException;
import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.framework.action.Outcome;
import com.encens.khipus.model.finances.CashAccount;
import com.encens.khipus.model.warehouse.ProductItem;
import com.encens.khipus.model.warehouse.ProductItemState;
import com.encens.khipus.model.warehouse.SubGroup;
import com.encens.khipus.model.warehouse.SubGroupState;
import com.encens.khipus.service.common.SequenceGeneratorService;
import com.encens.khipus.service.production.ProcessedProductService;
import com.encens.khipus.service.production.ProductionInputService;
import com.encens.khipus.service.warehouse.ProductItemService;
import com.encens.khipus.service.warehouse.WarehouseCatalogService;
import com.encens.khipus.util.Constants;
import com.encens.khipus.util.FormatUtils;
import com.encens.khipus.util.MessageUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.international.StatusMessage;

import java.util.Arrays;

/**
 * @author
 * @version 2.0
 */
@Name("productItemAction")
@Scope(ScopeType.CONVERSATION)
public class ProductItemAction extends GenericAction<ProductItem> {

    public static final String ACCOUNT_CLASS = "G";

    @In(create = true)
    private FunctionAction functionAction;

    @In
    private WarehouseCatalogService warehouseCatalogService;

    @In
    private SequenceGeneratorService sequenceGeneratorService;

    @In
    private ProductItemService productItemService;

    @In
    private ProcessedProductService processedProductService;

    @In
    private ProductionInputService productionInputService;

    @In
    private SessionUser sessionUser;

    @Create
    public void atCreateTime() {
        if (!isManaged()) {
            getInstance().setControlValued(true);
            getInstance().setSaleable(true);
            getInstance().setState(ProductItemState.VIG);
            assignCode();
        }
    }

    private void assignCode() {
        getInstance().getId().setProductItemCode(String.valueOf(sequenceGeneratorService.findNextSequenceValue(Constants.WAREHOUSE_PRODUCT_ITEM_SEQUENCE)));
    }

    private void updateCode() {
        getInstance().getId().setProductItemCode(String.valueOf(sequenceGeneratorService.nextValue(Constants.WAREHOUSE_PRODUCT_ITEM_SEQUENCE)));
    }

    @Factory(value = "productItem", scope = ScopeType.STATELESS)
    @Restrict("#{s:hasPermission('PRODUCTITEM','VIEW')}")
    public ProductItem initProductItem() {
        return getInstance();
    }

    @Factory(value = "productItemStates", scope = ScopeType.STATELESS)
    public ProductItemState[] getProductItemStates() {
        return ProductItemState.values();
    }

    @Override
    @End
    @Restrict("#{s:hasPermission('PRODUCTITEM','CREATE')}")
    public String create() {
        String validationOutcome = validations();
        if (!Outcome.SUCCESS.equals(validationOutcome)) {
            return validationOutcome;
        }
        try {
            productItemService.createProductItem(getInstance());
            createProductionItem(getInstance());
            addCreatedMessage();
            return Outcome.SUCCESS;
        } catch (EntryDuplicatedException e) {
            addDuplicatedMessage();
            return Outcome.REDISPLAY;
        } catch (ProductItemMinimalStockIsGreaterThanMaximumStockException e) {
            addProductItemMinimalStockIsGreaterThanMaximumStockErrorMessage();
            return Outcome.REDISPLAY;
        }
    }

    private void createProductionItem(ProductItem productItem) {

        if (productItem.getSubGroup().getGroup().getName().equals(Constants.WAREHOUSE_UHT_PRODUCT) ||
                productItem.getSubGroup().getGroup().getName().equals(Constants.WAREHOUSE_YOGURT_PRODUCT) ||
                productItem.getSubGroup().getGroup().getName().equals(Constants.WAREHOUSE_CHEESE_PRODUCT)) {
            processedProductService.createProductionProduct(getInstance());
        }
        if (productItem.getSubGroup().getGroup().getName().equals(Constants.WAREHOUSE_PRODUCTION_INPUT)) {
            productionInputService.createProductionInput(getInstance());
        }
    }

    private void updateProductionItem(ProductItem productItem) {

        if (productItem.getSubGroup().getGroup().getName().equals(Constants.WAREHOUSE_UHT_PRODUCT) ||
                productItem.getSubGroup().getGroup().getName().equals(Constants.WAREHOUSE_YOGURT_PRODUCT) ||
                productItem.getSubGroup().getGroup().getName().equals(Constants.WAREHOUSE_CHEESE_PRODUCT)) {
            processedProductService.updateProductionProduct(productItem);
        }
        if (productItem.getSubGroup().getGroup().getName().equals(Constants.WAREHOUSE_PRODUCTION_INPUT)) {
            productionInputService.updateProductionInput(getInstance());
        }
    }

    @Override
    @Restrict("#{s:hasPermission('PRODUCTITEM','CREATE')}")
    public void createAndNew() {
        String validationOutcome = validations();
        if (Outcome.SUCCESS.equals(validationOutcome)) {
            try {
                productItemService.createProductItem(getInstance());
                createProductionItem(getInstance());
                addCreatedMessage();
                createInstance();
                if (!functionAction.getHasSeverityErrorMessages()) {
                    atCreateTime();
                }
            } catch (EntryDuplicatedException e) {
                addDuplicatedMessage();
            } catch (ProductItemMinimalStockIsGreaterThanMaximumStockException e) {
                addProductItemMinimalStockIsGreaterThanMaximumStockErrorMessage();
            }
        }
    }

    @Override
    @End
    @Restrict("#{s:hasPermission('PRODUCTITEM','UPDATE')}")
    public String update() {
        String validationOutcome = validations();
        if (!Outcome.SUCCESS.equals(validationOutcome)) {
            return validationOutcome;
        }

        Long currentVersion = (Long) getVersion(getInstance());
        try {
            productItemService.updateProductItem(getInstance());
            updateProductionItem(getInstance());
            addUpdatedMessage();
            return Outcome.SUCCESS;
        } catch (EntryDuplicatedException e) {
            addDuplicatedMessage();
            setVersion(getInstance(), currentVersion);
            return Outcome.REDISPLAY;
        } catch (ConcurrencyException e) {
            try {
                setInstance(getService().findById(getEntityClass(), getId(getInstance()), true));
            } catch (EntryNotFoundException e1) {
                addNotFoundMessage();
                return Outcome.FAIL;
            }
            addUpdateConcurrencyMessage();
            return Outcome.REDISPLAY;
        } catch (ProductItemNotFoundException e) {
            addNotFoundMessage();
            return Outcome.FAIL;
        } catch (ProductItemMinimalStockIsGreaterThanMaximumStockException e) {
            addProductItemMinimalStockIsGreaterThanMaximumStockErrorMessage();
            return Outcome.REDISPLAY;
        }
    }

    public void assignProductItemAccount(CashAccount cashAccount) {
        getInstance().setCashAccount(cashAccount);
    }

    public void clearProductItemAccount() {
        getInstance().setCashAccount(null);
    }

    public void assignSubGroup(SubGroup subGroup) {
        getInstance().setSubGroup(subGroup);
    }

    public void clearSubGroup() {
        getInstance().setSubGroup(null);
    }

    public String getAccountClass() {
        return ACCOUNT_CLASS;
    }

    public boolean isUsedGroupMeasureUnit() {
        return null != getInstance().getGroupMeasureUnit();
    }

    @Override
    protected String getDisplayNameProperty() {
        return "name";
    }

    private String validations() {
        boolean existsErrors = false;
        if (null != getInstance().getGroupMeasureUnit() && getInstance().getUsageMeasureUnit().equals(getInstance().getGroupMeasureUnit())) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "ProductItem.error.measureUnitSelectedTwice",
                    MessageUtils.getMessage("ProductItem.usageMeasureUnit"),
                    MessageUtils.getMessage("ProductItem.groupMeasureUnit"));
            existsErrors = true;
        }

        SubGroup subGroup = getInstance().getSubGroup();
        if (!warehouseCatalogService.isValidState(SubGroup.class, subGroup.getId(), SubGroupState.VIG)) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "ProductItem.error.subGroupInvalid", subGroup.getName());
            existsErrors = true;
        }

        if (!isManaged() && !warehouseCatalogService.validateProductItemCode(getInstance().getId().getProductItemCode())) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "Common.error.duplicatedCode", getInstance().getId().getProductItemCode());
            assignCode();
            existsErrors = true;
        }

        if (existsErrors) {
            return Outcome.REDISPLAY;
        }

        return Outcome.SUCCESS;
    }

    /*messages*/
    @Override
    protected void addDuplicatedMessage() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                "Warehouse.common.message.duplicated", getInstance().getId().getProductItemCode());
    }

    public boolean isInUse() {
        return isManaged() && warehouseCatalogService.isInUse(
                Arrays.asList(
                        "select det from MovementDetail det where det.companyNumber='" + getInstance().getId().getCompanyNumber() + "' and det.productItemCode='" + getInstance().getId().getProductItemCode() + "'",
                        "select det from PurchaseOrderDetail det where  det.companyNumber='" + getInstance().getId().getCompanyNumber() + "' and det.productItemCode='" + getInstance().getId().getProductItemCode() + "'",
                        "select prov from Provide prov where prov.companyNumber='" + getInstance().getId().getCompanyNumber() + "' and prov.productItemCode='" + getInstance().getId().getProductItemCode() + "'"
                ));
    }

    public void addProductItemMinimalStockIsGreaterThanMaximumStockErrorMessage() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                "ProductItem.error.minimalStockIsGreaterThanMaximumStock",
                FormatUtils.formatNumber(getInstance().getMinimalStock(), messages.get("patterns.decimal6FNumber"), sessionUser.getLocale()),
                FormatUtils.formatNumber(getInstance().getMaximumStock(), messages.get("patterns.decimal6FNumber"), sessionUser.getLocale()));
    }
}
