package com.encens.khipus.action.finances.reports;

import com.encens.khipus.action.reports.GenericReportAction;
import com.encens.khipus.action.reports.PageFormat;
import com.encens.khipus.action.reports.PageOrientation;
import com.encens.khipus.model.employees.Employee;
import com.encens.khipus.model.finances.CashAccount;
import com.encens.khipus.model.finances.CostCenter;
import com.encens.khipus.model.finances.RotatoryFundCollectionState;
import com.encens.khipus.model.finances.RotatoryFundState;
import com.jatun.titus.reportgenerator.util.TypedReportData;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Encens S.R.L.
 * This class implements the rotatory fund by account report action
 *
 * @author
 * @version 2.26
 */
@Name("rotatoryFundByAccountReportAction")
@Scope(ScopeType.PAGE)
@Restrict("#{s:hasPermission('ROTFUNDBYACCOUNTREPORT','VIEW')}")
public class RotatoryFundByAccountReportAction extends GenericReportAction {
    private CostCenter costCenter;
    private String executorUnitCode;
    private CashAccount cashAccount;
    private Employee employee;
    private Date initDate;
    private Date endDate;

    private RotatoryFundState rotatoryFundStateAPR = RotatoryFundState.APR;
    private RotatoryFundState rotatoryFundStateLIQ = RotatoryFundState.LIQ;
    private RotatoryFundCollectionState rotatoryFundCollectionStateAPR = RotatoryFundCollectionState.APR;
    private Integer code;

    @Create
    public void init() {
        restrictions = new String[]{"lower(businessUnit.executorUnitCode) like concat(lower(#{rotatoryFundByAccountReportAction.executorUnitCode}),'%')",
                "rFCSDistribution.costCenter=#{rotatoryFundByAccountReportAction.costCenter}",
                "cashAccount=#{rotatoryFundByAccountReportAction.cashAccount}",
                "rFCollection.collectionDate>=#{rotatoryFundByAccountReportAction.initDate}",
                "rFCollection.collectionDate<=#{rotatoryFundByAccountReportAction.endDate}",
                "employee=#{rotatoryFundByAccountReportAction.employee}",
                "rotatoryFund.code=#{rotatoryFundByAccountReportAction.code}"
        };

        sortProperty = "cashAccount.accountCode, cashAccount.description, cashAccount.currency";
        groupByProperty = "cashAccount.accountCode, cashAccount.description, cashAccount.currency";
    }

    protected String getEjbql() {
        return "SELECT " +
                "      SUM(rFCSDistribution.amount), " +
                "      cashAccount.accountCode, " +
                "      cashAccount.description, " +
                "      cashAccount.currency " +
                "FROM  RotatoryFundCollectionSpendDistribution rFCSDistribution" +
                "      LEFT JOIN rFCSDistribution.cashAccount cashAccount" +
                "      LEFT JOIN rFCSDistribution.rotatoryFundCollection rFCollection" +
                "      LEFT JOIN rFCollection.rotatoryFund rotatoryFund" +
                "      LEFT JOIN rotatoryFund.employee employee" +
                "      LEFT JOIN rotatoryFund.businessUnit businessUnit" +
                "      LEFT JOIN rFCSDistribution.costCenter costCenter " +
                "WHERE (rotatoryFund.state=#{rotatoryFundByAccountReportAction.rotatoryFundStateAPR} " +
                "       OR rotatoryFund.state=#{rotatoryFundByAccountReportAction.rotatoryFundStateLIQ}) " +
                "      AND rFCollection.state=#{rotatoryFundByAccountReportAction.rotatoryFundCollectionStateAPR} ";
    }

    public void generateReport() {
        log.debug("Generate RotatoryFundByAccount Report........");
        TypedReportData subReportData = getSubReport();

        HashMap<String, Object> reportParameters = new HashMap<String, Object>();
        reportParameters.put("TOTALS_SUBREPORT", subReportData.getJasperReport());

        super.generateReport(
                "rotatoryFundByAccountReport",
                "/finances/reports/rotatoryFundByAccountReport.jrxml",
                PageFormat.LETTER,
                PageOrientation.PORTRAIT,
                messages.get("Reports.rotatoryFundByAccount.title"),
                reportParameters);
    }

    private TypedReportData getSubReport() {
        Map<String, Object> params = new HashMap<String, Object>();

        String ejbqlForSubreport = "SELECT " +
                "      cashAccount.companyNumber, " +
                "      cashAccount.currency " +
                "FROM  RotatoryFundCollectionSpendDistribution rFCSDistribution" +
                "      LEFT JOIN rFCSDistribution.cashAccount cashAccount" +
                "      LEFT JOIN rFCSDistribution.rotatoryFundCollection rFCollection" +
                "      LEFT JOIN rFCollection.rotatoryFund rotatoryFund" +
                "      LEFT JOIN rotatoryFund.employee employee" +
                "      LEFT JOIN rotatoryFund.businessUnit businessUnit" +
                "      LEFT JOIN rFCSDistribution.costCenter costCenter";
        String groupByForSubReport = "cashAccount.currency, cashAccount.companyNumber";
        String orderByForSubReport = "cashAccount.currency, cashAccount.companyNumber";

        //generate the sub report
        TypedReportData subReportData = super.generateSubReport(
                "rotatoryFundTotalsSubReport",
                "/finances/reports/rotatoryFundByAccountSubReport.jrxml",
                PageFormat.LETTER,
                PageOrientation.PORTRAIT,
                createQueryForSubreport("rotatoryFundTotalsSubreport", ejbqlForSubreport, new ArrayList(), orderByForSubReport, groupByForSubReport),
                params);
        return subReportData;
    }

    public CostCenter getCostCenter() {
        return costCenter;
    }

    public void setCostCenter(CostCenter costCenter) {
        this.costCenter = costCenter;
    }

    public String getExecutorUnitCode() {
        return executorUnitCode;
    }

    public void setExecutorUnitCode(String executorUnitCode) {
        this.executorUnitCode = executorUnitCode;
    }

    public void assignCostCenter(CostCenter costCenter) {
        this.costCenter = costCenter;
    }

    public void clearCostCenter() {
        setCostCenter(null);
    }

    public CashAccount getCashAccount() {
        return cashAccount;
    }

    public void setCashAccount(CashAccount cashAccount) {
        this.cashAccount = cashAccount;
    }

    public void clearCashAccount() {
        setCashAccount(null);
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public void clearEmployee() {
        setEmployee(null);
    }

    public Date getInitDate() {
        return initDate;
    }

    public void setInitDate(Date initDate) {
        this.initDate = initDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public RotatoryFundState getRotatoryFundStateAPR() {
        return rotatoryFundStateAPR;
    }

    public RotatoryFundState getRotatoryFundStateLIQ() {
        return rotatoryFundStateLIQ;
    }

    public RotatoryFundCollectionState getRotatoryFundCollectionStateAPR() {
        return rotatoryFundCollectionStateAPR;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }
}
