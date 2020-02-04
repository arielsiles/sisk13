package com.encens.khipus.action.customers.reports;

import com.encens.khipus.action.reports.GenericReportAction;
import com.encens.khipus.action.reports.PageFormat;
import com.encens.khipus.action.reports.PageOrientation;
import com.encens.khipus.exception.finances.CompanyConfigurationNotFoundException;
import com.encens.khipus.model.employees.Currency;
import com.encens.khipus.model.finances.CompanyConfiguration;
import com.encens.khipus.service.fixedassets.CompanyConfigurationService;
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
@Name("newCreditsReportAction")
@Scope(ScopeType.PAGE)
public class NewCreditsReportAction extends GenericReportAction {

    @In
    private CompanyConfigurationService companyConfigurationService;
    @In
    private FacesMessages facesMessages;

    private Date startDate;
    private Date endDate;
    private Currency currency;

    @Create
    public void init() {
        restrictions = new String[]{};
    }

    protected String getEjbql() {

        String ejbql = "";

        ejbql = " SELECT " +
                " credit.partner.productiveZone.number || '-' || credit.partner.productiveZone.name as  gabName, " +
                " credit.previousCode as creditCode," +
                " credit.partner.firstName || ' ' || credit.partner.lastName || ' ' || credit.partner.maidenName as partnerName," +
                " credit.grantDate," +
                " credit.amount," +
                " credit.expirationDate," +
                " credit.annualRate, " +
                " credit.quota" +
                " FROM Credit credit" +
                " WHERE credit.grantDate BETWEEN #{newCreditsReportAction.startDate} AND #{newCreditsReportAction.endDate} " +
                " AND credit.creditType.currency.id = " + currency.getId() +
                " ORDER BY credit.partner.productiveZone.name, credit.partner.firstName, credit.partner.lastName ";


        return ejbql;
    }


    public void generateReport() {

        CompanyConfiguration companyConfiguration = null;
        try {
            companyConfiguration = companyConfigurationService.findCompanyConfiguration();
        } catch (CompanyConfigurationNotFoundException e) {facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,"CompanyConfiguration.notFound");;}

        String documentTitle = messages.get("Reports.credit.newCredits.title");
        log.debug("Generating credit status report...................");
        HashMap<String, Object> reportParameters = new HashMap<String, Object>();
        reportParameters.put("companyName", companyConfiguration.getCompanyName());
        reportParameters.put("systemName", companyConfiguration.getSystemName());
        reportParameters.put("locationName", companyConfiguration.getLocationName());
        reportParameters.put("documentTitle", documentTitle);
        reportParameters.put("startDate", startDate);
        reportParameters.put("endDate", endDate);

        super.generateReport(
                "newCreditsReport",
                "/customers/reports/newCreditsReport.jrxml",
                PageFormat.LETTER,
                PageOrientation.LANDSCAPE,
                messages.get("Credit.report.newCreditsReport.title"),
                reportParameters);

    }


    public Currency getCurrency() {
        return currency;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
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
}