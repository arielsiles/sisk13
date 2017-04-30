package com.encens.khipus.action.finances.reports;

import com.encens.khipus.action.reports.GenericReportAction;
import com.encens.khipus.model.employees.Employee;
import com.encens.khipus.model.finances.RotatoryFundCollectionState;
import com.encens.khipus.util.DateUtils;
import com.encens.khipus.util.MessageUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Encens S.R.L.
 * Action to generate detail of retrieve by debtor report
 *
 * @author
 * @version $Id: RetrieveByDebtorReportAction.java  09-sep-2010 18:53:54$
 */
@Name("retrieveByDebtorReportAction")
@Scope(ScopeType.PAGE)
@Restrict("#{s:hasPermission('REPORTRETRIEVEBYDEBTOR','VIEW')}")
public class RetrieveByDebtorReportAction extends GenericReportAction {

    private Employee employee;
    private Date initDate;
    private Date endDate;
    private RotatoryFundCollectionState rotatoryFundCollectionState;

    public void generateReport() {
        log.debug("Generate RetrieveByDebtorReportAction........");

        //set default filters
        setRotatoryFundCollectionState(RotatoryFundCollectionState.APR);

        Map params = new HashMap();
        params.putAll(getReportParamsInfo());

        super.generateReport("retrieveByDebtorReport", "/finances/reports/retrieveByDebtorReport.jrxml", MessageUtils.getMessage("Reports.retrieveByDebtor.title"), params);
    }

    @Override
    protected String getEjbql() {

        return "SELECT " +
                "employee.id," +
                "employee.lastName," +
                "employee.maidenName," +
                "employee.firstName," +
                "rotatoryFund.id," +
                "documentType," +
                "rotatoryFund.code," +
                "rotatoryFundCollection.creationDate," +
                "rotatoryFundCollection.collectionAmount," +
                "rotatoryFundCollection.collectionCurrency," +
                "rotatoryFundCollection.description" +
                " FROM RotatoryFundCollection rotatoryFundCollection" +
                " LEFT JOIN rotatoryFundCollection.rotatoryFund rotatoryFund" +
                " LEFT JOIN rotatoryFund.documentType documentType" +
                " LEFT JOIN rotatoryFund.employee employee" +
                " WHERE (documentType.rotatoryFundType = #{enumerationUtil.getEnumValue('com.encens.khipus.model.finances.RotatoryFundType','LOAN')} OR " +
                " documentType.rotatoryFundType = #{enumerationUtil.getEnumValue('com.encens.khipus.model.finances.RotatoryFundType','ADVANCE')})";
    }


    @Create
    public void init() {
        restrictions = new String[]{
                "employee = #{retrieveByDebtorReportAction.employee}",
                "rotatoryFundCollection.state = #{retrieveByDebtorReportAction.rotatoryFundCollectionState}",
                "rotatoryFundCollection.creationDate >= #{retrieveByDebtorReportAction.initDate}",
                "rotatoryFundCollection.creationDate <= #{retrieveByDebtorReportAction.endDate}"
        };

        sortProperty = "employee.lastName, employee.maidenName,employee.firstName, rotatoryFundCollection.creationDate, rotatoryFund.id";
    }

    /**
     * compose required report params
     *
     * @return Map
     */
    private Map getReportParamsInfo() {
        Map paramMap = new HashMap();
        String dateRangeInfo = "";

        if (initDate != null) {
            dateRangeInfo = dateRangeInfo + MessageUtils.getMessage("Common.dateFrom") + " " + DateUtils.format(initDate, MessageUtils.getMessage("patterns.date")) + " ";
        }

        if (endDate != null) {
            dateRangeInfo = dateRangeInfo + MessageUtils.getMessage("Common.dateTo") + " " + DateUtils.format(endDate, MessageUtils.getMessage("patterns.date"));
        }

        paramMap.put("dateRangeParam", dateRangeInfo);
        return paramMap;
    }

    public void clearEmployee() {
        setEmployee(null);
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
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

    public RotatoryFundCollectionState getRotatoryFundCollectionState() {
        return rotatoryFundCollectionState;
    }

    public void setRotatoryFundCollectionState(RotatoryFundCollectionState rotatoryFundCollectionState) {
        this.rotatoryFundCollectionState = rotatoryFundCollectionState;
    }
}
