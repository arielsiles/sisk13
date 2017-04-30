package com.encens.khipus.action.finances.reports;

import com.encens.khipus.action.SessionUser;
import com.encens.khipus.action.reports.GenericReportAction;
import com.encens.khipus.model.admin.BusinessUnit;
import com.encens.khipus.model.employees.Employee;
import com.encens.khipus.model.finances.FinancesCurrency;
import com.encens.khipus.model.finances.RotatoryFundDocumentType;
import com.encens.khipus.service.finances.AccountingMovementService;
import com.encens.khipus.service.finances.FinanceDocumentService;
import com.encens.khipus.service.finances.PayableDocumentService;
import com.encens.khipus.service.finances.RotatoryFundService;
import com.encens.khipus.util.Constants;
import com.encens.khipus.util.DateUtils;
import com.encens.khipus.util.MessageUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author
 * @version 3.4
 */
@Name("rotatoryFundBalancesDetailReportAction")
@Scope(ScopeType.PAGE)
@Restrict("#{s:hasPermission('REPORTROTATORYFUNDBALANCESDETAIL','VIEW')}")
public class RotatoryFundBalancesDetailReportAction extends GenericReportAction {

    public static final String BUSINESS_UNIT_NAME_PARAMETER = "businessUnitName";
    public static final String EMPLOYEE_NAME_PARAMETER = "employeeName";
    public static final String DOCUMENT_TYPE_NAME_PARAMETER = "documentTypeName";
    public static final String ROTATORY_FUND_CODE_PARAMETER = "rotatoryFundCode";
    public static final String MOVEMENT_DATE_RANGE_PARAMETER = "movementDateRange";
    public static final String FINANCES_CURRENCY_NAME_PARAMETER = "financesCurrencyName";
    @In
    private RotatoryFundService rotatoryFundService;
    @In
    private AccountingMovementService accountingMovementService;
    @In
    private FinanceDocumentService financeDocumentService;
    @In
    private PayableDocumentService payableDocumentService;
    @In
    private SessionUser sessionUser;

    private BusinessUnit businessUnit;
    private Employee employee;
    private RotatoryFundDocumentType documentType;
    private Integer code;
    private Date movementStartDate;
    private Date movementEndDate;
    private FinancesCurrency financesCurrency;
    private Boolean showMovements = Boolean.TRUE;


    public void generateReport() {
        log.debug("Generate RotatoryFundBalancesDetailReportAction........");
        Map params = new HashMap();
        params.put("rotatoryFundService", rotatoryFundService);
        params.put("accountingMovementService", accountingMovementService);
        params.put("financeDocumentService", financeDocumentService);
        params.put("payableDocumentService", payableDocumentService);
        params.put("showMovements", Boolean.TRUE.equals(getShowMovements()));
        putFiltersValues(params);
        Map queryParameters = new HashMap();
        queryParameters.put("businessUnitId", getBusinessUnit() != null ? getBusinessUnit().getId() : null);
        queryParameters.put("rotatoryFundCode", getCode());
        queryParameters.put("movementStartDate", getMovementStartDate());
        queryParameters.put("movementEndDate", getMovementEndDate());
        queryParameters.put("financesCurrency", getFinancesCurrency());
        params.put("queryParameters", queryParameters);
        super.generateReport("rotatoryFundBalancesDetailReport", "/finances/reports/rotatoryFundBalancesDetailReport.jrxml", MessageUtils.getMessage("Reports.rotatoryFundBalancesDetail.title"), params);
    }

    @Override
    protected String getEjbql() {

        return "SELECT " +
                "documentType.id," +
                "documentType.code," +
                "documentType.name," +
                "cashAccount.companyNumber," +
                "cashAccount.accountCode," +
                "cashAccount.description," +
                "employee.id," +
                "employee.lastName," +
                "employee.maidenName," +
                "employee.firstName," +
                "rotatoryFund.code," +
                "rotatoryFund.amount," +
                "rotatoryFund.receivableResidue," +
                "rotatoryFund.payCurrency," +
                "rotatoryFund.id," +
                "rotatoryFund.date," +
                "rotatoryFund.state," +
                "rotatoryFundMovement.id," +
                "rotatoryFundMovement.date," +
                "rotatoryFundMovement.movementClass," +
                "rotatoryFundMovement.code," +
                "rotatoryFundMovement.transactionNumber," +
                "rotatoryFundMovement.movementType," +
                "rotatoryFundMovement.voucherType," +
                "rotatoryFundMovement.voucherNumber," +
                "rotatoryFundMovement.voucherDate," +
                "rotatoryFundMovement.bankPaymentDocumentType," +
                "rotatoryFundMovement.bankPaymentDocumentNumber," +
                "rotatoryFundMovement.cashBoxPaymentDocumentNumber," +
                "rotatoryFundMovement.cashBoxPaymentAccountNumber," +
                "rotatoryFundMovement.cashBoxPaymentAccountName," +
                "rotatoryFundMovement.documentBankCollectionDocumentType," +
                "rotatoryFundMovement.documentBankCollectionDocumentNumber," +
                "rotatoryFundMovement.documentCollectionDocumentType," +
                "rotatoryFundMovement.documentCollectionDocumentNumber," +
                "rotatoryFundMovement.cashAccountAdjustmentCollectionAccount," +
                "rotatoryFundMovement.cashAccountAdjustmentCollectionNumber," +
                "rotatoryFundMovement.depositAdjustmentCollectionDocumentType," +
                "rotatoryFundMovement.depositAdjustmentCollectionDocumentNumber," +
                "rotatoryFundMovement.purchaseOrderCollectionOrderNumber," +
                "rotatoryFundMovement.cashBoxCollectionAccountNumber," +
                "rotatoryFundMovement.cashBoxCollectionAccountName," +
                "rotatoryFundMovement.payrollCollectionName," +
                "rotatoryFundMovement.description," +
                "rotatoryFundMovement.observation," +
                "rotatoryFundMovement.paymentCurrency," +
                "rotatoryFundMovement.paymentAmount," +
                "rotatoryFundMovement.collectionCurrency," +
                "rotatoryFundMovement.collectionAmount," +
                "rotatoryFundMovement.exchangeRate" +
                " FROM RotatoryFund rotatoryFund" +
                " LEFT JOIN rotatoryFund.documentType documentType" +
                " LEFT JOIN rotatoryFund.cashAccount cashAccount" +
                " LEFT JOIN cashAccount.financesCurrency financesCurrency" +
                " LEFT JOIN rotatoryFund.businessUnit businessUnit" +
                " LEFT JOIN rotatoryFund.employee employee" +
                " LEFT JOIN rotatoryFund.rotatoryFundMovementList rotatoryFundMovement" +
                " where (rotatoryFund.state=#{enumerationUtil.getEnumValue('com.encens.khipus.model.finances.RotatoryFundState','APR')} " +
                " or rotatoryFund.state=#{enumerationUtil.getEnumValue('com.encens.khipus.model.finances.RotatoryFundState','LIQ')})" +
                " and rotatoryFundMovement.state=#{enumerationUtil.getEnumValue('com.encens.khipus.model.finances.RotatoryFundMovementState','APR')}";
    }


    @Create
    public void init() {
        restrictions = new String[]{"rotatoryFund.company=#{currentCompany}",
                "businessUnit = #{rotatoryFundBalancesDetailReportAction.businessUnit}",
                "employee = #{rotatoryFundBalancesDetailReportAction.employee}",
                "documentType = #{rotatoryFundBalancesDetailReportAction.documentType}",
                "rotatoryFund.code = #{rotatoryFundBalancesDetailReportAction.code}",
                "rotatoryFundMovement.voucherDate >= #{rotatoryFundBalancesDetailReportAction.movementStartDate}",
                "rotatoryFundMovement.voucherDate <= #{rotatoryFundBalancesDetailReportAction.movementEndDate}",
                "financesCurrency = #{rotatoryFundBalancesDetailReportAction.financesCurrency}"

        };

        sortProperty = "documentType.code, cashAccount.accountCode, employee.lastName, employee.maidenName, employee.firstName, rotatoryFund.code, rotatoryFundMovement.date, rotatoryFundMovement.voucherDate, rotatoryFundMovement.id";
    }

    public void clearEmployee() {
        setEmployee(null);
    }

    public BusinessUnit getBusinessUnit() {
        return businessUnit;
    }

    public void setBusinessUnit(BusinessUnit businessUnit) {
        this.businessUnit = businessUnit;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public RotatoryFundDocumentType getDocumentType() {
        return documentType;
    }

    public void setDocumentType(RotatoryFundDocumentType documentType) {
        this.documentType = documentType;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public Date getMovementStartDate() {
        return movementStartDate;
    }

    public void setMovementStartDate(Date movementStartDate) {
        this.movementStartDate = movementStartDate;
    }

    public Date getMovementEndDate() {
        return movementEndDate;
    }

    public void setMovementEndDate(Date movementEndDate) {
        this.movementEndDate = movementEndDate;
    }

    public FinancesCurrency getFinancesCurrency() {
        return financesCurrency;
    }

    public void setFinancesCurrency(FinancesCurrency financesCurrency) {
        this.financesCurrency = financesCurrency;
    }

    public Boolean getShowMovements() {
        return showMovements;
    }

    public void setShowMovements(Boolean showMovements) {
        this.showMovements = showMovements;
    }

    public void putFiltersValues(Map params) {
        params.put(BUSINESS_UNIT_NAME_PARAMETER, getBusinessUnit() != null ? getBusinessUnit().getFullName() : Constants.BLANK_SEPARATOR);

        params.put(EMPLOYEE_NAME_PARAMETER, getEmployee() != null ? getEmployee().getFullName() : Constants.BLANK_SEPARATOR);

        params.put(DOCUMENT_TYPE_NAME_PARAMETER, getDocumentType() != null ? getDocumentType().getFullName() : Constants.BLANK_SEPARATOR);

        params.put(ROTATORY_FUND_CODE_PARAMETER, getCode() != null ? String.valueOf(getCode()) : Constants.BLANK_SEPARATOR);

        String movementStartDateParameter = getMovementStartDate() != null ?
                MessageUtils.getMessage(Constants.RK_COMMON_FROM) + Constants.BLANK_SEPARATOR +
                        DateUtils.format(getMovementStartDate(), MessageUtils.getMessage(Constants.RK_PATTERNS_DATE))
                        + Constants.BLANK_SEPARATOR : Constants.BLANK_SEPARATOR;
        String movementEndDateParameter = getMovementEndDate() != null ?
                MessageUtils.getMessage(Constants.RK_COMMON_TO) + Constants.BLANK_SEPARATOR +
                        DateUtils.format(getMovementEndDate(), MessageUtils.getMessage(Constants.RK_PATTERNS_DATE)) : Constants.BLANK_SEPARATOR;

        params.put(MOVEMENT_DATE_RANGE_PARAMETER, movementStartDateParameter + movementEndDateParameter);

        params.put(FINANCES_CURRENCY_NAME_PARAMETER, getFinancesCurrency() != null ? getFinancesCurrency().getFullName() : Constants.BLANK_SEPARATOR);
    }
}
