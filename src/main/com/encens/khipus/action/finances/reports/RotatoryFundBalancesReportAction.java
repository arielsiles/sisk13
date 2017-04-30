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
 * Action to generate rotatory fund balances report
 *
 * @author
 * @version $Id: RotatoryFundBalancesReportAction.java  25-oct-2010 15:32:21$
 */
@Name("rotatoryFundBalancesReportAction")
@Scope(ScopeType.PAGE)
@Restrict("#{s:hasPermission('REPORTROTATORYFUNDBALANCES','VIEW')}")
public class RotatoryFundBalancesReportAction extends GenericReportAction {

    private RotatoryFundDocumentType documentType;
    private Employee employee;
    private RotatoryFundState rotatoryFundState;
    private String description;
    private BigDecimal startAmount;
    private BigDecimal endAmount;
    private BigDecimal startPayableResidue;
    private BigDecimal endPayableResidue;
    private BigDecimal startReceivableResidue;
    private BigDecimal endReceivableResidue;
    private Date initStartDate;
    private Date endStartDate;
    private Date initExpirationDate;
    private Date endExpirationDate;
    private BusinessUnit businessUnit;
    private RotatoryFundCollectionType rotatoryFundCollectionType;
    private CashAccount cashAccount;
    private Integer code;

    public void generateReport() {
        log.debug("Generate RotatoryFundBalancesReportAction........");

        Map params = new HashMap();

        super.generateReport("rotatoryFundBalancesReport", "/finances/reports/rotatoryFundBalancesReport.jrxml", MessageUtils.getMessage("Reports.rotatoryFundBalances.title"), params);
    }

    @Override
    protected String getEjbql() {

        return "SELECT " +
                "rotatoryFund.id," +
                "rotatoryFund.startDate," +
                "rotatoryFund.expirationDate," +
                "documentType," +
                "rotatoryFund.code," +
                "businessUnit.executorUnitCode," +
                "organization.name," +
                "employee.lastName," +
                "employee.maidenName," +
                "employee.firstName," +
                "job," +
                "rotatoryFund.amount," +
                "rotatoryFund.payCurrency," +
                "rotatoryFund.payableResidue," +
                "rotatoryFund.receivableResidue," +
                "rotatoryFund.discountByPayroll," +
                "cashAccount.accountCode," +
                "cashAccount.description," +
                "rotatoryFund.description," +
                "rotatoryFund.state" +
                " FROM RotatoryFund rotatoryFund" +
                " LEFT JOIN rotatoryFund.documentType documentType" +
                " LEFT JOIN rotatoryFund.businessUnit businessUnit" +
                " LEFT JOIN businessUnit.organization organization" +
                " LEFT JOIN rotatoryFund.employee employee" +
                " LEFT JOIN rotatoryFund.jobContract jobContract" +
                " LEFT JOIN jobContract.job job" +
                " LEFT JOIN rotatoryFund.cashAccount cashAccount";
    }


    @Create
    public void init() {
        restrictions = new String[]{"rotatoryFund.company=#{currentCompany}",
                "documentType = #{rotatoryFundBalancesReportAction.documentType}",
                "employee = #{rotatoryFundBalancesReportAction.employee}",
                "rotatoryFund.state = #{rotatoryFundBalancesReportAction.rotatoryFundState}",
                "lower(rotatoryFund.description) LIKE concat('%', concat(lower(#{rotatoryFundBalancesReportAction.description}), '%'))",
                "rotatoryFund.amount >= #{rotatoryFundBalancesReportAction.startAmount}",
                "rotatoryFund.amount <= #{rotatoryFundBalancesReportAction.endAmount}",
                "rotatoryFund.payableResidue >= #{rotatoryFundBalancesReportAction.startPayableResidue}",
                "rotatoryFund.payableResidue <= #{rotatoryFundBalancesReportAction.endPayableResidue}",
                "rotatoryFund.receivableResidue >= #{rotatoryFundBalancesReportAction.startReceivableResidue}",
                "rotatoryFund.receivableResidue <= #{rotatoryFundBalancesReportAction.endReceivableResidue}",
                "rotatoryFund.startDate >= #{rotatoryFundBalancesReportAction.initStartDate}",
                "rotatoryFund.startDate <= #{rotatoryFundBalancesReportAction.endStartDate}",
                "rotatoryFund.expirationDate >= #{rotatoryFundBalancesReportAction.initExpirationDate}",
                "rotatoryFund.expirationDate <= #{rotatoryFundBalancesReportAction.endExpirationDate}",
                "businessUnit = #{rotatoryFundBalancesReportAction.businessUnit}",
                "rotatoryFund.code = #{rotatoryFundBalancesReportAction.code}",
                "rotatoryFund.cashAccount = #{rotatoryFundBalancesReportAction.cashAccount}",
                "rotatoryFund IN (SELECT rf FROM RotatoryFundCollection rotatoryFundCollection " +
                        " LEFT JOIN rotatoryFundCollection.rotatoryFund rf" +
                        " WHERE rotatoryFundCollection.rotatoryFundCollectionType = #{rotatoryFundBalancesReportAction.rotatoryFundCollectionType})"
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

    public BigDecimal getStartPayableResidue() {
        return startPayableResidue;
    }

    public void setStartPayableResidue(BigDecimal startPayableResidue) {
        this.startPayableResidue = startPayableResidue;
    }

    public BigDecimal getEndPayableResidue() {
        return endPayableResidue;
    }

    public void setEndPayableResidue(BigDecimal endPayableResidue) {
        this.endPayableResidue = endPayableResidue;
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

    public Date getInitStartDate() {
        return initStartDate;
    }

    public void setInitStartDate(Date initStartDate) {
        this.initStartDate = initStartDate;
    }

    public Date getEndStartDate() {
        return endStartDate;
    }

    public void setEndStartDate(Date endStartDate) {
        this.endStartDate = endStartDate;
    }

    public Date getEndExpirationDate() {
        return endExpirationDate;
    }

    public void setEndExpirationDate(Date endExpirationDate) {
        this.endExpirationDate = endExpirationDate;
    }

    public Date getInitExpirationDate() {
        return initExpirationDate;
    }

    public void setInitExpirationDate(Date initExpirationDate) {
        this.initExpirationDate = initExpirationDate;
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
