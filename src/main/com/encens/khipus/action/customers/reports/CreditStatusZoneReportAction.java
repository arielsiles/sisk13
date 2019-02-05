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

    private Date dateTransaction;
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
                " partner.firstName || ' ' || partner.lastName || ' ' || partner.maidenName as partnerName," +
                " credit.grantDate," +
                " credit.annualRate," +
                " credit.amount," +
                " credit.lastPayment," +
                " credit.capitalBalance," +
                " credit.quota," +
                " credit.state, " +
                " credit.id as creditId" +
                " FROM Credit credit" +
                " LEFT JOIN credit.partner partner" +
                " LEFT JOIN partner.productiveZone productiveZone" +
                " ORDER BY credit.previousCode ";

        if (productiveZone != null){

            ejbql = " SELECT " +
                    " productiveZone.number || '-' || productiveZone.name as gabName, " +
                    " credit.previousCode as code," +
                    " partner.firstName || ' ' || partner.lastName || ' ' || partner.maidenName as partnerName," +
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
                    " WHERE partner.productiveZone = " + productiveZone +
                    " ORDER BY credit.previousCode ";
        }

        return ejbql;
    }


    public void generateReport() {

        String documentTitle = "ESTADO DE CARTERA POR GAB";
        log.debug("Generating credit status report...................");
        HashMap<String, Object> reportParameters = new HashMap<String, Object>();
        reportParameters.put("documentTitle", documentTitle);
        reportParameters.put("dateTransaction", dateTransaction);
        //reportParameters.put("endDate", endDate);

        super.generateReport(
                "summaryProviderKardexReport",
                "/customers/reports/creditStatusZoneReport.jrxml",
                PageFormat.LETTER,
                PageOrientation.LANDSCAPE,
                messages.get("Reports.credit.creditStatus.title"),
                reportParameters);

    }


    public ProductiveZone getProductiveZone() {
        return productiveZone;
    }

    public void setProductiveZone(ProductiveZone productiveZone) {
        this.productiveZone = productiveZone;
    }

    public void assignProductiveZone(ProductiveZone productiveZone){
        setProductiveZone(productiveZone);
    }

    public Date getDateTransaction() {
        return dateTransaction;
    }

    public void setDateTransaction(Date dateTransaction) {
        this.dateTransaction = dateTransaction;
    }
}