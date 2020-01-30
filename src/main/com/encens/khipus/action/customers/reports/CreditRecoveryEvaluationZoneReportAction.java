package com.encens.khipus.action.customers.reports;

import com.encens.khipus.action.reports.GenericReportAction;
import com.encens.khipus.action.reports.PageFormat;
import com.encens.khipus.action.reports.PageOrientation;
import com.encens.khipus.model.customers.CreditType;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import java.util.Date;
import java.util.HashMap;

/**
 * Encens S.R.L.
 * This class implements the purchaseOrder report action
 *
 * @author
 * @version 3.0
 */
@Name("creditRecoveryEvaluationZoneReportAction")
@Scope(ScopeType.PAGE)
public class CreditRecoveryEvaluationZoneReportAction extends GenericReportAction {

    private Date startDate;
    private Date endDate;
    private CreditType creditType;

    @Create
    public void init() {
        restrictions = new String[]{};
    }


    /** EN DESARROLLO **/
    protected String getEjbql() {

        String ejbql = "";

        //String dateParam = DateUtils.format(this.dateTransaction, "yyyy-MM-dd");
        ejbql = " SELECT " +
                " productiveZone.id, " +
                " productiveZone.number, " +
                " productiveZone.name " +
                " FROM ProductiveZone productiveZone" +
                "  ";



        return ejbql;
    }


    public void generateReport() {

        String documentTitle = messages.get("Reports.credit.recoveryEvaluationZone.titleReport");
        log.debug("Generating credit status report...................");
        HashMap<String, Object> reportParameters = new HashMap<String, Object>();
        reportParameters.put("documentTitle", documentTitle);
        reportParameters.put("startDate", startDate);
        reportParameters.put("endDate", endDate);

        super.generateReport(
                "summaryProviderKardexReport",
                "/customers/reports/creditRecoveryEvaluationZoneReport.jrxml",
                PageFormat.LETTER,
                PageOrientation.LANDSCAPE,
                messages.get("Reports.credit.recoveryEvaluationZone.titleFile"),
                reportParameters);

    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public CreditType getCreditType() {
        return creditType;
    }

    public void setCreditType(CreditType creditType) {
        this.creditType = creditType;
    }
}