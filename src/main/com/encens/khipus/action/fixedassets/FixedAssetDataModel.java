package com.encens.khipus.action.fixedassets;

import com.encens.khipus.exception.EntryNotFoundException;
import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.framework.service.GenericService;
import com.encens.khipus.model.employees.Employee;
import com.encens.khipus.model.finances.CostCenter;
import com.encens.khipus.model.fixedassets.FixedAsset;
import com.encens.khipus.model.fixedassets.FixedAssetGroup;
import com.encens.khipus.model.fixedassets.FixedAssetState;
import com.encens.khipus.model.fixedassets.FixedAssetSubGroup;
import com.encens.khipus.util.ListEntityManagerName;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;
import org.jboss.seam.log.Log;

import java.util.Arrays;
import java.util.List;

/**
 * Data model for FixedAsset
 *
 * @author
 * @version 2.26
 */

@Name("fixedAssetDataModel")
@Scope(ScopeType.PAGE)
public class FixedAssetDataModel extends QueryDataModel<Long, FixedAsset> {

    private static final String[] RESTRICTIONS =
            {"fixedAsset.fixedAssetCode = #{fixedAssetDataModel.criteria.fixedAssetCode}",
                    "fixedAssetSubGroup=#{fixedAssetDataModel.fixedAssetSubGroup}",
                    "fixedAsset.fixedAssetSubGroupCode = #{fixedAssetDataModel.criteria.fixedAssetSubGroupCode}",
                    "fixedAsset.fixedAssetGroupCode = #{fixedAssetDataModel.criteria.fixedAssetGroupCode}",
                    "fixedAsset.fixedAssetGroupCode = #{fixedAssetDataModel.fixedAssetGroup.groupCode}",
                    "lower(fixedAsset.barCode) like concat('%',concat(lower(#{fixedAssetDataModel.criteria.barCode}),'%'))",
                    "lower(purchaseOrder.orderNumber) like concat(lower(#{fixedAssetDataModel.orderNumber}), '%')",
                    "purchaseOrder=#{fixedAssetDataModel.criteria.purchaseOrder}",
                    "fixedAsset in(#{fixedAssetVoucherAction.fixedAssetMovementDetailList})",
                    "fixedAsset.state=#{fixedAssetDataModel.criteria.state}",
                    "lower(fixedAsset.detail) like concat('%',concat(lower(#{fixedAssetDataModel.criteria.detail}),'%'))",
                    "lower(fixedAsset.sequence) like concat(lower(#{fixedAssetDataModel.criteria.sequence}),'%')",
                    "lower(fixedAsset.trademark) like concat(lower(#{fixedAssetDataModel.criteria.trademark}),'%')",
                    "lower(fixedAsset.model) like concat(lower(#{fixedAssetDataModel.criteria.model}),'%')",
                    "fixedAsset.registrationDate >= #{fixedAssetDataModel.criteria.registrationDate}",
                    "fixedAsset.endDate <= #{fixedAssetDataModel.criteria.endDate}",
                    "fixedAsset.duration =#{fixedAssetDataModel.criteria.duration}",
                    "fixedAsset.depreciationRate =#{fixedAssetDataModel.criteria.depreciationRate}",
                    "businessUnit=#{fixedAssetDataModel.criteria.businessUnit}",
                    "costCenter=#{fixedAssetDataModel.costCenter}",
                    "fixedAsset.costCenterCode=#{fixedAssetDataModel.criteria.costCenterCode}",
                    "fixedAsset.custodianJobContract.contract.employee = #{fixedAssetDataModel.employee}",
                    "lower(employee.idNumber) like concat(lower(#{fixedAssetDataModel.idNumber}),'%')",
                    "fixedAsset.id not in (#{fixedAssetVoucherAction.selectedFixedAssetIdList})",
                    "fixedAsset.id not in (#{fixedAssetPurchaseOrderAction.selectedFixedAssetIdList})",
                    "fixedAsset.state in (#{fixedAssetPurchaseOrderAction.fixedAssetStateRestrictionList})",
                    "fixedAsset.state in (#{fixedAssetVoucherAction.stateRestrictionList})",
                    "fixedAsset.custodianJobContract = #{fixedAssetMaintenanceRequestAction.petitioner}"};


    private Employee employee;

    private FixedAssetGroup fixedAssetGroup;

    private FixedAssetSubGroup fixedAssetSubGroup;

    private CostCenter costCenter;

    private String orderNumber;

    private Boolean multiSelect = true;

    private Boolean enableValidStates = false;

    private List<FixedAssetState> validStateList = FixedAssetState.getValidState();

    public String idNumber;

    @In
    private GenericService genericService;

    @Logger
    private Log log;

    @Create
    public void init() {
        setEntityManagerName(ListEntityManagerName.BUSINESS_UNIT_LIST.getName());
        sortProperty = "fixedAsset.fixedAssetCode";
        sortAsc = false;
    }

    @Override
    public String getEjbql() {
        return "select fixedAsset from FixedAsset fixedAsset " +
                "left join fetch fixedAsset.businessUnit businessUnit " +
                "left join fetch fixedAsset.costCenter costCenter " +
                "left join fetch fixedAsset.purchaseOrder purchaseOrder " +
                "left join fetch fixedAsset.fixedAssetSubGroup fixedAssetSubGroup " +
                "left join fetch fixedAsset.custodianJobContract custodianJobContract " +
                "left join fetch custodianJobContract.contract contract " +
                "left join fetch contract.employee employee " +
                " where ((#{fixedAssetDataModel.enableValidStates}=false) or (#{fixedAssetDataModel.enableValidStates}=true and fixedAsset.state in (#{fixedAssetDataModel.validStateList})))";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public void assignEmployee(Employee employee) {
        try {
            this.employee = (null != employee) ? genericService.findById(Employee.class, employee.getId()) : null;
        } catch (EntryNotFoundException e) {
            entryNotFoundErrorLog(e);
        }

        setEmployee(employee);
    }

    private void entryNotFoundErrorLog(EntryNotFoundException e) {
        log.error("entity was removed by another user...", e);
    }

    public void clearEmployee() {
        setEmployee(null);
    }

    public void clearCostCenter() {
        setCostCenter(null);
    }

    public FixedAssetGroup getFixedAssetGroup() {
        return fixedAssetGroup;
    }

    public void setFixedAssetGroup(FixedAssetGroup fixedAssetGroup) {
        if (null != fixedAssetGroup) {
            this.fixedAssetGroup = getEntityManager().find(FixedAssetGroup.class, fixedAssetGroup.getId());
            fixedAssetSubGroup = null;
        } else {
            this.fixedAssetGroup = null;
        }
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public Boolean getMultiSelect() {
        return multiSelect;
    }

    public void setMultiSelect(Boolean multiSelect) {
        this.multiSelect = multiSelect;
    }

    public FixedAssetSubGroup getFixedAssetSubGroup() {
        return fixedAssetSubGroup;
    }

    public void setFixedAssetSubGroup(FixedAssetSubGroup fixedAssetSubGroup) {
        if (fixedAssetSubGroup != null) {
            this.fixedAssetSubGroup = getEntityManager().find(FixedAssetSubGroup.class, fixedAssetSubGroup.getId());
            this.fixedAssetGroup = getEntityManager().find(FixedAssetGroup.class, this.fixedAssetSubGroup.getFixedAssetGroup().getId());
        } else {
            this.fixedAssetSubGroup = null;
        }
    }

    public CostCenter getCostCenter() {
        return costCenter;
    }

    public void setCostCenter(CostCenter costCenter) {
        this.costCenter = costCenter;
    }

    public String getIdNumber() {
        return idNumber;
    }

    public void setIdNumber(String idNumber) {
        this.idNumber = idNumber;
    }

    public void clearFixedAssetGroup() {
        setFixedAssetGroup(null);
        fixedAssetSubGroup = null;
    }

    public void clearFixedAssetSubGroup() {
        setFixedAssetSubGroup(null);
    }

    public Boolean getEnableValidStates() {
        return enableValidStates;
    }

    public void setEnableValidStates(Boolean enableValidStates) {
        this.enableValidStates = enableValidStates;
    }

    public List<FixedAssetState> getValidStateList() {
        return validStateList;
    }

    public void setValidStateList(List<FixedAssetState> validStateList) {
        this.validStateList = validStateList;
    }

    @Override
    public void clear() {
        setCostCenter(null);
        setEmployee(null);
        setFixedAssetGroup(null);
        setFixedAssetSubGroup(null);
        setOrderNumber(null);
        super.clear();
    }
}