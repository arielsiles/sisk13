package com.encens.khipus.action.xproduction;

import com.encens.khipus.action.reports.GenericReportAction;
import com.encens.khipus.action.reports.PageFormat;
import com.encens.khipus.action.reports.PageOrientation;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import java.util.Date;
import java.util.HashMap;

@Name("dailyProductionReportAction")
@Scope(ScopeType.PAGE)
public class DailyProductionReportAction extends GenericReportAction {

    private Date initDate;
    private Date endDate;

    @Create
    public void init() {
        restrictions = new String[]{
                "plan.date >= #{dailyProductionReportAction.initDate}",
                "plan.date <= #{dailyProductionReportAction.endDate}"
        };

        sortProperty = "plan.date";

        /*groupByProperty = "";*/
    }

    protected String getEjbql() {
        return "SELECT " +
                "plan.date, " +
                "production.code as prodCode, " +
                "production.shiftType as shiftType, " +
                "productionGroup.code as groupCode, " +
                "productItem.productItemCode as codeArt, " +
                "productItem.name, " +
                "(product.quantity / 1000) " +
                "from XProductionProduct product " +
                "     join product.production production " +
                "     join production.productionPlan plan " +
                "     join product.productItem productItem " +
                "     join production.productionGroup productionGroup "
                ;
    }

    public void generateReport() {
        log.debug("generating DailyProductionReport......................................");

        HashMap<String, Object> reportParameters = new HashMap<String, Object>();
        super.generateReport(
                "dailyProductionReport",
                "/xproduction/reports/dailyProductionReport.jrxml",
                PageFormat.LETTER,
                PageOrientation.LANDSCAPE,
                messages.get("Reports.production.dailyProductionReport"),
                reportParameters);
    }

    public Date getInitDate() {
        return initDate;
    }

    public void setInitDate(Date initDate) {
        this.initDate = initDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }
}
