package com.encens.khipus.action.finances.reports;

import com.encens.khipus.action.reports.GenericReportAction;
import com.encens.khipus.model.admin.BusinessUnit;
import com.encens.khipus.model.employees.Employee;
import com.encens.khipus.model.finances.CashAccount;
import com.encens.khipus.model.finances.RotatoryFundCollectionType;
import com.encens.khipus.model.finances.RotatoryFundDocumentType;
import com.encens.khipus.model.finances.RotatoryFundState;
import com.encens.khipus.util.MessageUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Encens S.R.L.
 * Action to generate rotatory fund payments receivable report
 *
 * @author
 * @version $Id: RotatoryFundPaymentReceivableReport.java  27-oct-2010 15:01:26$
 */
@Name("rotatoryFundPaymentReceivableReportAction")
@Scope(ScopeType.PAGE)
@Restrict("#{s:hasPermission('REPORTROTATORYFUNDPAYRECEIVABLE','VIEW')}")
public class RotatoryFundPaymentReceivableReportAction extends GenericReportAction {

    private RotatoryFundDocumentType documentType;
    private Employee employee;
    private RotatoryFundState rotatoryFundState;
    private String description;
    private BigDecimal startAmount;
    private BigDecimal endAmount;
    private BigDecimal startReceivableResidue;
    private BigDecimal endReceivableResidue;
    private Date startDate;
    private Date expirationDate;
    private BusinessUnit businessUnit;
    private CashAccount cashAccount;
    private Integer code;

    public void generateReport() {
        log.debug("Generate RotatoryFundPaymentReceivableReportAction........");

        Map params = new HashMap();

        super.generateReport("rotatoryFundPaymentReceivableReport", "/finances/reports/rotatoryFundPaymentReceivableReport.jrxml", MessageUtils.getMessage("Reports.rotatoryFundPaymentReceivable.title"), params);
    }

    @Override
    protected String getEjbql() {

        return "SELECT " +
                "rotatoryFund.id," +
                "rotatoryFund.code," +
                "documentType," +
                "businessUnit.executorUnitCode," +
                "organization.name," +
                "costCenter.code," +
                "costCenter.description," +
                "employee.lastName," +
                "employee.maidenName," +
                "employee.firstName," +
                "charge.name," +
                "rotatoryFund.amount," +
                "rotatoryFund.payCurrency," +
                "rotatoryFund.receivableResidue," +
                "rotatoryFund.description," +
                "rotatoryFund.startDate," +
                "rotatoryFund.expirationDate" +
                " FROM RotatoryFund rotatoryFund" +
                " LEFT JOIN rotatoryFund.documentType documentType" +
                " LEFT JOIN rotatoryFund.cashAccount cashAccount" +
                " LEFT JOIN rotatoryFund.businessUnit businessUnit" +
                " LEFT JOIN rotatoryFund.costCenter costCenter" +
                " LEFT JOIN businessUnit.organization organization" +
                " LEFT JOIN rotatoryFund.employee employee" +
                " LEFT JOIN rotatoryFund.jobContract jobContract" +
                " LEFT JOIN jobContract.job job" +
                " LEFT JOIN job.charge charge";
    }


    @Create
    public void init() {
        restrictions = new String[]{"rotatoryFund.company=#{currentCompany}",
                "documentType = #{rotatoryFundPaymentReceivableReportAction.documentType}",
                "employee = #{rotatoryFundPaymentReceivableReportAction.employee}",
                "rotatoryFund.state = #{rotatoryFundPaymentReceivableReportAction.rotatoryFundState}",
                "lower(rotatoryFund.description) LIKE concat('%', concat(lower(#{rotatoryFundPaymentReceivableReportAction.description}), '%'))",
                "rotatoryFund.amount >= #{rotatoryFundPaymentReceivableReportAction.startAmount}",
                "rotatoryFund.amount <= #{rotatoryFundPaymentReceivableReportAction.endAmount}",
                "rotatoryFund.receivableResidue >= #{rotatoryFundPaymentReceivableReportAction.startReceivableResidue}",
                "rotatoryFund.receivableResidue <= #{rotatoryFundPaymentReceivableReportAction.endReceivableResidue}",
                "rotatoryFund.startDate >= #{rotatoryFundPaymentReceivableReportAction.startDate}",
                "rotatoryFund.expirationDate <= #{rotatoryFundPaymentReceivableReportAction.expirationDate}",
                "businessUnit = #{rotatoryFundPaymentReceivableReportAction.businessUnit}",
                "rotatoryFund.code = #{rotatoryFundPaymentReceivableReportAction.code}",
                "cashAccount = #{rotatoryFundPaymentReceivableReportAction.cashAccount}"
        };

        sortProperty = "rotatoryFund.startDate, rotatoryFund.expirationDate";
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

    public BigDecimal getStartReceivableResidue() {
        return startReceivableResidue;
    }

    public void setStartReceivableResidue(BigDecimal startReceivableResidue) {
        this.startReceivableResidue = startReceivableResidue;
    }

    public BigDecimal getEndReceivableResidue() {
        return endReceivableResidue;
    }

    public void setEndReceivableResidue(BigDecimal endReceivableResidue) {
        this.endReceivableResidue = endReceivableResidue;
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
