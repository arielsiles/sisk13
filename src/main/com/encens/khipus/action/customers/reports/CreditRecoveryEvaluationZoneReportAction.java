package com.encens.khipus.action.customers.reports;

import com.encens.khipus.action.reports.GenericReportAction;
import com.encens.khipus.action.reports.PageFormat;
import com.encens.khipus.action.reports.PageOrientation;
import com.encens.khipus.exception.finances.CompanyConfigurationNotFoundException;
import com.encens.khipus.model.customers.CreditType;
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
@Name("creditRecoveryEvaluationZoneReportAction")
@Scope(ScopeType.PAGE)
public class CreditRecoveryEvaluationZoneReportAction extends GenericReportAction {

    @In
    private CompanyConfigurationService companyConfigurationService;
    @In
    private FacesMessages facesMessages;

    private Date startDate;
    private Date endDate;
    private CreditType creditType;

    @Create
    public void init() {
        restrictions = new String[]{
                "credit.state <> #{creditAction.creditStateFIN} ",
                "credit.creditType = #{creditRecoveryEvaluationZoneReportAction.creditType}"
        };
    }

    protected String getEjbql() {

        String ejbql = "";

        //String dateParam = DateUtils.format(this.dateTransaction, "yyyy-MM-dd");
        ejbql = " SELECT DISTINCT " +
                " productiveZone.id, " +
                " productiveZone.number, " +
                " productiveZone.name " +
                " FROM Credit credit " +
                " JOIN credit.partner partner" +
                " JOIN partner.productiveZone productiveZone " +
                /*" WHERE credit.state <> #{creditAction.creditStateFIN} " +*/
                "  ";



        return ejbql;
    }


    public void generateReport() {

        CompanyConfiguration companyConfiguration = null;
        try {
            companyConfiguration = companyConfigurationService.findCompanyConfiguration();
        } catch (CompanyConfigurationNotFoundException e) {facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,"CompanyConfiguration.notFound");;}

        String documentTitle = messages.get("Reports.credit.recoveryEvaluationZone.titleReport");
        HashMap<String, Object> reportParameters = new HashMap<String, Object>();
        reportParameters.put("companyName", companyConfiguration.getCompanyName());
        reportParameters.put("systemName", companyConfiguration.getSystemName());
        reportParameters.put("locationName", companyConfiguration.getLocationName());
        reportParameters.put("documentTitle", documentTitle);
        reportParameters.put("startDate", startDate);
        reportParameters.put("endDate", endDate);

        super.generateReport(
                "summaryProviderKardexReport",
                "/customers/reports/creditRecoveryEvaluationZoneReport.jrxml",
                PageFormat.LETTER,
                PageOrientation.PORTRAIT,
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