package com.encens.khipus.action.accounting.reports;

import com.encens.khipus.action.reports.GenericReportAction;
import com.encens.khipus.action.reports.PageFormat;
import com.encens.khipus.action.reports.PageOrientation;
import com.encens.khipus.exception.finances.CompanyConfigurationNotFoundException;
import com.encens.khipus.exception.finances.FinancesCurrencyNotFoundException;
import com.encens.khipus.exception.finances.FinancesExchangeRateNotFoundException;
import com.encens.khipus.model.accounting.DocType;
import com.encens.khipus.model.admin.User;
import com.encens.khipus.model.finances.CompanyConfiguration;
import com.encens.khipus.model.finances.FinancesCurrencyType;
import com.encens.khipus.service.finances.FinancesExchangeRateService;
import com.encens.khipus.service.fixedassets.CompanyConfigurationService;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

/**
 * Encens S.R.L.
 * This class implements the valued warehouse residue report action
 *
 * @author
 * @version 2.3
 */

@Name("dailyCollectionReportAction")
@Scope(ScopeType.PAGE)
public class DailyCollectionReportAction extends GenericReportAction {

    private Date startDate;
    private Date endDate;
    private DocType documentType;

    @In
    User currentUser;
    @In
    private FinancesExchangeRateService financesExchangeRateService;
    @In
    private CompanyConfigurationService companyConfigurationService;
    @In
    protected FacesMessages facesMessages;

    @Create
    public void init() {
        restrictions = new String[]{
        };
    }

    @Override
    protected String getEjbql() {

        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        String start = df.format(startDate);
        String end   = df.format(endDate);

        return  " SELECT " +
                "        voucherDetail.cashAccount.accountCode, " +
                "        voucherDetail.cashAccount.description, " +
                "        sum(voucherDetail.debit) as debit, " +
                "        sum(voucherDetail.credit) as credit " +
                "  FROM  Voucher voucher " +
                "  LEFT  JOIN voucher.voucherDetailList voucherDetail " +
                "  WHERE voucher.date between '"+start+"' and '"+end+"' " +
                "  AND   voucher.documentType = '"+ documentType.getName() +"'" +
                "  AND   voucher.state <> 'ANL'" +
                "  GROUP BY voucherDetail.cashAccount.accountCode, voucherDetail.cashAccount.description";
    }

    public void generateReport() {

        String documentTitle = "RECAUDACION DIARIA";
        CompanyConfiguration companyConfiguration = null;
        try {
            companyConfiguration = companyConfigurationService.findCompanyConfiguration();
        } catch (CompanyConfigurationNotFoundException e) {
            e.printStackTrace();
        }

        String companyTitle = companyConfiguration.getTitle();
        String subTitle = companyConfiguration.getSubTitle();

        BigDecimal exchangeRate = BigDecimal.ZERO;
        try {
            exchangeRate = financesExchangeRateService.findLastExchangeRateByCurrency(FinancesCurrencyType.D.toString());
        }catch (FinancesExchangeRateNotFoundException e){addFinancesExchangeRateNotFoundExceptionMessage();
        }catch (FinancesCurrencyNotFoundException e){addFinancesCurrencyNotFoundMessage();}

        log.debug("Generating products produced report...................");

        HashMap<String, Object> reportParameters = new HashMap<String, Object>();

        reportParameters.put("documentTitle", documentTitle);
        reportParameters.put("startDate", startDate);
        reportParameters.put("endDate", endDate);
        reportParameters.put("exchangeRate", exchangeRate);
        reportParameters.put("userLoginParam", currentUser.getEmployee().getFullName());
        reportParameters.put("companyTitle", companyTitle);
        reportParameters.put("subTitle", subTitle);

        super.generateReport(
                "dailyCollectionReport",
                "/accounting/reports/dailyCollectionReport.jrxml",
                PageFormat.LETTER,
                PageOrientation.PORTRAIT,
                messages.get("Accounting.dailyCollection.report"),
                reportParameters);
    }

    private void addFinancesCurrencyNotFoundMessage() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                "FixedAssets.FinancesCurrencyNotFoundException");
    }

    private void addFinancesExchangeRateNotFoundExceptionMessage() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO,
                "FixedAssets.FinancesExchangeRateNotFoundException");
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

    public DocType getDocumentType() {
        return documentType;
    }

    public void setDocumentType(DocType documentType) {
        this.documentType = documentType;
    }
}
