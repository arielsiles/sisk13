package com.encens.khipus.action.customers.reports;

import com.encens.khipus.action.reports.GenericReportAction;
import com.encens.khipus.action.reports.PageFormat;
import com.encens.khipus.action.reports.PageOrientation;
import com.encens.khipus.exception.finances.CompanyConfigurationNotFoundException;
import com.encens.khipus.model.customers.SavingType;
import com.encens.khipus.model.finances.CompanyConfiguration;
import com.encens.khipus.service.fixedassets.CompanyConfigurationService;
import com.encens.khipus.util.DateUtils;
import com.encens.khipus.util.MessageUtils;
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
@Name("contributionDatesReportAction")
@Scope(ScopeType.PAGE)
public class ContributionDatesReportAction extends GenericReportAction {

    @In
    private CompanyConfigurationService companyConfigurationService;
    @In
    private FacesMessages facesMessages;

    private Date startDate;
    private Date endDate;
    private SavingType savingTypeDPF = SavingType.DPF;

    @Create
    public void init() {
        restrictions = new String[]{};
    }

    protected String getEjbql() {

        String ejbql = " SELECT " +
                " voucherDetail.account, " +
                " productiveZone.number || ' ' || productiveZone.name AS gab, " +
                " partner.idNumber, " +
                " partner.firstName || ' ' || partner.lastName || ' ' || partner.maidenName AS partnerName, " +
                " SUM(voucherDetail.credit) AS balanceMN " +
                " FROM VoucherDetail voucherDetail" +
                " LEFT JOIN voucherDetail.voucher voucher" +
                " LEFT JOIN voucherDetail.partner partner" +
                " LEFT JOIN partner.productiveZone productiveZone" +
                " WHERE voucher.state <> 'ANL'" +
                " AND voucherDetail.partner is not null" +
                " AND voucher.date between #{contributionDatesReportAction.startDate} AND #{contributionDatesReportAction.endDate} " +
                " AND voucherDetail.account = '3110100000'" +
                " GROUP BY productiveZone.number, partner.firstName, partner.lastName, partner.maidenName" +
                " HAVING SUM(voucherDetail.credit) > 0" +
                " ORDER BY productiveZone.number, partner.firstName, partner.lastName, partner.maidenName";

        return ejbql;
    }


    public void generateReport() {

        CompanyConfiguration companyConfiguration = null;
        try {
            companyConfiguration = companyConfigurationService.findCompanyConfiguration();
        } catch (CompanyConfigurationNotFoundException e) {facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,"CompanyConfiguration.notFound");;}

        String documentTitle = "A P O R T E S   D E   S O C I O S";
        //String startDate = DateUtils.format(getSqlQuery().getStartDate(), MessageUtils.getMessage("patterns.date"));
        String period = "Del " + DateUtils.format(startDate, MessageUtils.getMessage("patterns.date")) + " al " + DateUtils.format(endDate, MessageUtils.getMessage("patterns.date"));

        log.debug("Generating credit status report...................");
        HashMap<String, Object> reportParameters = new HashMap<String, Object>();
        reportParameters.put("companyName", companyConfiguration.getCompanyName());
        reportParameters.put("systemName", companyConfiguration.getSystemName());
        reportParameters.put("locationName", companyConfiguration.getLocationName());
        reportParameters.put("documentTitle", documentTitle);
        reportParameters.put("endDate", endDate);
        reportParameters.put("period", period);

        super.generateReport(
                "contributionStatusReport",
                "/customers/reports/contributionDatesReport.jrxml",
                PageFormat.LETTER,
                PageOrientation.PORTRAIT,
                messages.get("Account.report.contributionStatusReport.title"),
                reportParameters);

    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public SavingType getSavingTypeDPF() {
        return savingTypeDPF;
    }

    public void setSavingTypeDPF(SavingType savingTypeDPF) {
        this.savingTypeDPF = savingTypeDPF;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }
}