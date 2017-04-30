package com.encens.khipus.action.fixedassets.reports;

import com.encens.khipus.action.SessionUser;
import com.encens.khipus.action.reports.GenericReportAction;
import com.encens.khipus.action.reports.PageFormat;
import com.encens.khipus.action.reports.PageOrientation;
import com.encens.khipus.action.reports.ReportFormat;
import com.encens.khipus.model.admin.User;
import com.encens.khipus.model.finances.JobContract;
import com.encens.khipus.model.fixedassets.FixedAssetMaintenance;
import com.encens.khipus.model.fixedassets.FixedAssetMaintenanceRequest;
import com.encens.khipus.model.fixedassets.FixedAssetMaintenanceRequestState;
import com.encens.khipus.model.fixedassets.FixedAssetMaintenanceRequestStateHistory;
import com.encens.khipus.util.DateUtils;
import com.encens.khipus.util.FormatUtils;
import com.encens.khipus.util.MessageUtils;
import com.encens.khipus.util.ValidatorUtil;
import com.jatun.titus.reportgenerator.util.TypedReportData;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Encens S.R.L.
 * Action to generate fixed asset maintenance document
 *
 * @author
 * @version $Id: FixedAssetMaintenanceDocumentReportAction.java  10-nov-2010 19:17:30$
 */
@Name("fixedAssetMaintenanceDocumentReportAction")
@Scope(ScopeType.PAGE)
public class FixedAssetMaintenanceDocumentReportAction extends GenericReportAction {
    @In
    private User currentUser;
    @In
    private SessionUser sessionUser;

    private FixedAssetMaintenanceRequest fixedAssetMaintenanceRequest;

    @Restrict("#{s:hasPermission('FIXEDASSETMAINTENANCE','VIEW')}")
    public void generateReport(FixedAssetMaintenanceRequest fixedAssetMaintenanceRequest) {
        log.debug("Generate FixedAssetMaintenanceDocumentReportAction......" + fixedAssetMaintenanceRequest);

        setFixedAssetMaintenanceRequest(getEntityManager().find(FixedAssetMaintenanceRequest.class, fixedAssetMaintenanceRequest.getId()));

        Map params = new HashMap();
        setReportFormat(ReportFormat.PDF);
        params.putAll(getMaintenanceRequestReportParams(getFixedAssetMaintenanceRequest()));

        //add sub report
        addFixedAssetDetailSubReport(params);
        super.generateReport("voucherDocument", "/fixedassets/reports/fixedAssetMaintenanceDocumentReport.jrxml", PageFormat.LETTER, PageOrientation.PORTRAIT, MessageUtils.getMessage("Reports.maintenanceRequestDoc.title"), params);
    }

    @Override
    protected String getEjbql() {
        return "";
    }

    @Create
    public void init() {
        restrictions = new String[]{};
    }

    /**
     * add fixed asset detail sub report in main report
     *
     * @param mainReportParams main report Map params
     */
    private void addFixedAssetDetailSubReport(Map mainReportParams) {
        log.debug("Generating addFixedAssetDetailSubReport.............................");
        Map<String, Object> params = new HashMap<String, Object>();

        String ejbql = "SELECT " +
                "employee.lastName," +
                "employee.maidenName," +
                "employee.firstName," +
                "fixedAssetSubGroup.fixedAssetSubGroupCode," +
                "fixedAssetSubGroup.description," +
                "fixedAsset.barCode," +
                "fixedAsset.detail," +
                "fixedAsset.measurement," +
                "fixedAsset.registrationDate," +
                "fixedAsset.endDate," +
                "fixedAsset.ufvOriginalValue," +
                "fixedAsset.currencyType," +
                "fixedAsset.state" +
                " FROM FixedAssetMaintenanceRequest fixedAssetMaintenanceRequest" +
                " LEFT JOIN fixedAssetMaintenanceRequest.fixedAssets fixedAsset" +
                " LEFT JOIN fixedAsset.custodianJobContract custodianJobContract" +
                " LEFT JOIN custodianJobContract.contract contract" +
                " LEFT JOIN contract.employee employee" +
                " LEFT JOIN fixedAsset.fixedAssetSubGroup fixedAssetSubGroup";

        String[] restrictions = new String[]{
                "fixedAssetMaintenanceRequest = #{fixedAssetMaintenanceDocumentReportAction.fixedAssetMaintenanceRequest}"};

        String orderBy = "employee.lastName,employee.maidenName,employee.firstName";

        //generate the sub report
        String subReportKey = "FIXEDASSETMAINTENANCEDETAILSUBREPORT";
        TypedReportData subReportData = super.generateSubReport(
                subReportKey,
                "/fixedassets/reports/fixedAssetMaintenanceDetailDocSubReport.jrxml",
                PageFormat.LETTER,
                PageOrientation.PORTRAIT,
                createQueryForSubreport(subReportKey, ejbql, Arrays.asList(restrictions), orderBy),
                params);

        //add in main report params
        mainReportParams.putAll(subReportData.getReportParams());
        mainReportParams.put(subReportKey, subReportData.getJasperReport());
    }

    /**
     * get maintenance reques info as report params
     *
     * @param fixedAssetMaintenanceRequest
     * @return Map
     */
    private Map<String, Object> getMaintenanceRequestReportParams(FixedAssetMaintenanceRequest fixedAssetMaintenanceRequest) {

        Map<String, Object> paramMap = new HashMap<String, Object>();
        paramMap.put("userLoginParam", currentUser.getEmployee().getFullName());

        paramMap.put("codeParam", paramAsString(fixedAssetMaintenanceRequest.getCode()));
        paramMap.put("requestDateParam", formatDate(fixedAssetMaintenanceRequest.getRequestDate()));
        paramMap.putAll(getPetitionerInfoParams(fixedAssetMaintenanceRequest));
        paramMap.put("costCenterParam", fixedAssetMaintenanceRequest.getCostCenter() != null ? paramAsString(fixedAssetMaintenanceRequest.getCostCenter().getFullName()) : "");
        paramMap.put("executorUnitParam", fixedAssetMaintenanceRequest.getExecutorUnit() != null ? paramAsString(fixedAssetMaintenanceRequest.getExecutorUnit().getFullName()) : "");
        paramMap.put("requestStateParam", fixedAssetMaintenanceRequest.getRequestState() != null ? MessageUtils.getMessage(fixedAssetMaintenanceRequest.getRequestState().getResourceKey()) : "");
        paramMap.put("typeParam", fixedAssetMaintenanceRequest.getType() != null ? MessageUtils.getMessage(fixedAssetMaintenanceRequest.getType().getResourceKey()) : "");
        paramMap.put("maintenanceReasonParam", fixedAssetMaintenanceRequest.getMaintenanceReason() != null ? paramAsString(fixedAssetMaintenanceRequest.getMaintenanceReason().getValue()) : "");

        //input history detail info
        Boolean isRejectedStateParam = FixedAssetMaintenanceRequestState.REJECTED.equals(fixedAssetMaintenanceRequest.getRequestState());
        FixedAssetMaintenanceRequestStateHistory stateHistory = getFixedAssetMaintenanceRequestStateHistory(fixedAssetMaintenanceRequest);
        paramMap.put("inputDetailTitleParam", isRejectedStateParam
                ? MessageUtils.getMessage("FixedAssetMaintenanceRequest.maintenanceRequestRejected") : MessageUtils.getMessage("FixedAssetMaintenanceRequest.maintenanceRequestApproved"));
        paramMap.put("actionDateParam", stateHistory != null ? formatDate(stateHistory.getDate()) : "");
        paramMap.put("actionDescriptionParam", (stateHistory != null && stateHistory.getDescription() != null) ? paramAsString(stateHistory.getDescription().getValue()) : "");

        //maintenance detail info
        String estimatedReceiptDateParam = "";
        String receiptDate = "";
        String receiptState = "";
        String receiptDescription = "";
        String amountParam = "";

        FixedAssetMaintenance maintenance = fixedAssetMaintenanceRequest.getMaintenance();
        if (maintenance != null) {
            receiptDate = formatDate(maintenance.getReceiptDate());
            estimatedReceiptDateParam = formatDate(maintenance.getEstimatedReceiptDate());
            receiptState = maintenance.getReceiptState() != null ? paramAsString(maintenance.getReceiptState().getName()) : "";
            receiptDescription = maintenance.getReceiptDescription() != null ? paramAsString(maintenance.getReceiptDescription().getValue()) : "";
            if (maintenance.getAmount() != null && maintenance.getCurrency() != null) {
                amountParam = FormatUtils.toAcronym(FormatUtils.formatNumber(maintenance.getAmount(), messages.get("patterns.decimalNumber"), sessionUser.getLocale()),
                        maintenance.getCurrency().getSymbol());
            }
        }
        paramMap.put("showEstimatedReceiptDateParam", !ValidatorUtil.isBlankOrNull(estimatedReceiptDateParam) && !isRejectedStateParam);
        paramMap.put("estimatedReceiptDateParam", estimatedReceiptDateParam);
        paramMap.put("receiptDateParam", receiptDate);
        paramMap.put("receiptStateParam", receiptState);
        paramMap.put("receiptDescriptionParam", receiptDescription);
        paramMap.put("amountParam", amountParam);


        return paramMap;
    }

    /**
     * Get petitioner employee info as report params
     *
     * @param fixedAssetMaintenanceRequest
     * @return Map
     */
    private Map<String, Object> getPetitionerInfoParams(FixedAssetMaintenanceRequest fixedAssetMaintenanceRequest) {
        Map<String, Object> paramMap = new HashMap<String, Object>();
        String employeeName = "";
        String chargeName = "";

        JobContract jobContract = fixedAssetMaintenanceRequest.getPetitioner();
        if (jobContract != null) {
            if (jobContract.getContract() != null && jobContract.getContract().getEmployee() != null) {
                employeeName = paramAsString(jobContract.getContract().getEmployee().getFullName());
            }
            if (jobContract.getJob() != null && jobContract.getJob().getCharge() != null) {
                chargeName = paramAsString(jobContract.getJob().getCharge().getName());
            }
        }

        paramMap.put("employeeParam", employeeName);
        paramMap.put("chargeParam", chargeName);
        return paramMap;
    }

    /**
     * get maintenance history entitie
     *
     * @param fixedAssetMaintenanceRequest
     * @return FixedAssetMaintenanceRequestStateHistory
     */
    private FixedAssetMaintenanceRequestStateHistory getFixedAssetMaintenanceRequestStateHistory(FixedAssetMaintenanceRequest fixedAssetMaintenanceRequest) {
        for (FixedAssetMaintenanceRequestStateHistory stateHistory : fixedAssetMaintenanceRequest.getStateHistoryList()) {
            if (stateHistory.getState() == FixedAssetMaintenanceRequestState.APPROVED ||
                    stateHistory.getState() == FixedAssetMaintenanceRequestState.REJECTED) {
                return stateHistory;
            }
        }
        return null;
    }

    private String formatDate(Date date) {
        return (date != null) ? DateUtils.format(date, MessageUtils.getMessage("patterns.date")) : "";
    }

    private String paramAsString(Object value) {
        return value != null ? value.toString() : "";
    }

    public FixedAssetMaintenanceRequest getFixedAssetMaintenanceRequest() {
        return fixedAssetMaintenanceRequest;
    }

    public void setFixedAssetMaintenanceRequest(FixedAssetMaintenanceRequest fixedAssetMaintenanceRequest) {
        this.fixedAssetMaintenanceRequest = fixedAssetMaintenanceRequest;
    }
}
