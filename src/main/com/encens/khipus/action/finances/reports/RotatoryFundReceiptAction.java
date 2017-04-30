package com.encens.khipus.action.finances.reports;

import com.encens.khipus.action.reports.GenericReportAction;
import com.encens.khipus.action.reports.PageFormat;
import com.encens.khipus.action.reports.PageOrientation;
import com.encens.khipus.action.reports.ReportFormat;
import com.encens.khipus.model.finances.RotatoryFund;
import com.encens.khipus.service.finances.QuotaService;
import com.jatun.titus.reportgenerator.util.TypedReportData;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Encens S.R.L.
 * This class implements the rotatory fund receipt action
 *
 * @author
 * @version 2.20
 */
@Name("rotatoryFundReceiptAction")
@Scope(ScopeType.PAGE)
public class RotatoryFundReceiptAction extends GenericReportAction {
    RotatoryFund rotatoryFund;
    @In
    private QuotaService quotaService;

    @Create
    public void init() {
        restrictions = new String[]{"rotatoryFund.id=#{rotatoryFundReceiptAction.rotatoryFund.id}"};
    }

    protected String getEjbql() {
        return "SELECT rotatoryFund.id, " +
                "rotatoryFund.employee, " +
                "rotatoryFund.amount, " +
                "rotatoryFund.payCurrency, " +
                "rotatoryFund.businessUnit, " +
                "rotatoryFund.date, " +
                "rotatoryFund.description, " +
                "rotatoryFund.exchangeRate, " +
                "extensionSite.extension " +
                " FROM RotatoryFund rotatoryFund " +
                " LEFT JOIN rotatoryFund.employee.extensionSite extensionSite ";
    }

    public void generateReport(RotatoryFund rotatoryFund) {
        log.debug("generating RotatoryFundReceipt......................................" + rotatoryFund);
        this.rotatoryFund = rotatoryFund;
        setReportFormat(ReportFormat.PDF);
        HashMap<String, Object> reportParameters = new HashMap<String, Object>();
        reportParameters.putAll(getReportParams());

        //add sub reports
        addQuotaSubReport(reportParameters);
        addRotatoryFundCollectionSubReport(reportParameters);
        addRotatoryFundPaymentSubReport(reportParameters);

        super.generateReport(
                "rotatoryFundReceipt",
                "/finances/reports/rotatoryFundReceipt.jrxml",
                PageFormat.CUSTOM,
                PageOrientation.PORTRAIT,
                messages.get("Reports.rotatoryFunds.receipt"),
                reportParameters);
    }

    private Map<String, Object> getReportParams() {
        Map<String, Object> paramMap = new HashMap<String, Object>();
        BigDecimal residueSum = quotaService.getQuotaResidueSum(getRotatoryFund());

        paramMap.put("residueParam", residueSum);
        return paramMap;
    }


    /**
     * Add quota sub report in main report
     *
     * @param mainReportParams
     */
    private void addQuotaSubReport(Map mainReportParams) {
        log.debug("Generating addQuotaSubReport.............................");
        Map<String, Object> params = new HashMap<String, Object>();

        String quotaSubReportQuery = "SELECT " +
                "quota.id, " +
                "quota.expirationDate, " +
                "quota.amount, " +
                "quota.currency, " +
                "quota.state, " +
                "quota.residue " +
                "FROM Quota quota ";

        String[] quotaSubReportRestrictions = new String[]{
                "quota.rotatoryFund.id=#{rotatoryFundReceiptAction.rotatoryFund.id}"
        };
        String quotaSubReporOrderBy = "quota.expirationDate";

        TypedReportData quotaSubReportReportData = super.generateSubReport(
                "quotaSubReport",
                "/finances/reports/quotaSubReport.jrxml",
                PageFormat.LETTER,
                PageOrientation.PORTRAIT,
                createQueryForSubreport("quotaSubReport",
                        quotaSubReportQuery,
                        Arrays.asList(quotaSubReportRestrictions),
                        quotaSubReporOrderBy),
                params);

        //add in main report params
        mainReportParams.putAll(quotaSubReportReportData.getReportParams());
        mainReportParams.put("quotaSubReport", quotaSubReportReportData.getJasperReport());
    }

    /**
     * Add rotatory fund Collection sub report in main report
     *
     * @param mainReportParams
     */
    private void addRotatoryFundCollectionSubReport(Map mainReportParams) {
        log.debug("Generating addRotatoryFundCollectionSubReport.............................");
        String subReportKey = "ROTATORYFUNDCOLLECTIONSUBREPORT";

        Map<String, Object> params = new HashMap<String, Object>();

        String ejbql = "SELECT " +
                "rotatoryFundCollection.rotatoryFundCollectionType," +
                "rotatoryFundCollection.creationDate," +
                "rotatoryFundCollection.collectionAmount," +
                "rotatoryFundCollection.collectionCurrency," +
                "bankAccount.description," +
                "collectionDocument.collectionDocumentType," +
                "collectionDocument.number," +
                "rotatoryFundCollection.description," +
                "rotatoryFundCollection.state" +
                " FROM RotatoryFundCollection rotatoryFundCollection" +
                " LEFT JOIN rotatoryFundCollection.bankAccount bankAccount" +
                " LEFT JOIN rotatoryFundCollection.collectionDocument collectionDocument";

        String[] restrictions = new String[]{
                "rotatoryFundCollection.rotatoryFund.id=#{rotatoryFundReceiptAction.rotatoryFund.id}"};

        String orderBy = "rotatoryFundCollection.creationDate";

        //generate the sub report
        TypedReportData subReportData = super.generateSubReport(
                subReportKey,
                "/finances/reports/rotatoryFundCollectionSubReport.jrxml",
                PageFormat.LETTER,
                PageOrientation.PORTRAIT,
                createQueryForSubreport(subReportKey, ejbql, Arrays.asList(restrictions), orderBy),
                params);

        //add in main report params
        mainReportParams.putAll(subReportData.getReportParams());
        mainReportParams.put(subReportKey, subReportData.getJasperReport());
    }

    /**
     * Add rotatory fund payment sub report
     *
     * @param mainReportParams
     */
    private void addRotatoryFundPaymentSubReport(Map mainReportParams) {
        log.debug("Generating addRotatoryFundPaymentSubReport.............................");
        String subReportKey = "ROTATORYFUNDPAYMENTSUBREPORT";

        Map<String, Object> params = new HashMap<String, Object>();

        String ejbql = "SELECT " +
                "rotatoryFundPayment.creationDate," +
                "rotatoryFundPayment.rotatoryFundPaymentType," +
                "rotatoryFundPayment.beneficiaryName," +
                "rotatoryFundPayment.bankAccountNumber," +
                "rotatoryFundPayment.sourceAmount," +
                "rotatoryFundPayment.sourceCurrency," +
                "rotatoryFundPayment.paymentAmount," +
                "rotatoryFundPayment.paymentCurrency," +
                "rotatoryFundPayment.paymentDate," +
                "rotatoryFundPayment.exchangeRate," +
                "rotatoryFundPayment.description," +
                "rotatoryFundPayment.state," +
                "rotatoryFundPayment.reversionCause " +
                " FROM RotatoryFundPayment rotatoryFundPayment";

        String[] restrictions = new String[]{
                "rotatoryFundPayment.rotatoryFund.id=#{rotatoryFundReceiptAction.rotatoryFund.id}"};

        String orderBy = "rotatoryFundPayment.creationDate";

        //generate the sub report
        TypedReportData subReportData = super.generateSubReport(
                subReportKey,
                "/finances/reports/rotaFundPaymentSubReport.jrxml",
                PageFormat.CUSTOM,
                PageOrientation.PORTRAIT,
                createQueryForSubreport(subReportKey, ejbql, Arrays.asList(restrictions), orderBy),
                params);

        //add in main report params
        mainReportParams.putAll(subReportData.getReportParams());
        mainReportParams.put(subReportKey, subReportData.getJasperReport());
    }

    public RotatoryFund getRotatoryFund() {
        return rotatoryFund;
    }

    public void setRotatoryFund(RotatoryFund rotatoryFund) {
        this.rotatoryFund = rotatoryFund;
    }
}
