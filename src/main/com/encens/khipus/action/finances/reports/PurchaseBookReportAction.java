package com.encens.khipus.action.finances.reports;

import com.encens.khipus.action.reports.GenericReportAction;
import com.encens.khipus.model.finances.CollectionDocumentType;
import com.encens.khipus.model.purchases.PurchaseDocumentState;
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

    private CollectionDocumentType invoiceDocumentType = CollectionDocumentType.INVOICE;
    private PurchaseDocumentState nullifiedDocumentState = PurchaseDocumentState.NULLIFIED;

    public void generateReport() {
        log.debug("Generating reporte de libro de compras");
        Map params = readReportParamsInfo();

        super.generateReport("purchaseBookReport", "/finances/reports/purchaseBookReport.jrxml", MessageUtils.getMessage("Reports.purchaseBookReport.title"), params);
    }

    @Override
    protected String getEjbql() {
        return "select " +
                "purchaseDocument.nit, " +
                "purchaseDocument.name as socialName, " +
                "purchaseDocument.number as invoiceNumber, " +
                "purchaseDocument.authorizationNumber, " +
                "purchaseDocument.date, " +
                "purchaseDocument.amount, " +
                "purchaseDocument.ice, " +
                "purchaseDocument.exempt, " +
                "(purchaseDocument.amount-purchaseDocument.ice-purchaseDocument.exempt), " +
                "purchaseDocument.iva, " +
                "purchaseDocument.controlCode " +
                "from PurchaseDocument purchaseDocument " +
                "where purchaseDocument.type = #{purchaseBookReportAction.invoiceDocumentType} " +
                "and purchaseDocument.state <> #{purchaseBookReportAction.nullifiedDocumentState}";
    }

    @Create
    public void init() {
        restrictions = new String[]{
                "purchaseDocument.date >= #{purchaseBookReportAction.startDate}",
                "purchaseDocument.date <= #{purchaseBookReportAction.endDate}"
        };

        sortProperty = "purchaseDocument.date";
    }

    private Map readReportParamsInfo() {
        Map paramMap = new HashMap();
        Format formatter = new SimpleDateFormat("dd/MM/yyyy");

        String filterInfo = "";
        if (startDate != null) {
            filterInfo = filterInfo + MessageUtils.getMessage("Reports.salesBookReport.period") + ": " + formatter.format(startDate);
        }
        if (endDate != null) {
            filterInfo = filterInfo + " - " + formatter.format(endDate);
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


    public CollectionDocumentType getInvoiceDocumentType() {
        return invoiceDocumentType;
    }

    public void setInvoiceDocumentType(CollectionDocumentType invoiceDocumentType) {
        this.invoiceDocumentType = invoiceDocumentType;
    }

    public PurchaseDocumentState getNullifiedDocumentState() {
        return nullifiedDocumentState;
    }

    public void setNullifiedDocumentState(PurchaseDocumentState nullifiedDocumentState) {
        this.nullifiedDocumentState = nullifiedDocumentState;
    }
}
