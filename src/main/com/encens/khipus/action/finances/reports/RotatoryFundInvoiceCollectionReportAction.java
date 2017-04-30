package com.encens.khipus.action.finances.reports;

import com.encens.khipus.action.reports.GenericReportAction;
import com.encens.khipus.model.admin.User;
import com.encens.khipus.model.employees.Employee;
import com.encens.khipus.model.finances.FinancesCurrencyType;
import com.encens.khipus.model.finances.FinancesEntity;
import com.encens.khipus.model.finances.RotatoryFundDocumentType;
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
@Name("rotatoryFundInvoiceCollectionReportAction")
@Scope(ScopeType.PAGE)
@Restrict("#{s:hasPermission('REPORTROTATORYFUNDINVOICECOLLECTION','VIEW')}")
public class RotatoryFundInvoiceCollectionReportAction extends GenericReportAction {

    private RotatoryFundDocumentType documentType;
    private Integer rotatoryFundCode;
    private Integer rotatoryFundCollectionCode;
    private FinancesEntity financesEntity;
    private User approvedByUser;
    private Employee receiver;
    private Employee employee;
    private String gloss;
    private String observation;
    private BigDecimal startAmount;
    private BigDecimal endAmount;
    private Date startApprovalDate;
    private Date endApprovalDate;
    private Date startDocumentDate;
    private Date endDocumentDate;

    public void generateReport() {
        log.debug("Generate RotatoryFundInvoiceCollectionReport........");

        Map params = new HashMap();

        super.generateReport("rotatoryFundInvoiceCollectionReport", "/finances/reports/rotatoryFundInvoiceCollectionReport.jrxml", MessageUtils.getMessage("Reports.rotatoryFundInvoiceCollection.title"), params);
    }

    @Override
    protected String getEjbql() {

        return "SELECT " +
                "collectionDocument.date," +
                "rotatoryFund.code," +
                "documentType," +
                "collectionDocument.number," +
                "financesEntity.acronym," +
                "rotatoryFund.description," +
                "rotatoryFundCollection.observation," +
                "rotatoryFundCollection.sourceAmount," +
                "rotatoryFundCollection.sourceCurrency" +
                " FROM RotatoryFundCollection rotatoryFundCollection" +
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
                "rotatoryFundCollection.company=#{currentCompany}",
                "rotatoryFundCollection.state=#{enumerationUtil.getEnumValue('com.encens.khipus.model.finances.RotatoryFundCollectionState','APR')}",
                "collectionDocument.collectionDocumentType=#{enumerationUtil.getEnumValue('com.encens.khipus.model.finances.CollectionDocumentType','INVOICE')}",
                "documentType = #{rotatoryFundInvoiceCollectionReportAction.documentType}",
                "rotatoryFund.code = #{rotatoryFundInvoiceCollectionReportAction.rotatoryFundCode}",
                "rotatoryFundCollection.code = #{rotatoryFundInvoiceCollectionReportAction.rotatoryFundCollectionCode}",
                "financesEntity = #{rotatoryFundInvoiceCollectionReportAction.financesEntity}",
                "approvedByUser = #{rotatoryFundInvoiceCollectionReportAction.approvedByUser}",
                "receiver = #{rotatoryFundInvoiceCollectionReportAction.receiver}",
                "employee = #{rotatoryFundInvoiceCollectionReportAction.employee}",
                "lower(rotatoryFund.description) LIKE concat('%', concat(lower(#{rotatoryFundInvoiceCollectionReportAction.gloss}), '%'))",
                "lower(rotatoryFundCollection.observation) LIKE concat('%', concat(lower(#{rotatoryFundInvoiceCollectionReportAction.observation}), '%'))",
                "rotatoryFundCollection.sourceAmount >= #{rotatoryFundInvoiceCollectionReportAction.startAmount}",
                "rotatoryFundCollection.sourceAmount <= #{rotatoryFundInvoiceCollectionReportAction.endAmount}",
                "rotatoryFundCollection.approvalDate >= #{rotatoryFundInvoiceCollectionReportAction.startApprovalDate}",
                "rotatoryFundCollection.approvalDate <= #{rotatoryFundInvoiceCollectionReportAction.endApprovalDate}",
                "collectionDocument.date>=#{rotatoryFundInvoiceCollectionReportAction.startDocumentDate}",
                "collectionDocument.date<=#{rotatoryFundInvoiceCollectionReportAction.endDocumentDate}"
        };
        sortProperty = "collectionDocument.date, financesEntity.acronym";
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

    public FinancesCurrencyType[] getSingleFinancesCurrencyTypes() {
        return new FinancesCurrencyType[]{FinancesCurrencyType.D, FinancesCurrencyType.P};
    }
}
