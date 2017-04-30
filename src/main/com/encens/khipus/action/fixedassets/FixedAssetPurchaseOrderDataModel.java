package com.encens.khipus.action.fixedassets;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.employees.Employee;
import com.encens.khipus.model.finances.CollectionDocumentType;
import com.encens.khipus.model.finances.CostCenter;
import com.encens.khipus.model.finances.Provider;
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

@Name("fixedAssetPurchaseOrderDataModel")
@Scope(ScopeType.PAGE)
@Restrict("#{s:hasPermission('FIXEDASSETPURCHASEORDER','VIEW')}")
public class FixedAssetPurchaseOrderDataModel extends QueryDataModel<Long, PurchaseOrder> {
    private static final String[] RESTRICTIONS = {
            "lower(fixedAssetPurchaseOrder.gloss) like concat('%', concat(lower(#{fixedAssetPurchaseOrderDataModel.criteria.gloss}), '%'))",
            "fixedAssetPurchaseOrder.state = #{fixedAssetPurchaseOrderDataModel.criteria.state}",
            "lower(fixedAssetPurchaseOrder.orderNumber) like concat(lower(#{fixedAssetPurchaseOrderDataModel.criteria.orderNumber}), '%')",
            "lower(fixedAssetPurchaseOrder.invoiceNumber) like concat(lower(#{fixedAssetPurchaseOrderDataModel.criteria.invoiceNumber}), '%')",
            "fixedAssetPurchaseOrder.responsible = #{fixedAssetPurchaseOrderDataModel.responsible}",
            "fixedAssetPurchaseOrder.petitionerJobContract.contract.employee = #{fixedAssetPurchaseOrderDataModel.petitioner}",
            "fixedAssetPurchaseOrder.provider = #{fixedAssetPurchaseOrderDataModel.provider}",
            "fixedAssetPurchaseOrder.costCenter = #{fixedAssetPurchaseOrderDataModel.costCenter}",
            "fixedAssetPurchaseOrder.date >= #{fixedAssetPurchaseOrderDataModel.startDate}",
            "fixedAssetPurchaseOrder.date <= #{fixedAssetPurchaseOrderDataModel.endDate}",
            "fixedAssetPurchaseOrder.executorUnit = #{fixedAssetPurchaseOrderDataModel.criteria.executorUnit}",
            "lower(fixedAssetPurchaseOrder.costCenterCode) like concat(lower(#{fixedAssetPurchaseOrderDataModel.criteria.costCenterCode}), '%')",
            "lower(fixedAssetPurchaseOrder.providerCode) like concat(lower(#{fixedAssetPurchaseOrderDataModel.criteria.providerCode}), '%')",
            "fixedAssetPurchaseOrder.orderType = #{enumerationUtil.getEnumValue('com.encens.khipus.model.purchases.PurchaseOrderType', 'FIXEDASSET')}",
            "fixedAssetPurchaseOrder.documentType = #{fixedAssetPurchaseOrderDataModel.documentType}"
    };

    private Date startDate;
    private Date endDate;
    private Employee responsible;
    private Employee petitioner;
    private CostCenter costCenter;
    private Provider provider;
    private CollectionDocumentType documentType;

    @Create
    public void init() {
        setEntityManagerName(ListEntityManagerName.BUSINESS_UNIT_LIST.getName());
        sortProperty = "fixedAssetPurchaseOrder.date, fixedAssetPurchaseOrder.orderNumber";
        sortAsc = false;
    }

    @Override
    public String getEjbql() {
        return "select fixedAssetPurchaseOrder from PurchaseOrder fixedAssetPurchaseOrder";
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

    public Employee getResponsible() {
        return responsible;
    }

    public void setResponsible(Employee responsible) {
        this.responsible = responsible;
    }

    public CostCenter getCostCenter() {
        return costCenter;
    }

    public void setCostCenter(CostCenter costCenter) {
        this.costCenter = costCenter;
    }


    public void assignEmployee(Employee employee) {
        setResponsible(employee);
    }

    public void clearEmployee() {
        setResponsible(null);
    }

    public void clearProvider() {
        setProvider(null);
    }

    public void assignCostCenter(CostCenter costCenter) {
        setCostCenter(costCenter);
    }

    public void clearCostCenter() {
        setCostCenter(null);
    }

    public String getCostCenterFullName() {
        return getCostCenter() != null ? getCostCenter().getFullName() : null;
    }

    public Provider getProvider() {
        return provider;
    }

    public void setProvider(Provider provider) {
        this.provider = provider;
    }

    public Employee getPetitioner() {
        return petitioner;
    }

    public void setPetitioner(Employee petitioner) {
        this.petitioner = petitioner;
    }

    public void clearPetitioner() {
        setPetitioner(null);
    }

    public CollectionDocumentType getDocumentType() {
        return documentType;
    }

    public void setDocumentType(CollectionDocumentType documentType) {
        this.documentType = documentType;
    }

    @Override
    public void clear() {
        setCostCenter(null);
        setStartDate(null);
        setEndDate(null);
        setResponsible(null);
        setProvider(null);
        setPetitioner(null);
        setDocumentType(null);
        super.clear();
        update();
        search();
    }
}
