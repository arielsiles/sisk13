package com.encens.khipus.action.finances.reports;

import com.encens.khipus.action.reports.GenericReportAction;
import com.encens.khipus.util.MessageUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author
 * @version 1.0
 */
@Name("purchaseBookReportAction")
@Scope(ScopeType.PAGE)
@Restrict("#{s:hasPermission('PURCHASEBOOKREPORT','VIEW')}")
public class PurchaseBookReportAction extends GenericReportAction {
    private static final String INVOICETYPEVALUE = "1";
    private static final String IMPORTINVOICENUMBERVALUE = "0";
    private Date startDate;
    private Date endDate;

    public void generateReport() {
        log.debug("Generating reporte de libro de compras");
        Map params = readReportParamsInfo();

        super.generateReport("purchaseBookReport", "/finances/reports/purchaseBookReport.jrxml", MessageUtils.getMessage("Reports.purchaseBookReport.title"), params);
    }

    @Override
    protected String getEjbql() {
        return "select " +
                "financeAccountingDocument.nit, " +
                "financeAccountingDocument.socialName, " +
                "financeAccountingDocument.id.invoiceNumber, " +
                "financeAccountingDocument.id.authorizationNumber, " +
                "financeAccountingDocument.date, " +
                "financeAccountingDocument.amount, " +
                "financeAccountingDocument.ice, " +
                "financeAccountingDocument.exempt, " +
                "(financeAccountingDocument.amount-financeAccountingDocument.ice-financeAccountingDocument.exempt), " +
                "financeAccountingDocument.tax, " +
                "financeAccountingDocument.controlCode " +
                "from FinanceAccountingDocument financeAccountingDocument ";
    }

    @Create
    public void init() {
        restrictions = new String[]{
                "financeAccountingDocument.date >= #{purchaseBookReportAction.startDate}",
                "financeAccountingDocument.date <= #{purchaseBookReportAction.endDate}"
        };

        sortProperty = "financeAccountingDocument.date";
    }

    private Map readReportParamsInfo() {
        Map paramMap = new HashMap();
        Format formatter = new SimpleDateFormat("dd/MM/yyyy");

        String filterInfo = "";
        if (startDate != null) {
            filterInfo = filterInfo + MessageUtils.getMessage("Reports.purchaseBookReport.dateFrom") + ":" + formatter.format(startDate);
        }
        if (endDate != null) {
            filterInfo = filterInfo + " " + MessageUtils.getMessage("Reports.purchaseBookReport.dateTo") + ":" + formatter.format(endDate);
        }

        paramMap.put("filterInfoParam", filterInfo);
        paramMap.put("invoiceTypeParam", INVOICETYPEVALUE);
        paramMap.put("importInvoiceNumberParam", IMPORTINVOICENUMBERVALUE);
        return paramMap;
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
