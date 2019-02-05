package com.encens.khipus.action.customers.reports;

import com.encens.khipus.action.reports.GenericReportAction;
import com.encens.khipus.action.reports.PageFormat;
import com.encens.khipus.action.reports.PageOrientation;
import com.encens.khipus.model.customers.CreditState;
import com.encens.khipus.model.customers.CreditType;
import com.encens.khipus.model.production.ProductiveZone;
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
@Name("creditStatusZoneReportAction")
@Scope(ScopeType.PAGE)
public class CreditStatusZoneReportAction extends GenericReportAction {

    private Date date;
    private ProductiveZone productiveZone;

    @Create
    public void init() {
        restrictions = new String[]{};
    }


    protected String getEjbql() {

        String ejbql = "";

        ejbql = " SELECT " +
                " productiveZone.number || '-' || productiveZone.name as gabName, " +
                " credit.previousCode as code," +
                " partner.firstName || partner.lastName || partner.maidenName as partnerName," +
                " credit.grantDate," +
                " credit.annualRate," +
                " credit.amount," +
                " credit.lastPayment," +
                " credit.capitalBalance," +
                " credit.quota," +
                " credit.state" +
                " FROM Credit credit" +
                " LEFT JOIN credit.partner partner" +
                " LEFT JOIN partner.productiveZone productiveZone" +
                " ORDER BY productiveZone.number ";

        if (productiveZone != null){

            ejbql = " SELECT " +
                    " productiveZone.numero || '-' || productiveZone.name as gab, " +
                    " credit.previousCode as code," +
                    " partner.firstName," +
                    " partner.lastName," +
                    " partner.maidenName," +
                    " credit.grantDate," +
                    " credit.annualRate," +
                    " credit.amount," +
                    " credit.lastPayment," +
                    " credit.capitalBalance," +
                    " credit.quota," +
                    " credit.state" +
                    " FROM Credit credit" +
                    " LEFT JOIN credit.partner partner" +
                    " LEFT JOIN partner.productiveZone productiveZone" +
                    " WHERE productiveZone = " + productiveZone +
                    " ORDER BY firstName ";
        }

        return ejbql;
    }


    public void generateReport() {

        String documentTitle = "ESTADO DE CARTERA";
        log.debug("Generating credit status report...................");
        HashMap<String, Object> reportParameters = new HashMap<String, Object>();
        reportParameters.put("documentTitle", documentTitle);
        //reportParameters.put("startDate", startDate);
        //reportParameters.put("endDate", endDate);

        super.generateReport(
                "summaryProviderKardexReport",
                "/customers/reports/creditStatusZoneReport.jrxml",
                PageFormat.LETTER,
                PageOrientation.PORTRAIT,
                messages.get("Reports.credit.creditStatus.title"),
                reportParameters);

    }


    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public ProductiveZone getProductiveZone() {
        return productiveZone;
    }

    public void setProductiveZone(ProductiveZone productiveZone) {
        this.productiveZone = productiveZone;
    }
}