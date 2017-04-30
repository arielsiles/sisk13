package com.encens.khipus.action.warehouse;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.employees.Employee;
import com.encens.khipus.model.finances.CollectionDocumentType;
import com.encens.khipus.model.finances.CostCenter;
import com.encens.khipus.model.finances.Provide;
import com.encens.khipus.model.finances.Provider;
import com.encens.khipus.model.purchases.PurchaseOrder;
import com.encens.khipus.model.purchases.PurchaseOrderState;
import com.encens.khipus.model.warehouse.ProductItem;
import com.encens.khipus.model.warehouse.Warehouse;
import com.encens.khipus.util.ListEntityManagerName;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * @author
 * @version 2.2
 */

@Name("purchaseOrderWithCheckSearchDataModel")
@Scope(ScopeType.PAGE)
public class PurchaseOrderSearchDataModel extends QueryDataModel<Long, PurchaseOrder> {

    private static final String[] RESTRICTIONS = {
            "lower(warehousePurchaseOrder.gloss) like concat('%', concat(lower(#{purchaseOrderWithCheckSearchDataModel.criteria.gloss}), '%'))",
            "warehousePurchaseOrder.state = #{purchaseOrderWithCheckSearchDataModel.criteria.state}",
            "lower(warehousePurchaseOrder.orderNumber) like concat(lower(#{purchaseOrderWithCheckSearchDataModel.criteria.orderNumber}), '%')",
            "lower(warehousePurchaseOrder.invoiceNumber) like concat('%', concat(lower(#{purchaseOrderWithCheckSearchDataModel.criteria.invoiceNumber}), '%'))",
            "warehousePurchaseOrder.responsible = #{purchaseOrderWithCheckSearchDataModel.responsible}",
            "warehousePurchaseOrder.costCenter = #{purchaseOrderWithCheckSearchDataModel.costCenter}",
            "warehousePurchaseOrder.provider = #{purchaseOrderWithCheckSearchDataModel.criteria.provider}",
            "warehousePurchaseOrder.warehouse = #{purchaseOrderWithCheckSearchDataModel.warehouse}",
            "warehousePurchaseOrder.date >= #{purchaseOrderWithCheckSearchDataModel.startDate}",
            "warehousePurchaseOrder.date <= #{purchaseOrderWithCheckSearchDataModel.endDate}",
            "warehousePurchaseOrder.id <> #{purchaseOrderWithCheckSearchDataModel.criteria.id}",
            "warehousePurchaseOrder.executorUnit = #{purchaseOrderWithCheckSearchDataModel.criteria.executorUnit}",
            "warehousePurchaseOrder.orderType = #{enumerationUtil.getEnumValue('com.encens.khipus.model.purchases.PurchaseOrderType', 'WAREHOUSE')}",
            "warehousePurchaseOrder.documentType = #{purchaseOrderWithCheckSearchDataModel.documentType}"
    };

    private Date startDate;
    private Date endDate;
    private Employee responsible;
    private CostCenter costCenter;
    private Provider provider;
    private CollectionDocumentType documentType;
    private PurchaseOrderState state;
    private Warehouse warehouse;
    private Long id;

    @Create
    public void init() {
        setEntityManagerName(ListEntityManagerName.BUSINESS_UNIT_LIST.getName());
        sortProperty = "warehousePurchaseOrder.date, warehousePurchaseOrder.orderNumber";
        sortAsc = false;
    }

    @Override
    public String getEjbql() {
        return "select warehousePurchaseOrder" +
                " from PurchaseOrder warehousePurchaseOrder" +
                " left join fetch warehousePurchaseOrder.responsible responsible" +
                " left join fetch warehousePurchaseOrder.costCenter costCenter" +
                " left join fetch warehousePurchaseOrder.provider provider" +
                " left join fetch warehousePurchaseOrder.warehouse";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
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

    public CostCenter getCostCenter() {
        return costCenter;
    }

    public void setCostCenter(CostCenter costCenter) {
        this.costCenter = costCenter;
    }

    public void cleanCostCenter() {
        setCostCenter(null);
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

    public Provider getProvider() {
        return provider;
    }

    public void setProvider(Provider provider) {
        this.provider = provider;
    }

    public void cleanProvider() {
        setProvider(null);
    }

    public CollectionDocumentType getDocumentType() {
        return documentType;
    }

    public void setDocumentType(CollectionDocumentType documentType) {
        this.documentType = documentType;
    }

    public PurchaseOrderState getState() {
        return state;
    }

    public void setState(PurchaseOrderState state) {
        this.state = state;
    }

    public Warehouse getWarehouse() {
        return warehouse;
    }

    public void setWarehouse(Warehouse warehouse) {
        this.warehouse = warehouse;
    }

    @Override
    public void clear() {
        setStartDate(null);
        setEndDate(null);
        setResponsible(null);
        setCostCenter(null);
        setProvider(null);
        setDocumentType(null);
        super.clear();
        update();
        search();
    }

    public List<PurchaseOrder> getSelectedPurchaseOrders() {
        List ids = super.getSelectedIdList();

        List<PurchaseOrder> result = new ArrayList<PurchaseOrder>();
        for (Object id : ids) {
            result.add(getEntityManager().find(PurchaseOrder.class, id));
        }

        return result;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
