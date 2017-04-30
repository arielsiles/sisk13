package com.encens.khipus.action.warehouse;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.employees.Employee;
import com.encens.khipus.model.finances.CostCenter;
import com.encens.khipus.model.finances.Provider;
import com.encens.khipus.model.warehouse.ProductItem;
import com.encens.khipus.model.warehouse.WarehouseVoucher;
import com.encens.khipus.model.warehouse.WarehouseVoucherPK;
import com.encens.khipus.util.ListEntityManagerName;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * @author
 * @version 2.0
 */
@Name("warehouseVoucherDataModel")
@Scope(ScopeType.PAGE)
@Restrict("#{s:hasPermission('WAREHOUSEVOUCHER','VIEW')}")
public class WarehouseVoucherDataModel extends QueryDataModel<WarehouseVoucherPK, WarehouseVoucher> {
    private String invoiceNumber;
    private String orderNumber;
    private Provider provider;
    private CostCenter costCenter;
    private ProductItem productItem;
    private String inventoryMovementDescription;
    private Employee responsible;
    private Date startDate;
    private Date endDate;

    private static final String[] RESTRICTIONS = {
            "lower(warehouseVoucher.number) like concat(lower(#{warehouseVoucherDataModel.criteria.number}), '%')",
            "purchaseOrder.provider = #{warehouseVoucherDataModel.provider}",
            "warehouseVoucher.costCenter = #{warehouseVoucherDataModel.costCenter}",
            "warehouseVoucher.documentType = #{warehouseVoucherDataModel.criteria.documentType}",
            "responsible = #{warehouseVoucherDataModel.responsible}",
            "warehouseVoucher.id.transactionNumber in (select distinct movementDetail.transactionNumber " +
                    "from MovementDetail movementDetail " +
                    "where movementDetail.companyNumber=warehouseVoucher.id.companyNumber and " +
                    "movementDetail.productItem=#{warehouseVoucherDataModel.productItem})",
            "warehouseVoucher.id.transactionNumber in (select movement.id.transactionNumber " +
                    "from InventoryMovement movement " +
                    "where movement.id.companyNumber = warehouseVoucher.id.companyNumber and " +
                    "lower(movement.description) like concat('%', concat(lower(#{warehouseVoucherDataModel.inventoryMovementDescription}), '%')))",
            "warehouseVoucher.state = #{warehouseVoucherDataModel.criteria.state}",
            "lower(warehouseVoucher.purchaseOrder.invoiceNumber) like concat(lower(#{warehouseVoucherDataModel.invoiceNumber}), '%')",
            "lower(warehouseVoucher.purchaseOrder.orderNumber) like concat(lower(#{warehouseVoucherDataModel.orderNumber}), '%')",
            "warehouseVoucher.date >= #{warehouseVoucherDataModel.startDate}",
            "warehouseVoucher.date <= #{warehouseVoucherDataModel.endDate}"
    };

    @Create
    public void init() {
        setEntityManagerName(ListEntityManagerName.BUSINESS_UNIT_LIST.getName());
        sortProperty = "warehouseVoucher.number";
    }

    @Override
    public String getEjbql() {
        return "select warehouseVoucher" +
                " from WarehouseVoucher warehouseVoucher" +
                " left join fetch warehouseVoucher.responsible responsible" +
                " left join fetch warehouseVoucher.purchaseOrder purchaseOrder" +
                " left join fetch purchaseOrder.provider provider" +
                " left join fetch purchaseOrder.costCenter costCenter";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }

    public Provider getProvider() {
        return provider;
    }

    public void setProvider(Provider provider) {
        this.provider = provider;
    }

    public void cleanProvider() {
        setProvider(null);
    }

    public CostCenter getCostCenter() {
        return costCenter;
    }

    public void setCostCenter(CostCenter costCenter) {
        this.costCenter = costCenter;
    }

    public void clearCostCenter() {
        setCostCenter(null);
    }

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public ProductItem getProductItem() {
        return productItem;
    }

    public void setProductItem(ProductItem productItem) {
        this.productItem = null != productItem ? getEntityManager().find(ProductItem.class, productItem.getId()) : null;
    }

    public String getInventoryMovementDescription() {
        return inventoryMovementDescription;
    }

    public void setInventoryMovementDescription(String inventoryMovementDescription) {
        this.inventoryMovementDescription = inventoryMovementDescription;
    }

    public void cleanSearchFilters() {
        invoiceNumber = null;
        orderNumber = null;
        getCriteria().setPurchaseOrder(null);
        getCriteria().setDocumentType(null);
        getCriteria().setState(null);
    }

    public void assignProductItem(ProductItem productItem) {
        setProductItem(productItem);
    }

    public void clearProductItem() {
        productItem = null;
    }

    public Employee getResponsible() {
        return responsible;
    }

    public void setResponsible(Employee responsible) {
        this.responsible = responsible;
    }

    public void cleanResponsible() {
        setResponsible(null);
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    @Override
    public void clear() {
        setInvoiceNumber(null);
        setOrderNumber(null);
        setProductItem(null);
        setInventoryMovementDescription(null);
        cleanProvider();
        clearCostCenter();
        setStartDate(null);
        setEndDate(null);
        super.clear();
    }
}
