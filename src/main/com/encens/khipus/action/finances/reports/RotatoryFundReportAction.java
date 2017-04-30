package com.encens.khipus.action.finances.reports;

import com.encens.khipus.action.reports.GenericReportAction;
import com.encens.khipus.action.reports.PageFormat;
import com.encens.khipus.action.reports.PageOrientation;
import com.encens.khipus.model.admin.BusinessUnit;
import com.encens.khipus.model.employees.Employee;
import com.encens.khipus.model.finances.CashAccount;
import com.encens.khipus.model.finances.RotatoryFundCollectionType;
import com.encens.khipus.model.finances.RotatoryFundDocumentType;
import com.encens.khipus.model.finances.RotatoryFundState;
import com.encens.khipus.util.MessageUtils;
import com.jatun.titus.reportgenerator.util.TypedReportData;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
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
 * Action to generate rotatory fund report
 *
 * @author
 * @version $Id: RotatoryFundReportAction.java  03-sep-2010 18:17:52$
 */
@Name("rotatoryFundReportAction")
@Scope(ScopeType.PAGE)
@Restrict("#{s:hasPermission('REPORTROTATORYFUND','VIEW')}")
public class RotatoryFundReportAction extends GenericReportAction {

    private RotatoryFundDocumentType documentType;
    private Employee employee;
    private RotatoryFundState rotatoryFundState;
    private String description;
    private BigDecimal startAmount;
    private BigDecimal endAmount;
    private Date startDate;
    private Date expirationDate;
    private BusinessUnit businessUnit;
    private RotatoryFundCollectionType rotatoryFundCollectionType;
    private CashAccount cashAccount;
    private Integer code;

    public void generateReport() {
        log.debug("Generate RotatoryFundReportAction........");

        Map params = new HashMap();

        //add sub reports
        addQuotaSubReport(params);
        addRotatoryFundCollectionSubReport(params);
        addRotatoryFundPaymentSubReport(params);

        super.generateReport("rotatoryFundReport", "/finances/reports/rotatoryFundReport.jrxml", MessageUtils.getMessage("Reports.rotatoryFund.title"), params);
    }

    @Override
    protected String getEjbql() {

        return "SELECT " +
                "rotatoryFund.id," +
                "rotatoryFund.startDate," +
                "rotatoryFund.expirationDate," +
                "documentType," +
                "rotatoryFund.code," +
                "businessUnit," +
                "employee," +
                "charge.name," +
                "costCenter," +
                "rotatoryFund.amount," +
                "rotatoryFund.payCurrency," +
                "rotatoryFund.discountByPayroll," +
                "cashAccount," +
                "rotatoryFund.description," +
                "rotatoryFund.state" +
                " FROM RotatoryFund rotatoryFund" +
                " LEFT JOIN rotatoryFund.documentType documentType" +
                " LEFT JOIN rotatoryFund.cashAccount cashAccount" +
                " LEFT JOIN rotatoryFund.businessUnit businessUnit" +
                " LEFT JOIN rotatoryFund.employee employee" +
                " LEFT JOIN rotatoryFund.costCenter costCenter" +
                " LEFT JOIN rotatoryFund.jobContract jobContract" +
                " LEFT JOIN jobContract.job job" +
                " LEFT JOIN job.charge charge";
    }


    @Create
    public void init() {
        restrictions = new String[]{"rotatoryFund.company=#{currentCompany}",
                "documentType =#{rotatoryFundReportAction.documentType}",
                "rotatoryFund.employee=#{rotatoryFundReportAction.employee}",
                "rotatoryFund.state=#{rotatoryFundReportAction.rotatoryFundState}",
                "lower(rotatoryFund.description) LIKE concat('%', concat(lower(#{rotatoryFundReportAction.description}), '%'))",
                "rotatoryFund.amount>=#{rotatoryFundReportAction.startAmount}",
                "rotatoryFund.amount<=#{rotatoryFundReportAction.endAmount}",
                "rotatoryFund.startDate>=#{rotatoryFundReportAction.startDate}",
                "rotatoryFund.expirationDate<=#{rotatoryFundReportAction.expirationDate}",
                "rotatoryFund.businessUnit=#{rotatoryFundReportAction.businessUnit}",
                "rotatoryFund.code=#{rotatoryFundReportAction.code}",
                "cashAccount=#{rotatoryFundReportAction.cashAccount}",
                "rotatoryFund IN (SELECT rf FROM RotatoryFundCollection rotatoryFundCollection " +
                        " LEFT JOIN rotatoryFundCollection.rotatoryFund rf" +
                        " WHERE rotatoryFundCollection.rotatoryFundCollectionType = #{rotatoryFundReportAction.rotatoryFundCollectionType})"
        };

        sortProperty = "rotatoryFund.startDate, rotatoryFund.expirationDate";
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
                "FROM Quota quota " +
                " WHERE quota.rotatoryFund.id=$P{rotatoryFundIdParam}";

        String[] quotaSubReportRestrictions = new String[]{};
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
                " LEFT JOIN rotatoryFundCollection.collectionDocument collectionDocument" +
                " WHERE rotatoryFundCollection.rotatoryFund.id=$P{rotatoryFundIdParam}";

        String[] restrictions = new String[]{};

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
                "rotatoryFundPayment.state, " +
                "rotatoryFundPayment.reversionCause " +
                " FROM RotatoryFundPayment rotatoryFundPayment" +
                " WHERE rotatoryFundPayment.rotatoryFund.id=$P{rotatoryFundIdParam}";

        String[] restrictions = new String[]{};

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

    public RotatoryFundCollectionType[] getRotatoryFundCollectionTypeList() {
        return RotatoryFundCollectionType.values();
    }

    public void clearEmployee() {
        setEmployee(null);
    }

    public void clearCashAccount() {
        setCashAccount(null);
    }

    public RotatoryFundDocumentType getDocumentType() {
        return documentType;
    }

    public void setDocumentType(RotatoryFundDocumentType documentType) {
        this.documentType = documentType;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public RotatoryFundState getRotatoryFundState() {
        return rotatoryFundState;
    }

    public void setRotatoryFundState(RotatoryFundState rotatoryFundState) {
        this.rotatoryFundState = rotatoryFundState;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getStartAmount() {
        return startAmount;
    }

    public void setStartAmount(BigDecimal startAmount) {
        this.startAmount = startAmount;
    }

    public BigDecimal getEndAmount() {
        return endAmount;
    }

    public void setEndAmount(BigDecimal endAmount) {
        this.endAmount = endAmount;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(Date expirationDate) {
        this.expirationDate = expirationDate;
    }

    public BusinessUnit getBusinessUnit() {
        return businessUnit;
    }

    public void setBusinessUnit(BusinessUnit businessUnit) {
        this.businessUnit = businessUnit;
    }

    public RotatoryFundCollectionType getRotatoryFundCollectionType() {
        return rotatoryFundCollectionType;
    }

    public void setRotatoryFundCollectionType(RotatoryFundCollectionType rotatoryFundCollectionType) {
        this.rotatoryFundCollectionType = rotatoryFundCollectionType;
    }

    public CashAccount getCashAccount() {
        return cashAccount;
    }

    public void setCashAccount(CashAccount cashAccount) {
        this.cashAccount = cashAccount;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }
}
