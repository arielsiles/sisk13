package com.encens.khipus.action.warehouse;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.purchases.PurchaseOrder;
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
 */
@Name("warehousePurchaseOrderModalDataModel")
@Scope(ScopeType.PAGE)
@Restrict("#{s:hasPermission('WAREHOUSEPURCHASEORDER','VIEW')}")
public class WarehousePurchaseOrderModalDataModel extends QueryDataModel<Long, PurchaseOrder> {
    private static final String[] RESTRICTIONS = {
            "lower(warehousePurchaseOrder.gloss) like concat('%', concat(lower(#{warehousePurchaseOrderModalDataModel.criteria.gloss}), '%'))",
            "warehousePurchaseOrder.orderNumber = #{warehousePurchaseOrderModalDataModel.criteria.orderNumber}",
            "lower(warehousePurchaseOrder.invoiceNumber) like concat(lower(#{warehousePurchaseOrderModalDataModel.criteria.invoiceNumber}), '%')",
            "warehousePurchaseOrder.responsible.idNumber = #{warehousePurchaseOrderModalDataModel.responsibleIdNumber}",
            "employee.idNumber = #{warehousePurchaseOrderModalDataModel.petitionerIdNumber}",
            "warehousePurchaseOrder.date >= #{warehousePurchaseOrderModalDataModel.startDate}",
            "warehousePurchaseOrder.date <= #{warehousePurchaseOrderModalDataModel.endDate}",
            "warehousePurchaseOrder.executorUnit = #{warehousePurchaseOrderModalDataModel.criteria.executorUnit}",
            "lower(warehousePurchaseOrder.costCenterCode) like concat(lower(#{warehousePurchaseOrderModalDataModel.criteria.costCenterCode}), '%')",
            "lower(warehousePurchaseOrder.providerCode) like concat(lower(#{warehousePurchaseOrderModalDataModel.criteria.providerCode}), '%')",
            "warehousePurchaseOrder.state in (#{enumerationUtil.getEnumValuesByName('com.encens.khipus.model.purchases.PurchaseOrderState','FIN','LIQ')})",
            "warehousePurchaseOrder.orderType = #{enumerationUtil.getEnumValue('com.encens.khipus.model.purchases.PurchaseOrderType', 'WAREHOUSE')}",
    };

    private Date startDate;
    private Date endDate;

    public String responsibleIdNumber;
    private String petitionerIdNumber;

    @Create
    public void init() {
        setEntityManagerName(ListEntityManagerName.BUSINESS_UNIT_LIST.getName());
        sortProperty = "warehousePurchaseOrder.date, warehousePurchaseOrder.orderNumber";
        sortAsc = false;
    }

    @Override
    public String getEjbql() {
        return "select distinct warehousePurchaseOrder from PurchaseOrder warehousePurchaseOrder " +
                "left join warehousePurchaseOrder.petitionerJobContract petitionerJobContract " +
                "left join petitionerJobContract.contract contract " +
                "left join contract.employee employee";
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

    public String getResponsibleIdNumber() {
        return responsibleIdNumber;
    }

    public void setResponsibleIdNumber(String responsibleIdNumber) {
        this.responsibleIdNumber = responsibleIdNumber;
    }

    public String getPetitionerIdNumber() {
        return petitionerIdNumber;
    }

    public void setPetitionerIdNumber(String petitionerIdNumber) {
        this.petitionerIdNumber = petitionerIdNumber;
    }

    @Override
    public void clear() {
        setResponsibleIdNumber(null);
        setPetitionerIdNumber(null);
        setStartDate(null);
        setEndDate(null);
        super.clear();
    }
}