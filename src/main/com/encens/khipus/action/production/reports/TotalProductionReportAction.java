package com.encens.khipus.action.production.reports;

import com.encens.khipus.action.reports.GenericReportAction;
import com.encens.khipus.action.reports.PageFormat;
import com.encens.khipus.action.reports.PageOrientation;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

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

@Name("totalProductionReportAction")
@Scope(ScopeType.PAGE)
public class TotalProductionReportAction extends GenericReportAction {

    private Date startDate;
    private Date endDate;
    private Boolean compareFlag = false;

    @Create
    public void init() {

        restrictions = new String[]{
               //  "productionPlanning.date between #{productsProducedReportAction.startDate} and #{productsProducedReportAction.endDate}"
        };
        //sortProperty = "name";
    }

    @Override
    protected String getEjbql() {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        String start = df.format(startDate);
        String end = df.format(endDate);

        return  " SELECT totalProduction.articleCode," +
                "        totalProduction.name, " +
                "        sum(totalProduction.amountSC) as amountSC, " +
                "        sum(totalProduction.amountSP) as amountSP, " +
                "        sum(totalProduction.reprocessSC) as reprocessSC, " +
                "        sum(totalProduction.reprocessSP) as reprocessSP " +
                " FROM  TotalProduction totalProduction " +
                " WHERE totalProduction.date between '"+start+"' and '"+end+"' " +
                " GROUP BY totalProduction.articleCode, totalProduction.name " +
                " ORDER BY totalProduction.name";
    }

    public void generateReport() {

        if(compareFlag){
            log.debug("Generating products produced report...................");
            HashMap<String, Object> reportParameters = new HashMap<String, Object>();
            reportParameters.put("startDate",startDate);
            reportParameters.put("endDate",endDate);
            super.generateReport(
                    "productsProdecedReport",
                    "/production/reports/compareTotalProductionReport.jrxml",
                    PageFormat.LETTER,
                    PageOrientation.PORTRAIT,
                    messages.get("production.productsProduced.TitleReport"),
                    reportParameters);
        }else {
            log.debug("Generating products produced report...................");
            HashMap<String, Object> reportParameters = new HashMap<String, Object>();
            reportParameters.put("startDate",startDate);
            reportParameters.put("endDate",endDate);
            super.generateReport(
                    "productsProdecedReport",
                    "/production/reports/totalProductionReport.jrxml",
                    PageFormat.LETTER,
                    PageOrientation.PORTRAIT,
                    messages.get("production.productsProduced.TitleReport"),
                    reportParameters);
        }

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

    public Boolean getCompareFlag() {
        return compareFlag;
    }

    public void setCompareFlag(Boolean compareFlag) {
        this.compareFlag = compareFlag;
    }
}
