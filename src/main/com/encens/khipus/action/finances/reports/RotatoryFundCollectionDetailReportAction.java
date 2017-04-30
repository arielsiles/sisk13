package com.encens.khipus.action.finances.reports;

import com.encens.khipus.action.reports.GenericReportAction;
import com.encens.khipus.model.admin.BusinessUnit;
import com.encens.khipus.model.admin.User;
import com.encens.khipus.model.employees.Employee;
import com.encens.khipus.model.finances.*;
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
 * @author
 * @version 2.28
 */
@Name("rotatoryFundCollectionDetailReportAction")
@Scope(ScopeType.PAGE)
@Restrict("#{s:hasPermission('REPORTROTATORYFUNDCOLLECTIONDETAIL','VIEW')}")
public class RotatoryFundCollectionDetailReportAction extends GenericReportAction {

    private RotatoryFundDocumentType documentType;
    private Integer rotatoryFundCode;
    private Integer rotatoryFundCollectionCode;
    private CollectionDocumentType collectionDocumentType;
    private FinancesEntity financesEntity;
    private User approvedByUser;
    private Employee receiver;
    private Employee employee;
    private String gloss;
    private String observation;
    private BigDecimal startAmount;
    private BigDecimal endAmount;
    private FinancesCurrencyType currency;
    private Date startApprovalDate;
    private Date endApprovalDate;
    private Date startDocumentDate;
    private Date endDocumentDate;
    private BusinessUnit businessUnit;
    private CostCenter costCenter;
    private CashAccount cashAccount;

    public void generateReport() {
        log.debug("Generate rotatoryFundCollectionDetailReport........");

        Map params = new HashMap();

        super.generateReport("rotatoryFundCollectionDetailReport", "/finances/reports/rotatoryFundCollectionDetailReport.jrxml", MessageUtils.getMessage("Reports.rotatoryFundCollectionDetail.title"), params);
    }

    @Override
    protected String getEjbql() {

        return "SELECT " +
                "cashAccount.accountCode," +
                "cashAccount.description," +
                "collectionDocument.date," +
                "rotatoryFund.code," +
                "documentType," +
                "rotatoryFundCollection.code," +
                "receiver.lastName," +
                "receiver.maidenName," +
                "receiver.firstName," +
                "collectionDocument.collectionDocumentType," +
                "collectionDocument.number," +
                "financesEntity.acronym," +
                "rotatoryFund.description," +
                "rotatoryFundCollection.observation," +
                "spendDistribution.amount," +
                "rotatoryFundCollection.sourceCurrency" +
                " FROM RotatoryFundCollectionSpendDistribution spendDistribution" +
                " LEFT JOIN spendDistribution.businessUnit businessUnit" +
                " LEFT JOIN spendDistribution.costCenter costCenter" +
                " LEFT JOIN spendDistribution.cashAccount cashAccount" +
                " LEFT JOIN spendDistribution.rotatoryFundCollection rotatoryFundCollection" +
                " LEFT JOIN rotatoryFundCollection.approvedByEmployee approvedByUser" +
                " LEFT JOIN rotatoryFundCollection.receiver receiver" +
                " LEFT JOIN rotatoryFundCollection.collectionDocument collectionDocument" +
                " LEFT JOIN collectionDocument.financesEntity financesEntity" +
                " LEFT JOIN rotatoryFundCollection.rotatoryFund rotatoryFund" +
                " LEFT JOIN rotatoryFund.documentType documentType" +
                " LEFT JOIN rotatoryFund.employee employee";
    }


    @Create
    public void init() {
        restrictions = new String[]{
                "spendDistribution.company=#{currentCompany}",
                "rotatoryFundCollection.state=#{enumerationUtil.getEnumValue('com.encens.khipus.model.finances.RotatoryFundCollectionState','APR')}",
                "documentType = #{rotatoryFundCollectionDetailReportAction.documentType}",
                "rotatoryFund.code = #{rotatoryFundCollectionDetailReportAction.rotatoryFundCode}",
                "rotatoryFundCollection.code = #{rotatoryFundCollectionDetailReportAction.rotatoryFundCollectionCode}",
                "collectionDocument.collectionDocumentType = #{rotatoryFundCollectionDetailReportAction.collectionDocumentType}",
                "financesEntity = #{rotatoryFundCollectionDetailReportAction.financesEntity}",
                "approvedByUser = #{rotatoryFundCollectionDetailReportAction.approvedByUser}",
                "receiver = #{rotatoryFundCollectionDetailReportAction.receiver}",
                "employee = #{rotatoryFundCollectionDetailReportAction.employee}",
                "lower(rotatoryFund.description) LIKE concat('%', concat(lower(#{rotatoryFundCollectionDetailReportAction.gloss}), '%'))",
                "lower(rotatoryFundCollection.observation) LIKE concat('%', concat(lower(#{rotatoryFundCollectionDetailReportAction.observation}), '%'))",
                "spendDistribution.amount >= #{rotatoryFundCollectionDetailReportAction.startAmount}",
                "spendDistribution.amount <= #{rotatoryFundCollectionDetailReportAction.endAmount}",
                "rotatoryFundCollection.sourceCurrency = #{rotatoryFundCollectionDetailReportAction.currency}",
                "rotatoryFundCollection.approvalDate>=#{rotatoryFundCollectionDetailReportAction.startApprovalDate}",
                "rotatoryFundCollection.approvalDate<=#{rotatoryFundCollectionDetailReportAction.endApprovalDate}",
                "collectionDocument.date>=#{rotatoryFundCollectionDetailReportAction.startDocumentDate}",
                "collectionDocument.date<=#{rotatoryFundCollectionDetailReportAction.endDocumentDate}",
                "businessUnit = #{rotatoryFundCollectionDetailReportAction.businessUnit}",
                "costCenter = #{rotatoryFundCollectionDetailReportAction.costCenter}",
                "cashAccount = #{rotatoryFundCollectionDetailReportAction.cashAccount}"
        };

        sortProperty = "cashAccount.description, collectionDocument.date, rotatoryFundCollection.code";
    }

    public RotatoryFundDocumentType getDocumentType() {
        return documentType;
    }

    public void setDocumentType(RotatoryFundDocumentType documentType) {
        this.documentType = documentType;
    }

    public Integer getRotatoryFundCode() {
        return rotatoryFundCode;
    }

    public void setRotatoryFundCode(Integer rotatoryFundCode) {
        this.rotatoryFundCode = rotatoryFundCode;
    }

    public Integer getRotatoryFundCollectionCode() {
        return rotatoryFundCollectionCode;
    }

    public void setRotatoryFundCollectionCode(Integer rotatoryFundCollectionCode) {
        this.rotatoryFundCollectionCode = rotatoryFundCollectionCode;
    }

    public CollectionDocumentType getCollectionDocumentType() {
        return collectionDocumentType;
    }

    public void setCollectionDocumentType(CollectionDocumentType collectionDocumentType) {
        this.collectionDocumentType = collectionDocumentType;
    }

    public FinancesEntity getFinancesEntity() {
        return financesEntity;
    }

    public void setFinancesEntity(FinancesEntity financesEntity) {
        this.financesEntity = financesEntity;
    }

    public User getApprovedByUser() {
        return approvedByUser;
    }

    public void setApprovedByUser(User approvedByUser) {
        this.approvedByUser = approvedByUser;
    }

    public Employee getReceiver() {
        return receiver;
    }

    public void setReceiver(Employee receiver) {
        this.receiver = receiver;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public String getGloss() {
        return gloss;
    }

    public void setGloss(String gloss) {
        this.gloss = gloss;
    }

    public String getObservation() {
        return observation;
    }

    public void setObservation(String observation) {
        this.observation = observation;
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

    public FinancesCurrencyType getCurrency() {
        return currency;
    }

    public void setCurrency(FinancesCurrencyType currency) {
        this.currency = currency;
    }

    public Date getStartApprovalDate() {
        return startApprovalDate;
    }

    public void setStartApprovalDate(Date startApprovalDate) {
        this.startApprovalDate = startApprovalDate;
    }

    public Date getEndApprovalDate() {
        return endApprovalDate;
    }

    public void setEndApprovalDate(Date endApprovalDate) {
        this.endApprovalDate = endApprovalDate;
    }

    public Date getStartDocumentDate() {
        return startDocumentDate;
    }

    public void setStartDocumentDate(Date startDocumentDate) {
        this.startDocumentDate = startDocumentDate;
    }

    public Date getEndDocumentDate() {
        return endDocumentDate;
    }

    public void setEndDocumentDate(Date endDocumentDate) {
        this.endDocumentDate = endDocumentDate;
    }

    public BusinessUnit getBusinessUnit() {
        return businessUnit;
    }

    public void setBusinessUnit(BusinessUnit businessUnit) {
        this.businessUnit = businessUnit;
    }

    public CostCenter getCostCenter() {
        return costCenter;
    }

    public void setCostCenter(CostCenter costCenter) {
        this.costCenter = costCenter;
    }

    public CashAccount getCashAccount() {
        return cashAccount;
    }

    public void setCashAccount(CashAccount cashAccount) {
        this.cashAccount = cashAccount;
    }

    public FinancesCurrencyType[] getSingleFinancesCurrencyTypes() {
        return new FinancesCurrencyType[]{FinancesCurrencyType.D, FinancesCurrencyType.P};
    }

}

