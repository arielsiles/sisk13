package com.encens.khipus.action.warehouse;

import com.encens.khipus.exception.EntryNotFoundException;
import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.framework.action.Outcome;
import com.encens.khipus.model.finances.CostCenter;
import com.encens.khipus.model.warehouse.ProductInventory;
import com.encens.khipus.model.warehouse.ProductInventoryRecordType;
import com.encens.khipus.model.warehouse.ProductItem;
import com.encens.khipus.service.warehouse.ProductInventoryService;
import org.apache.commons.lang.StringUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.core.Manager;
import org.jboss.seam.international.StatusMessage;

/**
 * @author
 * @version 2.0
 */
@Name("productInventoryAction")
@Scope(ScopeType.CONVERSATION)
public class ProductInventoryAction extends GenericAction<ProductInventory> {

    private ProductItem productItem;
    private String productItemName;
    private String productItemFullname;
    private CostCenter costCenter;

    private String productItemCode;
    private ProductInventoryRecordType productInventoryRecordType;
    private Double quantityStock;
    private String descriptionStock;


    @In
    private ProductInventoryService productInventoryService;

    @In(required = false)
    ProductInventoryDataModel productInventoryDataModel;

    @Factory(value = "productInventory", scope = ScopeType.STATELESS)
    public ProductInventory initProductInventory() {
        return getInstance();
    }

    @Override
    public String create() {
        return Outcome.REDISPLAY;
    }

    @Override
    public void createAndNew() {
    }

    @Override
    public String update() {
        return Outcome.REDISPLAY;
    }

    @Override
    public String delete() {
        return Outcome.REDISPLAY;
    }

    public ProductItem getProductItem() {
        return productItem;
    }

    public void setProductItem(ProductItem productItem) {
        this.productItem = productItem;
    }

    public CostCenter getCostCenter() {
        return costCenter;
    }

    public void setCostCenter(CostCenter costCenter) {
        this.costCenter = costCenter;
    }


    public void assignProductItem(ProductItem productItem) {
        try {
            setProductItem(getService().findById(ProductItem.class, productItem.getId()));
        } catch (EntryNotFoundException e) {
            entryNotFoundErrorLog(e);
        }
    }

    public void clearProductItem() {
        setProductItem(null);
        setProductItemCode(null);
        setProductItemName(null);
        setProductItemFullname(null);
    }

    public void clearProductItemStock(){
        setProductItem(null);
        setProductItemCode(null);
        setProductItemName(null);
        setProductItemFullname(null);
        setProductInventoryRecordType(null);
        setQuantityStock(null);
        setDescriptionStock(null);
    }

    public void assignProductItem() {
        assignProductItem(productItem);
    }

    public void assignCostCenter() {
        assignCostCenter(costCenter);
    }

    public void assignCostCenter(CostCenter costCenter) {
        try {
            setCostCenter(getService().findById(CostCenter.class, costCenter.getId()));
        } catch (EntryNotFoundException e) {
            entryNotFoundErrorLog(e);
        }
    }

    public void clearCostCenter() {
        setCostCenter(null);
    }

    public void search() {
        productInventoryDataModel.filterByProductItemCode(productItemCode);
        //productInventoryDataModel.filterByProductItemName(productItemName);
        productInventoryDataModel.search();
    }

    public void clear() {
        clearCostCenter();
        clearProductItem();
        productInventoryDataModel.filterByProductItemCode(null == getProductItem() ? null : productItem.getId().getProductItemCode());
        productInventoryDataModel.search();
    }

    public String updateStock(){

        productItemCode = StringUtils.substringBetween(productItemFullname, "[", "]");
        System.out.println("___________UPDATE_STOCKS________");
        //System.out.println("COD ART: " + getInstance().getProductItem().getFullName());
        System.out.println("PRODUCT ITEM: " + productItem);
        System.out.println("COD ART: " + productItemCode);
        System.out.println("TIPO:" + productInventoryRecordType);
        System.out.println("CANTIDAD: " + quantityStock);
        System.out.println("DESCRIPCION: " + descriptionStock);

        productInventoryService.updateProductInventory(productItemCode, quantityStock, productInventoryRecordType, descriptionStock);
        clearProductItemStock();
        facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO,"STOCKS de productos se actualizo exitosamente.");
        return Outcome.SUCCESS;
    }

    public String cancel() {
        System.out.println("----> CANCEL...");
        clearProductItemStock();
        Manager.instance().endConversation(true);
        return Outcome.REDISPLAY;
    }

    public String getProductItemCode() {
        return productItemCode;
    }

    public void setProductItemCode(String productItemCode) {
        this.productItemCode = productItemCode;
    }

    public String getProductItemName() {
        return productItemName;
    }

    public void setProductItemName(String productItemName) {
        this.productItemName = productItemName;
    }

    public Double getQuantityStock() {
        return quantityStock;
    }

    public void setQuantityStock(Double quantityStock) {
        this.quantityStock = quantityStock;
    }

    public String getDescriptionStock() {
        return descriptionStock;
    }

    public void setDescriptionStock(String descriptionStock) {
        this.descriptionStock = descriptionStock;
    }

    public ProductInventoryRecordType getProductInventoryRecordType() {
        return productInventoryRecordType;
    }

    public void setProductInventoryRecordType(ProductInventoryRecordType productInventoryRecordType) {
        this.productInventoryRecordType = productInventoryRecordType;
    }

    public String getProductItemFullname() {
        return productItemFullname;
    }

    public void setProductItemFullname(String productItemFullname) {
        this.productItemFullname = productItemFullname;
    }
}
