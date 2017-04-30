package com.encens.khipus.action.fixedassets;

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
 * @version 2.2
 */

@Name("fixedAssetPurchaseOrderToApproveFixedAssetRegistrationDataModel")
@Scope(ScopeType.PAGE)
@Restrict("#{s:hasPermission('FIXEDASSETPURCHASEORDER','VIEW')}")
public class FixedAssetPurchaseOrderToApproveFixedAssetRegistrationDataModel extends QueryDataModel<Long, PurchaseOrder> {
    private static final String[] RESTRICTIONS = {
            "lower(fixedAssetPurchaseOrder.gloss) like concat('%', concat(lower(#{fixedAssetPurchaseOrderToApproveFixedAssetRegistrationDataModel.criteria.gloss}), '%'))",
            "fixedAssetPurchaseOrder.orderNumber = #{fixedAssetPurchaseOrderToApproveFixedAssetRegistrationDataModel.criteria.orderNumber}",
            "lower(fixedAssetPurchaseOrder.invoiceNumber) like concat(lower(#{fixedAssetPurchaseOrderToApproveFixedAssetRegistrationDataModel.criteria.invoiceNumber}), '%')",
            "fixedAssetPurchaseOrder.responsible.idNumber = #{fixedAssetPurchaseOrderToApproveFixedAssetRegistrationDataModel.responsibleIdNumber}",
            "employee.idNumber = #{fixedAssetPurchaseOrderToApproveFixedAssetRegistrationDataModel.petitionerIdNumber}",
            "fixedAssetPurchaseOrder.date >= #{fixedAssetPurchaseOrderToApproveFixedAssetRegistrationDataModel.startDate}",
            "fixedAssetPurchaseOrder.date <= #{fixedAssetPurchaseOrderToApproveFixedAssetRegistrationDataModel.endDate}",
            "fixedAssetPurchaseOrder.executorUnit = #{fixedAssetPurchaseOrderToApproveFixedAssetRegistrationDataModel.criteria.executorUnit}",
            "lower(fixedAssetPurchaseOrder.costCenterCode) like concat(lower(#{fixedAssetPurchaseOrderToApproveFixedAssetRegistrationDataModel.criteria.costCenterCode}), '%')",
            "lower(fixedAssetPurchaseOrder.providerCode) like concat(lower(#{fixedAssetPurchaseOrderToApproveFixedAssetRegistrationDataModel.criteria.providerCode}), '%')",
            "fixedAssetPurchaseOrder.state in (#{enumerationUtil.getEnumValuesByName('com.encens.khipus.model.purchases.PurchaseOrderState','FIN','LIQ')})",
            "fixedAssetPurchaseOrder.orderType = #{enumerationUtil.getEnumValue('com.encens.khipus.model.purchases.PurchaseOrderType', 'FIXEDASSET')}",
            "fixedAssetPurchaseOrder not in ( select purchaseOrder from FixedAssetVoucher voucher left join voucher.purchaseOrder purchaseOrder where voucher.purchaseOrder is not null and voucher.state=#{enumerationUtil.getEnumValue('com.encens.khipus.model.fixedassets.FixedAssetVoucherState', 'APR')})"
    };

    private Date startDate;
    private Date endDate;

    public String responsibleIdNumber;
    private String petitionerIdNumber;

    @Create
    public void init() {
        setEntityManagerName(ListEntityManagerName.BUSINESS_UNIT_LIST.getName());
        sortProperty = "fixedAssetPurchaseOrder.date, fixedAssetPurchaseOrder.orderNumber";
        sortAsc = false;
    }

    @Override
    public String getEjbql() {
        return "select distinct fixedAssetPurchaseOrder from PurchaseOrder fixedAssetPurchaseOrder " +
                "left join fixedAssetPurchaseOrder.petitionerJobContract petitionerJobContract " +
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