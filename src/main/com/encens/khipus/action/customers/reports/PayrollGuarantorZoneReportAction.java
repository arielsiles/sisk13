package com.encens.khipus.action.customers.reports;

import com.encens.khipus.action.reports.GenericReportAction;
import com.encens.khipus.action.reports.PageFormat;
import com.encens.khipus.action.reports.PageOrientation;
import com.encens.khipus.exception.finances.CompanyConfigurationNotFoundException;
import com.encens.khipus.model.employees.Currency;
import com.encens.khipus.model.finances.CompanyConfiguration;
import com.encens.khipus.model.production.ProductiveZone;
import com.encens.khipus.service.fixedassets.CompanyConfigurationService;
import com.encens.khipus.util.DateUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;

import java.util.Date;
import java.util.HashMap;

/**
 * Encens S.R.L.
 * This class implements the purchaseOrder report action
 *
 * @author
 * @version 3.0
 */
@Name("payrollGuarantorZoneReportAction")
@Scope(ScopeType.PAGE)
public class PayrollGuarantorZoneReportAction extends GenericReportAction {

    private Date dateTransaction = new Date();
    private ProductiveZone productiveZone;
    private Currency currency;

    @In
    private CompanyConfigurationService companyConfigurationService;
    @In
    private FacesMessages facesMessages;

    @Create
    public void init() {
        restrictions = new String[]{};
    }


    /** EN DESARROLLO **/
    protected String getEjbql() {

        String ejbql = "";

        String dateParam = DateUtils.format(this.dateTransaction, "yyyy-MM-dd");

        /*ejbql = " SELECT " +
                " productiveZone.number || '-' || productiveZone.name as gabName, " +
                " credit.state," +
                " creditType.name as creditTypeName," +
                " partner.firstName," +
                " partner.lastName," +
                " partner.maidenName," +
                " credit.grantDate," +
                " credit.amount," +
                " credit.id as creditId," +
                " credit.expirationDate," +
                " credit.previousCode as code," +
                " credit.annualRate, " +
                " credit.quota, " +

                " credit.amount - SUM(creditTransaction.capital) AS capitalBalance," +
                " MAX(creditTransaction.date) as lastPayment" +

                " FROM CreditTransaction creditTransaction" +
                " LEFT JOIN creditTransaction.credit credit" +
                " LEFT JOIN credit.creditType creditType" +
                " LEFT JOIN credit.partner partner" +
                " LEFT JOIN partner.productiveZone productiveZone " +
                " WHERE productiveZone.id = " + productiveZone.getId() +
                " AND creditTransaction.date <= '" + dateParam + "'" +
                " AND credit.state <> #{creditAction.creditStateFIN} "+
                " AND credit.creditType.currency.id = " + currency.getId() +
                " GROUP BY productiveZone.number, productiveZone.name, credit.state, creditType.name, " +
                " partner.firstName, partner.lastName, partner.maidenName, credit.grantDate, credit.amount, credit.id, " +
                " credit.expirationDate, credit.previousCode, credit.annualRate, credit.quota " +
                " ORDER BY partner.firstName, partner.lastName, partner.maidenName";*/

        ejbql = " SELECT " +
                " productiveZone.number || '-' || productiveZone.name as gabName, " +
                " credit.state," +
                " creditType.name as creditTypeName," +
                " partner.firstName," +
                " partner.lastName," +
                " partner.maidenName," +
                " credit.grantDate," +
                " credit.amount," +
                " credit.id as creditId" +
                " FROM Credit credit " +
                " LEFT JOIN credit.creditType creditType" +
                " LEFT JOIN credit.partner partner" +
                " LEFT JOIN partner.productiveZone productiveZone " +
                " WHERE productiveZone.id = " + productiveZone.getId() +
                " AND credit.state <> #{creditAction.creditStateFIN} "+
                " AND credit.creditType.currency.id = " + currency.getId() +
                " ORDER BY partner.firstName, partner.lastName, partner.maidenName";

        return ejbql;
    }


    public void generateReport() {

        CompanyConfiguration companyConfiguration = null;
        try {
            companyConfiguration = companyConfigurationService.findCompanyConfiguration();
        } catch (CompanyConfigurationNotFoundException e) {facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,"CompanyConfiguration.notFound");;}

        String documentTitle = "NOMINA DE SOCIOS PRESTATARIOS Y GARANTES";
        log.debug("Generating credit status report...................");
        HashMap<String, Object> reportParameters = new HashMap<String, Object>();
        reportParameters.put("companyName", companyConfiguration.getCompanyName());
        reportParameters.put("systemName", companyConfiguration.getSystemName());
        reportParameters.put("locationName", companyConfiguration.getLocationName());
        reportParameters.put("documentTitle", documentTitle);
        reportParameters.put("dateTransaction", dateTransaction);
        reportParameters.put("currency", currency.getSymbol());

        //reportParameters.put("endDate", endDate);

        super.generateReport(
                "summaryProviderKardexReport",
                "/customers/reports/payrollGuarantorZoneReport.jrxml",
                PageFormat.LETTER,
                PageOrientation.PORTRAIT,
                messages.get("Reports.credit.payrollGuarantor.title"),
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

    public Currency getCurrency() {
        return currency;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }
}