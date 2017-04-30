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
@Name("salesBookReportAction")
@Scope(ScopeType.PAGE)
@Restrict("#{s:hasPermission('SALESBOOKREPORT','VIEW')}")
public class SalesBookReportAction extends GenericReportAction {
    private Date startDate;
    private Date endDate;

    public void generateReport() {
        log.debug("Generating reporte de libro de ventas");
        Map params = readReportParamsInfo();

        super.generateReport("salesBookReport", "/finances/reports/salesBookReport.jrxml", MessageUtils.getMessage("Reports.salesBookReport.title"), params);
    }

    @Override
    protected String getEjbql() {
        return "select " +
                "salesBook.nit, " +
                "salesBook.socialName, " +
                "salesBook.invoiceNumber, " +
                "salesBook.authorizationNumber, " +
                "salesBook.date, " +
                "salesBook.amount, " +
                "salesBook.ice, " +
                "salesBook.exempt, " +
                "salesBook.netAmount, " +
                "salesBook.tax, " +
                "salesBook.status, " +
                "salesBook.controlCode " +
                "from SalesBook salesBook ";
    }

    @Create
    public void init() {
        restrictions = new String[]{
                "salesBook.date >= #{salesBookReportAction.startDate}",
                "salesBook.date <= #{salesBookReportAction.endDate}"
        };

        sortProperty = "salesBook.date";
    }

    private Map readReportParamsInfo() {
        Map paramMap = new HashMap();
        Format formatter = new SimpleDateFormat("dd/MM/yyyy");

        String filterInfo = "";
        if (startDate != null) {
            filterInfo = filterInfo + MessageUtils.getMessage("Reports.salesBookReport.dateFrom") + ":" + formatter.format(startDate);
        }
        if (endDate != null) {
            filterInfo = filterInfo + " " + MessageUtils.getMessage("Reports.salesBookReport.dateTo") + ":" + formatter.format(endDate);
        }

        paramMap.put("filterInfoParam", filterInfo);
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
