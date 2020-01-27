package com.encens.khipus.action.customers.reports;

import com.encens.khipus.action.reports.GenericReportAction;
import com.encens.khipus.action.reports.PageFormat;
import com.encens.khipus.action.reports.PageOrientation;
import com.encens.khipus.model.customers.CreditState;
import com.encens.khipus.model.customers.CreditType;
import com.encens.khipus.service.customers.CreditTransactionService;
import com.encens.khipus.util.DateUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
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
@Name("creditStatusReportAction")
@Scope(ScopeType.PAGE)
public class CreditStatusReportAction extends GenericReportAction {

    private Date endPeriodDate;

    private CreditState creditState;
    private CreditType creditType;

    @In
    private CreditTransactionService creditTransactionService;

    @Create
    public void init() {
        restrictions = new String[]{};
    }


    protected String getEjbql() {

        String ejbql = "";
        String restrictions = "";

        if (creditState != null)
            restrictions += " AND credit.state = '" + creditState.toString() + "'";
        if (creditType != null)
            restrictions += " AND credit.creditType = " + creditType.getId();

        //if (creditState != null && creditType != null && endPeriodDate != null){
        if (endPeriodDate != null){
            String dateParam = DateUtils.format(this.endPeriodDate, "yyyy-MM-dd");
            ejbql = " SELECT " +
                    " credit.state || ' - ' || creditType.name AS status," +
                    " credit.state," +
                    " creditType.name as creditTypeName," +
                    " productiveZone.name AS gabName," +
                    " partner.firstName," +
                    " partner.lastName," +
                    " partner.maidenName," +
                    " credit.grantDate," +
                    " credit.amount," +
                    " credit.id as creditId," +
                    " credit.expirationDate," +
                    " credit.previousCode," +

                    " credit.amount - SUM(creditTransaction.capital) AS capitalBalance," +
                    " MAX(creditTransaction.date) as lastPayment" +

                    " FROM CreditTransaction creditTransaction" +
                    " LEFT JOIN creditTransaction.credit credit" +
                    " LEFT JOIN credit.creditType creditType" +
                    " LEFT JOIN credit.partner partner" +
                    " LEFT JOIN partner.productiveZone productiveZone " +
                    " WHERE creditTransaction.date <= '" + dateParam + "'" +
                    /*" WHERE credit.state = '" + creditState.toString() + "'" +*/
                    /*" AND credit.creditType = " + creditType.getId() + "" +*/
                    restrictions +
                    /*" AND creditTransaction.creditTransactionType = '" + CreditTransactionType.ING + "'" +*/
                    " AND credit.state <> #{creditAction.creditStateFIN} "+
                    " GROUP BY credit.state, creditType.name, productiveZone.name, partner.firstName, " +
                    " partner.lastName, partner.maidenName, credit.grantDate, credit.amount, credit.capitalBalance ";
        }

        return ejbql;
    }


    public void generateReport() {

        String documentTitle = "ESTADO DE CARTERA";
        log.debug("Generating credit status report...................");
        HashMap<String, Object> reportParameters = new HashMap<String, Object>();
        reportParameters.put("documentTitle", documentTitle);
        reportParameters.put("endPeriodDate", endPeriodDate);


        super.generateReport(
                "summaryProviderKardexReport",
                "/customers/reports/creditStatusReport.jrxml",
                PageFormat.LETTER,
                PageOrientation.PORTRAIT,
                messages.get("Reports.credit.creditStatus.title"),
                reportParameters);

    }


    public CreditState getCreditState() {
        return creditState;
    }

    public void setCreditState(CreditState creditState) {
        this.creditState = creditState;
    }

    public CreditType getCreditType() {
        return creditType;
    }

    public void setCreditType(CreditType creditType) {
        this.creditType = creditType;
    }

    public Date getEndPeriodDate() {
        return endPeriodDate;
    }

    public void setEndPeriodDate(Date endPeriodDate) {
        this.endPeriodDate = endPeriodDate;
    }
}