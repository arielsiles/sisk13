package com.encens.khipus.action.fixedassets.reports;

import com.encens.khipus.action.SessionUser;
import com.encens.khipus.action.reports.GenericReportAction;
import com.encens.khipus.action.reports.PageFormat;
import com.encens.khipus.action.reports.PageOrientation;
import com.encens.khipus.action.reports.ReportFormat;
import com.encens.khipus.model.admin.User;
import com.encens.khipus.model.finances.CashAccount;
import com.encens.khipus.model.finances.FinancesBankAccount;
import com.encens.khipus.model.finances.JobContract;
import com.encens.khipus.model.fixedassets.FixedAssetPayment;
import com.encens.khipus.model.fixedassets.FixedAssetVoucher;
import com.encens.khipus.model.purchases.PurchaseOrderPaymentType;
import com.encens.khipus.util.DateUtils;
import com.encens.khipus.util.FormatUtils;
import com.encens.khipus.util.MessageUtils;
import com.jatun.titus.reportgenerator.util.TypedReportData;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Encens S.R.L.
 * Action to generate fixed asset voucher document
 *
 * @author
 * @version $Id: FixedAssetVoucherDocumentReportAction.java  03-nov-2010 17:46:03$
 */
@Name("fixedAssetVoucherDocumentReportAction")
@Scope(ScopeType.PAGE)
public class FixedAssetVoucherDocumentReportAction extends GenericReportAction {

    @In
    User currentUser;
    @In
    private SessionUser sessionUser;

    private FixedAssetVoucher fixedAssetVoucher;

    @Restrict("#{s:hasPermission('FIXEDASSETVOUCHER','VIEW')}")
    public void generateReport(FixedAssetVoucher fixedAssetVoucher, String reportFormat) {
        setReportFormat(ReportFormat.valueOf(reportFormat));
        generateReport(fixedAssetVoucher);
    }

    @Restrict("#{s:hasPermission('FIXEDASSETVOUCHER','VIEW')}")
    public void generateReport(FixedAssetVoucher fixedAssetVoucher) {
        log.debug("Generate FixedAssetVoucherDocumentReportAction......" + fixedAssetVoucher);
        setFixedAssetVoucher(getEntityManager().find(FixedAssetVoucher.class, fixedAssetVoucher.getId()));
        PageFormat pageFormat = PageFormat.LETTER;
        String templatePath;
        String fileName = getFixedAssetVoucher().getFixedAssetVoucherType() != null ? getFixedAssetVoucher().getFixedAssetVoucherType().getFullName() : "fixedAssetVoucherDocument";

        Map params = new HashMap();
        if (getFixedAssetVoucher().isRegistrationMovement()) {
            templatePath = "/fixedassets/reports/fixedAssetVoucherRegistrationDocReport.jrxml";
            params.putAll(getRegistrationDocumentParamsInfo(getFixedAssetVoucher()));
            addFixedAssetMovementForRegistrationSubReport(params);
            addPurchaseOrderFixedAssetPartSubReport(params);
        } else if (getFixedAssetVoucher().isDischargeMovement()) {
            templatePath = "/fixedassets/reports/fixedAssetVoucherDischargeDocReport.jrxml";
            params.putAll(getDischargeDocumentParamsInfo(getFixedAssetVoucher()));
            addFixedAssetMovementCommonSubReport(params);

        } else if (getFixedAssetVoucher().isTransferenceMovement()) {
            templatePath = "/fixedassets/reports/fixedAssetVoucherTransferenceDocReport.jrxml";
            params.putAll(getTransferenceDocumentParamsInfo(getFixedAssetVoucher()));
            addFixedAssetMovementTransferenceSubReport(params);
            pageFormat = PageFormat.LEGAL;

        } else if (getFixedAssetVoucher().isImprovementMovement()) {
            templatePath = "/fixedassets/reports/fixedAssetVoucherImprovementDocReport.jrxml";
            params.putAll(getImprovementDocumentParamsInfo(getFixedAssetVoucher()));
            addFixedAssetMovementImprovementSubReport(params);

        } else {
            //set default params
            templatePath = "/fixedassets/reports/fixedAssetVoucherRegistrationDocReport.jrxml";
            params.putAll(getRegistrationDocumentParamsInfo(getFixedAssetVoucher()));
            //add sub report
            addFixedAssetMovementCommonSubReport(params);
        }

        super.generateReport("fixedAssetVoucherDocument", templatePath, pageFormat, PageOrientation.LANDSCAPE, fileName, params);
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
     * add fixed asset movement detail sub report in main report
     *
     * @param mainReportParams main report Map params
     */
    private void addFixedAssetMovementCommonSubReport(Map mainReportParams) {
        log.debug("Generating addFixedAssetMovementCommonSubReport.............................");
        Map<String, Object> params = new HashMap<String, Object>();

        String ejbql = "SELECT " +
                "fixedAssetGroup.groupCode," +
                "fixedAssetGroup.description," +
                "fixedAssetSubGroup.fixedAssetSubGroupCode," +
                "fixedAssetSubGroup.description," +
                "fixedAsset.barCode," +
                "fixedAsset.detail," +
                "fixedAsset.measurement," +
                "fixedAsset.description," +
                "fixedAsset.trademark," +
                "fixedAsset.model," +
                "fixedAsset.duration," +
                "fixedAsset.ufvOriginalValue," +
                "fixedAsset.currencyType, " +
                "fixedAsset.sequence, " +
                "fixedAsset.registrationDate " +
                " FROM FixedAssetMovement fixedAssetMovement" +
                " LEFT JOIN fixedAssetMovement.fixedAsset fixedAsset" +
                " LEFT JOIN fixedAsset.fixedAssetSubGroup fixedAssetSubGroup" +
                " LEFT JOIN fixedAssetSubGroup.fixedAssetGroup fixedAssetGroup";

        String[] restrictions = new String[]{
                "fixedAssetMovement.fixedAssetVoucher = #{fixedAssetVoucherDocumentReportAction.fixedAssetVoucher}"};

        String orderBy = "fixedAssetGroup.groupCode";

        //generate the sub report
        String subReportKey = "FIXEDASSETMOVEMENTCOMMONSUBREPORT";
        TypedReportData subReportData = super.generateSubReport(
                subReportKey,
                "/fixedassets/reports/fixedAssetMovementCommonSubReport.jrxml",
                PageFormat.LETTER,
                PageOrientation.LANDSCAPE,
                createQueryForSubreport(subReportKey, ejbql, Arrays.asList(restrictions), orderBy),
                params);

        //add in main report params
        mainReportParams.putAll(subReportData.getReportParams());
        mainReportParams.put(subReportKey, subReportData.getJasperReport());
    }

    /**
     * add fixed asset movement transference detail sub report in main report
     *
     * @param mainReportParams main report Map params
     */
    private void addFixedAssetMovementTransferenceSubReport(Map mainReportParams) {
        log.debug("Generating addFixedAssetMovementTransferenceSubReport.............................");
        Map<String, Object> params = new HashMap<String, Object>();

        String ejbql = "SELECT " +
                "fixedAssetGroup.groupCode," +
                "fixedAssetGroup.description," +
                "fixedAssetSubGroup.fixedAssetSubGroupCode," +
                "fixedAssetSubGroup.description," +
                "fixedAsset.barCode," +
                "fixedAsset.detail," +
                "fixedAsset.measurement," +
                "fixedAsset.description," +
                "fixedAsset.trademark," +
                "fixedAsset.model," +
                "fixedAsset.duration," +
                "fixedAsset.ufvOriginalValue," +
                "fixedAsset.currencyType," +
                "lastBusinessUnit.executorUnitCode," +
                "organization.name," +
                "lastCostCenter.code," +
                "lastCostCenter.description," +
                "lastEmployee.lastName," +
                "lastEmployee.maidenName," +
                "lastEmployee.firstName," +
                "fixedAsset.sequence," +
                "fixedAsset.registrationDate" +
                " FROM FixedAssetMovement fixedAssetMovement" +
                " LEFT JOIN fixedAssetMovement.fixedAsset fixedAsset" +
                " LEFT JOIN fixedAsset.fixedAssetSubGroup fixedAssetSubGroup" +
                " LEFT JOIN fixedAssetSubGroup.fixedAssetGroup fixedAssetGroup" +
                " LEFT JOIN fixedAssetMovement.lastBusinessUnit lastBusinessUnit" +
                " LEFT JOIN lastBusinessUnit.organization organization" +
                " LEFT JOIN fixedAssetMovement.lastCostCenter lastCostCenter" +
                " LEFT JOIN fixedAssetMovement.lastCustodian lastEmployee";

        String[] restrictions = new String[]{
                "fixedAssetMovement.fixedAssetVoucher = #{fixedAssetVoucherDocumentReportAction.fixedAssetVoucher}"};

        String orderBy = "fixedAssetGroup.groupCode";

        //generate the sub report 
        String subReportKey = "FIXEDASSETMOVEMENTTRANSFERENCESUBREPORT";
        TypedReportData subReportData = super.generateSubReport(
                subReportKey,
                "/fixedassets/reports/fixedAssetMovementTransferenceSubReport.jrxml",
                PageFormat.LEGAL,
                PageOrientation.LANDSCAPE,
                createQueryForSubreport(subReportKey, ejbql, Arrays.asList(restrictions), orderBy),
                params);

        //add in main report params
        mainReportParams.putAll(subReportData.getReportParams());
        mainReportParams.put(subReportKey, subReportData.getJasperReport());
    }

    /**
     * add fixed asset voucher improvement movement detail sub report in main report
     *
     * @param mainReportParams a map of mainReportParams
     */
    private void addFixedAssetMovementImprovementSubReport(Map mainReportParams) {
        log.debug("Generating addFixedAssetMovementImprovementSubReport.............................");
        Map<String, Object> params = new HashMap<String, Object>();

        String ejbql = "SELECT " +
                "fixedAssetGroup.groupCode," +
                "fixedAssetGroup.description," +
                "fixedAssetSubGroup.fixedAssetSubGroupCode," +
                "fixedAssetSubGroup.description," +
                "fixedAsset.barCode," +
                "fixedAsset.detail," +
                "fixedAsset.measurement," +
                "fixedAsset.description," +
                "fixedAsset.trademark," +
                "fixedAsset.model," +
                "fixedAsset.duration," +
                "fixedAsset.ufvOriginalValue," +
                "fixedAsset.currencyType," +
                "fixedAssetMovement.bsAmount," +
                "fixedAsset.sequence," +
                "fixedAsset.registrationDate" +
                " FROM FixedAssetMovement fixedAssetMovement" +
                " LEFT JOIN fixedAssetMovement.fixedAsset fixedAsset" +
                " LEFT JOIN fixedAsset.fixedAssetSubGroup fixedAssetSubGroup" +
                " LEFT JOIN fixedAssetSubGroup.fixedAssetGroup fixedAssetGroup";

        String[] restrictions = new String[]{
                "fixedAssetMovement.fixedAssetVoucher = #{fixedAssetVoucherDocumentReportAction.fixedAssetVoucher}"};

        String orderBy = "fixedAssetGroup.groupCode";

        //generate the sub report
        String subReportKey = "FIXEDASSETMOVEMENTIMPROVEMENTSUBREPORT";
        TypedReportData subReportData = super.generateSubReport(
                subReportKey,
                "/fixedassets/reports/fixedAssetMovementImprovementSubReport.jrxml",
                PageFormat.LETTER,
                PageOrientation.LANDSCAPE,
                createQueryForSubreport(subReportKey, ejbql, Arrays.asList(restrictions), orderBy),
                params);

        //add in main report params
        mainReportParams.putAll(subReportData.getReportParams());
        mainReportParams.put(subReportKey, subReportData.getJasperReport());
    }


    /**
     * get common params
     *
     * @param fixedAssetVoucher voucher
     * @return Map Common params
     */
    private Map<String, Object> getCommonParams(FixedAssetVoucher fixedAssetVoucher) {

        Map<String, Object> paramMap = new HashMap<String, Object>();
        paramMap.put("userLoginParam", currentUser.getEmployee().getFullName());
        paramMap.put("movementTypeParam", fixedAssetVoucher.getFixedAssetVoucherType() != null ? paramAsString(fixedAssetVoucher.getFixedAssetVoucherType().getFullName()) : "");
        paramMap.put("voucherCodeParam", paramAsString(fixedAssetVoucher.getVoucherCode()));
        paramMap.put("stateParam", fixedAssetVoucher.getState() != null ? MessageUtils.getMessage(fixedAssetVoucher.getState().getResourceKey()) : "");
        return paramMap;
    }

    /**
     * Registration fixed asset voucher data as report params
     *
     * @param fixedAssetVoucher the fixedAsset voucher
     * @return Map
     */
    private Map<String, Object> getRegistrationDocumentParamsInfo(FixedAssetVoucher fixedAssetVoucher) {
        Map<String, Object> paramMap = new HashMap<String, Object>();

        paramMap.putAll(getCommonParams(fixedAssetVoucher));
        paramMap.put("purchaseOrderParam", fixedAssetVoucher.getPurchaseOrder() != null ? paramAsString(fixedAssetVoucher.getPurchaseOrder().getOrderNumber()) : "");
        paramMap.putAll(getCustodianInfoParams(fixedAssetVoucher));
        paramMap.put("businessUnitParam", fixedAssetVoucher.getBusinessUnit() != null ? paramAsString(fixedAssetVoucher.getBusinessUnit().getFullName()) : "");
        paramMap.put("costCenterParam", fixedAssetVoucher.getCostCenter() != null ? paramAsString(fixedAssetVoucher.getCostCenter().getFullName()) : "");
        paramMap.put("causeParam", paramAsString(fixedAssetVoucher.getCause()));
        return paramMap;
    }

    /**
     * Discharge fixed asset voucher data as report params
     *
     * @param fixedAssetVoucher voucher
     * @return Document params info
     */
    private Map<String, Object> getDischargeDocumentParamsInfo(FixedAssetVoucher fixedAssetVoucher) {
        Map<String, Object> paramMap = new HashMap<String, Object>();

        paramMap.putAll(getCommonParams(fixedAssetVoucher));
        paramMap.put("causeParam", paramAsString(fixedAssetVoucher.getCause()));
        return paramMap;
    }

    /**
     * Transference fixed asset voucher data as report params
     *
     * @param fixedAssetVoucher voucher
     * @return Transference Document params info
     */
    private Map<String, Object> getTransferenceDocumentParamsInfo(FixedAssetVoucher fixedAssetVoucher) {
        Map<String, Object> paramMap = new HashMap<String, Object>();

        paramMap.putAll(getCommonParams(fixedAssetVoucher));
        paramMap.putAll(getCustodianInfoParams(fixedAssetVoucher));
        paramMap.put("businessUnitParam", fixedAssetVoucher.getBusinessUnit() != null ? paramAsString(fixedAssetVoucher.getBusinessUnit().getFullName()) : "");
        paramMap.put("costCenterParam", fixedAssetVoucher.getCostCenter() != null ? paramAsString(fixedAssetVoucher.getCostCenter().getFullName()) : "");
        return paramMap;
    }

    /**
     * Improvement fixed asset voucher data as report params
     *
     * @param fixedAssetVoucher voucher
     * @return Improvement document params info
     */
    private Map<String, Object> getImprovementDocumentParamsInfo(FixedAssetVoucher fixedAssetVoucher) {
        Map<String, Object> paramMap = new HashMap<String, Object>();
        boolean isWithBank = false;
        String sourceAmountLabel = "";
        String paymentType = "";
        String beneficiaryName = "";
        String bankAccount = "";
        String bankAccountNumber = "";
        String cashBoxCashAccount = "";
        String sourceCurrency = "";
        String sourceAmount = "";
        String exchangeRate = "";
        String payAmount = "";
        String creationDate = "";
        String description = "";

        FixedAssetPayment fixedAssetPayment = fixedAssetVoucher.getFixedAssetPayment();
        if (fixedAssetPayment != null) {
            PurchaseOrderPaymentType purchaseOrderPaymentType = fixedAssetPayment.getPaymentType();
            isWithBank = PurchaseOrderPaymentType.PAYMENT_BANK_ACCOUNT.equals(purchaseOrderPaymentType) || PurchaseOrderPaymentType.PAYMENT_WITH_CHECK.equals(purchaseOrderPaymentType);
            sourceAmountLabel = PurchaseOrderPaymentType.PAYMENT_BANK_ACCOUNT.equals(purchaseOrderPaymentType) ? MessageUtils.getMessage("FixedAssetPayment.sourceBankAmount")
                    : PurchaseOrderPaymentType.PAYMENT_WITH_CHECK.equals(purchaseOrderPaymentType) ? MessageUtils.getMessage("FixedAssetPayment.sourceCheckAmount")
                    : MessageUtils.getMessage("FixedAssetPayment.sourceCashBoxAmount");

            paymentType = MessageUtils.getMessage(purchaseOrderPaymentType.getResourceKey());
            beneficiaryName = paramAsString(fixedAssetPayment.getBeneficiaryName());
            bankAccountNumber = paramAsString(fixedAssetPayment.getBankAccountNumber());

            FinancesBankAccount financesBankAccount = fixedAssetPayment.getBankAccount();
            if (financesBankAccount != null) {
                bankAccount = paramAsString(financesBankAccount.getDescription()) + " " + (financesBankAccount.getCurrency() != null ? MessageUtils.getMessage(financesBankAccount.getCurrency().getSymbolResourceKey()) : "");
            }

            CashAccount cashAccount = fixedAssetPayment.getCashBoxCashAccount();
            if (cashAccount != null) {
                cashBoxCashAccount = paramAsString(cashAccount.getFullName());
            }

            String sourceCurrencySymbol = "";
            if (fixedAssetPayment.getSourceCurrency() != null) {
                sourceCurrencySymbol = MessageUtils.getMessage(fixedAssetPayment.getSourceCurrency().getSymbolResourceKey());
                sourceCurrency = MessageUtils.getMessage(fixedAssetPayment.getSourceCurrency().getResourceKey()) + " (" + sourceCurrencySymbol + ")";
            }

            sourceAmount = formatDecimalNumber(fixedAssetPayment.getSourceAmount()) + " " + sourceCurrencySymbol;
            exchangeRate = formatDecimalNumber(fixedAssetPayment.getExchangeRate(), MessageUtils.getMessage("patterns.decimal6FNumber"));
            payAmount = formatDecimalNumber(fixedAssetPayment.getPayAmount()) + " " + MessageUtils.getMessage(fixedAssetPayment.getPayCurrency().getSymbolResourceKey());
            creationDate = formatDate(fixedAssetPayment.getCreationDate());
            description = paramAsString(fixedAssetPayment.getDescription());
        }

        paramMap.putAll(getCommonParams(fixedAssetVoucher));
        paramMap.put("paymentTypeParam", paymentType);
        paramMap.put("beneficiaryNameParam", beneficiaryName);
        paramMap.put("bankAccountParam", bankAccount);
        paramMap.put("bankAccountNumberParam", bankAccountNumber);
        paramMap.put("cashBoxCashAccountParam", cashBoxCashAccount);
        paramMap.put("sourceCurrencyParam", sourceCurrency);
        paramMap.put("sourceAmountParam", sourceAmount);
        paramMap.put("exchangeRateParam", exchangeRate);
        paramMap.put("payAmountParam", payAmount);
        paramMap.put("creationDateParam", creationDate);
        paramMap.put("descriptionParam", description);

        paramMap.put("isWithBankParam", String.valueOf(isWithBank));
        paramMap.put("sourceAmountLabelParam", sourceAmountLabel);
        return paramMap;
    }

    /**
     * Get custodian info as report params
     *
     * @param fixedAssetVoucher voucher
     * @return Map
     */
    private Map<String, Object> getCustodianInfoParams(FixedAssetVoucher fixedAssetVoucher) {
        Map<String, Object> paramMap = new HashMap<String, Object>();
        String employeeName = "";
        String chargeName = "";
        String organizationalUnitName = "";

        JobContract jobContract = fixedAssetVoucher.getCustodianJobContract();
        if (jobContract != null) {
            if (jobContract.getContract() != null && jobContract.getContract().getEmployee() != null) {
                employeeName = paramAsString(jobContract.getContract().getEmployee().getFullName());
            }
            if (jobContract.getJob() != null) {
                if (jobContract.getJob().getCharge() != null) {
                    chargeName = paramAsString(jobContract.getJob().getCharge().getName());
                }
                if (jobContract.getJob().getOrganizationalUnit() != null) {
                    organizationalUnitName = paramAsString(jobContract.getJob().getOrganizationalUnit().getFullName());
                }
            }
        }

        paramMap.put("custodianParam", employeeName);
        paramMap.put("chargeParam", chargeName);
        paramMap.put("organizationalUnitParam", organizationalUnitName);
        return paramMap;
    }

    private String paramAsString(Object value) {
        return value != null ? value.toString() : "";
    }

    private String formatDecimalNumber(BigDecimal bigDecimal) {
        return (bigDecimal != null) ? FormatUtils.formatNumber(bigDecimal, MessageUtils.getMessage("patterns.decimalNumber"), sessionUser.getLocale()) : "";
    }

    private String formatDecimalNumber(BigDecimal bigDecimal, String pattern) {
        return (bigDecimal != null) ? FormatUtils.formatNumber(bigDecimal, pattern, sessionUser.getLocale()) : "";
    }

    private String formatDate(Date date) {
        return (date != null) ? DateUtils.format(date, MessageUtils.getMessage("patterns.date")) : "";
    }

    public FixedAssetVoucher getFixedAssetVoucher() {
        return fixedAssetVoucher;
    }

    public void setFixedAssetVoucher(FixedAssetVoucher fixedAssetVoucher) {
        this.fixedAssetVoucher = fixedAssetVoucher;
    }

    /**
     * add fixed asset movement detail sub report in main report
     *
     * @param mainReportParams main report Map params
     */
    private void addFixedAssetMovementForRegistrationSubReport(Map mainReportParams) {
        log.debug("Generating addFixedAssetMovementForRegistrationSubReport.............................!!!!!!!");
        Map<String, Object> params = new HashMap<String, Object>();
        String subReportKey = "FIXEDASSETMOVEMENTCOMMONSUBREPORT";
        String showSubReportKey = "SHOW_FIXEDASSETMOVEMENTCOMMONSUBREPORT";

        String ejbql = "SELECT " +
                "fixedAssetGroup.groupCode," +
                "fixedAssetGroup.description," +
                "fixedAssetSubGroup.fixedAssetSubGroupCode," +
                "fixedAssetSubGroup.description," +
                "fixedAsset.barCode," +
                "fixedAsset.detail," +
                "fixedAsset.measurement," +
                "fixedAsset.description," +
                "fixedAsset.trademark," +
                "fixedAsset.model," +
                "fixedAsset.duration," +
                "fixedAsset.ufvOriginalValue," +
                "fixedAsset.currencyType, " +
                "fixedAsset.sequence, " +
                "fixedAsset.id, " +
                "fixedAssetPart.id, " +
                "fixedAssetPart.number, " +
                "fixedAssetPart.description, " +
                "measureUnit.name, " +
                "fixedAssetPart.unitPrice, " +
                "fixedAssetPart.serialNumber, " +
                "fixedAsset.registrationDate " +
                " FROM FixedAssetMovement fixedAssetMovement" +
                " LEFT JOIN fixedAssetMovement.fixedAsset fixedAsset" +
                " LEFT JOIN fixedAsset.fixedAssetSubGroup fixedAssetSubGroup" +
                " LEFT JOIN fixedAssetSubGroup.fixedAssetGroup fixedAssetGroup" +
                " LEFT JOIN fixedAsset.fixedAssetPartList fixedAssetPart" +
                " LEFT JOIN fixedAssetPart.measureUnit measureUnit";

        String[] restrictions = new String[]{
                "fixedAssetMovement.fixedAssetVoucher = #{fixedAssetVoucherDocumentReportAction.fixedAssetVoucher}"};

        String orderBy = "fixedAsset.id";

        //generate the sub report
        TypedReportData subReportData = super.generateSubReport(
                subReportKey,
                "/fixedassets/reports/fixedAssetMovementForRegistrationSubReport.jrxml",
                PageFormat.LETTER,
                PageOrientation.LANDSCAPE,
                createQueryForSubreport(subReportKey, ejbql, Arrays.asList(restrictions), orderBy),
                params);

        //add in main report params
        mainReportParams.putAll(subReportData.getReportParams());
        mainReportParams.put(subReportKey, subReportData.getJasperReport());
        mainReportParams.put(showSubReportKey, getFixedAssetVoucher().getPurchaseOrder().getPurchaseOrderCause().isFixedassetPurchase());

    }

    private void addPurchaseOrderFixedAssetPartSubReport(Map mainReportParams) {
        log.debug("Generating addPurchaseOrderFixedAssetPartSubReport.............................!!!!!!!");
        Map<String, Object> params = new HashMap<String, Object>();
        String subReportKey = "PURCHASEORDERFIXEDASSETPARTSUBREPORT";
        String showSubReportKey = "SHOW_PURCHASEORDERFIXEDASSETPARTSUBREPORT";

        String ejbql = "SELECT " +
                "fixedAsset.barCode," +
                "fixedAsset.detail," +
                "fixedAssetPart.description, " +
                "measureUnit.measureUnitCode, " +
                "measureUnit.name, " +
                "fixedAssetPart.serialNumber, " +
                "fixedAssetPart.unitPrice " +
                " FROM PurchaseOrderFixedAssetPart fixedAssetPart" +
                " LEFT JOIN fixedAssetPart.measureUnit measureUnit" +
                " LEFT JOIN fixedAssetPart.fixedAsset fixedAsset" +
                " LEFT JOIN fixedAssetPart.purchaseOrder purchaseOrder";

        String[] restrictions = new String[]{
                "purchaseOrder = #{fixedAssetVoucherDocumentReportAction.fixedAssetVoucher.purchaseOrder}"};

        String orderBy = "fixedAsset.barCode, fixedAssetPart.description";

        //generate the sub report
        TypedReportData subReportData = super.generateSubReport(
                subReportKey,
                "/fixedassets/reports/purchaseOrderFixedAssetPartSubReport.jrxml",
                PageFormat.LETTER,
                PageOrientation.LANDSCAPE,
                createQueryForSubreport(subReportKey, ejbql, Arrays.asList(restrictions), orderBy),
                params);

        //add in main report params
        mainReportParams.putAll(subReportData.getReportParams());
        mainReportParams.put(subReportKey, subReportData.getJasperReport());
        mainReportParams.put(showSubReportKey, getFixedAssetVoucher().getPurchaseOrder().getPurchaseOrderCause().isFixedassetPartsPurchase());
    }
}
