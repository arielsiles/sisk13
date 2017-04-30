package com.encens.khipus.action.fixedassets;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.admin.User;
import com.encens.khipus.model.employees.Employee;
import com.encens.khipus.model.finances.CostCenter;
import com.encens.khipus.model.fixedassets.FixedAssetMaintenanceReceiptState;
import com.encens.khipus.model.fixedassets.FixedAssetMaintenanceRequest;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.security.Identity;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Data model for FixedAssetMaintenanceRequest
 *
 * @author
 * @version 2.25
 */

@Name("fixedAssetMaintenanceRequestDataModel")
@Scope(ScopeType.PAGE)
public class FixedAssetMaintenanceRequestDataModel extends QueryDataModel<Long, FixedAssetMaintenanceRequest> {

    @In
    User currentUser;

    private Employee employee;
    private Date approveDate;
    private Date rejectDate;
    private Date receiptDate;
    private String maintenanceReason;
    private String approveDescription;
    private String rejectDescription;
    private String receiptDescription;
    private FixedAssetMaintenanceReceiptState receiptState;
    private static final String[] RESTRICTIONS = {
            "fixedAssetMaintenanceRequest.code = #{fixedAssetMaintenanceRequestDataModel.criteria.code}",
            "fixedAssetMaintenanceRequest.executorUnit = #{fixedAssetMaintenanceRequestDataModel.criteria.executorUnit}",
            "fixedAssetMaintenanceRequest.costCenter = #{fixedAssetMaintenanceRequestDataModel.criteria.costCenter}",
            "fixedAssetMaintenanceRequest.executorUnit = #{fixedAssetMaintenanceRequestDataModel.criteria.executorUnit}",
            "fixedAssetMaintenanceRequest.petitioner.contract.employee = #{fixedAssetMaintenanceRequestDataModel.employee}",
            "fixedAssetMaintenanceRequest.requestDate = #{fixedAssetMaintenanceRequestDataModel.criteria.requestDate}",
            "fixedAssetMaintenanceRequest.maintenance.deliveryDate = #{fixedAssetMaintenanceRequestDataModel.approveDate}",
            "fixedAssetMaintenanceRequest in (select maintReq from FixedAssetMaintenanceRequest maintReq left join maintReq.stateHistoryList hist " +
                    "where hist.state = 'REJECTED' " +
                    "and hist.date = #{fixedAssetMaintenanceRequestDataModel.rejectDate})",
            "fixedAssetMaintenanceRequest.maintenance.receiptDate = #{fixedAssetMaintenanceRequestDataModel.receiptDate}",
            "fixedAssetMaintenanceRequest.type = #{fixedAssetMaintenanceRequestDataModel.criteria.type}",
            "fixedAssetMaintenanceRequest.requestState = #{fixedAssetMaintenanceRequestDataModel.criteria.requestState}",
            "fixedAssetMaintenanceRequest.maintenance.receiptState = #{fixedAssetMaintenanceRequestDataModel.receiptState}",
            "lower(fixedAssetMaintenanceRequest.maintenanceReason.value) like concat('%', concat(lower(#{fixedAssetMaintenanceRequestDataModel.maintenanceReason}), '%'))",
            "lower(fixedAssetMaintenanceRequest.maintenance.deliveryDescription.value) like concat('%', concat(lower(#{fixedAssetMaintenanceRequestDataModel.approveDescription}), '%'))",
            "fixedAssetMaintenanceRequest in (select maintReq from FixedAssetMaintenanceRequest maintReq left join maintReq.stateHistoryList hist " +
                    "where hist.state = 'REJECTED' " +
                    "and lower(hist.description.value) like concat('%', concat(lower(#{fixedAssetMaintenanceRequestDataModel.rejectDescription})), '%'))",
            "lower(fixedAssetMaintenanceRequest.maintenance.receiptDescription.value) like concat('%', concat(lower(#{fixedAssetMaintenanceRequestDataModel.receiptDescription}), '%'))",
    };

    @Create
    public void init() {
        sortProperty = "fixedAssetMaintenanceRequest.requestDate";
        sortAsc = false;
    }

    @Override
    public String getEjbql() {
        return "select fixedAssetMaintenanceRequest from FixedAssetMaintenanceRequest fixedAssetMaintenanceRequest";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }

    public CostCenter getCostCenter() {
        return getCriteria().getCostCenter();
    }

    public void setCostCenter(CostCenter costCenter) {
        getCriteria().setCostCenter(costCenter);
    }

    public void clearCostCenter() {
        getCriteria().setCostCenter(null);
    }

    public Employee getEmployee() {
        if (Identity.instance().hasPermission("FIXEDASSETMAINTENANCE", "VIEW")) {
            return employee;
        } else {
            return currentUser.getEmployee();
        }
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public void clearEmployee() {
        setEmployee(null);
    }

    public Date getApproveDate() {
        return approveDate;
    }

    public void setApproveDate(Date approveDate) {
        this.approveDate = approveDate;
    }

    public Date getRejectDate() {
        return rejectDate;
    }

    public void setRejectDate(Date rejectDate) {
        this.rejectDate = rejectDate;
    }

    public Date getReceiptDate() {
        return receiptDate;
    }

    public void setReceiptDate(Date receiptDate) {
        this.receiptDate = receiptDate;
    }

    public String getMaintenanceReason() {
        return maintenanceReason;
    }

    public void setMaintenanceReason(String maintenanceReason) {
        this.maintenanceReason = maintenanceReason;
    }

    public String getApproveDescription() {
        return approveDescription;
    }

    public void setApproveDescription(String approveDescription) {
        this.approveDescription = approveDescription;
    }

    public String getRejectDescription() {
        return rejectDescription;
    }

    public void setRejectDescription(String rejectDescription) {
        this.rejectDescription = rejectDescription;
    }

    public String getReceiptDescription() {
        return receiptDescription;
    }

    public void setReceiptDescription(String receiptDescription) {
        this.receiptDescription = receiptDescription;
    }

    public FixedAssetMaintenanceReceiptState getReceiptState() {
        return receiptState;
    }

    public void setReceiptState(FixedAssetMaintenanceReceiptState receiptState) {
        this.receiptState = receiptState;
    }

    @Override
    public void clear() {
        setApproveDate(null);
        setRejectDate(null);
        setReceiptDate(null);
        setMaintenanceReason(null);
        setApproveDescription(null);
        setRejectDescription(null);
        setReceiptDescription(null);
        setReceiptState(null);
        clearCostCenter();
        clearEmployee();
        super.clear();
    }
}
