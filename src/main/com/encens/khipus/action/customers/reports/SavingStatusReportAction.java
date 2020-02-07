package com.encens.khipus.action.customers.reports;

import com.encens.khipus.action.reports.GenericReportAction;
import com.encens.khipus.action.reports.PageFormat;
import com.encens.khipus.action.reports.PageOrientation;
import com.encens.khipus.exception.finances.CompanyConfigurationNotFoundException;
import com.encens.khipus.model.customers.SavingType;
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
@Name("savingStatusReportAction")
@Scope(ScopeType.PAGE)
public class SavingStatusReportAction extends GenericReportAction {

    @In
    private CompanyConfigurationService companyConfigurationService;
    @In
    private FacesMessages facesMessages;

    private Date endDate;
    private SavingType savingTypeDPF = SavingType.DPF;

    @Create
    public void init() {
        restrictions = new String[]{};
    }

    protected String getEjbql() {

        String ejbql = "";

        ejbql = " SELECT " +
                " partnerAccount.accountNumber, " +
                " partnerAccount.code, " +
                " partner.firstName || ' ' || partner.lastName || ' ' || partner.maidenName as name, " +
                " partnerAccount.currency, " +
                " SUM(voucherDetail.credit) - SUM(voucherDetail.debit) -  AS balanceMN, " +
                " SUM(voucherDetail.creditMe) - SUM(voucherDetail.debitME) -  AS balanceME " +
                " FROM VoucherDetail voucherDetail" +
                " JOIN voucherDetail.voucher voucher" +
                " JOIN voucherDetail.partnerAccount partnerAccount" +
                " JOIN partnerAccount.partner partner" +
                " JOIN partnerAccount.accountType accountType" +
                " WHERE voucher.state <> 'ANL'" +
                " AND voucherDetail.partnerAccount is not null" +
                " AND voucher.date <= #{savingStatusReportAction.endDate} " +
                " AND savingType.savingType <> #{savingStatusReportAction.savingTypeDPF}" +
                " GROUP BY partnerAccount.accountNumber, partnerAccount.code, partner.firstName, partner.lastName, partner.maidenName, partnerAccount.currency";


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
        reportParameters.put("endDate", endDate);

        super.generateReport(
                "newCreditsReport",
                "/customers/reports/savingStatusReport.jrxml",
                PageFormat.LETTER,
                PageOrientation.PORTRAIT,
                messages.get("Account.report.savingStatusReport.title"),
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
}