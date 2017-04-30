package com.encens.khipus.action.fixedassets;

import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.framework.action.Outcome;
import com.encens.khipus.framework.service.GenericService;
import com.encens.khipus.model.common.Text;
import com.encens.khipus.model.finances.JobContract;
import com.encens.khipus.model.fixedassets.*;
import com.encens.khipus.service.common.SequenceGeneratorService;
import com.encens.khipus.service.fixedassets.FixedAssetMaintenanceService;
import com.encens.khipus.util.Constants;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.international.StatusMessage;

import javax.faces.event.ValueChangeEvent;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Actions for FixedAssetMaintenanceRequest
 *
 * @author
 * @version 2.25
 */
@Name("fixedAssetMaintenanceRequestAction")
@Scope(ScopeType.CONVERSATION)
public class FixedAssetMaintenanceRequestAction extends GenericAction<FixedAssetMaintenanceRequest> {

    @In
    private FixedAssetMaintenanceService fixedAssetMaintenanceService;

    @In
    private SequenceGeneratorService sequenceGeneratorService;

    private int action;
    private Date actionDate;
    private Date estimatedReceiptDate;
    private String actionDescription;

    @Factory(value = "fixedAssetMaintenanceRequest")
    @Restrict("#{s:hasPermission('FIXEDASSETMAINTENANCEREQUEST','VIEW') or s:hasPermission('FIXEDASSETMAINTENANCE','VIEW')}")
    public FixedAssetMaintenanceRequest initFixedAssetMaintenanceRequest() {
        if (!isManaged()) {
            getInstance().setRequestDate(Calendar.getInstance().getTime());
            getInstance().setRequestState(FixedAssetMaintenanceRequestState.PENDING);
            getInstance().setFixedAssets(new ArrayList<FixedAsset>());
        }

        return getInstance();
    }

    @Factory(value = "fixedAssetMaintenanceRequestType", scope = ScopeType.STATELESS)
    public FixedAssetMaintenanceRequestType[] getFixedAssetMaintenanceRequestType() {
        return FixedAssetMaintenanceRequestType.values();
    }

    @Factory(value = "fixedAssetMaintenanceRequestState", scope = ScopeType.STATELESS)
    public FixedAssetMaintenanceRequestState[] getFixedAssetMaintenanceRequestState() {
        return FixedAssetMaintenanceRequestState.values();
    }

    @Override
    protected GenericService getService() {
        return fixedAssetMaintenanceService;
    }

    @Override
    protected String getDisplayNameMessage() {
        return messages.get("FixedAssetMaintenanceRequest.maintenanceRequest");
    }

    public String getMaintenanceReason() {
        return getInstance().getMaintenanceReason() != null ? getInstance().getMaintenanceReason().getValue() : null;
    }

    public void setMaintenanceReason(String maintenanceReason) {
        if (getInstance().getMaintenanceReason() == null) {
            getInstance().setMaintenanceReason(new Text(maintenanceReason));
        } else {
            getInstance().getMaintenanceReason().setValue(maintenanceReason);
        }
    }

    public int getAction() {
        return action;
    }

    public void setAction(int action) {
        this.action = action;
    }

    public Date getActionDate() {
        for (FixedAssetMaintenanceRequestStateHistory stateHistory : getInstance().getStateHistoryList()) {
            if (stateHistory.getState() == FixedAssetMaintenanceRequestState.APPROVED ||
                    stateHistory.getState() == FixedAssetMaintenanceRequestState.REJECTED) {
                actionDate = stateHistory.getDate();
            }
        }
        return actionDate;
    }

    public void setActionDate(Date actionDate) {
        this.actionDate = actionDate;
    }

    public Date getEstimatedReceiptDate() {
        if (getInstance().getMaintenance() != null) {
            estimatedReceiptDate = getInstance().getMaintenance().getEstimatedReceiptDate();
        }

        return estimatedReceiptDate;
    }

    public void setEstimatedReceiptDate(Date estimatedReceiptDate) {
        this.estimatedReceiptDate = estimatedReceiptDate;
    }

    public void changeAction(ValueChangeEvent event) {
        if (event.getNewValue().equals(0)) { // APPROVE
            action = 0;
        } else if (event.getNewValue().equals(1)) { // REJECT
            action = 1;
        }
    }

    public boolean isEstimatedReceiptDateEnabled() {
        return action == 0;
    }

    public String getActionDescription() {
        for (FixedAssetMaintenanceRequestStateHistory stateHistory : getInstance().getStateHistoryList()) {
            if (stateHistory.getState() == FixedAssetMaintenanceRequestState.APPROVED ||
                    stateHistory.getState() == FixedAssetMaintenanceRequestState.REJECTED) {
                actionDescription = stateHistory.getDescription().getValue();
            }
        }
        return actionDescription;
    }

    public void setActionDescription(String actionDescription) {
        this.actionDescription = actionDescription;
    }

    public String getReceiptDescription() {
        if (getInstance().getMaintenance().getReceiptDescription() != null) {
            return getInstance().getMaintenance().getReceiptDescription().getValue();
        } else {
            return null;
        }
    }

    public void setReceiptDescription(String receiptDescription) {
        if (getInstance().getMaintenance().getReceiptDescription() == null) {
            getInstance().getMaintenance().setReceiptDescription(new Text(receiptDescription));
        } else {
            getInstance().getMaintenance().getReceiptDescription().setValue(receiptDescription);
        }
    }

    public JobContract getPetitioner() {
        return getInstance().getPetitioner();
    }

    public String getActionTitle() {
        String actionTitle = null;

        if (isPendingMaintenanceRequest()) {
            actionTitle = "FixedAssetMaintenanceRequest.maintenanceRequestAction";
        } else if (isApprovedMaintenanceRequest() || isFinishedMaintenanceRequest()) {
            actionTitle = "FixedAssetMaintenanceRequest.maintenanceRequestApproved";
        } else if (isRejectedMaintenanceRequest()) {
            actionTitle = "FixedAssetMaintenanceRequest.maintenanceRequestRejected";
        }

        return actionTitle;
    }

    public boolean isPendingMaintenanceRequest() {
        return getInstance().getRequestState() == FixedAssetMaintenanceRequestState.PENDING;
    }

    public boolean isApprovedMaintenanceRequest() {
        return getInstance().getRequestState() == FixedAssetMaintenanceRequestState.APPROVED;
    }

    public boolean isRejectedMaintenanceRequest() {
        return getInstance().getRequestState() == FixedAssetMaintenanceRequestState.REJECTED;
    }

    public boolean isFinishedMaintenanceRequest() {
        return getInstance().getRequestState() == FixedAssetMaintenanceRequestState.FINISHED;
    }

    public void changePetitioner() {
        JobContract petitioner = getInstance().getPetitioner();
        getInstance().setFixedAssets(new ArrayList<FixedAsset>());
        if (petitioner != null) {
            getInstance().setCostCenter(petitioner.getJob().getOrganizationalUnit().getCostCenter());
            getInstance().setExecutorUnit(petitioner.getJob().getOrganizationalUnit().getBusinessUnit());
        } else {
            getInstance().setCostCenter(null);
            getInstance().setExecutorUnit(null);
        }
    }

    public void addFixedAssetList(List<FixedAsset> selectedFixedAssetList) {
        for (FixedAsset fixedAsset : selectedFixedAssetList) {
            if (!getInstance().getFixedAssets().contains(fixedAsset)) {
                getInstance().getFixedAssets().add(fixedAsset);
            }
        }
    }

    public void removeFixedAsset(FixedAsset fixedAsset) {
        getInstance().getFixedAssets().remove(fixedAsset);
    }

    @End
    public String sendRequest() {
        getInstance().setCode(String.valueOf(sequenceGeneratorService.nextValue(Constants.FIXEDASSETMAINTENANCEREQUEST_CODE)));

        if (getInstance().getFixedAssets().size() > 0) {
            return create();
        } else {
            addFixedAssetsNotSelectedMessage();
            return Outcome.REDISPLAY;
        }
    }

    @End(beforeRedirect = true)
    public String processRequest() {
        FixedAssetMaintenanceRequestState newState;

        if (action == 0) { // APPROVE
            newState = FixedAssetMaintenanceRequestState.APPROVED;

            FixedAssetMaintenance maintenance = new FixedAssetMaintenance();
            maintenance.setState(FixedAssetMaintenanceState.IN_PROGRESS);
            maintenance.setDeliveryDate(actionDate);
            maintenance.setDeliveryDescription(new Text(actionDescription));
            maintenance.setEstimatedReceiptDate(estimatedReceiptDate);
            getInstance().setMaintenance(maintenance);
        } else { // REJECT
            newState = FixedAssetMaintenanceRequestState.REJECTED;
        }

        if (getInstance().getStateHistoryList() == null) {
            getInstance().setStateHistoryList(new ArrayList<FixedAssetMaintenanceRequestStateHistory>());
        }
        FixedAssetMaintenanceRequestStateHistory stateHistory = new FixedAssetMaintenanceRequestStateHistory();
        stateHistory.setDate(actionDate);
        stateHistory.setDescription(new Text(actionDescription));
        stateHistory.setState(newState);
        getInstance().getStateHistoryList().add(stateHistory);

        getInstance().setRequestState(newState);

        return update();
    }

    @End
    public String receiptMaintenance() {
        getInstance().setRequestState(FixedAssetMaintenanceRequestState.FINISHED);
        getInstance().getMaintenance().setState(FixedAssetMaintenanceState.COMPLETED);

        return update();
    }

    public void clearMaintenanceCurrencyAndAmount() {
        getInstance().getMaintenance().setCurrency(null);
        getInstance().getMaintenance().setAmount(null);
    }

    public void addFixedAssetsNotSelectedMessage() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                "FixedAssetMaintenanceRequest.message.fixedAssetsNotSelectedMessage");
    }
}
