package com.encens.khipus.action.finances.reports;

import com.encens.khipus.action.reports.GenericReportAction;
import com.encens.khipus.util.MessageUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

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
                "movement.date, " +
                "movement.number, " +
                "movement.authorizationNumber, " +
                "movement.state, " +
                "movement.nit, " +
                "movement.name, " +
                "movement.amount, " +
                "movement.amountIce, " +
                "movement.exemptExport, " +
                "movement.taxedSalesZero, " +
                "movement.subtotal, " +
                "movement.discount, " +
                "movement.amountFiscalDebit, " +
                "movement.fiscalDebit, " +
                "movement.controlCode, " +
                "movement.cuf " +
                "from Movement movement ";
    }

    @Create
    public void init() {
        restrictions = new String[]{
                "movement.date >= #{salesBookReportAction.startDate}",
                "movement.date <= #{salesBookReportAction.endDate}"
        };

        sortProperty = "movement.date";
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
